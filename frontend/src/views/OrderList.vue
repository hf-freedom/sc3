<template>
  <div class="order-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>

      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="订单号" width="300">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isParentOrder ? 'primary' : 'info'" size="mini">
              {{ scope.row.isParentOrder ? '主订单' : '子订单' }}
            </el-tag>
            <span style="margin-left: 10px; font-family: monospace;">{{ scope.row.id.substring(0, 12) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="parentOrderId" label="父订单号" width="200" v-if="!showParentOnly">
          <template slot-scope="scope">
            <span v-if="scope.row.parentOrderId" style="font-family: monospace; font-size: 12px;">
              {{ scope.row.parentOrderId.substring(0, 12) }}...
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusDescription" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.statusDescription }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="商品数" width="80"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="expireAt" label="过期时间" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 'PENDING_PAYMENT'" :style="{ color: isExpiring(scope.row.expireAt) ? '#F56C6C' : '' }">
              {{ formatTime(scope.row.expireAt) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDetail(scope.row)">详情</el-button>
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
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'OrderList',
  data() {
    return {
      loading: false,
      orders: [],
      showParentOnly: true
    }
  },
  created() {
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const res = await this.$axios.get('/api/orders')
        if (res.data.code === 200) {
          let orders = res.data.data
          if (this.showParentOnly) {
            orders = orders.filter(o => o.isParentOrder)
          }
          this.orders = orders
        }
      } catch (error) {
        this.$message.error('加载订单列表失败')
      } finally {
        this.loading = false
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
    viewDetail(row) {
      this.$router.push(`/orders/${row.id}`)
    },
    async payOrder(row) {
      try {
        const res = await this.$axios.post(`/api/orders/${row.id}/pay`)
        if (res.data.code === 200) {
          this.$message.success('支付成功')
          this.loadOrders()
        } else {
          this.$message.error(res.data.message || '支付失败')
        }
      } catch (error) {
        this.$message.error('支付失败')
      }
    },
    async cancelOrder(row) {
      try {
        await this.$confirm('确定要取消该订单吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const res = await this.$axios.post(`/api/orders/${row.id}/cancel`)
        if (res.data.code === 200) {
          this.$message.success('订单已取消')
          this.loadOrders()
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