<script setup lang="ts">
import type { LikeResponseDto } from '@/dto/like-response-dto';
import type { NoteExploreDto } from '@/dto/note-explore-dto';
import { User } from '@/dto/user';
import axios from '@/services/axios';
import { useAuthStore } from '@/stores/auth';
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router';

const me = ref<User>(new User())
const authStore = useAuthStore()
const router = useRouter()

const noteList = ref<Array<NoteExploreDto>>([])
const isLoading = ref(false)
const hasMore = ref(true)
const loadMoreRef = ref<HTMLDivElement | null>(null)
const noMoreContentRef = ref<HTMLDivElement | null>(null)
const category = ref('')
const page = ref(1)
const query = ref('')

onMounted(() => {
  me.value = authStore.getUser ? authStore.getUser : new User()

  // 加载笔记数据
  loadMoreNotes()

  // 监听滚动事件
  window.addEventListener('scroll', () => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;

    console.log('scrollTop: ' + scrollTop);
    console.log('windowHeight: ' + windowHeight);
    console.log('documentHeight: ' + documentHeight);

    if (scrollTop + windowHeight >= documentHeight - 300) {
      loadMoreNotes();
    }
  })

  // 分类导航设置点击事件
  document.querySelectorAll('.category-item').forEach(item => {
    item.addEventListener('click', () => {
      // 移除所有active类
      document.querySelectorAll('.category-item').forEach(i => {
        i.classList.remove('active');
      });

      // 添加active类
      item.classList.add('active');

      // 执行搜索
      performSearch()
    })
  })
})

// 执行搜索
function performSearch() {
  // 重置笔记网格数据
  noteList.value = [];
  page.value = 1;
  isLoading.value = false;
  hasMore.value = true;

  loadMoreNotes();
}

// 注销
function logout() {
  authStore.logout()

  // 跳转到登录页面
  router.push({ name: 'login' })
}

// 数字格式化，转为k/w单位
function formateNumber(num: number) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  } else {
    return num
  }
}

// 加载更多笔记数据
const loadMoreNotes = async () => {
  if (isLoading.value || !hasMore.value) {
    // 隐藏“加载”
    hideLoadMore()

    // 显示“没有更多”
    showNoMoreContent()
    return
  }

  isLoading.value = true
  // 显示“加载”
  showLoadMore()

  // 获取当前分类
  category.value = document.querySelector('.category-item.active')?.textContent?.trim() ?? '推荐'

  try {
    // 发送API请求
    const response = await axios.get(`/api/explore/note?page=${page.value}&category=${category.value}&query=${query.value}`)
    const data = response.data
    if (data.notes && data.notes.length > 0) {
      page.value++
      noteList.value = noteList.value.concat(data.notes)
      hasMore.value = data.hasMore
    } else {
      hasMore.value = false
    }

    isLoading.value = false

    // 隐藏“加载”
    hideLoadMore()

    if (!hasMore.value) {
      // 显示“没有更多”
      showNoMoreContent()
    }
  } catch (error) {
    console.log('加载更多笔记失败', error)
    isLoading.value = false
    // 隐藏“加载”
    hideLoadMore()
  }
}

function hideLoadMore() {
  if (loadMoreRef.value) {
    loadMoreRef.value.style.display = 'none'
  }
}

function showNoMoreContent() {
  if (noMoreContentRef.value) {
    noMoreContentRef.value.style.display = 'block'
  }
}

function showLoadMore() {
  if (loadMoreRef.value) {
    loadMoreRef.value.style.display = 'block'
  }
}

// 点击搜索
function handleSearch() {
  // 执行搜索
  performSearch()
}

// 底部导航
function navigateTo(page: string) {
  console.log('navigateTo: ' + page);

  if (page === 'home') {
    window.location.href = '/';
  } else if (page === 'publish') {
    window.location.href = '/note/publish';
  } else if (page === 'profile') {
    window.location.href = '/user/profile';
  } else {
    // 待实现的功能页面
    alert('暂未开放，敬请期待！');

    return;
  }
}

// 点赞
const handleLike = async (note: NoteExploreDto) => {
  try {
    // 调用API提交点赞
    const response = await axios.post(`/api/like/${note.noteId}`);
    const likeResponseDto: LikeResponseDto = response.data;

    note.likeCount = likeResponseDto.likeCount;
    note.liked = likeResponseDto.liked;
  } catch (error) {
    console.error('点赞错误：', error)
  }
}

// 路由到笔记详情页
function goNoteDetail(noteId: number) {
  router.push({
    name: 'note-detail',
    params: {
      noteId: noteId
    }
  })
}
</script>

<template>
  <!-- 顶部导航栏 -->
  <header>
    <nav class="navbar navbar-expand-lg">
      <div class="container">
        <a class="navbar-brand" href="/">
          <img src="/images/rn_logo.png" alt="RN" height="24">
        </a>

        <!-- 搜索框-->
        <div class="col-md-3">
          <div class="input-group">
            <input class="form-control" type="text" placeholder="搜索感兴趣的内容" aria-label="Search" id="searchInput"
              v-model="query">
            <button class="btn btn-outline-secondary" type="button" id="searchButton" @click="handleSearch">
              搜索
            </button>
          </div>
        </div>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
          aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
          </ul>

          <ul class="navbar-nav mb-2 mb-lg-0">
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" data-bs-target="dropdown" data-bs-toggle="dropdown"
                aria-expanded="false">
                {{ me.username }}
              </a>

              <ul class="dropdown-menu" id="dropdown">
                <li class="dropdown-item">
                  <a class="nav-link" href="/user/profile">个人资料</a>
                </li>
                <li class="dropdown-item">
                  <a class="nav-link" href="#" @click="logout">退出登录</a>
                </li>
              </ul>
            </li>

          </ul>

        </div>
      </div>
    </nav>
  </header>

  <!-- 分类导航 -->
  <header>
    <div class="container">
      <div class="category-item active">推荐</div>
      <div class="category-item">穿搭</div>
      <div class="category-item">美食</div>
      <div class="category-item">彩妆</div>
      <div class="category-item">影视</div>
      <div class="category-item">职场</div>
      <div class="category-item">情感</div>
      <div class="category-item">家居</div>
      <div class="category-item">游戏</div>
      <div class="category-item">旅行</div>
      <div class="category-item">健身</div>
    </div>
  </header>

  <main>
    <div class="container">
      <!-- 笔记卡片网格 -->
      <div class="masonry" id="notesGrid">
        <!-- 笔记卡片是通过Vue动态生成 -->
        <div class="masonry-item" v-for="note in noteList">
          <!-- 点击跳转到笔记详情页 -->
          <!--
          <a :href="`/note/${note.noteId}`"> -->
          <a href="#" @click="goNoteDetail(note.noteId)">  
            <img class="masonry-note-image" :src="note.cover" :alt="note.title">
          </a>
          <div class="note-content">
            <div class="note-title">{{ note.title }}</div>
            <div class="note-author-stats">
              <!-- 点击跳转到用户详情页 -->
              <a :href="`/user/profile/${note.userId}`">
                <div class="note-author">
                  <img class="author-avatar" :src="note.avatar ? note.avatar : '/images/rn_avatar.png'"
                    :alt="note.username">
                  <span class="author-name">{{ note.username }}</span>
                </div>
              </a>

              <div class="note-stats">
                <div class="stat-item">
                  <i :class="note.liked ? 'fa fa-heart liked like-btn' : 'fa fa-heart-o like-btn'"
                    @click="handleLike(note)">{{
                      formateNumber(note.likeCount) }}</i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 加载更多内容提示 -->
      <div class="load-more" id="loadMore" ref="loadMoreRef">
        <i class="fa fa-spinner fa-spin"></i>加载更多
      </div>
      <!-- 没有更多内容提示 -->
      <div class="no-more" id="noMoreContent" ref="noMoreContentRef">
        <p>已经到底啦~</p>
      </div>
    </div>
  </main>
  <footer>
    <!-- 底部导航栏 -->
    <div class="container bottom-nav">
      <div class="nav-item active" @click="navigateTo('home')">
        <i class="fa fa-home nav-icon"></i>
        <span class="nav-text">首页</span>
      </div>
      <div class="nav-item" @click="navigateTo('discover')">
        <i class="fa fa-compass nav-icon"></i>
        <span class="nav-text">发现</span>
      </div>
      <div class="nav-item" @click="navigateTo('publish')">
        <i class="fa fa-plus nav-icon"></i>
        <span class="nav-text">发布</span>
      </div>
      <div class="nav-item" @click="navigateTo('message')">
        <i class="fa fa-comment-o nav-icon"></i>
        <span class="nav-text">消息</span>
      </div>
      <div class="nav-item" @click="navigateTo('profile')">
        <i class="fa fa-user-o nav-icon"></i>
        <span class="nav-text">我的</span>
      </div>
    </div>
  </footer>

</template>
<style setup>
/* 全局样式 */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f5f5f5;
}

/* 分类导航 */
.category-nav {
  background-color: white;
  padding: 8px 0;
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
}

.category-item {
  display: inline-block;
  padding: 6px 12px;
  margin-right: 8px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.category-item.active {
  background-color: #ff2442;
  color: white;
}

/* 笔记卡片网格 */
.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 8px;
  padding: 8px;
}

.note-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.note-image-container {
  position: relative;
  padding-bottom: 100%;
  /* 保持正方形比例 */
  overflow: hidden;
  border-radius: 12px;
}

.note-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.note-tag {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.note-content {
  padding: 8px;
}

.note-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.note-author {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-right: 6px;
}

.author-name {
  font-size: 12px;
  color: #666;
}

.note-author-stats {
  display: flex;
  justify-content: space-between;
}

.note-stats {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.stat-item {
  margin-right: 12px;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 16px 0;
  color: #666;
  font-size: 14px;
}

/* 没有更多 */
.no-more {
  text-align: center;
  padding: 0 0 50px 0;
  color: #666;
  font-size: 14px;
  display: none;
}

/* 底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  box-shadow: 0 -1px 2px rgba(0, 0, 0, 0.05);
  z-index: 100;
  background-color: #f5f5f5;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #666;
  cursor: pointer;
}

.nav-item.active {
  color: #ff2442;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

.nav-text {
  font-size: 10px;
}

/* 去掉下划线 */
a {
  text-decoration: none;
}

/* 瀑布流布局 */
.masonry {
  column-count: 4;
  column-gap: 1em;
  padding: 10;
}

.masonry-item {
  display: inline-block;
  margin: 0 0 1.5em;
  width: 100%;
}

.masonry-note-image {
  border-radius: 12px;
  width: 100%;
  height: auto;
}

@media only screen and (max-width: 320px) {
  .masonry {
    column-count: 1;
  }
}

@media only screen and (min-width: 321px) and (max-width: 768px) {
  .masonry {
    column-count: 2;
  }
}

@media only screen and (min-width: 769px) and (max-width: 1200px) {
  .masonry {
    column-count: 3;
  }
}

@media only screen and (min-width: 1201px) {
  .masonry {
    column-count: 4;
  }
}

/* 点赞按钮样式 */
.liked {
  color: #ff2442;
}

.like-btn {
  cursor: pointer;
}
</style>