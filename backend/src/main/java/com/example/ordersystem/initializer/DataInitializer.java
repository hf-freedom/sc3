package com.example.ordersystem.initializer;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.entity.Warehouse;
import com.example.ordersystem.entity.WarehouseStock;
import com.example.ordersystem.service.ProductService;
import com.example.ordersystem.service.WarehouseService;
import com.example.ordersystem.service.WarehouseStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductService productService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseStockService warehouseStockService;

    @Override
    public void run(String... args) {
        if (productService.getAllProducts().isEmpty()) {
            initProducts();
            initWarehouses();
            initStocks();
        }
    }

    private void initProducts() {
        Product product1 = new Product();
        product1.setName("商品A");
        product1.setCategory("电子产品");
        product1.setPrice(new BigDecimal("100.00"));
        product1.setEnabled(true);
        productService.createProduct(product1);

        Product product2 = new Product();
        product2.setName("商品B");
        product2.setCategory("服装");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setEnabled(true);
        productService.createProduct(product2);

        Product product3 = new Product();
        product3.setName("商品C");
        product3.setCategory("食品");
        product3.setPrice(new BigDecimal("20.00"));
        product3.setEnabled(true);
        productService.createProduct(product3);
    }

    private void initWarehouses() {
        Warehouse warehouse1 = new Warehouse();
        warehouse1.setName("主仓库");
        warehouse1.setPriority(1);
        warehouse1.setEnabled(true);
        warehouseService.createWarehouse(warehouse1);

        Warehouse warehouse2 = new Warehouse();
        warehouse2.setName("备用仓库");
        warehouse2.setPriority(2);
        warehouse2.setEnabled(true);
        warehouseService.createWarehouse(warehouse2);

        Warehouse warehouse3 = new Warehouse();
        warehouse3.setName("区域仓库");
        warehouse3.setPriority(3);
        warehouse3.setEnabled(true);
        warehouseService.createWarehouse(warehouse3);
    }

    private void initStocks() {
        java.util.List<Product> products = productService.getAllProducts();
        java.util.List<Warehouse> warehouses = warehouseService.getAllWarehouses();

        for (Product product : products) {
            for (int i = 0; i < warehouses.size(); i++) {
                Warehouse warehouse = warehouses.get(i);
                WarehouseStock stock = new WarehouseStock();
                stock.setProductId(product.getId());
                stock.setWarehouseId(warehouse.getId());
                stock.setAvailableStock((i + 1) * 10);
                stock.setLockedStock(0);
                warehouseStockService.createOrUpdateStock(stock);
            }
        }
    }
}