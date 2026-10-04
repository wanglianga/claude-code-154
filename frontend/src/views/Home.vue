<template>
  <div>
    <h2 class="page-title">工作台</h2>

    <!-- 摊主视角 -->
    <template v-if="auth.isVendor.value">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="hover"><div class="kpi"><div class="num">{{ auth.state.user.creditScore }}</div>
            <div class="lbl">我的信用分</div></div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover"><div class="kpi"><div class="num">¥{{ auth.state.user.totalDonationYuan }}</div>
            <div class="lbl">历史累计捐赠</div></div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover"><div class="kpi"><div class="num">{{ myApps.length }}</div>
            <div class="lbl">我的报名场次</div></div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover"><div class="kpi">
            <el-tag :type="auth.state.user.foodRiskFlag ? 'danger' : 'success'">
              {{ auth.state.user.foodRiskFlag ? '有食品风险记录' : '无食品风险记录' }}
            </el-tag>
            <div class="lbl" style="margin-top:8px">食品资质状态</div></div></el-card>
        </el-col>
      </el-row>
      <el-card class="section-card" header="我的活动">
        <el-table :data="myApps" stripe>
          <el-table-column label="活动"><template #default="{ row }">{{ eventName(row.eventId) }}</template></el-table-column>
          <el-table-column prop="stallName" label="摊位名称" />
          <el-table-column prop="categoryLabel" label="品类" width="100" />
          <el-table-column prop="donationPercent" label="捐赠比例" width="100">
            <template #default="{ row }">{{ row.donationPercent }}%</template>
          </el-table-column>
          <el-table-column label="审核状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reviewComment" label="审核意见" show-overflow-tooltip />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button v-if="row.status !== 'APPROVED'" size="small" @click="$router.push(`/vendor/apply/${row.eventId}`)">
                编辑报名
              </el-button>
              <el-button v-else type="primary" size="small" @click="$router.push(`/vendor/pass/${row.eventId}`)">
                查看凭证
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 管理员/工作人员视角 -->
    <template v-else>
      <el-row :gutter="16" v-if="dash">
        <el-col :span="6"><el-card shadow="hover"><div class="kpi"><div class="num">{{ dash.stat.applicationCount }}</div>
          <div class="lbl">报名摊位数</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><div class="kpi"><div class="num">{{ dash.stat.visitorCount }}</div>
          <div class="lbl">现场客流</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><div class="kpi money">¥{{ dash.stat.donatedActual }}</div>
          <div class="lbl">实到公益捐赠 / 目标 ¥{{ dash.stat.donationGoal }}（{{ dash.stat.goalPercent }}%）</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><div class="kpi">
          <el-tag type="danger" size="large">{{ dash.stat.issueOpen }}</el-tag>
          <el-tag type="warning" size="large" style="margin-left:8px">{{ dash.stat.complaintOpen }}</el-tag>
          <div class="lbl" style="margin-top:8px">待处理异常 / 投诉</div></div></el-card></el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="14">
          <el-card header="快捷入口" class="section-card">
            <div class="quick">
              <el-button v-for="q in quickLinks" :key="q.path" :type="q.type" plain
                         @click="$router.push(q.path)">{{ q.label }}</el-button>
            </div>
            <el-divider>下次招商建议</el-divider>
            <el-skeleton :rows="3" animated :loading="!dash">
              <ul v-if="dash" class="tips">
                <li v-for="(t, i) in dash.suggestions" :key="i">{{ t }}</li>
              </ul>
            </el-skeleton>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card header="本场活动概览" class="section-card" v-if="dash">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="活动">{{ ev.current.value?.name }}</el-descriptions-item>
              <el-descriptions-item label="已分配 / 已核销">
                {{ dash.stat.assignedCount }} / {{ dash.stat.checkedInCount }}
              </el-descriptions-item>
              <el-descriptions-item label="提前离场">{{ dash.stat.earlyLeaveCount }}</el-descriptions-item>
              <el-descriptions-item label="总营业额">¥{{ dash.stat.salesTotal }}</el-descriptions-item>
              <el-descriptions-item label="履约良好摊位">{{ dash.stat.goodPerformerCount }}</el-descriptions-item>
              <el-descriptions-item label="存在异常摊位">{{ dash.stat.issueVendorCount }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import api from '../api.js'
import { useAuth } from '../store/auth.js'
import { useEvent } from '../store/event.js'

const auth = useAuth()
const ev = useEvent()
const myApps = ref([])
const dash = ref(null)

const quickLinks = [
  { path: '/review', label: '报名审核', type: 'primary' },
  { path: '/assign', label: '一键摊位分配', type: 'success' },
  { path: '/onsite', label: '现场核销巡检', type: 'warning' },
  { path: '/finance', label: '收款与捐赠核对', type: 'danger' },
  { path: '/issues', label: '异常处理', type: 'info' },
  { path: '/dashboard', label: '复盘与公示', type: 'primary' }
]

async function loadVendor() {
  myApps.value = await api.get('/my/applications')
}

async function loadStaff() {
  if (!ev.currentId.value) return
  dash.value = await api.get(`/events/${ev.currentId.value}/dashboard`)
}

function eventName(id) {
  return ev.state.events.find(e => e.id === id)?.name || `活动#${id}`
}

function statusType(s) {
  return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info'
}

onMounted(async () => {
  if (!ev.state.events.length) ev.setEvents(await api.get('/events'))
  if (auth.isVendor.value) loadVendor()
  else loadStaff()
})
watch(() => ev.currentId.value, () => { if (auth.isStaff.value) loadStaff() })
</script>

<style scoped>
.kpi { text-align: center; padding: 8px 0; }
.kpi .num { font-size: 28px; font-weight: 700; color: #1f3a5f; }
.kpi .lbl { color: #909399; font-size: 13px; margin-top: 4px; }
.quick { display: flex; flex-wrap: wrap; gap: 10px; }
.tips { margin: 0; padding-left: 20px; color: #606266; line-height: 1.9; font-size: 14px; }
</style>
