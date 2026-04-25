import Vue from 'vue'
import VueRouter from 'vue-router'
import ProductList from '@/views/ProductList'
import WarehouseList from '@/views/WarehouseList'
import CreateOrder from '@/views/CreateOrder'
import OrderList from '@/views/OrderList'
import OrderDetail from '@/views/OrderDetail'
import StockFlows from '@/views/StockFlows'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/products'
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList
  },
  {
    path: '/warehouses',
    name: 'WarehouseList',
    component: WarehouseList
  },
  {
    path: '/create-order',
    name: 'CreateOrder',
    component: CreateOrder
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: OrderList
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: OrderDetail
  },
  {
    path: '/stock-flows',
    name: 'StockFlows',
    component: StockFlows
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router