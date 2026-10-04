<template>
  <div>
    <h2 class="page-title">活动统一档案</h2>
    <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
              title="一个活动档案汇总：报名、审核、摊位分配、收款流水、公益捐赠、现场检查/换品、投诉与异常，便于追溯与复盘。" />

    <el-tabs v-model="tab">
      <el-tab-pane label="摊位全流程记录" name="records">
        <el-table :data="archive?.records || []" stripe border>
          <el-table-column label="摊主" width="100">
            <template #default="{ row }">{{ row.vendor?.name }}<div class="muted">信用{{ row.vendor?.creditScore }}</div></template>
          </el-table-column>
          <el-table-column label="报名与审核" width="200">
            <template #default="{ row }">
              <b>{{ row.application.stallName }}</b>（{{ row.application.categoryLabel }}）
              <el-tag size="small" :type="st(row.application.status)">{{ row.application.statusLabel }}</el-tag>
              <div class="muted">{{ row.application.goodsDescription }}</div>
              <div v-if="row.application.ruleHints" class="risk">⚠ {{ row.application.ruleHints }}</div>
            </template>
          </el-table-column>
          <el-table-column label="摊位" width="90">
            <template #default="{ row }">{{ row.stall?.code || '—' }}</template>
          </el-table-column>
          <el-table-column label="入场/离场" width="150">
            <template #default="{ row }">
              <el-tag v-if="row.assignment?.checkedIn" size="small" type="success">已入场</el-tag>
              <el-tag v-else size="small" type="info">未入场</el-tag>
              <el-tag v-if="row.assignment?.earlyLeave" size="small" type="danger" style="margin-left:4px">提前离场</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="收款/捐赠" width="170">
            <template #default="{ row }">
              <div v-if="row.donation">流水 ¥{{ row.donation.salesTotal }}（{{ row.donation.paymentMethod || row.sales?.length + '笔' }}）</div>
              <div v-if="row.donation">应捐 ¥{{ row.donation.expectedAmount }} / 实缴 ¥{{ row.donation.actualAmount }}</div>
              <el-tag v-if="row.donation" size="small" :type="dt(row.donation.status)">{{ row.donation.statusLabel }}</el-tag>
              <span v-else class="muted">无收款记录</span>
            </template>
          </el-table-column>
          <el-table-column label="检查/换品/异常" min-width="180">
            <template #default="{ row }">
              <el-tag v-for="i in row.inspections" :key="i.id" size="small"
                      :type="i.result === 'OK' ? 'success' : 'danger'" style="margin:2px">
                检查:{{ i.resultLabel }}
              </el-tag>
              <el-tag v-for="g in row.goodsChanges" :key="g.id" size="small"
                      :type="g.approved ? 'warning' : 'danger'" style="margin:2px">
                换品{{ g.approved ? '已批' : '未批' }}
              </el-tag>
              <el-tag v-for="x in row.issues" :key="x.id" size="small" type="danger" style="margin:2px">
                {{ x.typeLabel }}:{{ x.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane :label="`投诉（${archive?.complaints?.length || 0}）`" name="complaints">
        <el-table :data="archive?.complaints || []" stripe>
          <el-table-column prop="createdAt" label="时间" width="160" />
          <el-table-column prop="complainant" label="投诉人" width="110" />
          <el-table-column prop="content" label="内容" min-width="220" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 'RESOLVED' ? 'success' : 'danger'">{{ row.statusLabel }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="handleResult" label="处理结果" min-width="180" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane :label="`异常（${archive?.issues?.length || 0}）`" name="issues">
        <el-table :data="archive?.issues || []" stripe>
          <el-table-column prop="createdAt" label="时间" width="160" />
          <el-table-column prop="typeLabel" label="类型" width="160" />
          <el-table-column prop="description" label="描述" min-width="240" />
          <el-table-column prop="creditPenalty" label="扣分" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 'OPEN' ? 'danger' : 'success'">{{ row.statusLabel }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="resolution" label="处理措施" min-width="200" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="客流" name="traffic">
        <el-table :data="archive?.traffic || []" stripe>
          <el-table-column prop="period" label="时段" width="180" />
          <el-table-column prop="visitorCount" label="人数" width="120" />
          <el-table-column prop="note" label="备注" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="公示" name="report">
        <el-empty v-if="!archive?.publicReport" description="活动尚未发布居民公示，请前往「复盘与公示」发布" />
        <el-descriptions v-else :column="1" border>
          <el-descriptions-item label="发布时间">{{ archive.publicReport.publishedAt }}</el-descriptions-item>
          <el-descriptions-item label="公示总捐赠">¥{{ archive.publicReport.totalDonation }}</el-descriptions-item>
          <el-descriptions-item label="总客流">{{ archive.publicReport.totalVisitors }} 人次</el-descriptions-item>
          <el-descriptions-item label="履约良好摊位">{{ archive.publicReport.goodPerformers }} 个</el-descriptions-item>
          <el-descriptions-item label="异常摊位">{{ archive.publicReport.issueVendorCount }} 个</el-descriptions-item>
          <el-descriptions-item label="捐赠用途">{{ archive.publicReport.donationPurpose }}</el-descriptions-item>
          <el-descriptions-item label="异常处理情况">{{ archive.publicReport.issueSummary }}</el-descriptions-item>
          <el-descriptions-item label="下次招募计划">{{ archive.publicReport.nextPlan }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import api from '../../api.js'
import { useEvent } from '../../store/event.js'

const ev = useEvent()
const archive = ref(null)
const tab = ref('records')

function st(s) { return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] }
function dt(s) { return { PENDING_PAY: 'info', CONFIRMED: 'success', MISMATCH: 'danger', PUBLICIZED: '' }[s] }

async function load() {
  if (!ev.currentId.value) return
  archive.value = await api.get(`/events/${ev.currentId.value}/archive`)
}
onMounted(load)
watch(() => ev.currentId.value, load)
</script>

<style scoped>
.risk { color: #f56c6c; font-size: 12px; }
</style>
