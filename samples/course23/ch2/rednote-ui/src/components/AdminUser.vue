<script setup lang="ts">
import type { User } from '@/dto/user';
import axios from '@/services/axios';
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useRouter } from 'vue-router';

const route = useRoute()

// 用户分页
const userList = ref<Array<User>>([])
const totalPages = ref(0)
const currentPage = ref(1)

// 查询参数，默认第1页
const pageIndex = ref(route.query.page || 1)

const router = useRouter()

onMounted(() => {
  // 获取用户列表
  fetchUserList()
})

// 获取用户列表
const fetchUserList = async () => {
  try {
    const response = await axios.get(`/api/admin/user?page=${pageIndex.value}`)
    userList.value = response.data['userList']
    totalPages.value = response.data['totalPages']
    currentPage.value = response.data['currentPage']
  } catch (error) {
    console.error('获取用户列表失败：' + error)
  }
}

// 路由到编辑用户界面
function handleEdit(userId: number) {
  router.push({ path: `/admin/user/${userId}/edit` })
}

// 删除用户
const handleDelete = async (userId: number) => {
  if (!confirm('确定要删除该用户吗？')) {
    return
  }

  try {
    // 发送删除请求API
    await axios.delete(`/api/admin/user/${userId}`)

    // 删除成功后，从列表中删除该用户
    userList.value = userList.value.filter(user => user.userId !== userId)
  } catch (error) {
    console.error('删除用户失败：' + error)
  }
}
</script>
<template>
  <div class="card shadow mb-4">
    <div class="card-header py-3">
      <h2>用户列表</h2>
    </div>
    <div class="card-body">
      <div class="table-responsive small">
        <table class="table table-striped table-sm">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>电话</th>
              <th>角色</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in userList">
              <td>{{ user.userId }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.phone }}</td>
              <td>{{ user.role }}</td>
              <td>
                <button class="btn btn-sm btn-light" @click="handleEdit(user.userId)">
                  编辑
                </button>
                <button class="btn btn-sm btn-danger" @click="handleDelete(user.userId)">
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页控件 -->
      <div class="d-flex justify-content-center">
        <nav>
          <ul class="pagination" v-if="totalPages > 0">
            <li class="page-item" v-if="currentPage > 1">
              <a class="page-link" :href="`/admin/user?page=${currentPage - 1}`">
                上一页
              </a>
            </li>
            <li class="page-item" v-for="pageNum in Array.from({ length: totalPages }, (_, i) => i + 1)"
              :class="{ active: pageNum === currentPage }">
              <a class="page-link" :href="`/admin/user?page=${pageNum}`">
                {{ pageNum }}
              </a>
            </li>
            <li class="page-item" v-if="currentPage < totalPages">
              <a class="page-link" :href="`/admin/user?page=${currentPage + 1}`">
                下一页
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>
