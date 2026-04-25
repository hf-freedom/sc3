<template>
  <div class="create-order">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建订单</span>
        </div>
      </template>

      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="商品">
          <el-table :data="orderForm.items" style="width: 100%" border>
            <el-table-column prop="productName" label="商品名称" min-width="150"></el-table-column>
            <el-table-column prop="price" label="单价" width="120">
              <template slot-scope="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="totalStock" label="总库存" width="100"></el-table-column>
            <el-table-column prop="quantity" label="购买数量" width="150">
              <template slot-scope="scope">
                <el-input-number 
                  v-model="scope.row.quantity" 
                  :min="1" 
                  :max="scope.row.totalStock"
                  @change="calculateTotal">
                </el-input-number>
              </template>
            </el-table-column>
            <el-table-column prop="subtotal" label="小计" width="120">
              <template slot-scope="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template slot-scope="scope">
                <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="removeItem(scope.$index)"></el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" style="margin-top: 15px;" @click="showProductSelector">
            <i class="el-icon-plus"></i> 添加商品
          </el-button>
        </el-form-item>

        <el-divider></el-divider>

        <el-form-item label="订单金额">
          <el-input v-model="totalAmount" readonly style="width: 200px">
            <template slot="prepend">¥</template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">
            提交订单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog title="选择商品" :visible.sync="productSelectorVisible" width="800px">
      <el-table :data="products" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150"></el-table-column>
        <el-table-column prop="category" label="分类" width="120"></el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template slot-scope="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="totalStock" label="总库存" width="100"></el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="productSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelection">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'CreateOrder',
  data() {
    return {
      products: [],
      selectedProducts: [],
      productSelectorVisible: false,
      submitting: false,
      orderForm: {
        items: []
      }
    }
  },
  computed: {
    totalAmount() {
      return this.orderForm.items
        .reduce((sum, item) => sum + (item.price * item.quantity), 0)
        .toFixed(2)
    }
  },
  created() {
    this.loadProducts()
  },
  methods: {
    async loadProducts() {
      try {
        const res = await this.$axios.get('/api/products?enabledOnly=true')
        if (res.data.code === 200) {
          this.products = await Promise.all(res.data.data.map(async (product) => {
            const stockRes = await this.$axios.get(`/api/stocks/product/${product.id}`)
            let totalStock = 0
            if (stockRes.data.code === 200) {
              totalStock = stockRes.data.data.reduce((sum, s) => sum + (s.availableStock || 0), 0)
            }
            return {
              ...product,
              totalStock
            }
          }))
        }
      } catch (error) {
        this.$message.error('加载商品列表失败')
      }
    },
    showProductSelector() {
      this.selectedProducts = []
      this.productSelectorVisible = true
    },
    handleSelectionChange(selection) {
      this.selectedProducts = selection
    },
    confirmSelection() {
      this.selectedProducts.forEach(product => {
        const existing = this.orderForm.items.find(item => item.productId === product.id)
        if (!existing) {
          this.orderForm.items.push({
            productId: product.id,
            productName: product.name,
            price: product.price,
            totalStock: product.totalStock,
            quantity: 1
          })
        }
      })
      this.productSelectorVisible = false
    },
    removeItem(index) {
      this.orderForm.items.splice(index, 1)
    },
    calculateTotal() {
    },
    async submitOrder() {
      if (this.orderForm.items.length === 0) {
        this.$message.warning('请至少选择一个商品')
        return
      }

      this.submitting = true
      try {
        const request = {
          items: this.orderForm.items.map(item => ({
            productId: item.productId,
            quantity: item.quantity
          }))
        }

        const res = await this.$axios.post('/api/orders', request)
        if (res.data.code === 200) {
          this.$message.success('订单创建成功')
          const parentOrder = res.data.data.find(o => !o.parentOrderId)
          if (parentOrder) {
            this.$router.push(`/orders/${parentOrder.id}`)
          } else {
            this.$router.push('/orders')
          }
        } else {
          this.$message.error(res.data.message || '创建订单失败')
        }
      } catch (error) {
        this.$message.error('创建订单失败')
      } finally {
        this.submitting = false
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