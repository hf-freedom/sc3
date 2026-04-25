package com.example.ordersystem.controller;

import com.example.ordersystem.common.Result;
import com.example.ordersystem.entity.WarehouseStock;
import com.example.ordersystem.service.WarehouseStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class WarehouseStockController {

    @Autowired
    private WarehouseStockService warehouseStockService;

    @PostMapping
    public Result<WarehouseStock> createOrUpdateStock(@RequestBody WarehouseStock stock) {
        WarehouseStock created = warehouseStockService.createOrUpdateStock(stock);
        return Result.success(created);
    }

    @GetMapping("/{id}")
    public Result<WarehouseStock> getStockById(@PathVariable String id) {
        return warehouseStockService.getStockById(id)
                .map(Result::success)
                .orElse(Result.error("库存不存在"));
    }

    @GetMapping("/product/{productId}")
    public Result<List<WarehouseStock>> getStocksByProductId(@PathVariable String productId) {
        List<WarehouseStock> stocks = warehouseStockService.getStocksByProductId(productId);
        return Result.success(stocks);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public Result<List<WarehouseStock>> getStocksByWarehouseId(@PathVariable String warehouseId) {
        List<WarehouseStock> stocks = warehouseStockService.getStocksByWarehouseId(warehouseId);
        return Result.success(stocks);
    }

    @GetMapping
    public Result<List<WarehouseStock>> getAllStocks() {
        List<WarehouseStock> stocks = warehouseStockService.getAllStocks();
        return Result.success(stocks);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteStock(@PathVariable String id) {
        warehouseStockService.deleteStock(id);
        return Result.success();
    }
}