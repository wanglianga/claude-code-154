import { ref } from 'vue'
import { api } from '../api/client'

// 当前选中的活动（跨页面共享）
const events = ref([])
const currentEventId = ref(Number(localStorage.getItem('cm_event_id')) || null)

export function useEvents() {
  async function loadEvents() {
    events.value = await api.get('/events')
    if (!currentEventId.value && events.value.length) {
      currentEventId.value = events.value[0].id
      localStorage.setItem('cm_event_id', String(currentEventId.value))
    }
    return events.value
  }

  function selectEvent(id) {
    currentEventId.value = Number(id)
    localStorage.setItem('cm_event_id', String(id))
  }

  return { events, currentEventId, loadEvents, selectEvent }
}

export function fmtMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
