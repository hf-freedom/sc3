package com.example.ordersystem.repository;

import com.example.ordersystem.entity.Warehouse;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class WarehouseRepository {
    private final Map<String, Warehouse> warehouseMap = new ConcurrentHashMap<>();

    public Warehouse save(Warehouse warehouse) {
        if (warehouse.getId() == null) {
            warehouse.setId(UUID.randomUUID().toString());
        }
        if (warehouse.getCreatedAt() == null) {
            warehouse.setCreatedAt(System.currentTimeMillis());
        }
        warehouse.setUpdatedAt(System.currentTimeMillis());
        warehouseMap.put(warehouse.getId(), warehouse);
        return warehouse;
    }

    public Optional<Warehouse> findById(String id) {
        return Optional.ofNullable(warehouseMap.get(id));
    }

    public List<Warehouse> findAll() {
        return warehouseMap.values().stream()
                .sorted(Comparator.comparingInt(Warehouse::getPriority))
                .collect(Collectors.toList());
    }

    public List<Warehouse> findAllEnabled() {
        return warehouseMap.values().stream()
                .filter(Warehouse::getEnabled)
                .sorted(Comparator.comparingInt(Warehouse::getPriority))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        warehouseMap.remove(id);
    }
}