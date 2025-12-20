<template>
  <el-menu :default-active="activeMenu" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
    <sidebar-item v-for="route in permission_routes" :key="route.path" :item="route" :base-path="route.path" />
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
// 假设你有一个根据角色过滤后生成的路由列表
const userStore = useUserStore()
const permission_routes = computed(() => userStore.routes)
</script>