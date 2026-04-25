package com.example.ordersystem.service;

import com.example.ordersystem.entity.WarehouseStock;
import com.example.ordersystem.repository.WarehouseStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WarehouseStockService {

    @Autowired
    private WarehouseStockRepository warehouseStockRepository;

    public WarehouseStock createOrUpdateStock(WarehouseStock stock) {
        Optional<WarehouseStock> existingOpt = warehouseStockRepository
                .findByProductIdAndWarehouseId(stock.getProductId(), stock.getWarehouseId());
        
        if (existingOpt.isPresent()) {
            WarehouseStock existing = existingOpt.get();
            if (stock.getAvailableStock() != null) {
                existing.setAvailableStock(stock.getAvailableStock());
            }
            if (stock.getLockedStock() == null) {
                existing.setLockedStock(0);
            }
            return warehouseStockRepository.save(existing);
        }
        
        if (stock.getLockedStock() == null) {
            stock.setLockedStock(0);
        }
        return warehouseStockRepository.save(stock);
    }

    public Optional<WarehouseStock> getStockById(String id) {
        return warehouseStockRepository.findById(id);
    }

    public Optional<WarehouseStock> getStockByProductAndWarehouse(String productId, String warehouseId) {
        return warehouseStockRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    public List<WarehouseStock> getStocksByProductId(String productId) {
        return warehouseStockRepository.findByProductId(productId);
    }

    public List<WarehouseStock> getStocksByWarehouseId(String warehouseId) {
        return warehouseStockRepository.findByWarehouseId(warehouseId);
    }

    public List<WarehouseStock> getAllStocks() {
        return warehouseStockRepository.findAll();
    }

    public int getTotalAvailableStock(String productId) {
        return warehouseStockRepository.getTotalAvailableStock(productId);
    }

    public void deleteStock(String id) {
        warehouseStockRepository.deleteById(id);
    }
}