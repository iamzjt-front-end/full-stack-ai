<script setup lang="ts">
import { store } from '../store/index.ts'
</script>

<template>
  <button @click="store.increment()">晁盖号令</button>
  : {{ store.count }}
</template>