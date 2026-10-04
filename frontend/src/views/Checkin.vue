<template>
  <div class="checkin-wrap">
    <el-card class="box">
      <template #header><b>摊主入场核销</b></template>

      <template v-if="!auth.isStaff.value">
        <el-result icon="warning" title="工作人员登录后核销" sub-title="此页面为现场工作人员扫码核销入口">
          <template #extra>
            <el-tag>核销码：{{ code }}</el-tag>
            <div style="margin-top:14px">
              <el-button type="primary" @click="goLogin">前往登录</el-button>
            </div>
          </template>
        </el-result>
      </template>

      <template v-else>
        <el-skeleton :rows="4" animated :loading="loading">
          <template v-if="info">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="摊主">{{ info.vendor.name }}（信用 {{ info.vendor.creditScore }}）</el-descriptions-item>
              <el-descriptions-item label="摊位">{{ info.stall.code }} · {{ info.stall.trafficLabel }}</el-descriptions-item>
              <el-descriptions-item label="报名品类">{{ info.application.categoryLabel }}</el-descriptions-item>
              <el-descriptions-item label="核销码">{{ info.assignment.checkinCode }}</el-descriptions-item>
            </el-descriptions>
            <el-tag v-if="info.assignment.checkedIn" type="info" style="margin-top:14px;width:100%;text-align:center">
              已于 {{ info.assignment.checkInAt }} 核销
            </el-tag>
            <el-button v-else type="primary" size="large" style="width:100%;margin-top:14px" @click="doCheckin">
              确认核销入场
            </el-button>
            <el-button style="width:100%;margin-top:10px" @click="$router.push('/onsite')">
              进入现场工作台
            </el-button>
          </template>
        </el-skeleton>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api.js'
import { useAuth } from '../store/auth.js'

const route = useRoute()
const router = useRouter()
const auth = useAuth()
const code = ref(route.query.code || '')
const info = ref(null)
const loading = ref(false)

function goLogin() {
  router.push({ pathname: '/login', query: { redirect: `/checkin?code=${code.value}` } })
}

onMounted(async () => {
  if (auth.isStaff.value && code.value) {
    loading.value = true
    try {
      info.value = await api.get(`/checkin/${code.value}`)
    } finally {
      loading.value = false
    }
  }
})

async function doCheckin() {
  info.value = await api.post(`/checkin/${code.value}`)
  ElMessage.success('核销成功')
}
</script>

<style scoped>
.checkin-wrap { min-height: 100vh; background: #f5f7fa; display: flex; align-items: center; justify-content: center; }
.box { width: 430px; }
</style>
