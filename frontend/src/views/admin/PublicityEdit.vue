<template>
  <div class="page-container">
    <el-card class="section-card">
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span>居民公示发布 · {{ event?.name }}</span>
          <el-tag v-if="pub?.published" type="success">已于 {{ pub.publishedAt }} 发布</el-tag>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="14">
          <el-form :model="form" label-width="130px">
            <el-form-item label="公示标题">
              <el-input v-model="form.title" />
            </el-form-item>
            <el-form-item label="善款用途说明">
              <el-input v-model="form.fundUsage" type="textarea" :rows="3"
                        placeholder="如：全部善款用于 12 户困难家庭学生助学包，采购凭证在社区公告栏公示" />
            </el-form-item>
            <el-form-item label="摊主履约情况">
              <el-input v-model="form.fulfillmentSummary" type="textarea" :rows="3"
                        placeholder="如：应到 5 摊实到 5 摊，1 摊提前离场，其余按承诺经营" />
            </el-form-item>
            <el-form-item label="异常处理情况">
              <el-input v-model="form.incidentSummary" type="textarea" :rows="3"
                        placeholder="如：卖品不符、用电超载、捐赠核对等异常的处置结果" />
            </el-form-item>
            <el-form-item label="公开逐摊位明细">
              <el-switch v-model="form.includeDetail" />
              <el-text type="info" size="small" style="margin-left:8px;">
                响应公益组织公示要求，逐摊位列出捐赠比例、流水与捐赠额
              </el-text>
            </el-form-item>
            <el-form-item>
              <el-button @click="save(false)">保存草稿</el-button>
              <el-button type="primary" @click="save(true)">保存并对居民发布</el-button>
              <el-button text @click="preview">预览居民端效果</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="10">
          <el-card shadow="never" header="本次汇总（自动取数）">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="营业总额">¥{{ fmtMoney(stats.totalSales) }}</el-descriptions-item>
              <el-descriptions-item label="实际捐赠总额">
                <b style="color:#f56c6c;">¥{{ fmtMoney(stats.totalDonation) }}</b>
              </el-descriptions-item>
              <el-descriptions-item label="承诺应捐合计">¥{{ fmtMoney(stats.expectedDonation) }}</el-descriptions-item>
              <el-descriptions-item label="客流">{{ stats.visitorCount }}</el-descriptions-item>
              <el-descriptions-item label="到场率">{{ stats.attendanceRate }}%</el-descriptions-item>
              <el-descriptions-item label="投诉/异常">{{ stats.complaintCount }} / {{ stats.incidentCount }}</el-descriptions-item>
            </el-descriptions>
            <el-alert v-if="Number(stats.incidentOpen) > 0" type="warning" :closable="false" style="margin-top:10px;">
              仍有 {{ stats.incidentOpen }} 起异常未闭环，建议处理完成后再发布公示
            </el-alert>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api/client'
import { fmtMoney } from '../../composables/useEvents'

const route = useRoute()
const eventId = Number(route.params.id)
const event = ref(null)
const stats = ref({})
const pub = ref(null)
const form = reactive({
  title: '', fundUsage: '', fulfillmentSummary: '', incidentSummary: '', includeDetail: true, publish: false
})

async function save(publish) {
  form.publish = publish
  await api.post('/admin/publicity', { eventId, ...form })
  ElMessage.success(publish ? '已发布，居民端可查看' : '草稿已保存')
  load()
}
function preview() {
  window.open('#/public', '_blank')
}
async function load() {
  event.value = await api.get(`/events/${eventId}`)
  const archive = await api.get(`/events/${eventId}/archive`)
  stats.value = archive.stats
  pub.value = await api.get(`/admin/events/${eventId}/publicity`)
  if (pub.value) {
    Object.assign(form, {
      title: pub.value.title || '',
      fundUsage: pub.value.fundUsage || '',
      fulfillmentSummary: pub.value.fulfillmentSummary || '',
      incidentSummary: pub.value.incidentSummary || '',
      includeDetail: !!pub.value.donationDetailJson
    })
  } else {
    form.title = event.value.name + ' 公益成果公示'
  }
}
onMounted(load)
</script>
