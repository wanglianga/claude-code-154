import { reactive, computed } from 'vue'

/** 当前选中活动（跨页面共享，localStorage 持久化） */
const state = reactive({
  events: [],
  currentId: Number(localStorage.getItem('cm_event_id')) || null
})

export function useEvent() {
  const currentId = computed(() => state.currentId)
  const current = computed(() => state.events.find(e => e.id === state.currentId) || null)

  function setEvents(list) {
    state.events = list || []
    if ((!state.currentId || !state.events.find(e => e.id === state.currentId)) && state.events.length) {
      select(state.events[0].id)
    }
  }

  function select(id) {
    state.currentId = Number(id)
    localStorage.setItem('cm_event_id', String(id))
  }

  return { state, currentId, current, setEvents, select }
}
