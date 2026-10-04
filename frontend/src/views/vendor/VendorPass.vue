<template>
  <div>
    <h2 class="page-title">我的入场凭证</h2>
    <el-row :gutter="16" v-if="pass">
      <el-col :span="9">
        <el-card class="pass-card">
          <template #header><b>摊主入场二维码</b></template>
          <div class="qr-box">
            <img v-if="qrBlob" :src="qrBlob" alt="入场二维码" />
            <div class="code">核销码：{{ pass.assignment.checkinCode }}</div>
            <el-tag :type="pass.assignment.checkedIn ? 'info' : 'success'" size="large">
              {{ pass.assignment.checkedIn ? `已于 ${pass.assignment.checkInAt} 核销入场` : '待核销' }}
            </el-tag>
          </div>
          <el-descriptions :column="1" border size="small" style="margin-top:14px">
            <el-descriptions-item label="摊位">{{ pass.stall.code }}（{{ zoneText }}）</el-descriptions-item>
            <el-descriptions-item label="位置">{{ pass.stall.trafficLabel }} · {{ pass.stall.sizeSqm }}㎡</el-descriptions-item>
            <el-descriptions-item label="品类">{{ pass.application.categoryLabel }}</el-descriptions-item>
            <el-descriptions-item label="收款方式">{{ pass.application.paymentMethod }}</el-descriptions-item>
            <el-descriptions-item label="捐赠承诺">{{ pass.application.donationPercent }}% 营业额</el-descriptions-item>
          </el-descriptions>
          <el-alert type="info" :closable="false" show-icon style="margin-top:12px"
                    title="请截图保存二维码，活动当天凭码核销入场并领取物资。" />
        </el-card>
      </el-col>
      <el-col :span="15">
        <el-card header="物资领取清单">
          <el-table :data="pass.supplies" border>
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="name" label="物资" />
            <el-table-column label="数量" width="120">
              <template #default="{ row }">{{ row.qty }} {{ row.unit }}</template>
            </el-table-column>
          </el-table>
          <el-tag v-if="pass.assignment.suppliesClaimed" type="success" style="margin-top:12px">
            物资已于 {{ pass.assignment.suppliesClaimedAt }} 全部领取
          </el-tag>
          <el-tag v-else type="warning" style="margin-top:12px">核销入场后在物资处领取</el-tag>
        </el-card>
        <el-card header="分配说明" style="margin-top:16px">
          <p class="reason">{{ pass.assignment.reason }}</p>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-else description="报名通过并完成摊位分配后，这里会显示入场二维码和物资清单" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api.js'
import { useAuth } from '../../store/auth.js'
import { useEvent } from '../../store/event.js'

const route = useRoute()
const auth = useAuth()
const ev = useEvent()
const pass = ref(null)
const qrBlob = ref('')

const zoneText = computed(() => {
  if (!pass.value) return ''
  return { A: '主通道高人流区', B: '中人流区', C: '低人流区', F: '食品专区', K: '亲子专区' }[pass.value.stall.zone] || pass.value.stall.zone
})

onMounted(async () => {
  if (!ev.state.events.length) ev.setEvents(await api.get('/events'))
  const id = route.params.eventId || ev.currentId.value
  if (id) {
    try {
      pass.value = await api.get(`/my/event/${id}/pass`)
      const resp = await fetch(`/api/assignments/${pass.value.assignment.id}/qrcode`, {
        headers: { Authorization: `Bearer ${auth.state.token}` }
      })
      qrBlob.value = URL.createObjectURL(await resp.blob())
    } catch (e) {
      pass.value = null
    }
  }
})
</script>

<style scoped>
.pass-card { text-align: center; }
.qr-box { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 8px 0; }
.qr-box img { width: 220px; height: 220px; border: 1px solid #ebeef5; border-radius: 8px; }
.code { font-size: 20px; font-weight: 700; letter-spacing: 4px; color: #1f3a5f; }
.reason { color: #606266; line-height: 1.8; margin: 0; }
</style>
