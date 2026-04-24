import axios from "axios"
import { useAuthStore } from "@/stores/auth"
import router from "@/router"

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.getToken) {
      config.headers.Authorization = `Bearer ${authStore.getToken}`
    }
    return config
  },
  (error) => {
    console.error('请求错误：' + error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('响应错误：' + error)
    const { status, data } = error.response || {}

    // 根据状态码的不同处理不同的错误
    switch (status) {
      case 401:
        // 认证失败，跳转到登录页
        const authStore = useAuthStore()
        authStore.logout()
        router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
        break;
      case 403:
        // 权限不足，显示提示
        alert(data.message || '权限不足')
        break;
      case 404:
        // 资源不存在，显示提示
        alert(data.message || '资源不存在')
        break;
      case 500:
        // 服务器内部错误，显示提示
        alert(data.message || '服务器内部错误，请稍后再试')
        break;
      default:
        // 其他错误，显示提示
        alert(data.message || '未知错误，请稍后再试')
    }

    return Promise.reject(error)
  }
)

export default service