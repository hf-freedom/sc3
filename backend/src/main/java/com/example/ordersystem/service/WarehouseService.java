package com.example.ordersystem.service;

import com.example.ordersystem.entity.Warehouse;
import com.example.ordersystem.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    public Warehouse createWarehouse(Warehouse warehouse) {
        warehouse.setEnabled(true);
        if (warehouse.getPriority() == null) {
            warehouse.setPriority(warehouseRepository.findAll().size() + 1);
        }
        return warehouseRepository.save(warehouse);
    }

    public Warehouse updateWarehouse(String id, Warehouse warehouse) {
        Optional<Warehouse> existingOpt = warehouseRepository.findById(id);
        if (!existingOpt.isPresent()) {
            throw new RuntimeException("仓库不存在");
        }
        Warehouse existing = existingOpt.get();
        if (warehouse.getName() != null) {
            existing.setName(warehouse.getName());
        }
        if (warehouse.getPriority() != null) {
            existing.setPriority(warehouse.getPriority());
        }
        if (warehouse.getEnabled() != null) {
            existing.setEnabled(warehouse.getEnabled());
        }
        return warehouseRepository.save(existing);
    }

    public Optional<Warehouse> getWarehouseById(String id) {
        return warehouseRepository.findById(id);
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public List<Warehouse> getAllEnabledWarehouses() {
        return warehouseRepository.findAllEnabled();
    }

    public void deleteWarehouse(String id) {
        warehouseRepository.deleteById(id);
    }
}