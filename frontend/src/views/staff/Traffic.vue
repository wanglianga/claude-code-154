<template>
  <div>
    <h2 class="page-title">客流统计</h2>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card header="按时段登记客流">
          <el-form label-width="90px">
            <el-form-item label="时段">
              <el-input v-model="form.period" placeholder="如 14:00-15:00" />
            </el-form-item>
            <el-form-item label="入场人数">
              <el-input-number v-model="form.visitorCount" :min="0" :step="50" style="width:100%" />
            </el-form-item>
            <el-form-item label="备注"><el-input v-model="form.note" placeholder="入口/天气等" /></el-form-item>
            <el-button type="primary" style="width:100%" @click="submit">登记</el-button>
          </el-form>
          <el-divider />
          <el-statistic title="累计客流" :value="total" />
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card header="客流时段走势">
          <div ref="chartRef" style="height:420px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import api from '../../api.js'
import { useEvent } from '../../store/event.js'

const ev = useEvent()
const list = ref([])
const chartRef = ref(null)
let chart = null
const form = reactive({ period: '', visitorCount: 100, note: '' })
const total = ref(0)

async function load() {
  if (!ev.currentId.value) return
  list.value = await api.get(`/events/${ev.currentId.value}/traffic`)
  total.value = list.value.reduce((s, t) => s + t.visitorCount, 0)
  await nextTick()
  render()
}

function render() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: list.value.map(t => t.period || t.createdAt?.substring(11, 16)) },
    yAxis: { type: 'value', name: '人数' },
    series: [{
      type: 'bar', data: list.value.map(t => t.visitorCount),
      itemStyle: { color: '#409eff', borderRadius: [6, 6, 0, 0] },
      label: { show: true, position: 'top' }
    }]
  })
}

async function submit() {
  if (!form.period) { ElMessage.warning('请填写时段'); return }
  await api.post(`/events/${ev.currentId.value}/traffic`, form)
  ElMessage.success('客流已登记')
  form.visitorCount = 100
  form.note = ''
  load()
}

onMounted(load)
watch(() => ev.currentId.value, load)
</script>
