<template>
  <div class="page-container">
    <el-card class="section-card">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>智能摊位分配
            <el-text type="info" size="small">
              综合摊主信用、历史捐赠、公益比例、亲子属性、食品风险，匹配摊位大小/人流/用电/噪音/同类冲突
            </el-text>
          </span>
          <div>
            <el-button @click="load">刷新</el-button>
            <el-button type="primary" :loading="loading" @click="autoAllocate">一键智能分配（待分配摊位）</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="14">
        <el-col :span="14">
          <el-table :data="apps" border stripe>
            <el-table-column label="摊主/品类" min-width="150">
              <template #default="{ row }">
                <div>{{ row.shopName }}</div>
                <el-tag size="small">{{ row.categoryLabel }}</el-tag>
                <el-tag size="small" type="danger" style="margin-left:4px;">捐{{ row.donationRate }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="信用" width="70">
              <template #default="{ row }">{{ row.vendor?.creditScore }}</template>
            </el-table-column>
            <el-table-column label="分配结果" min-width="170">
              <template #default="{ row }">
                <template v-if="row.stall">
                  <el-tag type="success">{{ row.stall.zone }} {{ row.stall.code }}</el-tag>
                  <el-button size="small" link type="primary" @click="openReassign(row)">改派</el-button>
                  <el-button size="small" link type="danger" @click="release(row)">释放</el-button>
                  <div style="font-size:12px; color:#909399; margin-top:2px;">评分 {{ row.allocationScore }}</div>
                </template>
                <el-tag v-else-if="row.status === 'APPROVED'" type="warning">待分配</el-tag>
                <span v-else style="color:#c0c4cc;">未通过审核</span>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="10">
          <el-card shadow="never" header="分配理由（最近一次自动分配）">
            <el-empty v-if="!results.length" description="点击右上角按钮执行分配" :image-size="70" />
            <el-timeline v-else>
              <el-timeline-item v-for="r in results" :key="r.applicationId"
                              :type="r.allocated ? 'success' : 'danger'">
                <b>{{ r.shopName }}</b>
                <span v-if="r.allocated"> → {{ r.zone }} {{ r.stallCode }}（{{ r.score }}分）</span>
                <span v-else style="color:#f56c6c;"> → 无法分配</span>
                <div style="font-size:12px; color:#909399;">{{ r.reason }}</div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="reassignDlg" title="手动改派摊位" width="480px">
      <el-alert type="info" :closable="false" style="margin-bottom:10px;">
        手动改派可覆盖智能规则，请自行确认供电/食品区/噪音等条件。
      </el-alert>
      <el-select v-model="targetStallId" placeholder="选择目标摊位" style="width:100%;">
        <el-option v-for="s in freeStalls" :key="s.id"
                   :label="`${s.zone} ${s.code}｜人流${s.trafficLevel}｜大小${s.sizeLevel}${s.powerAvailable ? '｜电'+s.powerCapacity+'W' : ''}`"
                   :value="s.id" />
      </el-select>
      <template #footer>
        <el-button @click="reassignDlg = false">取消</el-button>
        <el-button type="primary" @click="submitReassign">确认改派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api/client'

const route = useRoute()
const eventId = Number(route.params.id)
const apps = ref([])
const stalls = ref([])
const results = ref([])
const loading = ref(false)
const reassignDlg = ref(false)
const targetApp = ref(null)
const targetStallId = ref(null)

const freeStalls = computed(() => stalls.value.filter((s) => s.status === 'FREE'))

async function autoAllocate() {
  loading.value = true
  try {
    results.value = await api.post('/allocations/auto', { eventId })
    const failed = results.value.filter((r) => !r.allocated)
    if (failed.length) {
      ElMessage.warning(`${results.value.length - failed.length} 个已分配，${failed.length} 个无满足硬约束的摊位`)
    } else {
      ElMessage.success(`已分配 ${results.value.length} 个摊位`)
    }
    await load()
  } finally {
    loading.value = false
  }
}

function openReassign(row) {
  targetApp.value = row
  targetStallId.value = null
  reassignDlg.value = true
}
async function submitReassign() {
  if (!targetStallId.value) {
    ElMessage.warning('请选择摊位')
    return
  }
  await api.post('/allocations/reassign', { applicationId: targetApp.value.id, stallId: targetStallId.value })
  ElMessage.success('改派成功')
  reassignDlg.value = false
  load()
}
async function release(row) {
  await ElMessageBox.confirm(`释放 ${row.shopName} 的摊位？`, '提示', { type: 'warning' })
  await api.post(`/applications/${row.id}/release-stall`, {})
  ElMessage.success('已释放')
  load()
}
async function load() {
  apps.value = await api.get(`/events/${eventId}/applications`)
  stalls.value = await api.get(`/stalls?eventId=${eventId}`)
}
onMounted(load)
</script>
