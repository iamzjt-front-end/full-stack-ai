<script setup lang="ts">
import ChildComponent from './components/ChildComponent.vue'

// 导入模板引用ref
import { ref } from 'vue';

// 使用 ref() 函数来声明响应式状态
const msg = ref('')

// 声明函数
function handleEvent() {
  // 需要在.value上操作
  msg.value = '收到！'
}

</script>

<template>
  <main>
    <ChildComponent @show-me-the-money-event="handleEvent"/>

    <p>父：{{ msg }}</p>
  </main>
</template>
