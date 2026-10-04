<template>
  <div>
    <h2 class="page-title">摊位分配</h2>
    <el-card class="section-card">
      <div class="toolbar">
        <div>
          <el-button type="primary" :icon="MagicStick" :loading="running" @click="autoAssign"
                     v-if="auth.isAdmin.value">一键智能分配</el-button>
          <el-tag type="info" effect="plain" style="margin-left:10px">
            规则：信用/历史捐赠/公益比例评分 → 高人流优先；用电按回路容量；食品入 F 区；噪音入耐噪位；亲子入 K 区；同类摊位避免相邻
          </el-tag>
        </div>
        <div class="legend">
          <span v-for="z in zoneLegend" :key="z.v" :style="{ background: z.c }" class="lg">{{ z.l }}</span>
        </div>
      </div>

      <div v-if="result" class="result-box">
        <el-alert v-if="result.assigned?.length" type="success" :closable="false" show-icon
                  :title="`本次自动分配 ${result.assigned.length} 个摊位`" style="margin-bottom:8px" />
        <el-alert v-for="f in result.failed" :key="f.applicationId" type="error" :closable="false" show-icon
                  :title="`${f.vendorName}（${f.stallName}）分配失败：${f.reason}`" style="margin-bottom:8px" />
        <div v-if="result.circuitLoad && Object.keys(result.circuitLoad).length" class="power-load">
          回路负载：
          <el-tag v-for="(w, c) in result.circuitLoad" :key="c"
                  :type="w >= 1800 ? 'danger' : 'success'" effect="plain" style="margin-right:8px">
            {{ c }}: {{ w }}W
          </el-tag>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="15">
        <el-card header="场地摊位图（点击已占用摊位可调换）">
          <div class="map">
            <div v-for="s in stalls" :key="s.id" class="stall"
                 :class="{ occupied: assignedMap[s.id], danger: zoneOf(s.zone)==='F' }"
                 :style="stallStyle(s)"
                 @click="clickStall(s)">
              <div class="code">{{ s.code }}</div>
              <div v-if="assignedMap[s.id]" class="who">
                {{ assignedMap[s.id].application?.stallName }}
                <el-icon v-if="assignedMap[s.id].application?.category === 'FOOD'"><Food /></el-icon>
              </div>
              <div v-if="s.powerAvailable" class="pow">⚡{{ s.maxPowerWatt }}W</div>
              <div v-if="s.familySpot" class="pow">🧒亲子</div>
            </div>
            <div class="zone-note">A 主通道高人流 ｜ B 中人流 ｜ C 低人流 ｜ F 食品专区（耐噪供电）｜ K 亲子专区</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card header="分配明细与理由">
          <el-table :data="assignments" stripe height="560">
            <el-table-column label="摊位" width="70">
              <template #default="{ row }">{{ row.stall?.code }}</template>
            </el-table-column>
            <el-table-column label="摊主/品类" min-width="140">
              <template #default="{ row }">
                <b>{{ row.application?.stallName }}</b>
                <div class="muted">{{ row.vendor?.name }} · {{ row.application?.categoryLabel }} · 评分{{ row.score }}</div>
              </template>
            </el-table-column>
            <el-table-column label="分配理由" min-width="220">
              <template #default="{ row }">
                <div class="reason">{{ row.reason }}</div>
                <el-tag v-if="!row.autoAssigned" size="small" type="warning">人工调整</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="row.checkedIn ? 'success' : 'info'">
                  {{ row.checkedIn ? '已入场' : '待入场' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 人工调换弹窗 -->
    <el-dialog v-model="moveDlg" title="人工调换摊位" width="420px">
      <p v-if="moving">将 <b>{{ moving.application?.stallName }}</b>（{{ moving.vendor?.name }}）
        从 {{ moving.stall?.code }} 调换到：</p>
      <el-select v-model="targetStallId" placeholder="选择空闲摊位" style="width:100%">
        <el-option v-for="s in freeStalls" :key="s.id"
                   :label="`${s.code} ${zoneOf(s.zone)} ${s.trafficLabel}${s.powerAvailable ? ' ⚡'+s.maxPowerWatt+'W' : ''} ${s.sizeSqm}㎡`"
                   :value="s.id" />
      </el-select>
      <template #footer>
        <el-button @click="moveDlg = false">取消</el-button>
        <el-button type="primary" @click="confirmMove">确认调换</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { MagicStick, Food } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../api.js'
import { useAuth } from '../../store/auth.js'
import { useEvent } from '../../store/event.js'

const auth = useAuth()
const ev = useEvent()
const stalls = ref([])
const assignments = ref([])
const result = ref(null)
const running = ref(false)
const moveDlg = ref(false)
const moving = ref(null)
const targetStallId = ref(null)

const zoneLegend = [
  { v: 'A', l: 'A 高人流', c: '#409eff' },
  { v: 'B', l: 'B 中人流', c: '#67c23a' },
  { v: 'C', l: 'C 低人流', c: '#909399' },
  { v: 'F', l: 'F 食品', c: '#f56c6c' },
  { v: 'K', l: 'K 亲子', c: '#e6a23c' }
]

const assignedMap = computed(() => {
  const m = {}
  assignments.value.forEach(a => { m[a.stallId] = a })
  return m
})
const occupiedIds = computed(() => new Set(assignments.value.map(a => a.stallId)))
const freeStalls = computed(() => stalls.value.filter(s => !occupiedIds.value.has(s.id)))

function zoneOf(z) { return z }
function stallStyle(s) {
  const colorMap = { A: '#ecf5ff', B: '#f0f9eb', C: '#f4f4f5', F: '#fef0f0', K: '#fdf6ec' }
  return {
    left: s.posX + '%', top: s.posY + '%',
    background: colorMap[s.zone] || '#f4f4f5',
    borderColor: { A: '#409eff', B: '#67c23a', C: '#909399', F: '#f56c6c', K: '#e6a23c' }[s.zone]
  }
}

async function load() {
  if (!ev.currentId.value) return
  const data = await api.get(`/events/${ev.currentId.value}/assignments`)
  stalls.value = data.stalls
  assignments.value = data.assignments
}

async function autoAssign() {
  running.value = true
  try {
    result.value = await api.post(`/events/${ev.currentId.value}/auto-assign`)
    if (!result.value.failed.length) ElMessage.success(`已自动分配 ${result.value.assigned.length} 个摊位`)
    load()
  } finally {
    running.value = false
  }
}

function clickStall(s) {
  if (!auth.isAdmin.value) return
  const a = assignedMap.value[s.id]
  if (!a) { ElMessage.info('该摊位空闲，可在分配明细中调换到此处'); return }
  moving.value = a
  targetStallId.value = null
  moveDlg.value = true
}

async function confirmMove() {
  if (!targetStallId.value) { ElMessage.warning('请选择目标摊位'); return }
  await api.post(`/assignments/${moving.value.id}/reassign`, { stallId: targetStallId.value })
  ElMessage.success('调换成功（已重新校验用电/专区约束）')
  moveDlg.value = false
  load()
}

onMounted(load)
watch(() => ev.currentId.value, load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.legend { display: flex; gap: 6px; }
.lg { color: #fff; font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.result-box { margin-top: 12px; }
.power-load { margin-top: 8px; font-size: 13px; color: #606266; }
.map { position: relative; height: 560px; background: #fafbfc; border: 1px dashed #dcdfe6; border-radius: 8px;
  background-image: linear-gradient(#eef2f7 1px, transparent 1px), linear-gradient(90deg, #eef2f7 1px, transparent 1px);
  background-size: 10% 10%; }
.stall { position: absolute; transform: translate(-50%, -50%); width: 12%; min-width: 90px; min-height: 62px;
  border: 2px solid #409eff; border-radius: 8px; padding: 4px 6px; cursor: pointer;
  display: flex; flex-direction: column; justify-content: center; align-items: center; font-size: 12px;
  transition: all .15s; }
.stall:hover { box-shadow: 0 0 0 3px rgba(64,158,255,.25); }
.stall.occupied { border-style: solid; }
.stall .code { font-weight: 700; color: #303133; }
.stall .who { font-size: 12px; color: #1f3a5f; text-align: center; line-height: 1.3; }
.stall .pow { font-size: 11px; color: #e6a23c; }
.zone-note { position: absolute; bottom: 6px; left: 10px; font-size: 12px; color: #909399; }
.reason { font-size: 12px; color: #606266; line-height: 1.5; }
</style>
