<template>
  <div class="public-wrap">
    <div class="public-header">
      <h1>社区公益集市 · 公示平台</h1>
      <p>公开每一场公益集市的捐赠总额、资金用途、摊主履约与异常处理，让下一次招募更可信</p>
      <router-link to="/login" class="staff-link">工作人员 / 摊主登录 →</router-link>
    </div>

    <div class="container">
      <el-row :gutter="18">
        <el-col v-for="e in events" :key="e.id" :span="8">
          <el-card shadow="hover" class="event-card" @click="$router.push(`/public/events/${e.id}`)">
            <el-tag type="danger" effect="dark" size="small">已公示</el-tag>
            <h3>{{ e.name }}</h3>
            <p class="muted">{{ e.location }} · {{ e.startTime }}</p>
            <div class="donation">
              <span class="lbl">公示捐赠总额</span>
              <span class="amount">¥{{ e.totalDonation }}</span>
            </div>
            <el-button type="primary" plain style="width:100%;margin-top:12px">查看公益明细与履约公示</el-button>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!events.length" description="暂无已公示的活动" />

      <el-card class="complaint-entry">
        <template #header><b>我要向社区反映情况</b></template>
        <el-form :inline="true">
          <el-form-item label="活动">
            <el-select v-model="form.eventId" placeholder="选择活动" style="width:240px">
              <el-option v-for="e in events" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="称呼"><el-input v-model="form.complainant" placeholder="可匿名" /></el-form-item>
          <el-form-item label="联系方式"><el-input v-model="form.contact" /></el-form-item>
          <el-form-item label="内容" style="width:420px">
            <el-input v-model="form.content" placeholder="您的意见或投诉" />
          </el-form-item>
          <el-button type="danger" @click="submit">提交</el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api.js'

const events = ref([])
const form = reactive({ eventId: null, complainant: '', contact: '', content: '' })

onMounted(async () => { events.value = await api.get('/public/events') })

async function submit() {
  if (!form.eventId || !form.content) { ElMessage.warning('请选择活动并填写反映内容'); return }
  await api.post(`/public/events/${form.eventId}/complaints`, {
    complainant: form.complainant || '匿名居民', contact: form.contact, content: form.content
  })
  ElMessage.success('已提交，社区将跟进处理')
  form.content = ''
}
</script>

<style scoped>
.public-wrap { min-height: 100vh; background: #f5f7fa; }
.public-header {
  background: linear-gradient(135deg, #1f3a5f, #3a6ea5);
  color: #fff; text-align: center; padding: 46px 20px 40px; position: relative;
}
.public-header h1 { margin: 0 0 10px; font-size: 28px; }
.public-header p { margin: 0; opacity: .85; }
.staff-link { position: absolute; right: 24px; top: 20px; color: #ffd04b; font-size: 14px; }
.container { max-width: 1100px; margin: -24px auto 40px; padding: 0 16px; }
.event-card { cursor: pointer; margin-bottom: 18px; }
.event-card h3 { margin: 10px 0 6px; color: #1f3a5f; }
.donation { display: flex; justify-content: space-between; align-items: baseline; margin-top: 10px; }
.donation .lbl { color: #909399; font-size: 13px; }
.donation .amount { color: #e6a23c; font-size: 26px; font-weight: 700; }
.complaint-entry { margin-top: 20px; }
.muted { color: #909399; font-size: 13px; }
</style>
