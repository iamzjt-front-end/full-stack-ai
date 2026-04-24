import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

const app = createApp(App)

// 创建一个Pinia实例并将其传递给应用
app.use(createPinia())

app.mount('#app')
