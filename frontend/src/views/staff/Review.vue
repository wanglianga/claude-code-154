<template>
  <div>
    <h2 class="page-title">报名审核</h2>
    <el-radio-group v-model="filter" style="margin-bottom:12px">
      <el-radio-button label="">全部</el-radio-button>
      <el-radio-button label="PENDING">待审核</el-radio-button>
      <el-radio-button label="APPROVED">已通过</el-radio-button>
      <el-radio-button label="REJECTED">已驳回</el-radio-button>
    </el-radio-group>

    <el-table :data="filtered" stripe border>
      <el-table-column label="摊主" width="110">
        <template #default="{ row }">{{ row.vendor?.name }}</template>
      </el-table-column>
      <el-table-column label="信用/历史捐赠" width="150">
        <template #default="{ row }">
          <el-tag size="small" :type="row.vendor?.creditScore >= 80 ? 'success' : 'danger'">
            信用 {{ row.vendor?.creditScore }}
          </el-tag>
          <div class="muted">累计捐 ¥{{ row.vendor?.totalDonationYuan }}</div>
          <el-tag v-if="row.vendor?.foodRiskFlag" size="small" type="danger">食品风险</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="摊位/品类" width="170">
        <template #default="{ row }">
          <b>{{ row.application.stallName }}</b>
          <el-tag size="small" style="margin-left:4px">{{ row.application.categoryLabel }}</el-tag>
          <div class="muted">{{ row.application.goodsDescription }}</div>
        </template>
      </el-table-column>
      <el-table-column label="食品资质" width="150">
        <template #default="{ row }">
          <template v-if="row.application.category === 'FOOD'">
            <el-tag :type="row.application.foodQualified ? 'success' : 'danger'" size="small">
              {{ row.application.foodQualified ? '有资质' : '无资质' }}
            </el-tag>
            <div v-if="row.application.foodCertNo" class="muted">{{ row.application.foodCertNo }}</div>
          </template>
          <span v-else class="muted">非食品类</span>
        </template>
      </el-table-column>
      <el-table-column label="捐赠%" width="80">
        <template #default="{ row }">{{ row.application.donationPercent }}%</template>
      </el-table-column>
      <el-table-column label="需求" width="180">
        <template #default="{ row }">
          <el-tag v-if="row.application.powerNeedWatt > 0" size="small" type="warning">电{{ row.application.powerNeedWatt }}W</el-tag>
          <el-tag v-if="row.application.noisy" size="small" type="danger" style="margin-left:4px">噪音/油烟</el-tag>
          <el-tag v-if="row.application.familyStall" size="small" type="success" style="margin-left:4px">亲子</el-tag>
          <div class="muted">桌{{ row.application.tableNeed }} 椅{{ row.application.chairNeed }} · {{ row.application.paymentMethod }}</div>
        </template>
      </el-table-column>
      <el-table-column label="规则校验" width="220">
        <template #default="{ row }">
          <el-text v-if="!row.application.ruleHints" type="success">符合活动规则</el-text>
          <el-text v-else type="danger">{{ row.application.ruleHints }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.application.status)">{{ row.application.statusLabel }}</el-tag>
          <div v-if="row.assignment" class="muted">已分配摊位</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <template v-if="row.application.status === 'PENDING' && isAdmin">
            <el-button size="small" type="success" @click="review(row, true)">通过</el-button>
            <el-button size="small" type="danger" @click="review(row, false)">驳回</el-button>
          </template>
          <span v-else-if="row.application.status !== 'PENDING'" class="muted">
            {{ row.application.reviewComment || '—' }}
          </span>
          <el-tag v-else type="info" size="small">仅管理员</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import api from '../../api.js'
import { useAuth } from '../../store/auth.js'
import { useEvent } from '../../store/event.js'

const auth = useAuth()
const ev = useEvent()
const isAdmin = computed(() => auth.isAdmin.value)
const list = ref([])
const filter = ref('PENDING')

const filtered = computed(() => filter.value
  ? list.value.filter(r => r.application.status === filter.value)
  : list.value)

function statusType(s) { return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] }

async function load() {
  if (!ev.currentId.value) return
  list.value = await api.get(`/events/${ev.currentId.value}/applications`)
}

async function review(row, approved) {
  let comment = ''
  if (!approved) {
    const r = await ElMessageBox.prompt('请填写驳回原因（摊主补正后可重新提交）', '驳回报名', {
      confirmButtonText: '确认驳回', cancelButtonText: '取消', inputType: 'textarea'
    }).catch(() => null)
    if (!r) return
    comment = r.value
  } else {
    const r = await ElMessageBox.confirm('确认该摊位符合活动规则并通过审核？', '审核通过', {
      confirmButtonText: '通过', cancelButtonText: '取消'
    }).catch(() => null)
    if (r !== 'confirm') return
  }
  await api.post(`/applications/${row.application.id}/review`, { approved, comment })
  ElMessage.success(approved ? '已通过，可进行摊位分配' : '已驳回')
  load()
}

onMounted(load)
watch(() => ev.currentId.value, load)
</script>
