<template>
  <div>
    <h2 class="page-title">投诉处理</h2>
    <el-card class="section-card">
      <el-button type="primary" @click="dlg = true">登记居民投诉</el-button>
    </el-card>
    <el-card>
      <el-table :data="list" stripe>
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="投诉人" width="120">
          <template #default="{ row }">{{ row.complainant }}<div class="muted">{{ row.contact }}</div></template>
        </el-table-column>
        <el-table-column label="涉及摊位" width="140">
          <template #default="{ row }">{{ stallName(row.assignmentId) || '未指定摊位' }}</template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" min-width="220" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleResult" label="处理结果" min-width="180" />
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'RESOLVED'" size="small" type="primary" @click="openHandle(row)">办理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dlg" title="登记投诉" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="涉及摊位">
          <el-select v-model="form.assignmentId" clearable filterable placeholder="可留空" style="width:100%">
            <el-option v-for="a in assignments" :key="a.id"
                       :label="`${a.stall?.code} ${a.application?.stallName}`" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉人"><el-input v-model="form.complainant" placeholder="可填匿名" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.contact" /></el-form-item>
        <el-form-item label="投诉内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="handleDlg" title="办理投诉" width="480px">
      <el-form label-width="90px">
        <el-form-item label="办理状态">
          <el-radio-group v-model="handleForm.status">
            <el-radio-button value="PROCESSING">处理中</el-radio-button>
            <el-radio-button value="RESOLVED">已办结</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理结果"><el-input v-model="handleForm.handleResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDlg = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">保存</el-button>
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
const dlg = ref(false)
const form = reactive({ assignmentId: null, complainant: '', contact: '', content: '' })
const handleDlg = ref(false)
const handleForm = reactive({ id: null, status: 'PROCESSING', handleResult: '' })

function statusType(s) { return { OPEN: 'danger', PROCESSING: 'warning', RESOLVED: 'success' }[s] }
function stallName(aid) {
  const a = assignments.value.find(x => x.id === aid)
  return a ? `${a.stall?.code} ${a.application?.stallName}` : ''
}

async function load() {
  if (!ev.currentId.value) return
  list.value = await api.get(`/events/${ev.currentId.value}/complaints`)
  const data = await api.get(`/events/${ev.currentId.value}/assignments`)
  assignments.value = data.assignments
}
async function submit() {
  if (!form.content) { ElMessage.warning('请填写投诉内容'); return }
  await api.post(`/events/${ev.currentId.value}/complaints`, form)
  ElMessage.success('投诉已登记')
  dlg.value = false
  Object.assign(form, { assignmentId: null, complainant: '', contact: '', content: '' })
  load()
}
function openHandle(row) {
  handleForm.id = row.id
  handleForm.status = row.status === 'OPEN' ? 'PROCESSING' : row.status
  handleForm.handleResult = row.handleResult || ''
  handleDlg.value = true
}
async function submitHandle() {
  await api.post(`/complaints/${handleForm.id}/handle`, handleForm)
  ElMessage.success('已保存办理结果')
  handleDlg.value = false
  load()
}
onMounted(load)
watch(() => ev.currentId.value, load)
</script>
