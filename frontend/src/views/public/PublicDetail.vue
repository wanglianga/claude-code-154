<template>
  <div class="public-wrap">
    <div class="public-header">
      <router-link to="/public" class="back">← 返回公示列表</router-link>
      <h1 v-if="data">{{ data.event.name }}</h1>
      <p v-if="data">{{ data.event.location }} · {{ data.event.startTime }}</p>
    </div>

    <div class="container" v-if="data">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="big-stat"><div class="amount">¥{{ data.report.totalDonation }}</div>
            <div class="lbl">公益捐赠总额</div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="big-stat"><div class="amount" style="color:#409eff">{{ data.report.totalVisitors }}</div>
            <div class="lbl">参与居民（人次）</div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="big-stat"><div class="amount" style="color:#67c23a">{{ data.report.goodPerformers }}</div>
            <div class="lbl">履约良好摊主</div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="big-stat"><div class="amount" style="color:#f56c6c">{{ data.report.issueVendorCount }}</div>
            <div class="lbl">异常摊位（均已处理）</div></el-card>
        </el-col>
      </el-row>

      <el-card header="捐赠资金用途" class="block">
        <p class="purpose">{{ data.report.donationPurpose }}</p>
        <p class="muted">发布时间：{{ data.report.publishedAt }}</p>
      </el-card>

      <el-card header="摊主履约与捐赠明细公示" class="block">
        <el-table :data="data.vendors" stripe>
          <el-table-column prop="stallCode" label="摊位号" width="90" />
          <el-table-column prop="stallName" label="摊位名称" min-width="150" />
          <el-table-column prop="vendorName" label="摊主" width="110" />
          <el-table-column prop="category" label="品类" width="100" />
          <el-table-column label="实际捐赠" width="130">
            <template #default="{ row }"><span class="money">¥{{ row.actualDonation }}</span></template>
          </el-table-column>
          <el-table-column label="履约情况" width="150">
            <template #default="{ row }">
              <el-tag :type="row.goodFaith === '履约良好' ? 'success' : 'warning'" size="small">
                {{ row.goodFaith }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card header="异常与投诉处理公示" class="block">
        <el-table :data="data.issues" stripe>
          <el-table-column prop="type" label="异常类型" width="180" />
          <el-table-column label="处理状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === '待处理' ? 'danger' : 'success'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="resolution" label="处理措施" min-width="240" />
        </el-table>
        <el-empty v-if="!data.issues.length" description="本场无异常记录" :image-size="60" />
      </el-card>

      <el-card header="下一场活动" class="block">
        <p class="purpose">{{ data.report.nextPlan || '社区将持续举办公益集市，欢迎诚信摊主报名、居民监督。' }}</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../../api.js'

const route = useRoute()
const data = ref(null)
onMounted(async () => { data.value = await api.get(`/public/events/${route.params.id}`) })
</script>

<style scoped>
.public-wrap { min-height: 100vh; background: #f5f7fa; padding-bottom: 40px; }
.public-header {
  background: linear-gradient(135deg, #1f3a5f, #3a6ea5);
  color: #fff; text-align: center; padding: 40px 20px; position: relative;
}
.public-header h1 { margin: 0 0 8px; font-size: 24px; }
.back { position: absolute; left: 24px; top: 20px; color: #ffd04b; font-size: 14px; }
.container { max-width: 1000px; margin: -26px auto 0; padding: 0 16px; }
.big-stat { text-align: center; padding: 10px 0; }
.amount { font-size: 28px; font-weight: 700; color: #e6a23c; }
.lbl { color: #909399; font-size: 13px; margin-top: 6px; }
.block { margin-top: 16px; }
.purpose { font-size: 15px; line-height: 1.8; color: #303133; margin: 0 0 6px; }
.muted { color: #909399; font-size: 12px; }
.money { color: #e6a23c; font-weight: 600; }
</style>
