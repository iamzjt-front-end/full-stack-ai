<script setup lang="ts">
// 导入模板引用ref
import {ref} from "vue"

// 使用ref()函数来声明响应式状态
const count = ref(0)

// 声明函数来操作上述count状态
function increment() {
  // 需要在.value上操作
  count.value++
}
</script>

<template>
  <button @click="increment">点击了 {{count}} 次</button>
</template>