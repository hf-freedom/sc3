<template>
  <div class="product-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <el-button type="primary" @click="showAddDialog">添加商品</el-button>
        </div>
      </template>
      
      <el-table :data="products" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="商品ID" width="300">
          <template slot-scope="scope">
            <el-tag type="info" size="mini">{{ scope.row.id.substring(0, 8) }}...</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150"></el-table-column>
        <el-table-column prop="category" label="分类" width="120"></el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.price }}
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
            <el-button size="mini" type="primary" @click="editProduct(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" placeholder="请输入商品名称"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="请输入分类"></el-input>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="库存详情" :visible.sync="stockDialogVisible" width="700px">
      <div v-if="currentProduct">
        <h4 style="margin-bottom: 20px;">商品: {{ currentProduct.name }}</h4>
        <el-table :data="stocks" style="width: 100%">
          <el-table-column prop="warehouseName" label="仓库名称" min-width="150"></el-table-column>
          <el-table-column prop="availableStock" label="可用库存" width="120"></el-table-column>
          <el-table-column prop="lockedStock" label="锁定库存" width="120"></el-table-column>
          <el-table-column label="总库存" width="120">
            <template slot-scope="scope">
              {{ (scope.row.availableStock || 0) + (scope.row.lockedStock || 0) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.id" type="success">已设置</el-tag>
              <el-tag v-else type="info">未设置</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button size="mini" type="primary" @click="editStock(scope.row)">
                {{ scope.row.id ? '调整' : '设置' }}
              </el-button>
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
  name: 'ProductList',
  data() {
    return {
      loading: false,
      products: [],
      warehouses: [],
      stocks: [],
      currentProduct: null,
      dialogVisible: false,
      stockDialogVisible: false,
      editStockDialogVisible: false,
      isEdit: false,
      dialogTitle: '添加商品',
      form: {
        id: null,
        name: '',
        category: '',
        price: 0,
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
    this.loadProducts()
    this.loadWarehouses()
  },
  methods: {
    async loadProducts() {
      this.loading = true
      try {
        const res = await this.$axios.get('/api/products')
        if (res.data.code === 200) {
          this.products = res.data.data
        }
      } catch (error) {
        this.$message.error('加载商品列表失败')
      } finally {
        this.loading = false
      }
    },
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
    showAddDialog() {
      this.isEdit = false
      this.dialogTitle = '添加商品'
      this.form = {
        id: null,
        name: '',
        category: '',
        price: 0,
        enabled: true
      }
      this.dialogVisible = true
    },
    editProduct(product) {
      this.isEdit = true
      this.dialogTitle = '编辑商品'
      this.form = { ...product }
      this.dialogVisible = true
    },
    async saveProduct() {
      if (!this.form.name) {
        this.$message.warning('请输入商品名称')
        return
      }
      try {
        let res
        if (this.isEdit) {
          res = await this.$axios.put(`/api/products/${this.form.id}`, this.form)
        } else {
          res = await this.$axios.post('/api/products', this.form)
        }
        if (res.data.code === 200) {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadProducts()
        } else {
          this.$message.error(res.data.message || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    async viewStocks(product) {
      this.currentProduct = product
      try {
        const res = await this.$axios.get(`/api/stocks/product/${product.id}`)
        if (res.data.code === 200) {
          const existingStocks = res.data.data
          this.stocks = this.warehouses.map(warehouse => {
            const existingStock = existingStocks.find(s => s.warehouseId === warehouse.id)
            if (existingStock) {
              return {
                ...existingStock,
                warehouseName: warehouse.name
              }
            } else {
              return {
                id: null,
                productId: product.id,
                warehouseId: warehouse.id,
                warehouseName: warehouse.name,
                availableStock: 0,
                lockedStock: 0
              }
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
          this.viewStocks(this.currentProduct)
          this.loadProducts()
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