<template>
  <el-container style="height: 100vh">
    <el-aside width="230px" class="aside">
      <div class="logo">
        <el-icon :size="22" color="#fff"><Shop /></el-icon>
        <span>公益集市管理平台</span>
      </div>
      <el-menu :default-active="$route.path" router background-color="#1f3a5f"
               text-color="#c8d6e5" active-text-color="#ffd04b">
        <el-menu-item index="/home"><el-icon><House /></el-icon><span>工作台</span></el-menu-item>

        <el-sub-menu v-if="auth.isVendor.value" index="vendor">
          <template #title><el-icon><Goods /></el-icon><span>摊主服务</span></template>
          <el-menu-item index="/vendor/apply">摊位报名</el-menu-item>
          <el-menu-item index="/vendor/pass">我的入场凭证</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="auth.isStaff.value" index="prepare">
          <template #title><el-icon><DocumentChecked /></el-icon><span>筹备与审核</span></template>
          <el-menu-item v-if="auth.isAdmin.value" index="/admin/events">活动与摊位管理</el-menu-item>
          <el-menu-item index="/review">报名审核</el-menu-item>
          <el-menu-item index="/assign">摊位分配</el-menu-item>
          <el-menu-item v-if="auth.isAdmin.value" index="/admin/vendors">摊主信用档案</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="auth.isStaff.value" index="onsite">
          <template #title><el-icon><Aim /></el-icon><span>现场管理</span></template>
          <el-menu-item index="/onsite">核销与巡检</el-menu-item>
          <el-menu-item index="/finance">收款与捐赠</el-menu-item>
          <el-menu-item index="/complaints">投诉处理</el-menu-item>
          <el-menu-item index="/issues">异常管理</el-menu-item>
          <el-menu-item index="/traffic">客流统计</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="auth.isStaff.value" index="review">
          <template #title><el-icon><DataAnalysis /></el-icon><span>复盘与公示</span></template>
          <el-menu-item index="/archive">活动统一档案</el-menu-item>
          <el-menu-item index="/dashboard">复盘与公示</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/public"><el-icon><View /></el-icon><span>居民公示页</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="event-switch" v-if="auth.isStaff.value && ev.state.events.length">
          <el-tag type="warning" effect="dark" size="small">当前活动</el-tag>
          <el-select :model-value="ev.currentId.value" size="small" style="width: 260px"
                     @change="v => ev.select(v)">
            <el-option v-for="e in ev.state.events" :key="e.id" :label="`${e.name}（${e.statusLabel}）`" :value="e.id" />
          </el-select>
        </div>
        <div class="spacer" />
        <el-tag :type="roleTag" effect="plain">{{ auth.state.user?.roleLabel }}</el-tag>
        <el-dropdown @command="onCommand">
          <span class="user-name">
            <el-icon><UserFilled /></el-icon>
            {{ auth.state.user?.name }}
            <el-tag size="small" type="success" effect="plain" style="margin-left:6px">
              信用 {{ auth.state.user?.creditScore }}
            </el-tag>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="public">居民公示页</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api.js'
import { useAuth } from '../store/auth.js'
import { useEvent } from '../store/event.js'

const auth = useAuth()
const ev = useEvent()
const router = useRouter()

const roleTag = computed(() => ({
  ADMIN: 'danger', STAFF: 'warning', VENDOR: 'success'
}[auth.role.value] || 'info'))

onMounted(async () => {
  const list = await api.get('/events')
  ev.setEvents(list)
})

function onCommand(cmd) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  } else if (cmd === 'public') {
    window.open('/public', '_blank')
  }
}
</script>

<style scoped>
.aside { background: #1f3a5f; overflow-y: auto; }
.logo {
  height: 60px; display: flex; align-items: center; gap: 10px;
  color: #fff; font-size: 16px; font-weight: 600; padding: 0 18px;
  background: #16304f;
}
.aside :deep(.el-menu) { border-right: none; }
.header {
  background: #fff; display: flex; align-items: center; gap: 12px;
  border-bottom: 1px solid #e4e7ed;
}
.spacer { flex: 1; }
.user-name { cursor: pointer; display: flex; align-items: center; gap: 4px; color: #303133; }
.main { background: #f5f7fa; padding: 18px; }
</style>
