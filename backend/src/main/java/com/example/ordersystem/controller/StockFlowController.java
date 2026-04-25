package com.example.ordersystem.controller;

import com.example.ordersystem.common.Result;
import com.example.ordersystem.entity.StockFlow;
import com.example.ordersystem.service.StockFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-flows")
@CrossOrigin(origins = "*")
public class StockFlowController {

    @Autowired
    private StockFlowService stockFlowService;

    @GetMapping
    public Result<List<StockFlow>> getAllStockFlows() {
        List<StockFlow> flows = stockFlowService.getAllStockFlows();
        return Result.success(flows);
    }

    @GetMapping("/order/{orderId}")
    public Result<List<StockFlow>> getStockFlowsByOrderId(@PathVariable String orderId) {
        List<StockFlow> flows = stockFlowService.getStockFlowsByOrderId(orderId);
        return Result.success(flows);
    }

    @GetMapping("/product/{productId}")
    public Result<List<StockFlow>> getStockFlowsByProductId(@PathVariable String productId) {
        List<StockFlow> flows = stockFlowService.getStockFlowsByProductId(productId);
        return Result.success(flows);
    }
}