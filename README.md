# 订单拆单和库存锁定系统

## 项目概述

这是一个复杂订单拆单和库存锁定系统，包含前端 Vue 应用和后端 SpringBoot 应用。

## 功能特性

### 商品管理
- 商品包含：商品ID、名称、分类、价格、库存、是否启用
- 支持添加、编辑、删除商品
- 支持查看和调整各仓库库存

### 仓库管理
- 仓库包含：仓库ID、仓库名称、仓库优先级、是否启用
- 支持多仓库库存管理
- 每个商品在不同仓库有不同库存

### 订单管理
- 支持创建订单，订单可包含多个商品
- **自动拆单功能**：根据仓库优先级自动拆单
  - 例：商品A要买10件，仓库1有6件，仓库2有10件 → 拆成2个子订单
- **库存锁定**：下单时锁定库存，防止超卖
- 订单状态：待支付、已支付、已取消、已发货、部分发货、已完成、退款中

### 超时机制
- 订单30分钟未支付自动取消
- 自动释放锁定的库存

### 库存流水
- 记录每一次库存变化：锁定、释放、扣减、回滚
- 可按订单、商品查询流水记录

## 技术栈

### 后端
- SpringBoot 2.7.18
- Java 8
- Lombok
- 内存存储（ConcurrentHashMap）

### 前端
- Vue 2.6
- Vue Router 3
- Element UI
- Axios

## 端口配置

- 后端端口：8001
- 前端端口：3001

## 启动方式

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

或编译后运行：

```bash
cd backend
mvn clean package
java -jar target/order-system-1.0.0.jar
```

### 前端启动

```bash
cd frontend
npm install
npm run serve
```

## 默认测试数据

系统启动时会自动创建以下测试数据：

### 商品
- 商品A（电子产品，¥100.00）
- 商品B（服装，¥50.00）
- 商品C（食品，¥20.00）

### 仓库
- 主仓库（优先级1）
- 备用仓库（优先级2）
- 区域仓库（优先级3）

### 库存（每个商品在各仓库的可用库存）
- 主仓库：10件
- 备用仓库：20件
- 区域仓库：30件

## API 接口

### 商品管理
- `GET /api/products` - 获取商品列表
- `GET /api/products?enabledOnly=true` - 获取启用的商品列表
- `GET /api/products/{id}` - 获取商品详情
- `POST /api/products` - 创建商品
- `PUT /api/products/{id}` - 更新商品
- `DELETE /api/products/{id}` - 删除商品

### 仓库管理
- `GET /api/warehouses` - 获取仓库列表
- `GET /api/warehouses/{id}` - 获取仓库详情
- `POST /api/warehouses` - 创建仓库
- `PUT /api/warehouses/{id}` - 更新仓库
- `DELETE /api/warehouses/{id}` - 删除仓库

### 库存管理
- `GET /api/stocks` - 获取所有库存
- `GET /api/stocks/product/{productId}` - 获取商品在各仓库的库存
- `GET /api/stocks/warehouse/{warehouseId}` - 获取仓库所有商品库存
- `POST /api/stocks` - 创建/更新库存

### 订单管理
- `GET /api/orders` - 获取订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `GET /api/orders/parent/{parentOrderId}` - 获取子订单列表
- `POST /api/orders` - 创建订单
- `POST /api/orders/{id}/pay` - 支付订单
- `POST /api/orders/{id}/cancel` - 取消订单

### 库存流水
- `GET /api/stock-flows` - 获取所有流水
- `GET /api/stock-flows/order/{orderId}` - 按订单查询流水
- `GET /api/stock-flows/product/{productId}` - 按商品查询流水
