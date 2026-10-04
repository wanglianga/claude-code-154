<template>
  <div class="page-container">
    <el-card style="max-width:760px;">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>我的信用记录</span>
          <el-tag size="large" :type="scoreType(score)">{{ score }} 分</el-tag>
        </div>
      </template>
      <el-alert type="info" :closable="false" style="margin-bottom:14px;">
        信用分初始 100。按时入场、足额捐赠会加分；卖品不符、用电超载、提前离场、被投诉、捐赠流水不符会扣分。
        信用、历史捐赠、食品风险与用电需求会共同影响下一次活动的摊位位置。
      </el-alert>
      <el-progress :percentage="scorePercent" :status="score >= 100 ? 'success' : score >= 80 ? '' : 'exception'"
                   :stroke-width="18" text-inside />
      <el-divider />
      <el-timeline>
        <el-timeline-item v-for="log in logs" :key="log.id" :timestamp="log.createdAt"
                          :type="log.delta >= 0 ? 'success' : 'danger'">
          <el-tag size="small" :type="log.delta >= 0 ? 'success' : 'danger'">
            {{ log.delta >= 0 ? '+' : '' }}{{ log.delta }}
          </el-tag>
          {{ log.reason }}
          <span style="color:#909399; font-size:12px; margin-left:6px;">（变动后 {{ log.scoreAfter }} 分）</span>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!logs.length" description="暂无信用变动记录" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { api } from '../../api/client'

const auth = useAuthStore()
const logs = ref([])
const score = computed(() => auth.user?.creditScore ?? 100)
const scorePercent = computed(() => Math.min(100, Math.round(score.value / 120 * 100)))

function scoreType(s) {
  return s >= 100 ? 'success' : s >= 80 ? 'warning' : 'danger'
}
onMounted(async () => {
  const r = await api.get('/my/credit')
  logs.value = r.logs || []
})
</script>
