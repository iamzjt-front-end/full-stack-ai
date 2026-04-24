<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

// 返回
function goBack() {
  router.back();
}
</script>
<template>
  <div class="container align-items-center min-vh-100 py-4">
    <div class="error-container">
      <!-- 错误图标 -->
      <div class="error-image">
        <i class="fa fa-lock fa-5x text-danger"></i>
      </div>

      <!-- 错误标题 -->
      <h2 class="error-title">访问受限</h2>

      <!-- 错误信息 -->
      <p class="error-message">
        你没有权限访问此页面。<br>
        请检查你的权限或联系管理员。
      </p>

      <!-- 返回按钮 -->
      <button class="btn btn-primary" @click="goBack">返回上一页</button>

      <!-- 跳转到首页 -->
      <p class="back-home">
        <a href="/">返回RN首页</a>
      </p>
    </div>
  </div>
</template>
<style setup>
body {
  background-color: #fef6f6;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.error-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
}

.error-icon {
  font-size: 80px;
  color: #ff2442;
  margin-bottom: 20px;
}

.error-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}

.error-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

.btn-primary {
  background-color: #ff2442;
  border-color: #ff2442;
  border-radius: 12px;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  width: 100%;
}

.btn-primary:hover,
.btn-primary:focus {
  background-color: #e61e3a;
  border-color: #e61e3a;
  box-shadow: 0 4px 12px rgba(255, 36, 66, 0.2);
}

.back-home {
  margin-top: 20px;
  font-size: 14px;
  color: #999;
}

.back-home a {
  color: #ff2442;
  text-decoration: none;
}

.back-home a:hover {
  text-decoration: underline;
}

.error-image {
  width: 200px;
  height: 200px;
  margin: 0 auto 30px;
  background-color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.error-image img {
  width: 120px;
  height: 120px;
}
</style>