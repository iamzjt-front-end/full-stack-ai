<script setup lang="ts">
import Footer from './components/Footer.vue'

// 导入模板引用ref
import { ref } from "vue"

// 导入provide()函数，为后代组件提供数据
import { provide } from "vue"

// 使用 ref() 函数来声明响应式状态
const msg = ref('')

// 提供注入名称及值
provide('msg', msg)

// 声明函数
function callForDescendant() {
  // 需要操作.value
  msg.value = '子孙安在？'
}

</script>

<template>
  <main>
    <button @click="callForDescendant">呼叫儿孙</button>
    <Footer />
  </main>
</template>
