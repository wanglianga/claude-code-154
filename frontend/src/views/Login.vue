<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="brand">
        <el-icon :size="34" color="#1f3a5f"><Shop /></el-icon>
        <h1>社区公益集市管理平台</h1>
        <p>摊主报名 · 社区审核 · 智能分配 · 现场核销 · 公益公示</p>
      </div>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password
                    placeholder="密码" :prefix-icon="Lock" @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">
          登 录
        </el-button>
      </el-form>
      <el-divider>测试账号（点击填充）</el-divider>
      <div class="accounts">
        <el-tag v-for="a in accounts" :key="a.u" class="acc-tag" :type="a.type"
                @click="fill(a)">{{ a.label }}：{{ a.u }} / {{ a.p }}</el-tag>
      </div>
      <div class="public-link">
        <router-link to="/public" target="_blank">居民无需登录，点此查看公益公示 →</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api.js'
import { useAuth } from '../store/auth.js'

const router = useRouter()
const auth = useAuth()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const accounts = [
  { label: '社区管理员', u: 'admin', p: 'admin123', type: 'danger' },
  { label: '工作人员', u: 'staff', p: 'staff123', type: 'warning' },
  { label: '摊主·张阿姨', u: 'vendor1', p: 'vendor123', type: 'success' }
]

function fill(a) {
  form.username = a.u
  form.password = a.p
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await api.post('/auth/login', form)
    auth.setLogin(res.token, res.user)
    ElMessage.success(`欢迎，${res.user.name}`)
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1f3a5f 0%, #3a6ea5 60%, #6ba8d9 100%);
}
.login-card {
  width: 430px; background: #fff; border-radius: 14px; padding: 34px 38px;
  box-shadow: 0 18px 50px rgba(0, 0, 0, .25);
}
.brand { text-align: center; margin-bottom: 20px; }
.brand h1 { font-size: 20px; margin: 10px 0 6px; color: #1f3a5f; }
.brand p { margin: 0; color: #909399; font-size: 13px; }
.accounts { display: flex; flex-wrap: wrap; gap: 8px; }
.acc-tag { cursor: pointer; }
.public-link { text-align: center; margin-top: 16px; font-size: 13px; }
</style>
