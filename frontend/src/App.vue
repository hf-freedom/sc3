<template>
  <div id="app">
    <el-container>
      <el-header>
        <el-menu :default-active="activeMenu" mode="horizontal" router>
          <el-menu-item index="/products">
            <i class="el-icon-goods"></i>
            <span>商品列表</span>
          </el-menu-item>
          <el-menu-item index="/warehouses">
            <i class="el-icon-office-building"></i>
            <span>仓库管理</span>
          </el-menu-item>
          <el-menu-item index="/create-order">
            <i class="el-icon-edit"></i>
            <span>创建订单</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <i class="el-icon-s-order"></i>
            <span>订单列表</span>
          </el-menu-item>
          <el-menu-item index="/stock-flows">
            <i class="el-icon-s-data"></i>
            <span>库存流水</span>
          </el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
}

.el-header {
  background-color: #409EFF;
  padding: 0;
}

.el-menu--horizontal {
  border-bottom: none;
}

.el-menu--horizontal > .el-menu-item {
  color: #fff;
  border-bottom: none;
}

.el-menu--horizontal > .el-menu-item:hover,
.el-menu--horizontal > .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.2);
  border-bottom: none;
}

.el-main {
  padding: 20px;
}
</style>