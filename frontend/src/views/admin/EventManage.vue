<template>
  <div class="page-container">
    <el-card class="section-card">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>活动与公益目标</span>
          <el-button type="primary" :icon="Plus" @click="openCreate">新建活动</el-button>
        </div>
      </template>
      <el-table :data="events" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column prop="eventDate" label="日期" width="120" />
        <el-table-column prop="location" label="地点" width="160" />
        <el-table-column label="公益目标" min-width="220">
          <template #default="{ row }">
            <div>{{ row.publicGoal }}</div>
            <div style="color:#e6a23c; font-size:12px;">目标筹款 ¥{{ fmtMoney(row.goalAmount) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="dictType(EVENT_STATUS, row.status)" size="small">{{ dictLabel(EVENT_STATUS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="primary" @click="$router.push(`/events/${row.id}/stalls`)">平面图</el-button>
            <el-button size="small" type="success" @click="$router.push(`/events/${row.id}/applications`)">审核</el-button>
            <el-dropdown split-button size="small" @click="goFlow(row)" style="margin-top:4px;">
              流程
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="setStatus(row, 'RECRUITING')">设为招募中</el-dropdown-item>
                  <el-dropdown-item @click="setStatus(row, 'REVIEWING')">设为审核分配中</el-dropdown-item>
                  <el-dropdown-item @click="setStatus(row, 'ONGOING')">设为进行中</el-dropdown-item>
                  <el-dropdown-item @click="setStatus(row, 'FINISHED')">设为已结束</el-dropdown-item>
                  <el-dropdown-item divided @click="$router.push(`/events/${row.id}/publicity`)">发布居民公示</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog" :title="form.id ? '编辑活动' : '新建活动'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.eventDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="公益目标">
          <el-input v-model="form.publicGoal" type="textarea" :rows="2" placeholder="如：为困难家庭学生筹集助学款" />
        </el-form-item>
        <el-form-item label="筹款目标(元)">
          <el-input-number v-model="form.goalAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api/client'
import { useEvents, fmtMoney } from '../../composables/useEvents'
import { EVENT_STATUS, dictLabel, dictType } from '../../dict'

const { events, loadEvents, selectEvent } = useEvents()
const dialog = ref(false)
const form = reactive({ id: null, name: '', eventDate: '', location: '', publicGoal: '', goalAmount: 0 })

function openCreate() {
  Object.assign(form, { id: null, name: '', eventDate: '', location: '', publicGoal: '', goalAmount: 0 })
  dialog.value = true
}
function openEdit(row) {
  Object.assign(form, { ...row, goalAmount: Number(row.goalAmount) })
  dialog.value = true
}
async function save() {
  if (!form.name) {
    ElMessage.warning('请填写活动名称')
    return
  }
  if (form.id) {
    await api.put(`/events/${form.id}`, form)
  } else {
    await api.post('/events', form)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  loadEvents()
}
async function setStatus(row, status) {
  await api.put(`/events/${row.id}/status?status=${status}`, {})
  ElMessage.success('状态已更新')
  loadEvents()
}
function goFlow(row) {
  selectEvent(row.id)
}
onMounted(loadEvents)
</script>
