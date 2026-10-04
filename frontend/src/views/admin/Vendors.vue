<template>
  <div>
    <h2 class="page-title">摊主信用档案</h2>
    <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
              title="摊主信用分、历史捐赠、投诉与食品风险记录会影响后续活动的摊位位置：信用高、捐赠多者优先高人流区；食品风险者降权并限制专区。" />
    <el-card>
      <el-table :data="vendors" stripe>
        <el-table-column prop="name" label="摊主" width="120" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column label="信用分" width="160">
          <template #default="{ row }">
            <el-progress :percentage="Math.max(0, Math.min(100, row.creditScore))"
                         :status="row.creditScore >= 80 ? 'success' : row.creditScore >= 60 ? '' : 'exception'" />
          </template>
        </el-table-column>
        <el-table-column label="历史累计捐赠" width="160">
          <template #default="{ row }"><span class="money">¥{{ row.totalDonationYuan }}</span></template>
        </el-table-column>
        <el-table-column label="食品风险" width="120">
          <template #default="{ row }">
            <el-tag :type="row.foodRiskFlag ? 'danger' : 'success'">
              {{ row.foodRiskFlag ? '有风险记录' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api.js'
const vendors = ref([])
onMounted(async () => { vendors.value = await api.get('/vendors') })
</script>
