<template>
  <div class="page-container">
    <el-card>
      <el-tabs v-model="tab">
        <!-- 投诉 -->
        <el-tab-pane :label="`投诉处理(${complaints.length})`" name="complaints">
          <div style="margin-bottom:10px;">
            <el-button type="primary" @click="openComplaint">登记居民投诉</el-button>
          </div>
          <el-table :data="complaints" border size="small">
            <el-table-column prop="createdAt" label="时间" width="170" />
            <el-table-column label="被投诉摊位" width="150">
              <template #default="{ row }">{{ shopName(row.applicationId) }}</template>
            </el-table-column>
            <el-table-column prop="complaintType" label="类型" width="100" />
            <el-table-column prop="content" label="投诉内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="reporterName" label="投诉人" width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="dictType(COMPLAINT_STATUS, row.status)">{{ dictLabel(COMPLAINT_STATUS, row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="处理结果" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.handleResult || '—' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="openHandle(row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 异常 -->
        <el-tab-pane :label="`现场异常档案(${incidents.length})`" name="incidents">
          <div style="margin-bottom:10px; display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
            <el-button type="danger" @click="openIncident">人工登记异常</el-button>
            <el-divider direction="vertical" />
            <span style="font-size:13px; color:#606266;">现场客流：</span>
            <el-input-number v-model="visitorCount" :min="0" size="small" />
            <el-button size="small" @click="saveVisitors">更新客流</el-button>
          </div>
          <el-table :data="incidents" border size="small">
            <el-table-column prop="createdAt" label="时间" width="170" />
            <el-table-column label="类型" width="170">
              <template #default="{ row }">
                <el-tag size="small" :type="row.severity >= 3 ? 'danger' : 'warning'">
                  {{ dictLabel(INCIDENT_TYPES, row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="关联摊位" width="140">
              <template #default="{ row }">{{ row.applicationId ? shopName(row.applicationId) : '活动整体' }}</template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 'RESOLVED' ? 'success' : 'danger'">
                  {{ row.status === 'RESOLVED' ? '已处理' : '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="resolution" label="处理结果" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <el-button v-if="row.status === 'OPEN'" size="small" type="success" @click="openResolve(row)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 投诉登记 -->
    <el-dialog v-model="complaintDlg" title="登记居民投诉" width="480px">
      <el-form :model="cf" label-width="90px">
        <el-form-item label="被投诉摊位">
          <el-select v-model="cf.applicationId" clearable filterable style="width:100%">
            <el-option v-for="a in apps" :key="a.id" :label="a.shopName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉类型">
          <el-select v-model="cf.complaintType" style="width:100%">
            <el-option v-for="t in ['噪音', '卫生', '卖品不符', '价格争议', '服务态度', '其他']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉人"><el-input v-model="cf.reporterName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="cf.reporterPhone" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="cf.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="complaintDlg = false">取消</el-button>
        <el-button type="primary" @click="submitComplaint">提交</el-button>
      </template>
    </el-dialog>

    <!-- 投诉处理 -->
    <el-dialog v-model="handleDlg" title="处理投诉" width="440px">
      <el-form label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="handle.status" style="width:100%">
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已处理" value="RESOLVED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果"><el-input v-model="handle.handleResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDlg = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认</el-button>
      </template>
    </el-dialog>

    <!-- 异常登记 -->
    <el-dialog v-model="incidentDlg" title="人工登记现场异常" width="480px">
      <el-form :model="inf" label-width="100px">
        <el-form-item label="异常类型">
          <el-select v-model="inf.type" style="width:100%">
            <el-option v-for="t in INCIDENT_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联摊位">
          <el-select v-model="inf.applicationId" clearable filterable style="width:100%">
            <el-option v-for="a in apps" :key="a.id" :label="a.shopName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-radio-group v-model="inf.severity">
            <el-radio :value="1">提示</el-radio>
            <el-radio :value="2">警告</el-radio>
            <el-radio :value="3">严重</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="inf.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="incidentDlg = false">取消</el-button>
        <el-button type="danger" @click="submitIncident">登记</el-button>
      </template>
    </el-dialog>

    <!-- 异常处理 -->
    <el-dialog v-model="resolveDlg" title="处理结果" width="440px">
      <el-input v-model="resolveText" type="textarea" :rows="3" placeholder="说明处置措施与结果" />
      <template #footer>
        <el-button @click="resolveDlg = false">取消</el-button>
        <el-button type="success" @click="submitResolve">确认闭环</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api/client'
import { COMPLAINT_STATUS, INCIDENT_TYPES, dictLabel, dictType } from '../../dict'

const eventId = Number(localStorage.getItem('cm_event_id'))
const tab = ref('complaints')
const complaints = ref([])
const incidents = ref([])
const apps = ref([])
const visitorCount = ref(0)

const complaintDlg = ref(false)
const cf = reactive({ applicationId: null, complaintType: '其他', reporterName: '', reporterPhone: '', content: '' })
const handleDlg = ref(false)
const handle = reactive({ id: null, status: 'RESOLVED', handleResult: '' })
const incidentDlg = ref(false)
const inf = reactive({ type: 'OTHER', applicationId: null, severity: 2, description: '' })
const resolveDlg = ref(false)
const resolveText = ref('')
const resolvingId = ref(null)

function shopName(id) {
  return apps.value.find((a) => a.id === id)?.shopName || ('#' + id)
}
function openComplaint() {
  Object.assign(cf, { applicationId: null, complaintType: '其他', reporterName: '', reporterPhone: '', content: '' })
  complaintDlg.value = true
}
async function submitComplaint() {
  if (!cf.content) {
    ElMessage.warning('请填写投诉内容')
    return
  }
  await api.post('/onsite/complaints', { eventId, ...cf })
  ElMessage.success('投诉已登记')
  complaintDlg.value = false
  load()
}
function openHandle(row) {
  Object.assign(handle, { id: row.id, status: 'RESOLVED', handleResult: row.handleResult || '' })
  handleDlg.value = true
}
async function submitHandle() {
  await api.post(`/onsite/complaints/${handle.id}/handle`, { status: handle.status, handleResult: handle.handleResult })
  ElMessage.success('已更新（投诉成立将扣减摊主信用）')
  handleDlg.value = false
  load()
}
function openIncident() {
  Object.assign(inf, { type: 'OTHER', applicationId: null, severity: 2, description: '' })
  incidentDlg.value = true
}
async function submitIncident() {
  if (!inf.description) {
    ElMessage.warning('请填写描述')
    return
  }
  await api.post('/onsite/incidents', { eventId, ...inf })
  ElMessage.success('异常已进入活动档案')
  incidentDlg.value = false
  load()
}
function openResolve(row) {
  resolvingId.value = row.id
  resolveText.value = row.resolution || ''
  resolveDlg.value = true
}
async function submitResolve() {
  await api.post(`/onsite/incidents/${resolvingId.value}/resolve`, { resolution: resolveText.value })
  ElMessage.success('异常已闭环')
  resolveDlg.value = false
  load()
}
async function saveVisitors() {
  await api.put(`/onsite/events/${eventId}/visitors`, { visitorCount: visitorCount.value })
  ElMessage.success('客流已更新')
}
async function load() {
  apps.value = await api.get(`/events/${eventId}/applications`)
  complaints.value = await api.get(`/onsite/events/${eventId}/complaints`)
  incidents.value = await api.get(`/onsite/events/${eventId}/incidents`)
  const events = await api.get('/events')
  visitorCount.value = events.find((e) => e.id === eventId)?.visitorCount || 0
}
onMounted(load)
</script>
