package com.example.ordersystem.task;

import com.example.ordersystem.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderTimeoutTask.class);

    @Autowired
    private OrderService orderService;

    @Scheduled(fixedRate = 60000)
    public void processExpiredOrders() {
        logger.info("开始检查超时订单...");
        try {
            orderService.processExpiredOrders();
            logger.info("超时订单检查完成");
        } catch (Exception e) {
            logger.error("处理超时订单时发生错误", e);
        }
    }
}