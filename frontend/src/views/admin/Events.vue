<template>
  <div>
    <h2 class="page-title">活动与摊位管理</h2>
    <el-card class="section-card">
      <template #header>
        <div class="head">
          <b>活动列表</b>
          <el-button type="primary" :icon="Plus" @click="openEvent(null)">新建活动</el-button>
        </div>
      </template>
      <el-table :data="events" stripe>
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column prop="location" label="地点" width="160" />
        <el-table-column prop="startTime" label="开始" width="160" />
        <el-table-column label="公益目标" width="120">
          <template #default="{ row }">¥{{ row.donationGoal }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.statusLabel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="openEvent(row)">编辑</el-button>
            <el-button size="small" type="success" @click="ev.select(row.id)">设为当前</el-button>
            <el-dropdown @command="s => changeStatus(row, s)" style="margin-left:8px">
              <el-button size="small">状态<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="s in ['PLANNED', 'ONGOING', 'FINISHED', 'PUBLISHED']" :key="s" :command="s">
                    {{ statusLabel[s] }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <template #header>
        <div class="head">
          <b>摊位布局（{{ ev.current.value?.name }}，共 {{ stalls.length }} 个摊位）</b>
          <el-button type="primary" :icon="Plus" @click="openStall(null)">新增摊位</el-button>
        </div>
      </template>
      <el-table :data="stalls" stripe>
        <el-table-column prop="code" label="编号" width="90" />
        <el-table-column label="分区" width="140">
          <template #default="{ row }">
            <el-tag :color="zoneColor(row.zone)" style="color:#fff;border:none">{{ zoneLabel(row.zone) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sizeSqm" label="面积(㎡)" width="90" />
        <el-table-column prop="trafficLabel" label="人流" width="90" />
        <el-table-column label="供电" width="160">
          <template #default="{ row }">
            <span v-if="row.powerAvailable">{{ row.maxPowerWatt }}W / 回路{{ row.powerCircuit }}</span>
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="特性" width="170">
          <template #default="{ row }">
            <el-tag v-if="row.noiseSuitable" size="small" type="danger" effect="plain">耐噪</el-tag>
            <el-tag v-if="row.familySpot" size="small" type="warning" effect="plain" style="margin-left:4px">亲子</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="坐标" width="110">
          <template #default="{ row }">{{ row.posX }},{{ row.posY }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="openStall(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 活动编辑弹窗 -->
    <el-dialog v-model="eventDlg" :title="eventForm.id ? '编辑活动' : '新建活动'" width="560px">
      <el-form :model="eventForm" label-width="110px">
        <el-form-item label="活动名称"><el-input v-model="eventForm.name" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="eventForm.location" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="eventForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="eventForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="eventForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="公益目标(元)"><el-input-number v-model="eventForm.donationGoal" :min="0" :step="100" /></el-form-item>
        <el-form-item label="捐赠用途"><el-input v-model="eventForm.donationPurpose" /></el-form-item>
        <el-form-item label="总供电容量(W)"><el-input-number v-model="eventForm.totalPowerWatt" :min="0" :step="500" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="eventDlg = false">取消</el-button>
        <el-button type="primary" @click="saveEvent">保存</el-button>
      </template>
    </el-dialog>

    <!-- 摊位编辑弹窗 -->
    <el-dialog v-model="stallDlg" :title="stallForm.id ? '编辑摊位' : '新增摊位'" width="520px">
      <el-form :model="stallForm" label-width="100px">
        <el-form-item label="编号"><el-input v-model="stallForm.code" placeholder="如 A-06" /></el-form-item>
        <el-form-item label="分区">
          <el-select v-model="stallForm.zone">
            <el-option v-for="z in zones" :key="z.v" :label="z.l" :value="z.v" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积(㎡)"><el-input-number v-model="stallForm.sizeSqm" :min="3" :max="20" :step="1" /></el-form-item>
        <el-form-item label="人流等级">
          <el-radio-group v-model="stallForm.trafficLevel">
            <el-radio-button value="HIGH">高人流</el-radio-button>
            <el-radio-button value="MEDIUM">中人流</el-radio-button>
            <el-radio-button value="LOW">低人流</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="提供供电">
          <el-switch v-model="stallForm.powerAvailable" />
        </el-form-item>
        <template v-if="stallForm.powerAvailable">
          <el-form-item label="最大功率(W)"><el-input-number v-model="stallForm.maxPowerWatt" :min="100" :max="3000" :step="100" /></el-form-item>
          <el-form-item label="供电回路"><el-input v-model="stallForm.powerCircuit" placeholder="如 PA" /></el-form-item>
        </template>
        <el-form-item label="耐噪/油烟"><el-switch v-model="stallForm.noiseSuitable" /></el-form-item>
        <el-form-item label="亲子摊位"><el-switch v-model="stallForm.familySpot" /></el-form-item>
        <el-form-item label="示意图坐标">
          <el-input-number v-model="stallForm.posX" :min="0" :max="100" /> X
          <el-input-number v-model="stallForm.posY" :min="0" :max="100" style="margin-left:8px" /> Y
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stallDlg = false">取消</el-button>
        <el-button type="primary" @click="saveStall">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api.js'
import { useEvent } from '../../store/event.js'

const ev = useEvent()
const events = ref([])
const stalls = ref([])
const zones = [
  { v: 'A', l: 'A 主通道高人流区' }, { v: 'B', l: 'B 中人流区' },
  { v: 'C', l: 'C 低人流区' }, { v: 'F', l: 'F 食品专区' }, { v: 'K', l: 'K 亲子专区' }
]
const statusLabel = { PLANNED: '筹备中', ONGOING: '进行中', FINISHED: '已结束', PUBLISHED: '已公示' }

const eventDlg = ref(false)
const eventForm = reactive({})
const stallDlg = ref(false)
const stallForm = reactive({})

function statusType(s) { return { PLANNED: 'info', ONGOING: 'success', FINISHED: 'warning', PUBLISHED: 'danger' }[s] }
function zoneLabel(z) { return { A: 'A 高人流', B: 'B 中人流', C: 'C 低人流', F: 'F 食品专区', K: 'K 亲子专区' }[z] || z }
function zoneColor(z) { return { A: '#409eff', B: '#67c23a', C: '#909399', F: '#f56c6c', K: '#e6a23c' }[z] || '#909399' }

async function loadEvents() {
  events.value = await api.get('/events')
  ev.setEvents(events.value)
}
async function loadStalls() {
  if (ev.currentId.value) stalls.value = await api.get(`/events/${ev.currentId.value}/stalls`)
}
function openEvent(row) {
  Object.keys(eventForm).forEach(k => delete eventForm[k])
  if (row) Object.assign(eventForm, row)
  else Object.assign(eventForm, { name: '', location: '', description: '', startTime: '', endTime: '', donationGoal: 5000, donationPurpose: '', totalPowerWatt: 8000 })
  eventDlg.value = true
}
async function saveEvent() {
  if (eventForm.id) await api.put(`/events/${eventForm.id}`, eventForm)
  else await api.post('/events', eventForm)
  ElMessage.success('保存成功')
  eventDlg.value = false
  loadEvents()
}
async function changeStatus(row, s) {
  await api.put(`/events/${row.id}/status`, { status: s })
  ElMessage.success('状态已更新')
  loadEvents()
}
function openStall(row) {
  Object.keys(stallForm).forEach(k => delete stallForm[k])
  if (row) Object.assign(stallForm, row)
  else Object.assign(stallForm, { eventId: ev.currentId.value, code: 'A-', zone: 'A', sizeSqm: 6, trafficLevel: 'MEDIUM', powerAvailable: false, maxPowerWatt: 0, powerCircuit: 'N', noiseSuitable: false, familySpot: false, posX: 50, posY: 50 })
  stallDlg.value = true
}
async function saveStall() {
  if (stallForm.id) await api.put(`/events/stalls/${stallForm.id}`, stallForm)
  else await api.post(`/events/${ev.currentId.value}/stalls`, stallForm)
  ElMessage.success('保存成功')
  stallDlg.value = false
  loadStalls()
}

onMounted(async () => { await loadEvents(); await loadStalls() })
watch(() => ev.currentId.value, loadStalls)
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
</style>
