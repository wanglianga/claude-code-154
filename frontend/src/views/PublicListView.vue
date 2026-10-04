<template>
  <div style="min-height:100vh; background:#f5f7fa;">
    <div style="background:linear-gradient(135deg,#1f8a70,#2d6cdf); color:#fff; padding:26px 20px;">
      <div style="max-width:960px; margin:0 auto; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px;">
        <div>
          <div style="font-size:22px; font-weight:700;">🏘️ 社区公益集市 · 居民公示端</div>
          <div style="opacity:.9; margin-top:6px; font-size:13px;">公开总捐赠、善款用途、摊主履约与异常处理，让每一份善意都可追溯</div>
        </div>
        <el-button @click="$router.push('/login')">工作人员/摊主登录</el-button>
      </div>
    </div>

    <div style="max-width:960px; margin:18px auto; padding:0 16px 40px;">
      <el-card v-if="!eventId" class="section-card">
        <template #header>已结束活动的公益成果公示</template>
      <el-empty v-if="!events.length" description="暂无已结束的活动" />
        <el-row :gutter="14">
          <el-col v-for="e in events" :key="e.id" :span="12">
            <el-card shadow="hover" class="section-card" style="cursor:pointer;" @click="openEvent(e.id)">
              <div style="font-size:16px; font-weight:600;">{{ e.name }}</div>
              <div style="color:#909399; font-size:12px; margin:6px 0;">{{ e.eventDate }} · {{ e.location }}</div>
              <div style="font-size:13px; color:#606266;">{{ e.publicGoal }}</div>
              <el-tag size="small" :type="e.status === 'PUBLISHED' ? 'success' : 'info'" style="margin-top:8px;">
                {{ e.status === 'PUBLISHED' ? '已发布公示，点击查看' : '活动已结束，公示待发布' }}
              </el-tag>
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <el-card v-else>
        <el-page-header @back="eventId = null" content="返回活动列表" style="margin-bottom:14px;" />
        <el-empty v-if="!view.published" description="该活动公示尚未发布，请关注社区公告栏" />
        <template v-else>
          <el-result icon="success" :title="view.title" :sub-title="`${view.event.eventDate} · ${view.event.location}`" />
          <el-row :gutter="14">
            <el-col :span="8">
              <div class="stat-card" style="background:#f56c6c;">
                <div class="num">¥{{ fmtMoney(view.totalDonation) }}</div>
                <div class="label">公益捐赠总额</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card" style="background:#409eff;">
                <div class="num">¥{{ fmtMoney(view.totalSales) }}</div>
                <div class="label">集市营业总额</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card" style="background:#67c23a;">
                <div class="num">{{ view.event.visitorCount }}</div>
                <div class="label">现场参与居民（客流）</div>
              </div>
            </el-col>
          </el-row>

          <el-descriptions :column="1" border style="margin-top:16px;">
            <el-descriptions-item label="公益目标">{{ view.event.publicGoal }}</el-descriptions-item>
            <el-descriptions-item label="筹款目标">¥{{ fmtMoney(view.event.goalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="善款用途">
              <div style="white-space:pre-wrap;">{{ view.fundUsage }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="摊主履约情况">
              <div style="white-space:pre-wrap;">{{ view.fulfillmentSummary }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="异常处理情况">
              <div style="white-space:pre-wrap;">{{ view.incidentSummary }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ view.publishedAt }}</el-descriptions-item>
          </el-descriptions>

          <el-card v-if="view.donationDetails?.length" shadow="never" style="margin-top:16px;"
                   header="逐摊位捐赠明细（应公益组织公示要求）">
            <el-table :data="view.donationDetails" border size="small">
              <el-table-column prop="shopName" label="摊位" min-width="130" />
              <el-table-column prop="vendorName" label="摊主/组织" width="130" />
              <el-table-column prop="category" label="品类" width="90" />
              <el-table-column label="捐赠比例" width="90">
                <template #default="{ row }">{{ row.rate }}%</template>
              </el-table-column>
              <el-table-column label="营业额" width="100">
                <template #default="{ row }">¥{{ fmtMoney(row.salesAmount) }}</template>
              </el-table-column>
              <el-table-column label="实际捐赠" width="100">
                <template #default="{ row }">
                  <b style="color:#f56c6c;">¥{{ fmtMoney(row.donationAmount) }}</b>
                </template>
              </el-table-column>
              <el-table-column prop="voucherNo" label="捐赠凭证" min-width="120" />
            </el-table>
          </el-card>

          <el-alert type="success" :closable="false" style="margin-top:16px;"
                    title="感谢每一位摊主与居民的参与，公开透明是下一次招募最好的名片。" />
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'
import { fmtMoney } from '../composables/useEvents'

const events = ref([])
const eventId = ref(null)
const view = ref({})

async function openEvent(id) {
  eventId.value = id
  view.value = await api.get(`/public/events/${id}/publicity`)
}
onMounted(async () => {
  events.value = await api.get('/public/events')
})
</script>
