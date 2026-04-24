<script setup lang="ts">
import type { AdminDashboardDto } from '@/dto/admin-dashboard-dto';
import axios from '@/services/axios';
import { onMounted, ref } from 'vue';


const adminDashboardDto = ref<AdminDashboardDto>({
  userCount: 0,
  noteCount: 0,
  commentCount: 0,
  noteBrowseCountDtoList: [],
  noteBrowseTimeDtoList: []
})

onMounted(() => {
  // 获取数据看板数据
  getAdminDashboard()
})

// 获取数据看板数据
const getAdminDashboard = async () => {
  try {
    // 调用API
    const response = await axios.get('/api/admin/dashboard');
    adminDashboardDto.value = response.data
  } catch (error) {
    console.error('获取数据看板数据失败：' + error)
  }
}
</script>
<template>
  <div class="card shadow mb-4">
    <div class="card-header py-3">
      <h2>数据看板</h2>
    </div>
    <div class="card-body">
      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-primary shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                  用户总数
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ adminDashboardDto.userCount }}
                </div>
                <div class="col-auto">
                  <i class="fa fa-users fa-2x text-gray-300"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-success shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                  笔记总数
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ adminDashboardDto.noteCount }}
                </div>
                <div class="col-auto">
                  <i class="fa fa-file-text fa-2x text-gray-300"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-info shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                  评论总数
                </div>
                <div class="h5 mb-0 font-weight-bold text-gray-800">
                  {{ adminDashboardDto.commentCount }}
                </div>
                <div class="col-auto">
                  <i class="fa fa-comments fa-2x text-gray-300"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-info shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                  访问量排行
                </div>

                <ol class="list-group list-group-numbered">
                  <li class="list-group-item d-flex justify-content-between align-item-start"
                    v-for="note in adminDashboardDto.noteBrowseCountDtoList">
                    <div class="ms-2 me-auto">
                      {{ note.title }}
                    </div>
                    <span class="badge text-bg-primary rounded-pill">
                      {{ note.browseCount }}
                    </span>
                  </li>
                </ol>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-xl-3 col-md-6 mb-4">
        <div class="card border-left-info shadow h-100 py-2">
          <div class="card-body">
            <div class="row no-gutters align-items-center">
              <div class="col mr-2">
                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                  访问时长排行
                </div>

                <ol class="list-group list-group-numbered">
                  <li class="list-group-item d-flex justify-content-between align-item-start"
                    v-for="note in adminDashboardDto.noteBrowseTimeDtoList">
                    <div class="ms-2 me-auto">
                      {{ note.title }}
                    </div>
                    <span class="badge text-bg-primary rounded-pill">
                      {{ note.browseTime }}
                    </span>
                  </li>
                </ol>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
