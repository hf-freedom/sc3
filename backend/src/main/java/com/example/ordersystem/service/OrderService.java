package com.example.ordersystem.service;

import com.example.ordersystem.dto.CreateOrderRequest;
import com.example.ordersystem.dto.OrderDetailVO;
import com.example.ordersystem.dto.OrderListVO;
import com.example.ordersystem.entity.*;
import com.example.ordersystem.enums.OrderStatus;
import com.example.ordersystem.enums.StockFlowType;
import com.example.ordersystem.repository.OrderRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseStockService warehouseStockService;

    @Autowired
    private StockFlowService stockFlowService;

    private static final int ORDER_EXPIRE_MINUTES = 30;
    private final Object paymentLock = new Object();

    @Transactional
    public List<Order> createOrder(CreateOrderRequest request) {
        String parentOrderId = UUID.randomUUID().toString();
        
        Map<String, List<WarehouseStock>> productStockMap = new HashMap<>();
        List<Warehouse> enabledWarehouses = warehouseService.getAllEnabledWarehouses();
        
        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {
            Product product = productService.getProductById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + item.getProductId()));
            
            if (!product.getEnabled()) {
                throw new RuntimeException("商品已下架: " + product.getName());
            }
            
            int totalAvailable = warehouseStockService.getTotalAvailableStock(item.getProductId());
            if (totalAvailable < item.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName() + 
                        ", 需要: " + item.getQuantity() + ", 可用: " + totalAvailable);
            }
            
            List<WarehouseStock> availableStocks = enabledWarehouses.stream()
                    .map(w -> warehouseStockService.getStockByProductAndWarehouse(item.getProductId(), w.getId()))
                    .filter(opt -> opt.isPresent())
                    .map(opt -> opt.get())
                    .filter(s -> s.getAvailableStock() > 0)
                    .sorted((s1, s2) -> {
                        int p1 = enabledWarehouses.stream()
                                .filter(w -> w.getId().equals(s1.getWarehouseId()))
                                .findFirst()
                                .map(Warehouse::getPriority)
                                .orElse(Integer.MAX_VALUE);
                        int p2 = enabledWarehouses.stream()
                                .filter(w -> w.getId().equals(s2.getWarehouseId()))
                                .findFirst()
                                .map(Warehouse::getPriority)
                                .orElse(Integer.MAX_VALUE);
                        return Integer.compare(p1, p2);
                    })
                    .collect(Collectors.toList());
            
            productStockMap.put(item.getProductId(), availableStocks);
        }
        
        Map<String, List<SplitItem>> warehouseItemsMap = splitOrder(request, productStockMap, enabledWarehouses);
        
        Order parentOrder = createParentOrder(parentOrderId, request, warehouseItemsMap);
        List<Order> childOrders = createChildOrders(parentOrderId, warehouseItemsMap);
        
        lockStocks(childOrders, productStockMap);
        
        List<Order> allOrders = new ArrayList<>();
        allOrders.add(parentOrder);
        allOrders.addAll(childOrders);
        
        return orderRepository.saveAll(allOrders);
    }

    private Map<String, List<SplitItem>> splitOrder(
            CreateOrderRequest request,
            Map<String, List<WarehouseStock>> productStockMap,
            List<Warehouse> enabledWarehouses
    ) {
        Map<String, List<SplitItem>> warehouseItemsMap = new LinkedHashMap<>();
        
        for (Warehouse warehouse : enabledWarehouses) {
            warehouseItemsMap.put(warehouse.getId(), new ArrayList<>());
        }
        
        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {
            String productId = item.getProductId();
            int remainingQuantity = item.getQuantity();
            List<WarehouseStock> availableStocks = productStockMap.get(productId);
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));
            
            for (WarehouseStock stock : availableStocks) {
                if (remainingQuantity <= 0) break;
                
                int takeQuantity = Math.min(remainingQuantity, stock.getAvailableStock());
                
                SplitItem splitItem = new SplitItem();
                splitItem.setProductId(productId);
                splitItem.setProductName(product.getName());
                splitItem.setPrice(product.getPrice());
                splitItem.setQuantity(takeQuantity);
                splitItem.setWarehouseId(stock.getWarehouseId());
                
                warehouseItemsMap.get(stock.getWarehouseId()).add(splitItem);
                remainingQuantity -= takeQuantity;
            }
            
            if (remainingQuantity > 0) {
                throw new RuntimeException("商品库存拆分失败: " + product.getName());
            }
        }
        
        return warehouseItemsMap;
    }

    private Order createParentOrder(String parentOrderId, CreateOrderRequest request, 
                                     Map<String, List<SplitItem>> warehouseItemsMap) {
        Order parentOrder = new Order();
        parentOrder.setId(parentOrderId);
        parentOrder.setStatus(OrderStatus.PENDING_PAYMENT);
        parentOrder.setCreatedAt(System.currentTimeMillis());
        parentOrder.setExpireAt(System.currentTimeMillis() + ORDER_EXPIRE_MINUTES * 60 * 1000);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> allItems = new ArrayList<>();
        
        for (Map.Entry<String, List<SplitItem>> entry : warehouseItemsMap.entrySet()) {
            for (SplitItem item : entry.getValue()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(UUID.randomUUID().toString());
                orderItem.setProductId(item.getProductId());
                orderItem.setProductName(item.getProductName());
                orderItem.setPrice(item.getPrice());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                orderItem.setWarehouseId(item.getWarehouseId());
                orderItem.setDeliveredQuantity(0);
                allItems.add(orderItem);
                totalAmount = totalAmount.add(orderItem.getSubtotal());
            }
        }
        
        parentOrder.setItems(allItems);
        parentOrder.setTotalAmount(totalAmount);
        
        return parentOrder;
    }

    private List<Order> createChildOrders(String parentOrderId, 
                                           Map<String, List<SplitItem>> warehouseItemsMap) {
        List<Order> childOrders = new ArrayList<>();
        
        for (Map.Entry<String, List<SplitItem>> entry : warehouseItemsMap.entrySet()) {
            String warehouseId = entry.getKey();
            List<SplitItem> items = entry.getValue();
            
            if (items.isEmpty()) continue;
            
            Order childOrder = new Order();
            childOrder.setId(UUID.randomUUID().toString());
            childOrder.setParentOrderId(parentOrderId);
            childOrder.setStatus(OrderStatus.PENDING_PAYMENT);
            childOrder.setWarehouseId(warehouseId);
            childOrder.setCreatedAt(System.currentTimeMillis());
            childOrder.setExpireAt(System.currentTimeMillis() + ORDER_EXPIRE_MINUTES * 60 * 1000);
            
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (SplitItem item : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(UUID.randomUUID().toString());
                orderItem.setOrderId(childOrder.getId());
                orderItem.setProductId(item.getProductId());
                orderItem.setProductName(item.getProductName());
                orderItem.setPrice(item.getPrice());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                orderItem.setWarehouseId(warehouseId);
                orderItem.setDeliveredQuantity(0);
                orderItems.add(orderItem);
                totalAmount = totalAmount.add(orderItem.getSubtotal());
            }
            
            childOrder.setItems(orderItems);
            childOrder.setTotalAmount(totalAmount);
            childOrders.add(childOrder);
        }
        
        return childOrders;
    }

    private void lockStocks(List<Order> childOrders, Map<String, List<WarehouseStock>> productStockMap) {
        for (Order order : childOrders) {
            for (OrderItem item : order.getItems()) {
                Optional<WarehouseStock> stockOpt = warehouseStockService
                        .getStockByProductAndWarehouse(item.getProductId(), order.getWarehouseId());
                
                if (!stockOpt.isPresent()) {
                    throw new RuntimeException("库存不存在");
                }
                
                WarehouseStock stock = stockOpt.get();
                WarehouseStock stockBefore = cloneStock(stock);
                
                if (stock.getAvailableStock() < item.getQuantity()) {
                    throw new RuntimeException("库存不足: 商品=" + item.getProductName() + 
                            ", 仓库=" + order.getWarehouseId());
                }
                
                stock.setAvailableStock(stock.getAvailableStock() - item.getQuantity());
                stock.setLockedStock(stock.getLockedStock() + item.getQuantity());
                
                WarehouseStock updatedStock = warehouseStockService.createOrUpdateStock(stock);
                
                stockFlowService.createStockFlow(
                        item.getProductId(),
                        order.getWarehouseId(),
                        order.getId(),
                        StockFlowType.LOCK,
                        item.getQuantity(),
                        stockBefore,
                        updatedStock,
                        "创建订单锁定库存"
                );
            }
        }
    }

    private WarehouseStock cloneStock(WarehouseStock stock) {
        WarehouseStock clone = new WarehouseStock();
        clone.setId(stock.getId());
        clone.setProductId(stock.getProductId());
        clone.setWarehouseId(stock.getWarehouseId());
        clone.setAvailableStock(stock.getAvailableStock());
        clone.setLockedStock(stock.getLockedStock());
        return clone;
    }

    @Transactional
    public void payOrder(String orderId) {
        synchronized (paymentLock) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("订单不存在"));
            
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                throw new RuntimeException("订单状态不正确，无法支付");
            }
            
            if (order.getExpireAt() != null && order.getExpireAt() < System.currentTimeMillis()) {
                throw new RuntimeException("订单已过期，请重新下单");
            }
            
            if (order.getParentOrderId() == null) {
                List<Order> childOrders = orderRepository.findByParentOrderId(order.getId());
                for (Order childOrder : childOrders) {
                    processPayment(childOrder);
                }
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            } else {
                processPayment(order);
                updateParentOrderStatus(order.getParentOrderId());
            }
        }
    }

    private void processPayment(Order order) {
        Order currentOrder = orderRepository.findById(order.getId()).orElse(null);
        if (currentOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (currentOrder.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return;
        }
        
        for (OrderItem item : order.getItems()) {
            Optional<WarehouseStock> stockOpt = warehouseStockService
                    .getStockByProductAndWarehouse(item.getProductId(), order.getWarehouseId());
            
            if (!stockOpt.isPresent()) {
                throw new RuntimeException("库存不存在");
            }
            
            WarehouseStock stock = stockOpt.get();
            
            if (stock.getLockedStock() < item.getQuantity()) {
                throw new RuntimeException("锁定库存不足，无法支付");
            }
            
            WarehouseStock stockBefore = cloneStock(stock);
            
            stock.setLockedStock(stock.getLockedStock() - item.getQuantity());
            
            WarehouseStock updatedStock = warehouseStockService.createOrUpdateStock(stock);
            
            stockFlowService.createStockFlow(
                    item.getProductId(),
                    order.getWarehouseId(),
                    order.getId(),
                    StockFlowType.DEDUCT,
                    item.getQuantity(),
                    stockBefore,
                    updatedStock,
                    "支付成功扣减锁定库存"
            );
        }
        
        currentOrder.setStatus(OrderStatus.PAID);
        orderRepository.save(currentOrder);
    }

    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        if (order.getParentOrderId() == null) {
            List<Order> childOrders = orderRepository.findByParentOrderId(order.getId());
            for (Order childOrder : childOrders) {
                processCancel(childOrder);
            }
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        } else {
            processCancel(order);
            updateParentOrderStatus(order.getParentOrderId());
        }
    }

    private void processCancel(Order order) {
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            releaseLockedStock(order, "取消订单释放锁定库存");
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        } else if (order.getStatus() == OrderStatus.PAID) {
            order.setStatus(OrderStatus.REFUNDING);
            orderRepository.save(order);
        } else if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDING) {
            return;
        } else {
            throw new RuntimeException("订单状态不正确，无法取消");
        }
    }

    private void releaseLockedStock(Order order, String remark) {
        for (OrderItem item : order.getItems()) {
            Optional<WarehouseStock> stockOpt = warehouseStockService
                    .getStockByProductAndWarehouse(item.getProductId(), order.getWarehouseId());
            
            if (!stockOpt.isPresent()) {
                continue;
            }
            
            WarehouseStock stock = stockOpt.get();
            WarehouseStock stockBefore = cloneStock(stock);
            
            stock.setAvailableStock(stock.getAvailableStock() + item.getQuantity());
            stock.setLockedStock(stock.getLockedStock() - item.getQuantity());
            
            WarehouseStock updatedStock = warehouseStockService.createOrUpdateStock(stock);
            
            stockFlowService.createStockFlow(
                    item.getProductId(),
                    order.getWarehouseId(),
                    order.getId(),
                    StockFlowType.RELEASE,
                    item.getQuantity(),
                    stockBefore,
                    updatedStock,
                    remark
            );
        }
    }

    @Transactional
    public void processExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredOrders();
        for (Order order : expiredOrders) {
            if (order.getParentOrderId() == null) {
                List<Order> childOrders = orderRepository.findByParentOrderId(order.getId());
                for (Order childOrder : childOrders) {
                    if (childOrder.getStatus() == OrderStatus.PENDING_PAYMENT) {
                        releaseLockedStock(childOrder, "订单超时自动取消释放库存");
                        childOrder.setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(childOrder);
                    }
                }
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            } else {
                if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    releaseLockedStock(order, "订单超时自动取消释放库存");
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                    updateParentOrderStatus(order.getParentOrderId());
                }
            }
        }
    }

    private void updateParentOrderStatus(String parentOrderId) {
        Optional<Order> parentOpt = orderRepository.findById(parentOrderId);
        if (!parentOpt.isPresent()) return;
        
        Order parentOrder = parentOpt.get();
        List<Order> childOrders = orderRepository.findByParentOrderId(parentOrderId);
        
        boolean allCancelled = childOrders.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.CANCELLED);
        boolean allPaid = childOrders.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.CANCELLED);
        boolean anyPaid = childOrders.stream()
                .anyMatch(o -> o.getStatus() == OrderStatus.PAID);
        
        if (allCancelled) {
            parentOrder.setStatus(OrderStatus.CANCELLED);
        } else if (allPaid && anyPaid) {
            parentOrder.setStatus(OrderStatus.PAID);
        }
        
        orderRepository.save(parentOrder);
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getChildOrders(String parentOrderId) {
        return orderRepository.findByParentOrderId(parentOrderId);
    }

    public OrderListVO toOrderListVO(Order order) {
        OrderListVO vo = new OrderListVO();
        vo.setId(order.getId());
        vo.setParentOrderId(order.getParentOrderId());
        vo.setStatus(order.getStatus().name());
        vo.setStatusDescription(order.getStatus().getDescription());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setItemCount(order.getItems() != null ? order.getItems().size() : 0);
        vo.setIsParentOrder(order.getParentOrderId() == null);
        vo.setCreatedAt(order.getCreatedAt());
        vo.setExpireAt(order.getExpireAt());
        return vo;
    }

    public OrderDetailVO toOrderDetailVO(Order order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setParentOrderId(order.getParentOrderId());
        vo.setStatus(order.getStatus().name());
        vo.setStatusDescription(order.getStatus().getDescription());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());
        vo.setExpireAt(order.getExpireAt());
        
        if (order.getWarehouseId() != null) {
            warehouseService.getWarehouseById(order.getWarehouseId())
                    .ifPresent(w -> vo.setWarehouseName(w.getName()));
        }
        
        if (order.getItems() != null) {
            List<OrderDetailVO.OrderItemVO> itemVOs = order.getItems().stream()
                    .map(this::toOrderItemVO)
                    .collect(Collectors.toList());
            vo.setItems(itemVOs);
        }
        
        return vo;
    }

    private OrderDetailVO.OrderItemVO toOrderItemVO(OrderItem item) {
        OrderDetailVO.OrderItemVO vo = new OrderDetailVO.OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        vo.setWarehouseId(item.getWarehouseId());
        vo.setDeliveredQuantity(item.getDeliveredQuantity());
        
        if (item.getWarehouseId() != null) {
            warehouseService.getWarehouseById(item.getWarehouseId())
                    .ifPresent(w -> vo.setWarehouseName(w.getName()));
        }
        
        return vo;
    }

    @Data
    private static class SplitItem {
        private String productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private String warehouseId;
    }
}