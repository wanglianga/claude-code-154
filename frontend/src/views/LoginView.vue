<template>
  <div style="min-height:100vh; display:flex; align-items:center; justify-content:center;
              background: linear-gradient(135deg,#1f8a70 0%,#2d6cdf 100%);">
    <el-card style="width: 420px;">
      <template #header>
        <div style="text-align:center; font-size:18px; font-weight:700;">🤝 社区公益集市平台</div>
        <div style="text-align:center; color:#909399; font-size:12px; margin-top:4px;">
          摊主审核 · 摊位分配 · 收款捐赠 · 公益公示
        </div>
      </template>

      <el-tabs v-model="tab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="form" @keyup.enter="doLogin">
            <el-form-item>
              <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock"
                        size="large" show-password />
            </el-form-item>
            <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="doLogin">
              登 录
            </el-button>
          </el-form>
          <el-alert style="margin-top:12px;" type="info" :closable="false">
            <div style="font-size:12px; line-height:1.8;">
              演示账号：社区 admin/admin123<br/>
              现场工作人员 staff/staff123<br/>
              摊主 laowang / 123456（或自行注册）
            </div>
          </el-alert>
        </el-tab-pane>

        <el-tab-pane label="摊主注册" name="register">
          <el-form :model="reg">
            <el-form-item><el-input v-model="reg.username" placeholder="设置用户名" /></el-form-item>
            <el-form-item><el-input v-model="reg.password" type="password" placeholder="设置密码" show-password /></el-form-item>
            <el-form-item><el-input v-model="reg.realName" placeholder="摊主姓名/摊位名" /></el-form-item>
            <el-form-item><el-input v-model="reg.phone" placeholder="联系电话" /></el-form-item>
            <el-button type="success" style="width:100%" :loading="loading" @click="doRegister">注册并成为摊主</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div style="text-align:center; margin-top:12px;">
        <router-link to="/public" style="font-size:13px;">居民公益公示入口（无需登录）→</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { api } from '../api/client'

const router = useRouter()
const auth = useAuthStore()
const tab = ref('login')
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const reg = reactive({ username: '', password: '', realName: '', phone: '' })

async function doLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!reg.username || !reg.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    await api.post('/auth/register', { ...reg })
    ElMessage.success('注册成功，请登录')
    tab.value = 'login'
    form.username = reg.username
    form.password = reg.password
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>
