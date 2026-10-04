<template>
  <div>
    <h2 class="page-title">收款与捐赠</h2>
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card header="摊位收款流水登记" class="section-card">
          <el-form :inline="true" :model="form">
            <el-form-item label="摊位">
              <el-select v-model="form.assignmentId" placeholder="选择已入场摊位" filterable style="width:260px">
                <el-option v-for="a in assignments" :key="a.id"
                           :label="`${a.stall?.code} ${a.application?.stallName}（${a.vendor?.name}）`"
                           :value="a.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="金额(元)">
              <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="50" />
            </el-form-item>
            <el-form-item label="方式">
              <el-select v-model="form.paymentMethod" style="width:110px">
                <el-option v-for="m in ['微信', '支付宝', '聚合码', '现金']" :key="m" :label="m" :value="m" />
              </el-select>
            </el-form-item>
            <el-form-item label="凭证号">
              <el-input v-model="form.receiptNo" placeholder="流水号/备注" style="width:140px" />
            </el-form-item>
            <el-button type="primary" @click="addSale">登记收款</el-button>
          </el-form>

          <el-table :data="sales" stripe height="380">
            <el-table-column prop="createdAt" label="时间" width="160" />
            <el-table-column label="摊位" width="100">
              <template #default="{ row }">{{ stallCode(row.assignmentId) }}</template>
            </el-table-column>
            <el-table-column label="金额" width="110">
              <template #default="{ row }"><span class="money">¥{{ row.amount }}</span></template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="方式" width="90" />
            <el-table-column prop="receiptNo" label="凭证号" width="130" />
            <el-table-column prop="note" label="备注" min-width="120" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card header="公益捐赠核对（按承诺比例 × 营业额）">
          <el-table :data="donations" stripe height="500">
            <el-table-column label="摊位">
              <template #default="{ row }">
                <b>{{ appOf(row.assignmentId)?.stallName }}</b>
                <div class="muted">{{ stallCode(row.assignmentId) }} · {{ row.percent }}%</div>
              </template>
            </el-table-column>
            <el-table-column label="营业/应捐/实缴" width="190">
              <template #default="{ row }">
                <div>流水 ¥{{ row.salesTotal }}</div>
                <div>应捐 <b>¥{{ row.expectedAmount }}</b></div>
                <div>实缴 ¥{{ row.actualAmount }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态/操作" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="donType(row.status)">{{ row.statusLabel }}</el-tag>
                <el-button size="small" type="primary" link style="margin-top:4px"
                           @click="openDonate(row)">核实缴纳</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 捐赠核实弹窗 -->
    <el-dialog v-model="dlg" title="核实公益捐赠" width="420px">
      <el-descriptions v-if="current" :column="1" border size="small">
        <el-descriptions-item label="摊位">{{ appOf(current.assignmentId)?.stallName }}</el-descriptions-item>
        <el-descriptions-item label="流水营业额">¥{{ current.salesTotal }}</el-descriptions-item>
        <el-descriptions-item label="承诺比例">{{ current.percent }}%</el-descriptions-item>
        <el-descriptions-item label="应捐金额"><b class="money">¥{{ current.expectedAmount }}</b></el-descriptions-item>
      </el-descriptions>
      <el-form style="margin-top:14px" label-width="90px">
        <el-form-item label="实际缴纳">
          <el-input-number v-model="donateForm.actualAmount" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="收据号"><el-input v-model="donateForm.receiptNo" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="donateForm.note" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <el-alert type="warning" :closable="false" show-icon
                title="实缴金额与应捐不一致时，系统自动登记「捐赠金额与流水不一致」异常并扣信用分 10 分" />
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="confirmDonation">确认核实</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api.js'
import { useEvent } from '../../store/event.js'

const ev = useEvent()
const assignments = ref([])
const sales = ref([])
const donations = ref([])
const form = reactive({ assignmentId: null, amount: 100, paymentMethod: '微信', receiptNo: '' })
const dlg = ref(false)
const current = ref(null)
const donateForm = reactive({ actualAmount: 0, receiptNo: '', note: '' })

function stallCode(aid) {
  return assignments.value.find(a => a.id === aid)?.stall?.code || '—'
}
function appOf(aid) {
  return assignments.value.find(a => a.id === aid)?.application
}
function donType(s) {
  return { PENDING_PAY: 'info', CONFIRMED: 'success', MISMATCH: 'danger', PUBLICIZED: '' }[s] || 'info'
}

async function load() {
  if (!ev.currentId.value) return
  const data = await api.get(`/events/${ev.currentId.value}/assignments`)
  assignments.value = data.assignments
  sales.value = await api.get(`/events/${ev.currentId.value}/sales`)
  donations.value = await api.get(`/events/${ev.currentId.value}/donations`)
  // 为尚未生成捐赠记录的摊位同步一次（应捐随流水变化）
  await Promise.all(assignments.value.map(a => api.get(`/assignments/${a.id}/donation`).catch(() => null)))
  donations.value = await api.get(`/events/${ev.currentId.value}/donations`)
}

async function addSale() {
  if (!form.assignmentId) { ElMessage.warning('请选择摊位'); return }
  await api.post(`/events/${ev.currentId.value}/sales`, form)
  ElMessage.success('收款已登记，应捐金额已同步')
  form.receiptNo = ''
  load()
}

function openDonate(row) {
  current.value = row
  donateForm.actualAmount = Number(row.expectedAmount)
  donateForm.receiptNo = row.receiptNo || ''
  donateForm.note = ''
  dlg.value = true
}

async function confirmDonation() {
  const d = await api.post(`/assignments/${current.value.assignmentId}/donation/confirm`, donateForm)
  ElMessage.success(d.status === 'CONFIRMED' ? '捐赠已核实，金额一致' : '已登记金额不符异常')
  dlg.value = false
  load()
}

onMounted(load)
watch(() => ev.currentId.value, load)
</script>
