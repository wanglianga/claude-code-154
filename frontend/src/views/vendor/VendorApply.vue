<template>
  <div>
    <h2 class="page-title">摊位报名</h2>
    <el-card v-if="event" class="section-card">
      <template #header>
        <div class="card-head">
          <b>{{ event.name }}</b>
          <span class="muted">{{ event.location }} · {{ event.startTime }} ~ {{ event.endTime }}</span>
          <el-tag type="warning" size="small">公益目标 ¥{{ event.donationGoal }}（{{ event.donationPurpose }}）</el-tag>
        </div>
      </template>

      <el-alert v-if="existing?.status === 'REJECTED'" type="error" :closable="false" show-icon
                :title="`报名已驳回：${existing.reviewComment || '请按规则补正后重新提交'}`" style="margin-bottom:14px" />

      <el-form :model="form" label-width="130px" style="max-width: 820px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="摊位/招牌名称" required>
          <el-input v-model="form.stallName" placeholder="如：暖手心作铺" />
        </el-form-item>
        <el-form-item label="售卖品类" required>
          <el-radio-group v-model="form.category">
            <el-radio-button v-for="c in categories" :key="c.value" :value="c.value">{{ c.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="售卖内容描述">
          <el-input v-model="form.goodsDescription" type="textarea" :rows="2"
                    placeholder="请如实填写售卖商品，现场检查会核对是否与报名一致" />
        </el-form-item>

        <el-divider content-position="left">食品资质（食品类必填）</el-divider>
        <el-form-item label="具备食品资质">
          <el-switch v-model="form.foodQualified" active-text="已取得食品经营许可/健康证" />
        </el-form-item>
        <template v-if="form.foodQualified">
          <el-form-item label="许可证/健康证号">
            <el-input v-model="form.foodCertNo" placeholder="如 SP3401-2026-0088" />
          </el-form-item>
          <el-form-item label="资质附件">
            <el-input v-model="form.foodCertAttachment" placeholder="证件照片文件名/网盘链接（演示填写）" />
          </el-form-item>
        </template>

        <el-divider content-position="left">公益承诺</el-divider>
        <el-form-item label="公益捐赠比例">
          <el-slider v-model="form.donationPercent" show-input :max="100" :step="1" />
          <span class="muted">按现场营业额的该比例缴纳公益捐赠，活动结束后向居民公示用途</span>
        </el-form-item>

        <el-divider content-position="left">现场需求</el-divider>
        <el-form-item label="用电需求(W)">
          <el-input-number v-model="form.powerNeedWatt" :min="0" :max="3000" :step="100" />
          <span class="muted" style="margin-left:10px">超过 1500W 需社区统筹供电回路</span>
        </el-form-item>
        <el-form-item label="桌子需求">
          <el-input-number v-model="form.tableNeed" :min="0" :max="4" />
          <span style="margin-left:24px">椅子需求</span>
          <el-input-number v-model="form.chairNeed" :min="0" :max="8" style="margin-left:10px" />
        </el-form-item>
        <el-form-item label="亲子摊位">
          <el-switch v-model="form.familyStall" active-text="亲子共同经营，优先安排亲子专区" />
        </el-form-item>
        <el-form-item label="有噪音/油烟">
          <el-switch v-model="form.noisy" active-text="现场制作产生噪音或油烟，需安排耐噪专区" />
        </el-form-item>

        <el-divider content-position="left">收款方式</el-divider>
        <el-form-item label="收款方式" required>
          <el-select v-model="form.paymentMethod" style="width: 200px">
            <el-option v-for="m in ['微信', '支付宝', '聚合码', '现金', '现金+微信']" :key="m" :label="m" :value="m" />
          </el-select>
          <el-input v-model="form.paymentAccount" placeholder="收款账号备注（可留空）" style="width: 300px; margin-left: 10px" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="saving" @click="submit">
            {{ existing ? '重新提交报名' : '提交报名' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-empty v-else description="暂无活动" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api.js'
import { useEvent } from '../../store/event.js'

const route = useRoute()
const ev = useEvent()
const event = computed(() => {
  const id = route.params.eventId || ev.currentId.value
  return ev.state.events.find(e => e.id === Number(id)) || null
})
const existing = ref(null)
const saving = ref(false)

const categories = [
  { value: 'HANDMADE', label: '手作' },
  { value: 'SECONDHAND', label: '二手物品' },
  { value: 'FOOD', label: '食品' },
  { value: 'CHARITY', label: '公益义卖' },
  { value: 'COMMERCIAL', label: '商业摊位' }
]

const form = reactive({
  stallName: '', category: 'HANDMADE', goodsDescription: '',
  foodQualified: false, foodCertNo: '', foodCertAttachment: '',
  donationPercent: 5, powerNeedWatt: 0, tableNeed: 1, chairNeed: 2,
  familyStall: false, noisy: false, paymentMethod: '微信', paymentAccount: ''
})

async function load() {
  if (!event.value) return
  const list = await api.get('/my/applications')
  existing.value = list.find(a => a.eventId === event.value.id) || null
  if (existing.value && existing.value.status !== 'APPROVED') {
    Object.assign(form, {
      stallName: existing.value.stallName,
      category: existing.value.category,
      goodsDescription: existing.value.goodsDescription,
      foodQualified: existing.value.foodQualified,
      foodCertNo: existing.value.foodCertNo,
      foodCertAttachment: existing.value.foodCertAttachment,
      donationPercent: Number(existing.value.donationPercent),
      powerNeedWatt: existing.value.powerNeedWatt,
      tableNeed: existing.value.tableNeed,
      chairNeed: existing.value.chairNeed,
      familyStall: existing.value.familyStall,
      noisy: existing.value.noisy,
      paymentMethod: existing.value.paymentMethod,
      paymentAccount: existing.value.paymentAccount
    })
  }
}

async function submit() {
  if (!form.stallName) { ElMessage.warning('请填写摊位名称'); return }
  saving.value = true
  try {
    const res = await api.post(`/events/${event.value.id}/applications`, form)
    existing.value = res
    if (res.ruleHints) {
      ElMessage.warning('已提交，但有规则提示：' + res.ruleHints)
    } else {
      ElMessage.success('报名提交成功，等待社区审核')
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!ev.state.events.length) ev.setEvents(await api.get('/events'))
  await load()
})
watch(() => route.params.eventId, load)
</script>

<style scoped>
.card-head { display: flex; align-items: center; gap: 12px; }
</style>
