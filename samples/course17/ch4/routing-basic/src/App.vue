<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <P>当前路径：{{ $route.fullPath }}</P>
  <nav>
    <RouterLink to="/">Home</RouterLink>
    <RouterLink to="/about">About</RouterLink>
  </nav>

  <main>
    <RouterView />
  </main>
</template>
