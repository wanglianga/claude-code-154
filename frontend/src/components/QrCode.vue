<template>
  <div class="qr-center">
    <canvas ref="canvasEl"></canvas>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import QRCode from 'qrcode'

const props = defineProps({
  text: { type: String, required: true },
  size: { type: Number, default: 180 }
})
const canvasEl = ref(null)

async function render() {
  if (!canvasEl.value || !props.text) {
    return
  }
  await QRCode.toCanvas(canvasEl.value, props.text, { width: props.size, margin: 1 })
}
onMounted(render)
watch(() => props.text, render)
</script>
