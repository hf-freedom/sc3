<template>
  <div class="warehouse-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>仓库列表</span>
          <el-button type="primary" @click="showAddDialog">添加仓库</el-button>
        </div>
      </template>
      
      <el-table :data="warehouses" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="仓库ID" width="300">
          <template slot-scope="scope">
            <el-tag type="info" size="mini">{{ scope.row.id.substring(0, 8) }}...</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="仓库名称" min-width="150"></el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.priority === 1 ? 'danger' : scope.row.priority === 2 ? 'warning' : 'info'">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewStocks(scope.row)">库存</el-button>
            <el-button size="mini" type="primary" @click="editWarehouse(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="仓库名称">
          <el-input v-model="form.name" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="1" style="width: 100%"></el-input-number>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            数字越小，优先级越高（拆单时优先分配）
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWarehouse">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="stockDialogTitle" :visible.sync="stockDialogVisible" width="700px">
      <div v-if="currentWarehouse">
        <h4 style="margin-bottom: 20px;">仓库: {{ currentWarehouse.name }}</h4>
        <el-table :data="stocks" style="width: 100%">
          <el-table-column prop="productName" label="商品名称" min-width="150"></el-table-column>
          <el-table-column prop="availableStock" label="可用库存" width="120"></el-table-column>
          <el-table-column prop="lockedStock" label="锁定库存" width="120"></el-table-column>
          <el-table-column label="总库存" width="120">
            <template slot-scope="scope">
              {{ (scope.row.availableStock || 0) + (scope.row.lockedStock || 0) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button size="mini" @click="editStock(scope.row)">调整</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog title="调整库存" :visible.sync="editStockDialogVisible" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="可用库存">
          <el-input-number v-model="stockForm.availableStock" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editStockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStock">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'WarehouseList',
  data() {
    return {
      loading: false,
      warehouses: [],
      products: [],
      stocks: [],
      currentWarehouse: null,
      dialogVisible: false,
      stockDialogVisible: false,
      editStockDialogVisible: false,
      stockDialogTitle: '仓库库存',
      isEdit: false,
      dialogTitle: '添加仓库',
      form: {
        id: null,
        name: '',
        priority: 1,
        enabled: true
      },
      stockForm: {
        id: null,
        productId: null,
        warehouseId: null,
        availableStock: 0
      }
    }
  },
  created() {
    this.loadWarehouses()
    this.loadProducts()
  },
  methods: {
    async loadWarehouses() {
      this.loading = true
      try {
        const res = await this.$axios.get('/api/warehouses')
        if (res.data.code === 200) {
          this.warehouses = res.data.data
        }
      } catch (error) {
        this.$message.error('加载仓库列表失败')
      } finally {
        this.loading = false
      }
    },
    async loadProducts() {
      try {
        const res = await this.$axios.get('/api/products')
        if (res.data.code === 200) {
          this.products = res.data.data
        }
      } catch (error) {
        console.error('加载商品列表失败', error)
      }
    },
    showAddDialog() {
      this.isEdit = false
      this.dialogTitle = '添加仓库'
      this.form = {
        id: null,
        name: '',
        priority: this.warehouses.length + 1,
        enabled: true
      }
      this.dialogVisible = true
    },
    editWarehouse(warehouse) {
      this.isEdit = true
      this.dialogTitle = '编辑仓库'
      this.form = { ...warehouse }
      this.dialogVisible = true
    },
    async saveWarehouse() {
      if (!this.form.name) {
        this.$message.warning('请输入仓库名称')
        return
      }
      try {
        let res
        if (this.isEdit) {
          res = await this.$axios.put(`/api/warehouses/${this.form.id}`, this.form)
        } else {
          res = await this.$axios.post('/api/warehouses', this.form)
        }
        if (res.data.code === 200) {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadWarehouses()
        } else {
          this.$message.error(res.data.message || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    async viewStocks(warehouse) {
      this.currentWarehouse = warehouse
      this.stockDialogTitle = `仓库库存 - ${warehouse.name}`
      try {
        const res = await this.$axios.get(`/api/stocks/warehouse/${warehouse.id}`)
        if (res.data.code === 200) {
          this.stocks = res.data.data.map(stock => {
            const product = this.products.find(p => p.id === stock.productId)
            return {
              ...stock,
              productName: product ? product.name : stock.productId
            }
          })
        }
      } catch (error) {
        this.$message.error('加载库存信息失败')
      }
      this.stockDialogVisible = true
    },
    editStock(stock) {
      this.stockForm = {
        id: stock.id,
        productId: stock.productId,
        warehouseId: stock.warehouseId,
        availableStock: stock.availableStock
      }
      this.editStockDialogVisible = true
    },
    async saveStock() {
      try {
        const res = await this.$axios.post('/api/stocks', this.stockForm)
        if (res.data.code === 200) {
          this.$message.success('库存调整成功')
          this.editStockDialogVisible = false
          this.viewStocks(this.currentWarehouse)
          this.loadWarehouses()
        } else {
          this.$message.error(res.data.message || '调整失败')
        }
      } catch (error) {
        this.$message.error('调整库存失败')
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