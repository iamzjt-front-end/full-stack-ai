import { defineStore } from "pinia"
/*import axios from "axios"*/
import axios from "@/services/axios"
import type { User } from "@/dto/user"

export const useAuthStore = defineStore("auth", {
  state: () => ({
    user: null,
    token: localStorage.getItem("token") || null,
    isAuthenticated: false,
  }),
  getters: {
    getUser: (state) => state.user,
    getToken: (state) => state.token,
    getIsAuthenticated: (state) => state.isAuthenticated,
  },
  actions: {
    // 登录
    async login(username: string, password: string) {
      try {
        const response = await axios.post("/api/auth/login", {
          username,
          password,
        })
        this.token = response.data

        if (this.token) {
          localStorage.setItem("token", this.token)
          this.isAuthenticated = true
          axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`

          // 获取用户信息
          await this.fetchUser()

          return true;
        } else {
          localStorage.removeItem("token")
          this.isAuthenticated = false

          return false;
        }
      } catch (error) {
        this.logout()
        throw error
      }
    },
    // 获取用户信息
    async fetchUser() {
      try {
        const response = await axios.get("/api/user/profile")
        this.user = response.data
      } catch (error) {
        this.logout()
        throw error
      }
    },
    // 注销
    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false;
      localStorage.removeItem('token')
      axios.defaults.headers.common['Authorization'] = null
    },
    // 检查认证状态（比如页面刷新后恢复）
    async checkAuth() {
      const storedToken = localStorage.getItem('token')
      if (storedToken) {
        this.token = storedToken
        this.isAuthenticated = true
        await this.fetchUser()
      }
    },
    // 检查是否具备指定角色
    hasRole(role: any) {
      if (!this.getUser) return false

      return (this.getUser as User).role === (role as string)
    },
  }
})

/*
// axios拦截器，自动刷新JWT
axios.interceptors.request.use((config) => { 
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})
*/
