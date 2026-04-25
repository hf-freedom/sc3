package com.example.ordersystem.controller;

import com.example.ordersystem.common.Result;
import com.example.ordersystem.entity.Warehouse;
import com.example.ordersystem.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@CrossOrigin(origins = "*")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @PostMapping
    public Result<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        Warehouse created = warehouseService.createWarehouse(warehouse);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Warehouse> updateWarehouse(@PathVariable String id, @RequestBody Warehouse warehouse) {
        try {
            Warehouse updated = warehouseService.updateWarehouse(id, warehouse);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Warehouse> getWarehouseById(@PathVariable String id) {
        return warehouseService.getWarehouseById(id)
                .map(Result::success)
                .orElse(Result.error("仓库不存在"));
    }

    @GetMapping
    public Result<List<Warehouse>> getAllWarehouses(
            @RequestParam(required = false, defaultValue = "false") Boolean enabledOnly) {
        List<Warehouse> warehouses;
        if (enabledOnly) {
            warehouses = warehouseService.getAllEnabledWarehouses();
        } else {
            warehouses = warehouseService.getAllWarehouses();
        }
        return Result.success(warehouses);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteWarehouse(@PathVariable String id) {
        warehouseService.deleteWarehouse(id);
        return Result.success();
    }
}