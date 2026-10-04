<template>
  <div class="page-container">
    <el-row :gutter="14">
      <el-col :span="11">
        <el-card header="登记收款流水">
          <el-form :model="pay" label-width="100px">
            <el-form-item label="摊位">
              <el-select v-model="pay.applicationId" filterable style="width:100%" placeholder="选择摊主">
                <el-option v-for="a in apps" :key="a.id" :label="a.shopName" :value="a.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="收款方式">
              <el-select v-model="pay.paymentMethod" style="width:100%">
                <el-option v-for="m in PAY_METHODS" :key="m.value" :label="m.label" :value="m.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="金额(元)">
              <el-input-number v-model="pay.amount" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
            <el-form-item label="凭证单号">
              <el-input v-model="pay.receiptNo" placeholder="支付平台单号/现金交接单号" />
            </el-form-item>
            <el-form-item label="备注"><el-input v-model="pay.remark" /></el-form-item>
            <el-button type="primary" style="width:100%" @click="addPayment">登记流水（自动重算应捐金额）</el-button>
          </el-form>

          <el-divider />

          <el-card shadow="never" header="捐赠核对（按承诺比例 × 流水）">
            <el-table :data="apps" border size="small" max-height="320">
              <el-table-column prop="shopName" label="摊主" min-width="110" show-overflow-tooltip />
              <el-table-column label="流水" width="80">
                <template #default="{ row }">¥{{ fmtMoney(row.salesAmount) }}</template>
              </el-table-column>
              <el-table-column label="应捐" width="80">
                <template #default="{ row }">¥{{ fmtMoney(row.donation?.expectedAmount) }}</template>
              </el-table-column>
              <el-table-column label="实捐" width="80">
                <template #default="{ row }">¥{{ fmtMoney(row.donation?.actualAmount) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.donation" size="small" :type="dictType(DONATION_STATUS, row.donation.status)">
                    {{ dictLabel(DONATION_STATUS, row.donation.status) }}
                  </el-tag>
                  <span v-else>—</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="openDonate(row)">核对</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-card>
      </el-col>

      <el-col :span="13">
        <el-card class="section-card">
          <template #header>
            <div style="display:flex; justify-content:space-between;">
              <span>收款流水（合计 ¥{{ fmtMoney(totalPayment) }}）</span>
              <el-tag type="danger">实际捐赠合计 ¥{{ fmtMoney(totalDonation) }}</el-tag>
            </div>
          </template>
          <el-table :data="payments" border size="small" max-height="520">
            <el-table-column prop="recordedAt" label="时间" width="170" />
            <el-table-column label="摊主" width="130">
              <template #default="{ row }">{{ shopName(row.applicationId) }}</template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="方式" width="80" />
            <el-table-column prop="amount" label="金额" width="100" />
            <el-table-column prop="receiptNo" label="凭证号" width="150" />
            <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="donateDlg" title="捐赠核对" width="440px">
      <el-descriptions v-if="donateApp" :column="1" border size="small">
        <el-descriptions-item label="承诺比例">{{ donateApp.donationRate }}%</el-descriptions-item>
        <el-descriptions-item label="流水合计">¥{{ fmtMoney(donateApp.salesAmount) }}</el-descriptions-item>
        <el-descriptions-item label="应捐金额">
          <b style="color:#f56c6c;">¥{{ fmtMoney(donateApp.donation?.expectedAmount) }}</b>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px" style="margin-top:12px;">
        <el-form-item label="实际捐赠(元)">
          <el-input-number v-model="donateForm.actualAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="捐赠凭证号">
          <el-input v-model="donateForm.voucherNo" placeholder="募捐收据/转账单号" />
        </el-form-item>
      </el-form>
      <el-alert type="warning" :closable="false">
        实际金额与应捐不一致时，将自动登记「捐赠金额与流水不一致」异常并扣减信用分。
      </el-alert>
      <template #footer>
        <el-button @click="donateDlg = false">取消</el-button>
        <el-button type="primary" @click="confirmDonation">确认核对</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api/client'
import { fmtMoney } from '../../composables/useEvents'
import { PAY_METHODS, DONATION_STATUS, dictLabel, dictType } from '../../dict'

const eventId = Number(localStorage.getItem('cm_event_id'))
const apps = ref([])
const payments = ref([])
const pay = reactive({ applicationId: null, paymentMethod: 'WECHAT', amount: 0, receiptNo: '', remark: '' })

const donateDlg = ref(false)
const donateApp = ref(null)
const donateForm = reactive({ actualAmount: 0, voucherNo: '' })

const totalPayment = computed(() => payments.value.reduce((s, p) => s + Number(p.amount), 0))
const totalDonation = computed(() =>
  apps.value.reduce((s, a) => s + Number(a.donation?.actualAmount || 0), 0))

function shopName(id) {
  return apps.value.find((a) => a.id === id)?.shopName || ('#' + id)
}
async function addPayment() {
  if (!pay.applicationId || !pay.amount) {
    ElMessage.warning('请选择摊位并填写金额')
    return
  }
  await api.post('/finance/payments', { eventId, ...pay })
  ElMessage.success('流水已登记')
  pay.amount = 0
  pay.receiptNo = ''
  pay.remark = ''
  load()
}
function openDonate(row) {
  donateApp.value = row
  donateForm.actualAmount = Number(row.donation?.expectedAmount || 0)
  donateForm.voucherNo = row.donation?.voucherNo || ''
  donateDlg.value = true
}
async function confirmDonation() {
  const res = await api.post('/finance/donations/confirm', {
    applicationId: donateApp.value.id,
    actualAmount: donateForm.actualAmount,
    voucherNo: donateForm.voucherNo
  })
  if (res.status === 'DISPUTED') {
    ElMessage.warning('金额不符，已登记异常并扣减信用')
  } else {
    ElMessage.success('捐赠核对一致，已确认')
  }
  donateDlg.value = false
  load()
}
async function load() {
  apps.value = await api.get(`/events/${eventId}/applications`)
  payments.value = await api.get(`/finance/events/${eventId}/payments`)
}
onMounted(load)
</script>
