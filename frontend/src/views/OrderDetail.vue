<template>
  <div class="order-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <div v-if="order">
        <el-descriptions title="订单信息" :column="3" border>
          <el-descriptions-item label="订单号">
            <span style="font-family: monospace;">{{ order.id }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单类型">
            <el-tag :type="order.parentOrderId ? 'info' : 'primary'">
              {{ order.parentOrderId ? '子订单' : '主订单' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">
              {{ order.statusDescription }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="父订单号" v-if="order.parentOrderId">
            <span style="font-family: monospace; font-size: 12px;">{{ order.parentOrderId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="仓库" v-if="order.warehouseName">
            {{ order.warehouseName }}
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span style="font-size: 18px; color: #F56C6C; font-weight: bold;">
              ¥{{ order.totalAmount }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(order.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatTime(order.updatedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="过期时间" v-if="order.status === 'PENDING_PAYMENT'">
            <span :style="{ color: isExpiring(order.expireAt) ? '#F56C6C' : '' }">
              {{ formatTime(order.expireAt) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider></el-divider>

        <h4 style="margin-bottom: 15px;">订单商品</h4>
        <el-table :data="order.items" style="width: 100%" border>
          <el-table-column prop="productName" label="商品名称" min-width="150"></el-table-column>
          <el-table-column prop="price" label="单价" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80"></el-table-column>
          <el-table-column prop="subtotal" label="小计" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.subtotal }}
            </template>
          </el-table-column>
          <el-table-column prop="warehouseName" label="仓库" width="120">
            <template slot-scope="scope">
              {{ scope.row.warehouseName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="deliveredQuantity" label="已发货数量" width="100"></el-table-column>
        </el-table>

        <el-divider></el-divider>

        <div v-if="isParentOrder && childOrders.length > 0">
          <h4 style="margin-bottom: 15px;">子订单列表</h4>
          <el-table :data="childOrders" style="width: 100%" border>
            <el-table-column prop="id" label="子订单号" width="280">
              <template slot-scope="scope">
                <span style="font-family: monospace; font-size: 12px;">{{ scope.row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="仓库" width="120"></el-table-column>
            <el-table-column prop="statusDescription" label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ scope.row.statusDescription }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="金额" width="100">
              <template slot-scope="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template slot-scope="scope">
                {{ formatTime(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template slot-scope="scope">
                <el-button size="mini" @click="viewChildDetail(scope.row)">详情</el-button>
                <el-button 
                  size="mini" 
                  type="primary" 
                  v-if="scope.row.status === 'PENDING_PAYMENT'"
                  @click="payOrder(scope.row)">
                  支付
                </el-button>
                <el-button 
                  size="mini" 
                  type="warning" 
                  v-if="scope.row.status === 'PENDING_PAYMENT'"
                  @click="cancelOrder(scope.row)">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-divider></el-divider>

        <div style="text-align: center;">
          <el-button 
            type="primary" 
            size="large"
            v-if="order.status === 'PENDING_PAYMENT'"
            @click="payOrder(order)">
            立即支付
          </el-button>
          <el-button 
            type="warning" 
            size="large"
            v-if="order.status === 'PENDING_PAYMENT'"
            @click="cancelOrder(order)">
            取消订单
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'OrderDetail',
  data() {
    return {
      order: null,
      childOrders: []
    }
  },
  computed: {
    isParentOrder() {
      return this.order && !this.order.parentOrderId
    }
  },
  created() {
    this.loadOrderDetail()
  },
  methods: {
    async loadOrderDetail() {
      const orderId = this.$route.params.id
      try {
        const res = await this.$axios.get(`/api/orders/${orderId}`)
        if (res.data.code === 200) {
          this.order = res.data.data
          
          if (!this.order.parentOrderId) {
            const childRes = await this.$axios.get(`/api/orders/parent/${orderId}`)
            if (childRes.data.code === 200) {
              this.childOrders = childRes.data.data
            }
          }
        } else {
          this.$message.error(res.data.message || '加载订单详情失败')
        }
      } catch (error) {
        this.$message.error('加载订单详情失败')
      }
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING_PAYMENT': 'warning',
        'PAID': 'success',
        'CANCELLED': 'info',
        'SHIPPED': 'primary',
        'PARTIALLY_SHIPPED': 'primary',
        'COMPLETED': 'success',
        'REFUNDING': 'danger'
      }
      return typeMap[status] || 'info'
    },
    formatTime(timestamp) {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN')
    },
    isExpiring(expireAt) {
      if (!expireAt) return false
      const remaining = expireAt - Date.now()
      return remaining < 5 * 60 * 1000
    },
    viewChildDetail(row) {
      this.$router.push(`/orders/${row.id}`)
    },
    async payOrder(orderInfo) {
      try {
        const res = await this.$axios.post(`/api/orders/${orderInfo.id}/pay`)
        if (res.data.code === 200) {
          this.$message.success('支付成功')
          this.loadOrderDetail()
        } else {
          this.$message.error(res.data.message || '支付失败')
        }
      } catch (error) {
        this.$message.error('支付失败')
      }
    },
    async cancelOrder(orderInfo) {
      try {
        await this.$confirm('确定要取消该订单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const res = await this.$axios.post(`/api/orders/${orderInfo.id}/cancel`)
        if (res.data.code === 200) {
          this.$message.success('订单已取消')
          this.loadOrderDetail()
        } else {
          this.$message.error(res.data.message || '取消失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('取消订单失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>