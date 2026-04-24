<script setup lang="ts">
// defineEmits() 宏来声明它要触发的事件
const emit = defineEmits(['showMeTheMoneyEvent'])

// 声明函数
function callForFather() {
  // 使用 emit 方法触发自定义事件
  emit('showMeTheMoneyEvent')
}
</script>

<template>
  <button @click="callForFather">呼叫老爹</button>
</template>