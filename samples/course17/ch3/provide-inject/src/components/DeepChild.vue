<script setup lang="ts">
// 导入inject
import { inject } from 'vue'

// inject() 函数注入上层组件提供的数据
const msg = inject('msg', '暂无'); // 默认值是'暂无'
</script>

<template>
  <p>孙子收到祖先显灵：{{ msg }}</p>
</template>