package com.example.ordersystem.repository;

import com.example.ordersystem.entity.WarehouseStock;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class WarehouseStockRepository {
    private final Map<String, WarehouseStock> stockMap = new ConcurrentHashMap<>();

    public WarehouseStock save(WarehouseStock stock) {
        if (stock.getId() == null) {
            stock.setId(UUID.randomUUID().toString());
        }
        if (stock.getCreatedAt() == null) {
            stock.setCreatedAt(System.currentTimeMillis());
        }
        stock.setUpdatedAt(System.currentTimeMillis());
        stockMap.put(stock.getId(), stock);
        return stock;
    }

    public Optional<WarehouseStock> findById(String id) {
        return Optional.ofNullable(stockMap.get(id));
    }

    public Optional<WarehouseStock> findByProductIdAndWarehouseId(String productId, String warehouseId) {
        return stockMap.values().stream()
                .filter(s -> productId.equals(s.getProductId()) && warehouseId.equals(s.getWarehouseId()))
                .findFirst();
    }

    public List<WarehouseStock> findByProductId(String productId) {
        return stockMap.values().stream()
                .filter(s -> productId.equals(s.getProductId()))
                .collect(Collectors.toList());
    }

    public List<WarehouseStock> findByWarehouseId(String warehouseId) {
        return new ArrayList<>(stockMap.values().stream()
                .filter(s -> warehouseId.equals(s.getWarehouseId()))
                .collect(Collectors.toList()));
    }

    public List<WarehouseStock> findAll() {
        return new ArrayList<>(stockMap.values());
    }

    public int getTotalAvailableStock(String productId) {
        return stockMap.values().stream()
                .filter(s -> productId.equals(s.getProductId()))
                .mapToInt(WarehouseStock::getAvailableStock)
                .sum();
    }

    public void deleteById(String id) {
        stockMap.remove(id);
    }
}