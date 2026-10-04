<template>
  <div>
    <h2 class="page-title">复盘与居民公示</h2>

    <el-row :gutter="16" v-if="dash">
      <el-col :span="4"><el-card shadow="hover" class="stat"><div class="num">{{ dash.stat.applicationCount }}</div><div class="lbl">报名摊位</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="stat"><div class="num">{{ dash.stat.checkedInCount }}/{{ dash.stat.assignedCount }}</div><div class="lbl">核销/分配</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="stat"><div class="num">{{ dash.stat.visitorCount }}</div><div class="lbl">总客流</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="stat"><div class="num money">¥{{ dash.stat.donatedActual }}</div><div class="lbl">实到捐赠（{{ dash.stat.goalPercent }}%）</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="stat"><div class="num" style="color:#e6a23c">{{ dash.stat.complaintCount }}</div><div class="lbl">投诉（{{ dash.stat.complaintOpen }} 待办）</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover" class="stat"><div class="num" style="color:#f56c6c">{{ dash.stat.issueCount }}</div><div class="lbl">异常（{{ dash.stat.issueOpen }} 待办）</div></el-card></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card header="公益金额目标达成" class="section-card">
          <div ref="goalChart" style="height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="报名品类与异常分布" class="section-card">
          <div ref="pieChart" style="height:260px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card header="各回路用电负载" class="section-card">
      <div v-if="dash && !Object.keys(dash.powerLoad).length" class="muted">本场无用电摊位</div>
      <el-progress v-for="(w, c) in (dash?.powerLoad || {})" :key="c"
                   :percentage="Math.min(100, Math.round(w / 20))"
                   :status="w >= 1800 ? 'exception' : 'success'"
                   :format="() => `${c}: ${w}W / 2000W`" style="margin-bottom:10px" />
    </el-card>

    <el-card header="摊主履约情况" class="section-card">
      <el-table :data="dash?.performance || []" stripe>
        <el-table-column prop="stallCode" label="摊位" width="80" />
        <el-table-column prop="stallName" label="摊位名" min-width="130" />
        <el-table-column prop="vendorName" label="摊主" width="100" />
        <el-table-column prop="category" label="品类" width="90" />
        <el-table-column label="入场/离场" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="row.checkedIn ? 'success' : 'info'">{{ row.checkedIn ? '已入场' : '未入场' }}</el-tag>
            <el-tag v-if="row.earlyLeave" size="small" type="danger" style="margin-left:4px">提前离场</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="营业额/实捐" width="150">
          <template #default="{ row }">¥{{ row.salesTotal }} / <b class="money">¥{{ row.actualDonation }}</b></template>
        </el-table-column>
        <el-table-column label="异常/投诉" width="100">
          <template #default="{ row }">{{ row.issueCount }} / {{ row.complaintCount }}</template>
        </el-table-column>
        <el-table-column label="履约评分" width="170">
          <template #default="{ row }">
            <el-progress :percentage="row.fulfillmentScore"
                         :status="row.fulfillmentScore >= 80 ? 'success' : row.fulfillmentScore >= 60 ? '' : 'exception'" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card header="下次招商建议" class="section-card">
      <el-timeline>
        <el-timeline-item v-for="(s, i) in dash?.suggestions || []" :key="i" :timestamp="`建议 ${i + 1}`"
                          type="primary">{{ s }}</el-timeline-item>
      </el-timeline>
    </el-card>

    <el-card class="section-card">
      <template #header>
        <div class="head">
          <b>居民公示发布</b>
          <el-button type="danger" @click="publishDlg = true" v-if="auth.isAdmin.value">
            {{ published ? '更新公示' : '发布居民公示' }}
          </el-button>
          <el-tag v-else type="info">仅管理员可发布</el-tag>
        </div>
      </template>
      <el-alert v-if="published" type="success" :closable="false" show-icon
                title="该活动已公示，居民可在公开页查看总捐赠、用途、摊主履约与异常处理" />
      <el-alert v-else type="warning" :closable="false" show-icon
                title="公示前请确认投诉全部办结、捐赠差额处理完毕；发布后捐赠记录将标记为已公示。" />
      <div style="margin-top:10px">
        <router-link to="/public" target="_blank">打开居民公示页 →</router-link>
      </div>
    </el-card>

    <!-- 公示弹窗 -->
    <el-dialog v-model="publishDlg" title="发布居民公示" width="560px">
      <el-form :model="pubForm" label-width="110px">
        <el-form-item label="公示总捐赠">¥{{ dash?.stat.donatedActual }}</el-form-item>
        <el-form-item label="捐赠用途">
          <el-input v-model="pubForm.donationPurpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="异常处理说明">
          <el-input v-model="pubForm.issueSummary" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="下次招募计划">
          <el-input v-model="pubForm.nextPlan" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDlg = false">取消</el-button>
        <el-button type="danger" @click="doPublish">确认发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import api from '../../api.js'
import { useAuth } from '../../store/auth.js'
import { useEvent } from '../../store/event.js'

const auth = useAuth()
const ev = useEvent()
const dash = ref(null)
const published = ref(false)
const publishDlg = ref(false)
const pubForm = reactive({ donationPurpose: '', issueSummary: '', nextPlan: '' })
const goalChart = ref(null)
const pieChart = ref(null)
let goalC, pieC

async function load() {
  if (!ev.currentId.value) return
  dash.value = await api.get(`/events/${ev.currentId.value}/dashboard`)
  published.value = !!dash.value.published
  if (!pubForm.donationPurpose) pubForm.donationPurpose = ev.current.value?.donationPurpose || ''
  await nextTick()
  renderCharts()
}

function renderCharts() {
  if (!dash.value) return
  const s = dash.value.stat
  if (goalChart.value) {
    goalC = goalC || echarts.init(goalChart.value)
    goalC.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 70, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: ['公益目标', '应捐金额', '实到捐赠'] },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar', barWidth: 46,
        data: [
          { value: Number(s.donationGoal), itemStyle: { color: '#909399' } },
          { value: Number(s.donatedExpected), itemStyle: { color: '#e6a23c' } },
          { value: Number(s.donatedActual), itemStyle: { color: '#67c23a' } }
        ],
        label: { show: true, position: 'top' }
      }]
    })
  }
  if (pieChart.value) {
    pieC = pieC || echarts.init(pieChart.value)
    const cats = Object.entries(dash.value.categoryDistribution).filter(([, v]) => v > 0).map(([name, value]) => ({ name, value }))
    const issues = Object.entries(dash.value.issueDistribution).map(([name, value]) => ({ name: '异常:' + name, value }))
    pieC.setOption({
      tooltip: { trigger: 'item' },
      legend: { type: 'scroll', bottom: 0, textStyle: { fontSize: 11 } },
      series: [
        { name: '品类', type: 'pie', radius: ['0%', '38%'], center: ['50%', '45%'],
          data: cats, label: { formatter: '{b} {c}' }, color: ['#409eff', '#67c23a', '#f56c6c', '#e6a23c', '#909399'] },
        { name: '异常', type: 'pie', radius: ['48%', '68%'], center: ['50%', '45%'],
          data: issues, label: { formatter: '{b} {c}' }, color: ['#f89898', '#fbbd63', '#f56c6c', '#e36a6a', '#d96666', '#b03a3a', '#8b5a5a'] }
      ]
    })
  }
}

async function doPublish() {
  await api.post(`/events/${ev.currentId.value}/publish`, pubForm)
  ElMessage.success('公示已发布，居民可查看')
  publishDlg.value = false
  load()
}

onMounted(load)
watch(() => ev.currentId.value, load)
</script>

<style scoped>
.stat { text-align: center; }
.stat .num { font-size: 24px; font-weight: 700; color: #1f3a5f; }
.stat .lbl { color: #909399; font-size: 12px; margin-top: 4px; }
.head { display: flex; justify-content: space-between; align-items: center; }
</style>
