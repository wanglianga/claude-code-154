<template>
  <router-view />
</template>

<script setup>
</script>

<style>
html, body, #app {
  margin: 0;
  height: 100%;
  font-family: "PingFang SC", "Microsoft YaHei", system-ui, sans-serif;
  background: #f5f7fa;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 14px;
  color: #1f2d3d;
}
.section-card {
  margin-bottom: 16px;
}
.muted { color: #909399; font-size: 13px; }
.money { color: #e6a23c; font-weight: 600; }
</style>
