package com.example.ordersystem.service;

import com.example.ordersystem.entity.StockFlow;
import com.example.ordersystem.entity.WarehouseStock;
import com.example.ordersystem.enums.StockFlowType;
import com.example.ordersystem.repository.StockFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockFlowService {

    @Autowired
    private StockFlowRepository stockFlowRepository;

    public StockFlow createStockFlow(
            String productId,
            String warehouseId,
            String orderId,
            StockFlowType type,
            Integer quantity,
            WarehouseStock stockBefore,
            WarehouseStock stockAfter,
            String remark
    ) {
        StockFlow flow = new StockFlow();
        flow.setProductId(productId);
        flow.setWarehouseId(warehouseId);
        flow.setOrderId(orderId);
        flow.setType(type);
        flow.setQuantity(quantity);
        flow.setBeforeAvailable(stockBefore.getAvailableStock());
        flow.setAfterAvailable(stockAfter.getAvailableStock());
        flow.setBeforeLocked(stockBefore.getLockedStock());
        flow.setAfterLocked(stockAfter.getLockedStock());
        flow.setRemark(remark);
        return stockFlowRepository.save(flow);
    }

    public List<StockFlow> getAllStockFlows() {
        return stockFlowRepository.findAll();
    }

    public List<StockFlow> getStockFlowsByOrderId(String orderId) {
        return stockFlowRepository.findByOrderId(orderId);
    }

    public List<StockFlow> getStockFlowsByProductId(String productId) {
        return stockFlowRepository.findByProductId(productId);
    }
}