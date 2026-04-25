<template>
  <div class="stock-flows">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存流水</span>
        </div>
      </template>

      <el-table :data="flows" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="流水号" width="180">
          <template slot-scope="scope">
            <span style="font-family: monospace; font-size: 12px;">{{ scope.row.id.substring(0, 16) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getFlowType(scope.row.type)">
              {{ getFlowDescription(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productId" label="商品ID" width="180">
          <template slot-scope="scope">
            <span style="font-family: monospace; font-size: 12px;">{{ scope.row.productId.substring(0, 12) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库" width="120"></el-table-column>
        <el-table-column prop="quantity" label="数量" width="80">
          <template slot-scope="scope">
            <span :style="{ color: isNegativeType(scope.row.type) ? '#F56C6C' : '#67C23A' }">
              {{ isNegativeType(scope.row.type) ? '-' : '+' }}{{ scope.row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="可用库存变化" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.beforeAvailable }} → {{ scope.row.afterAvailable }}</span>
            <el-tag v-if="scope.row.beforeAvailable !== scope.row.afterAvailable" :type="scope.row.afterAvailable > scope.row.beforeAvailable ? 'success' : 'danger'" size="mini" style="margin-left: 5px;">
              {{ scope.row.afterAvailable > scope.row.beforeAvailable ? '+' : '' }}{{ scope.row.afterAvailable - scope.row.beforeAvailable }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="锁定库存变化" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.beforeLocked }} → {{ scope.row.afterLocked }}</span>
            <el-tag v-if="scope.row.beforeLocked !== scope.row.afterLocked" :type="scope.row.afterLocked > scope.row.beforeLocked ? 'warning' : 'info'" size="mini" style="margin-left: 5px;">
              {{ scope.row.afterLocked > scope.row.beforeLocked ? '+' : '' }}{{ scope.row.afterLocked - scope.row.beforeLocked }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderId" label="关联订单" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.orderId" style="font-family: monospace; font-size: 12px;">{{ scope.row.orderId.substring(0, 12) }}...</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'StockFlows',
  data() {
    return {
      loading: false,
      flows: [],
      warehouses: []
    }
  },
  created() {
    this.loadWarehouses()
    this.loadStockFlows()
  },
  methods: {
    async loadWarehouses() {
      try {
        const res = await this.$axios.get('/api/warehouses')
        if (res.data.code === 200) {
          this.warehouses = res.data.data
        }
      } catch (error) {
        console.error('加载仓库列表失败', error)
      }
    },
    async loadStockFlows() {
      this.loading = true
      try {
        const res = await this.$axios.get('/api/stock-flows')
        if (res.data.code === 200) {
          this.flows = res.data.data.map(flow => {
            const warehouse = this.warehouses.find(w => w.id === flow.warehouseId)
            return {
              ...flow,
              warehouseName: warehouse ? warehouse.name : flow.warehouseId
            }
          })
        }
      } catch (error) {
        this.$message.error('加载库存流水失败')
      } finally {
        this.loading = false
      }
    },
    getFlowType(type) {
      const typeMap = {
        'LOCK': 'warning',
        'RELEASE': 'info',
        'DEDUCT': 'danger',
        'ROLLBACK': 'success'
      }
      return typeMap[type] || 'info'
    },
    getFlowDescription(type) {
      const descMap = {
        'LOCK': '锁定',
        'RELEASE': '释放',
        'DEDUCT': '扣减',
        'ROLLBACK': '回滚'
      }
      return descMap[type] || type
    },
    isNegativeType(type) {
      return ['DEDUCT', 'LOCK'].includes(type)
    },
    formatTime(timestamp) {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN')
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