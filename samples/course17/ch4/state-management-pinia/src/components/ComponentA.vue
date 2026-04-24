<script setup lang="ts">
import { useCounterStore } from '@/stores/counter';
const store = useCounterStore()
</script>

<template>
  <button @click="store.increment()">晁盖号令</button>
  : {{ store.count }}
</template>