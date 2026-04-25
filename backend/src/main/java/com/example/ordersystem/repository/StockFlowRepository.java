package com.example.ordersystem.repository;

import com.example.ordersystem.entity.StockFlow;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class StockFlowRepository {
    private final Map<String, StockFlow> flowMap = new ConcurrentHashMap<>();

    public StockFlow save(StockFlow flow) {
        if (flow.getId() == null) {
            flow.setId(UUID.randomUUID().toString());
        }
        if (flow.getCreatedAt() == null) {
            flow.setCreatedAt(System.currentTimeMillis());
        }
        flowMap.put(flow.getId(), flow);
        return flow;
    }

    public Optional<StockFlow> findById(String id) {
        return Optional.ofNullable(flowMap.get(id));
    }

    public List<StockFlow> findAll() {
        return flowMap.values().stream()
                .sorted(Comparator.comparingLong(StockFlow::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<StockFlow> findByOrderId(String orderId) {
        return flowMap.values().stream()
                .filter(f -> orderId.equals(f.getOrderId()))
                .sorted(Comparator.comparingLong(StockFlow::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<StockFlow> findByProductId(String productId) {
        return flowMap.values().stream()
                .filter(f -> productId.equals(f.getProductId()))
                .sorted(Comparator.comparingLong(StockFlow::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}