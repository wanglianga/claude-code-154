<template>
  <div>
    <h2 class="page-title">现场核销与巡检</h2>
    <el-row :gutter="16">
      <el-col :span="9">
        <el-card header="扫码 / 输入核销码">
          <el-input v-model="code" size="large" placeholder="请输入或扫描 6 位核销码" clearable
                    @keyup.enter="lookup">
            <template #prepend>#</template>
            <template #append>
              <el-button @click="lookup"><el-icon><Search /></el-icon></el-button>
            </template>
          </el-input>
          <el-button type="primary" size="large" style="width:100%;margin-top:12px" :disabled="!info"
                     @click="checkin">核 销 入 场</el-button>
          <el-alert v-if="info?.assignment.checkedIn" type="success" :closable="false" show-icon
                    :title="`该摊主已于 ${info.assignment.checkInAt} 核销入场`" style="margin-top:12px" />

          <el-divider>摊位快速选择</el-divider>
          <el-select v-model="quickId" placeholder="从已分配摊位选择" filterable style="width:100%"
                     @change="loadAssignment">
            <el-option v-for="a in assignments" :key="a.id"
                       :label="`${a.stall?.code} ${a.application?.stallName}（${a.vendor?.name}）`"
                       :value="a.id" />
          </el-select>
        </el-card>

        <el-card v-if="info" header="摊主信息与物资清单" style="margin-top:16px">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="摊主">{{ info.vendor.name }}（信用 {{ info.vendor.creditScore }}）</el-descriptions-item>
            <el-descriptions-item label="摊位">{{ info.stall.code }} · {{ info.stall.trafficLabel }}</el-descriptions-item>
            <el-descriptions-item label="报名品类">{{ info.application.categoryLabel }}：{{ info.application.goodsDescription }}</el-descriptions-item>
            <el-descriptions-item label="食品资质">
              <el-tag :type="info.application.foodQualified ? 'success' : 'danger'" size="small">
                {{ info.application.foodQualified ? '有资质 ' + (info.application.foodCertNo || '') : '无资质' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="收款方式">{{ info.application.paymentMethod }} {{ info.application.paymentAccount || '' }}</el-descriptions-item>
          </el-descriptions>
          <el-table :data="info.supplies" size="small" border style="margin-top:10px">
            <el-table-column prop="name" label="应领物资" />
            <el-table-column label="数量" width="90">
              <template #default="{ row }">{{ row.qty }} {{ row.unit }}</template>
            </el-table-column>
          </el-table>
          <el-button v-if="!info.assignment.suppliesClaimed" type="warning" plain style="width:100%;margin-top:10px"
                     @click="claimSupplies">确认物资已发放</el-button>
          <el-tag v-else type="success" style="width:100%;margin-top:10px;text-align:center">
            物资已领取 {{ info.assignment.suppliesClaimedAt }}
          </el-tag>
        </el-card>
      </el-col>

      <el-col :span="15">
        <el-card header="现场检查与记录">
          <template v-if="current">
            <el-space wrap>
              <el-button type="success" @click="inspect('OK')">检查：与报名相符</el-button>
              <el-button type="warning" @click="inspect('WARNING')">警告 / 限期整改</el-button>
              <el-button type="danger" @click="inspect('MISMATCH')">检查：售卖与报名不符（自动记异常 -15 分）</el-button>
              <el-button @click="goodsDlg = true">登记临时换品</el-button>
              <el-button type="danger" plain :disabled="current.earlyLeave" @click="earlyLeave">
                {{ current.earlyLeave ? `已于 ${current.leaveAt} 提前离场` : '登记提前离场（-8 分）' }}
              </el-button>
            </el-space>

            <el-divider>检查记录</el-divider>
            <el-table :data="inspections" size="small" stripe>
              <el-table-column prop="createdAt" label="时间" width="160" />
              <el-table-column label="结果" width="140">
                <template #default="{ row }">
                  <el-tag size="small" :type="resultType(row.result)">{{ row.resultLabel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="actualGoods" label="实际售卖" min-width="160" />
              <el-table-column prop="note" label="备注" min-width="120" />
            </el-table>

            <el-divider>临时换品记录</el-divider>
            <el-table :data="changes" size="small" stripe>
              <el-table-column prop="createdAt" label="时间" width="160" />
              <el-table-column prop="originalGoods" label="原内容" min-width="140" />
              <el-table-column prop="changedGoods" label="更换为" min-width="140" />
              <el-table-column label="审批" width="90">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.approved ? 'success' : 'danger'">
                    {{ row.approved ? '已批准' : '未批准' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </template>
          <el-empty v-else description="请先在左侧扫码或选择摊位" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 现场检查弹窗 -->
    <el-dialog v-model="inspectDlg" :title="`现场检查 — ${inspectForm.resultLabel}`" width="480px">
      <el-form label-width="90px">
        <el-form-item label="实际售卖"><el-input v-model="inspectForm.actualGoods" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="inspectForm.note" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectDlg = false">取消</el-button>
        <el-button type="primary" @click="submitInspect">提交检查</el-button>
      </template>
    </el-dialog>

    <!-- 临时换品弹窗 -->
    <el-dialog v-model="goodsDlg" title="登记临时换品" width="480px">
      <el-form label-width="90px">
        <el-form-item label="原报名内容">
          <el-input :model-value="current?.application?.goodsDescription" disabled type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="更换为"><el-input v-model="goodsForm.changedGoods" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="goodsForm.reason" /></el-form-item>
        <el-form-item label="工作人员意见">
          <el-switch v-model="goodsForm.approved" active-text="批准换品" inactive-text="不批准（记异常 -10 分）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="goodsDlg = false">取消</el-button>
        <el-button type="primary" @click="submitGoods">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api.js'
import { useEvent } from '../../store/event.js'

const ev = useEvent()
const code = ref(new URLSearchParams(location.search).get('code') || '')
const info = ref(null)
const assignments = ref([])
const archive = ref(null)
const quickId = ref(null)
const current = ref(null)
const inspections = ref([])
const changes = ref([])

const inspectDlg = ref(false)
const inspectForm = reactive({ result: '', resultLabel: '', actualGoods: '', note: '' })
const goodsDlg = ref(false)
const goodsForm = reactive({ changedGoods: '', reason: '', approved: true })

function resultType(r) { return { OK: 'success', WARNING: 'warning', MISMATCH: 'danger' }[r] }

async function loadList() {
  if (!ev.currentId.value) return
  const data = await api.get(`/events/${ev.currentId.value}/assignments`)
  assignments.value = data.assignments
}

async function lookup() {
  if (!code.value) return
  try {
    info.value = await api.get(`/checkin/${code.value}`)
    quickId.value = info.value.assignment.id
    await loadAssignment(info.value.assignment.id)
  } catch (e) { info.value = null }
}

async function checkin() {
  info.value = await api.post(`/checkin/${code.value}`)
  ElMessage.success('核销成功，摊主可入场')
  await loadAssignment(info.value.assignment.id)
  loadList()
}

async function loadAssignment(id) {
  current.value = assignments.value.find(a => a.id === id) || null
  // 检查与换品历史来自统一活动档案（同一活动数据量小，一次拉取）
  const arc = await api.get(`/events/${ev.currentId.value}/archive`)
  archive.value = arc
  const rec = arc.records.find(r => r.assignment?.id === id)
  inspections.value = rec?.inspections || []
  changes.value = rec?.goodsChanges || []
}

async function claimSupplies() {
  const a = await api.post(`/assignments/${info.value.assignment.id}/supplies`)
  info.value.assignment = a
  ElMessage.success('物资发放已确认')
}

function inspect(result) {
  inspectForm.result = result
  inspectForm.resultLabel = { OK: '与报名相符', WARNING: '警告/限期整改', MISMATCH: '售卖与报名不符' }[result]
  inspectForm.actualGoods = ''
  inspectForm.note = ''
  inspectDlg.value = true
}

async function submitInspect() {
  await api.post(`/assignments/${current.value.id}/inspections`, {
    result: inspectForm.result,
    actualGoods: inspectForm.actualGoods,
    note: inspectForm.note
  })
  ElMessage.success('检查记录已提交')
  inspectDlg.value = false
  loadAssignment(current.value.id)
}

async function submitGoods() {
  if (!goodsForm.changedGoods) { ElMessage.warning('请填写更换后的售卖内容'); return }
  await api.post(`/assignments/${current.value.id}/goods-changes`, { ...goodsForm })
  ElMessage.success(goodsForm.approved ? '换品已批准' : '未批准，已登记异常')
  goodsDlg.value = false
  loadAssignment(current.value.id)
}

async function earlyLeave() {
  await ElMessageBox.confirm('确认登记该摊主提前离场？将扣信用分 8 分', '提前离场', { type: 'warning' })
  await api.post(`/assignments/${current.value.id}/early-leave`)
  ElMessage.success('已登记')
  loadAssignment(current.value.id)
  loadList()
}

onMounted(async () => {
  await loadList()
  if (code.value) lookup()
})
watch(() => ev.currentId.value, loadList)
</script>
