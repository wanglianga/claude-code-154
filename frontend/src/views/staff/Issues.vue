<template>
  <div>
    <h2 class="page-title">异常管理</h2>
    <el-card class="section-card">
      <el-button type="danger" @click="dlg = true">登记异常</el-button>
      <el-tag type="info" effect="plain" style="margin-left:10px">
        覆盖：食品资质不完整、售卖与报名不符、同类摊位冲突、用电超载、提前离场、捐赠与流水不一致
      </el-tag>
    </el-card>
    <el-card>
      <el-table :data="list" stripe>
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="类型" width="160">
          <template #default="{ row }"><el-tag type="danger" size="small">{{ row.typeLabel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="涉及摊位" width="160">
          <template #default="{ row }">{{ stallText(row.assignmentId) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="异常描述" min-width="240" />
        <el-table-column label="扣分" width="80">
          <template #default="{ row }">-{{ row.creditPenalty }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'OPEN' ? 'danger' : 'success'">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resolution" label="处理结果" min-width="200" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 'OPEN'" size="small" type="primary" @click="openResolve(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg" title="登记异常" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="异常类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option v-for="t in types" :key="t.v" :label="t.l" :value="t.v" />
          </el-select>
        </el-form-item>
        <el-form-item label="涉及摊位">
          <el-select v-model="form.assignmentId" clearable filterable placeholder="可留空" style="width:100%">
            <el-option v-for="a in assignments" :key="a.id"
                       :label="`${a.stall?.code} ${a.application?.stallName}（${a.vendor?.name}）`" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="信用扣分">
          <el-input-number v-model="form.creditPenalty" :min="0" :max="50" />
          <span class="muted">食品资质类异常会同时标记摊主食品风险</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="danger" @click="submit">登记并扣分</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resolveDlg" title="处理异常" width="500px">
      <el-form label-width="100px">
        <el-form-item label="处理措施"><el-input v-model="form2.resolution" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="追加扣分">
          <el-switch v-model="form2.penalize" active-text="处理同时追加信用扣分" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDlg = false">取消</el-button>
        <el-button type="primary" @click="submitResolve">办结</el-button>
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
const list = ref([])
const assignments = ref([])
const types = [
  { v: 'FOOD_QUALIFICATION', l: '食品资质不完整' },
  { v: 'MISMATCH', l: '现场售卖与报名不符' },
  { v: 'CATEGORY_CONFLICT', l: '同类摊位冲突' },
  { v: 'POWER_OVERLOAD', l: '用电超载' },
  { v: 'EARLY_LEAVE', l: '摊主提前离场' },
  { v: 'DONATION_MISMATCH', l: '捐赠金额与流水不一致' },
  { v: 'OTHER', l: '其他异常' }
]
const dlg = ref(false)
const form = reactive({ type: 'FOOD_QUALIFICATION', assignmentId: null, description: '', creditPenalty: 10 })
const resolveDlg = ref(false)
const form2 = reactive({ id: null, resolution: '', penalize: false })

function stallText(aid) {
  const a = assignments.value.find(x => x.id === aid)
  return a ? `${a.stall?.code} ${a.application?.stallName}` : '—'
}

async function load() {
  if (!ev.currentId.value) return
  list.value = await api.get(`/events/${ev.currentId.value}/issues`)
  const data = await api.get(`/events/${ev.currentId.value}/assignments`)
  assignments.value = data.assignments
}
async function submit() {
  if (!form.description) { ElMessage.warning('请填写异常描述'); return }
  await api.post(`/events/${ev.currentId.value}/issues`, form)
  ElMessage.success('异常已登记并扣减信用分')
  dlg.value = false
  Object.assign(form, { type: 'FOOD_QUALIFICATION', assignmentId: null, description: '', creditPenalty: 10 })
  load()
}
function openResolve(row) {
  form2.id = row.id
  form2.resolution = ''
  form2.penalize = false
  resolveDlg.value = true
}
async function submitResolve() {
  if (!form2.resolution) { ElMessage.warning('请填写处理措施'); return }
  await api.post(`/issues/${form2.id}/resolve`, form2)
  ElMessage.success('异常已办结')
  resolveDlg.value = false
  load()
}
onMounted(load)
watch(() => ev.currentId.value, load)
</script>
