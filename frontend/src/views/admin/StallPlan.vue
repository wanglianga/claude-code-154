<template>
  <div class="page-container">
    <el-card class="section-card">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
          <span>摊位平面图（{{ stalls.length }} 个）
            <el-text type="info" size="small">
              分配综合考虑：大小 / 人流 / 用电容量 / 噪音敏感 / 同类相邻冲突 / 公益属性
            </el-text>
          </span>
          <div>
            <el-button @click="batchDialog = true">按行列批量生成</el-button>
            <el-button type="primary" :icon="Plus" @click="openAdd">新增摊位</el-button>
          </div>
        </div>
      </template>

      <div class="zone-legend" style="margin-bottom:10px;">
        <span><i class="zone-dot" style="background:#67c23a;"></i>食品区（餐饮必须在此）</span>
        <span><i class="zone-dot" style="background:#f56c6c;"></i>入口区（高人流/噪音敏感）</span>
        <span><i class="zone-dot" style="background:#409eff;"></i>中心区（高人流）</span>
        <span><i class="zone-dot" style="background:#909399;"></i>边角区（低人流）</span>
        <span><i class="zone-dot" style="background:#fff;border:1px solid #409eff;"></i>蓝色=已分配</span>
      </div>

      <div class="stall-grid">
        <div v-for="(row, r) in grid" :key="r" class="stall-row">
          <div v-for="s in row" :key="s.id" class="stall-box"
               :class="{ assigned: s.status === 'ASSIGNED' }"
               :style="s.status === 'ASSIGNED' ? '' : { borderTop: `3px solid ${zoneColor(s.zone)}` }"
               @click="openStall(s)">
            <div style="display:flex; justify-content:space-between;">
              <span class="stall-code">{{ s.code }}</span>
              <span class="stall-zone">{{ s.zone }}</span>
            </div>
            <div class="stall-shop">{{ shopOf(s) || '空闲' }}</div>
            <div class="stall-tags">
              <el-tag size="small" type="info">人流{{ s.trafficLevel }}</el-tag>
              <el-tag size="small" type="info">大小{{ s.sizeLevel }}</el-tag>
              <el-tag v-if="s.powerAvailable" size="small" type="warning">电{{ s.powerCapacity }}W</el-tag>
              <el-tag v-if="s.noiseSensitive" size="small" type="danger">噪敏</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 摊位编辑 -->
    <el-dialog v-model="dialog" :title="form.id ? `编辑摊位 ${form.code}` : '新增摊位'" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="摊位编号"><el-input v-model="form.code" placeholder="如 A-01" /></el-form-item>
        <el-form-item label="区域">
          <el-select v-model="form.zone" style="width:100%">
            <el-option v-for="z in zones" :key="z" :label="z" :value="z" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="行"><el-input-number v-model="form.rowNo" :min="1" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="列"><el-input-number v-model="form.colNo" :min="1" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="摊位大小">
              <el-select v-model="form.sizeLevel"><el-option :value="1" label="小" /><el-option :value="2" label="中" /><el-option :value="3" label="大" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人流等级">
              <el-select v-model="form.trafficLevel"><el-option :value="1" label="低" /><el-option :value="2" label="中" /><el-option :value="3" label="高" /></el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="可供电"><el-switch v-model="form.powerAvailable" /></el-form-item>
        <el-form-item v-if="form.powerAvailable" label="容量(瓦)">
          <el-input-number v-model="form.powerCapacity" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="噪音敏感"><el-switch v-model="form.noiseSensitive" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="form.id" type="danger" @click="remove">删除</el-button>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量生成 -->
    <el-dialog v-model="batchDialog" title="按行列批量生成摊位" width="480px">
      <el-alert type="info" :closable="false" style="margin-bottom:12px;">
        将按 行×列 生成；最后一行设为食品区（供电 1000W），第一行为入口高人流区（噪音敏感），中间为中心区，两侧为边角区。
      </el-alert>
      <el-form label-width="100px">
        <el-form-item label="行数"><el-input-number v-model="batch.rows" :min="1" :max="10" /></el-form-item>
        <el-form-item label="列数"><el-input-number v-model="batch.cols" :min="1" :max="12" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialog = false">取消</el-button>
        <el-button type="primary" @click="generate">生成（清空现有摊位后重建）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, getToken } from '../../api/client'

const route = useRoute()
const eventId = Number(route.params.id)
const stalls = ref([])
const apps = ref([])
const dialog = ref(false)
const batchDialog = ref(false)
const batch = reactive({ rows: 4, cols: 5 })
const zones = ['入口区', '中心区', '边角区', '食品区']

const emptyForm = () => ({
  id: null, eventId, code: '', zone: '中心区', rowNo: 1, colNo: 1,
  sizeLevel: 2, trafficLevel: 2, powerAvailable: false, powerCapacity: 0, noiseSensitive: false
})
const form = reactive(emptyForm())

const appMap = computed(() => {
  const m = new Map()
  for (const a of apps.value) {
    if (a.stallId) {
      m.set(a.stallId, a)
    }
  }
  return m
})

function shopOf(s) {
  return appMap.value.get(s.id)?.shopName || ''
}

function zoneColor(zone) {
  return { 食品区: '#67c23a', 入口区: '#f56c6c', 中心区: '#409eff', 边角区: '#909399' }[zone] || '#dcdfe6'
}

// 按 row/col 渲染网格；缺失位置留空占位
const grid = computed(() => {
  const byPos = new Map()
  for (const s of stalls.value) {
    byPos.set(`${s.rowNo}-${s.colNo}`, s)
  }
  const maxRow = Math.max(1, ...stalls.value.map((s) => s.rowNo || 1))
  const maxCol = Math.max(1, ...stalls.value.map((s) => s.colNo || 1))
  const out = []
  for (let r = 1; r <= maxRow; r++) {
    const row = []
    for (let c = 1; c <= maxCol; c++) {
      row.push(byPos.get(`${r}-${c}`) || { id: `empty-${r}-${c}`, code: '', zone: '', status: 'EMPTY', rowNo: r, colNo: c })
    }
    out.push(row)
  }
  return out
})

function openAdd() {
  Object.assign(form, emptyForm())
  dialog.value = true
}
function openStall(s) {
  if (s.status === 'EMPTY') {
    Object.assign(form, emptyForm(), { rowNo: s.rowNo, colNo: s.colNo, code: '' })
    dialog.value = true
    return
  }
  Object.assign(form, s)
  dialog.value = true
}
async function save() {
  if (!form.code) {
    ElMessage.warning('请填写摊位编号')
    return
  }
  if (form.id) {
    await api.put(`/stalls/${form.id}`, form)
  } else {
    delete form.id
    await api.post('/stalls', { ...form, eventId })
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove() {
  await ElMessageBox.confirm('确认删除该摊位？', '提示', { type: 'warning' })
  await fetch(`/api/stalls/${form.id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  dialog.value = false
  ElMessage.success('已删除')
  load()
}
async function generate() {
  // 删除全部
  for (const s of stalls.value) {
    await fetch(`/api/stalls/${s.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${getToken()}` }
    })
  }
  for (let r = 1; r <= batch.rows; r++) {
    for (let c = 1; c <= batch.cols; c++) {
      let zone = '边角区'
      if (r === 1) zone = '入口区'
      else if (r === batch.rows) zone = '食品区'
      else if (c >= 2 && c <= batch.cols - 1) zone = '中心区'
      await api.post('/stalls', {
        eventId,
        code: String.fromCharCode(64 + r) + '-' + String(c).padStart(2, '0'),
        zone, rowNo: r, colNo: c,
        sizeLevel: r === batch.rows || (r <= 2 && c === Math.ceil(batch.cols / 2)) ? 3 : 2,
        trafficLevel: zone === '入口区' || zone === '中心区' ? 3 : zone === '食品区' ? 2 : 1,
        powerAvailable: r === batch.rows,
        powerCapacity: r === batch.rows ? 1000 : 0,
        noiseSensitive: r === 1
      })
    }
  }
  batchDialog.value = false
  ElMessage.success('已生成 ' + batch.rows * batch.cols + ' 个摊位')
  load()
}

async function load() {
  stalls.value = await api.get(`/stalls?eventId=${eventId}`)
  apps.value = await api.get(`/events/${eventId}/applications`)
}
onMounted(load)
</script>
