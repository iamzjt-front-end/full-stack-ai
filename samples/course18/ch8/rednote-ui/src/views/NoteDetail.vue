<script setup lang="ts">
import { NoteDetailDto } from '@/dto/note-detail-dto';
import { User } from '@/dto/user';
import { useAuthStore } from '@/stores/auth';
import axios from '@/services/axios';
import { onMounted, onUnmounted, ref } from 'vue';
import { onBeforeRouteLeave, useRoute } from 'vue-router';
import router from '@/router';
import { LikeResponseDto } from '@/dto/like-response-dto';
import { CommentResponseDto } from '@/dto/comment-response-dto';

const carouselContainerRef = ref<HTMLDivElement | null>(null);
const note = ref<NoteDetailDto>(new NoteDetailDto());
const currentIndex = ref<number>(0);
const me = ref<User>(new User());
const authStore = useAuthStore();

// 获取路由参数中的noteId
const route = useRoute();
const noteId = ref(route.params.noteId);

const previewModalVisible = ref(false);
const previewModalRef = ref<HTMLDivElement | null>(null);
const previewCurrentIndex = ref(0);

const likeResponseDto = ref<LikeResponseDto>(new LikeResponseDto());

// 评论状态
const newComment = ref('');
const commentResponseDtoArray = ref<Array<CommentResponseDto>>([]);

// 回复状态
const replyModalRef = ref<HTMLDivElement | null>(null);
const replyContentRef = ref<HTMLTextAreaElement | null>(null);
const replyToCommentResponseDto = ref<CommentResponseDto>(new CommentResponseDto());

// 埋点处理
// 开始时间
const startTime = ref<number>(Date.now());

onMounted(() => {
  // 获取当前用户信息
  me.value = authStore.getUser ? authStore.getUser : new User();

  // 获取笔记详情
  fetchNote(noteId.value)

  // 监听键盘按键
  window.addEventListener('keydown', handleKeydown);

  // 加载笔记评论
  fetchNoteComments()
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
});

const fetchNote = async (noteId: any) => {
  try {
    const response = await axios.get(`/api/note/${noteId}`);
    note.value = response.data;

    // 刷新点赞状态
    likeResponseDto.value.likeCount = note.value.likeCount;
    likeResponseDto.value.liked = note.value.liked;
  } catch (error) {
    console.error('获取笔记详情失败：' + error);
  }
}

// 更新轮播位置
function updateCarouselPosition() {
  if (carouselContainerRef.value) {
    carouselContainerRef.value.style.transform = `translateX(-${currentIndex.value * 100}%)`;
  }
}

// 上一张
function prevSlide() {
  currentIndex.value = Math.max(currentIndex.value - 1, 0);
  updateCarouselPosition();
}

// 下一张
function nextSlide() {
  if (note.value.images && note.value.images.length > 0) {
    currentIndex.value = Math.min(currentIndex.value + 1, note.value.images.length - 1);
    updateCarouselPosition();
  }

}

//  取消发布
function handleBack() {
  router.back();
}

// 删除笔记
const deleteNote = async () => {
  try {
    if (confirm('确定要删除该笔记吗？')) {
      await axios.delete(`/api/note/${noteId.value}`);

      alert('删除成功');

      // 跳转到用户信息页面
      router.push({ name: 'profile-placeholder' });
    }
  }
  catch (error) {
    console.error('删除失败：', error);
  }
}

// 键盘事件处理
const handleKeydown = (event: KeyboardEvent) => {
  if (previewModalVisible.value) {
    switch (event.key) {
      case 'Escape':
        closePreview();
        break;
      case 'ArrowLeft':
        previewPrev();
        break;
      case 'ArrowRight':
        previewNext();
        break;
    }
  }
};

// 打开预览
function openPreview(index: number) {
  console.log("openPreview " + index);
  previewCurrentIndex.value = index;
  previewModalVisible.value = true;

  if (previewModalRef.value) {
    previewModalRef.value.style.display = 'flex';
  }

  // 防止背景滚动
  document.body.style.overflow = 'hidden';
}

// 关闭预览
function closePreview() {
  previewModalVisible.value = false;

  if (previewModalRef.value) {
    previewModalRef.value.style.display = 'none';
  }

  // 恢复背景滚动
  document.body.style.overflow = '';
  // 更新轮播位置
  updateCarouselPosition();
}

// 预览上一张
function previewPrev() {
  if (note.value.images && note.value.images.length > 0) {
    previewCurrentIndex.value = Math.max(0, previewCurrentIndex.value - 1);
  }
}

// 预览下一张
function previewNext() {
  if (note.value.images && note.value.images.length > 0) {
    previewCurrentIndex.value = Math.min(note.value.images.length - 1, previewCurrentIndex.value + 1);
  }
}

// 点赞
const handleLike = async () => {
  try {
    const response = await axios.post(`/api/like/${noteId.value}`)
    likeResponseDto.value = response.data
  } catch (error) {
    console.error('点赞错误：', error)
  }
}

// 发布评论
const postComment = async () => {
  if (newComment.value.trim() === '') {
    return
  }

  try {
    const response = await axios.post(`/api/comment/${noteId.value}`,
      // 传递的是纯文本内容
      newComment.value.trim(),
      { headers: { 'Content-Type': 'text/plain' } }
    )

    // 返回的评论列表插入到原来列表顶部
    commentResponseDtoArray.value.unshift(response.data)
    newComment.value = ''
  } catch (error) {
    console.error('发布评论错误：', error)
  }
}

// 加载笔记评论
const fetchNoteComments = async () => {
  try {
    const response = await axios.get(`/api/comment/${noteId.value}`)
    commentResponseDtoArray.value = response.data
  } catch (error) {
    console.error('获取笔记评论错误：', error)
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  const formattedDate = date.toLocaleString();
  return formattedDate
}

// 删除评论
const deleteComment = async (commentId: number) => {
  if (!confirm('确定要删除这条评论吗？')) {
    return
  }

  try {
    await axios.delete(`/api/comment/${commentId}`)

    // 删除成功后，将该评论从列表中删除
    commentResponseDtoArray.value =
      commentResponseDtoArray.value.filter(comment => comment.commentId !== commentId)
  } catch (error) {
    console.error('删除评论错误：', error)
  }
}

// 回复评论
function handleReplyComment(comment: CommentResponseDto) {
  // 显示回复框
  showReplyModal(comment)

  // 设置当前回复的评论
  replyToCommentResponseDto.value = comment
}

// 显示回复框
function showReplyModal(comment: CommentResponseDto) {
  if (replyModalRef.value) {
    replyModalRef.value.classList.add('show')
    replyModalRef.value.style.display = 'block'

    // 防止背景滚动
    document.body.classList.add('modal-open')

    // 自动聚焦到数框
    setTimeout(() => {
      replyContentRef.value?.focus()
    }, 100)
  }
}

// 隐藏回复框
function hideReplyModal() {
  if (replyModalRef.value && replyContentRef.value) {
    replyModalRef.value.classList.remove('show')
    replyModalRef.value.style.display = 'none'

    // 恢复背景滚动
    document.body.classList.remove('modal-open')

    // 清空输入框
    replyContentRef.value.value = ''
  }

  replyToCommentResponseDto.value = new CommentResponseDto()
}

// 提交回复
const submitReply = async () => {
  const replyContent = replyContentRef.value

  if (!replyContent) {
    return
  }

  try {
    const parentCommentId = replyToCommentResponseDto.value.commentId

    const response = await axios.post(`/api/comment/${noteId.value}/reply/${parentCommentId}`,
      // 传递文本内容
      replyContentRef.value?.value.trim(),
      {
        headers: {
          'Content-Type': 'text/plain'
        }
      }
    )

    // 回复添加到父级评论的回复列表中
    /*const commentIndex = commentResponseDtoArray.value.findIndex(item => item.commentId === parentCommentId)
    if (commentIndex !== -1) { 
      if (!commentResponseDtoArray.value[commentIndex].replies) { 
        commentResponseDtoArray.value[commentIndex].replies = []
      }
      commentResponseDtoArray.value[commentIndex].replies.unshift(response.data)
    }*/
    commentResponseDtoArray.value.forEach(root => {
      deepTraverse(root, response.data, root.replies)
    })

    // 关闭回复框
    hideReplyModal()
  } catch (error) {
    console.error('提交回复失败：' + error)
  }
}

// 先遍历找根评论，再找子回复
const deepTraverse = (root: CommentResponseDto, response: CommentResponseDto, replies: Array<CommentResponseDto>) => {
  if (root.commentId === response.parentCommentId) {
    root.replies.unshift(response)
    return
  } else {
    for (const node of replies) {
      if (node.commentId === response.parentCommentId) {
        // 子节点的评论也算在根节点头上
        root.replies.unshift(response)
        break
      }
    }
  }
}

// 删除回复
const deleteReply = async (commentId: number) => {
  if (!confirm('确定要删除这条回复吗？')) {
    return
  }

  try {
    await axios.delete(`/api/comment/${commentId}`)

    // 从列表中删除回复
    commentResponseDtoArray.value = commentResponseDtoArray.value.filter(comment => {
      comment.replies = comment.replies.filter(reply => reply.commentId !== commentId)
      return comment.replies
    })
  } catch (error) {
    console.error('删除回复失败：' + error)
  }
}

// 统计访问笔记的时长
const handleBrowseTime = async() => { 
  // 获取访问时长
  const browseTime = Date.now() - startTime.value

  try { 
    // 发送访问时长数据到API
    await axios.post('/api/log/browse', {
      userId: me.value.userId,
      noteId: noteId.value,
      browseTime: browseTime,
      userAgent: navigator.userAgent
    })
  } catch (error) {
    console.error('发送访问时长数据失败：' + error)
  }
}

// 离开当前页面的时候
onBeforeRouteLeave((to, from, next) => { 
  // 统计访问笔记的时长
  handleBrowseTime()

  next()
})
</script>
<template>
  <!-- 主内容区 -->
  <main class="container py-4 main-content">
    <!-- 笔记内容 -->
    <div class="note-container">
      <!-- 笔记图片 -->
      <div class="note-images">
        <!-- 图片轮播容器 -->
        <div class="carousel-container" id="carouselContainer" ref="carouselContainerRef">
          <!-- 动态生成轮播项 -->
          <div class="carousel-item-img" v-for="(image, index) in note.images">
            <!-- 在img上加 preview-trigger -->
            <img class="note-image preview-trigger" :src="image" :alt="note.title" @click="openPreview(index)">
          </div>
        </div>

        <!-- 轮播指示器 -->
        <div class="carousel-indicator" id="carouselIndicator">
          <span id="currentSlide">{{ currentIndex + 1 }}</span> / <span id="totalSlides">{{ note.images.length }}</span>
        </div>

        <!-- 轮播控制按钮 -->
        <div class="carousel-control prev" @click="prevSlide">
          <i class="fa fa-angle-left"></i>
        </div>
        <div class="carousel-control next" @click="nextSlide">
          <i class="fa fa-angle-right"></i>
        </div>
      </div>

      <!-- 笔记内容区 -->
      <div class="note-content">
        <!-- 标题 -->
        <h1 class="note-title">{{ note.title }}</h1>

        <!-- 内容 -->
        <p class="note-text">
          {{ note.content }}<br><br>
        </p>

        <!-- 话题 -->
        <div class="note-tags">
          <span class="tag" v-for="topic in note.topics">
            {{ topic }}
          </span>
        </div>

        <!-- 操作栏 -->
        <div class="note-action-bar">
          <!-- 返回 -->
          <button class="btn btn-light btn-sm" @click="handleBack">
            <i class="fa fa-arrow-left"></i>
          </button>
          <!-- 编辑 -->
          <a :href="'/note/' + note.noteId + '/edit'">
            <button class="btn btn-light btn-sm" v-if="me.username === note.username">
              <i class="fa fa-edit"></i>
            </button>
          </a>
          <!-- 删除 -->
          <button class="btn btn-light btn-sm" v-if="me.username === note.username" @click="deleteNote">
            <i class="fa fa-trash"></i>
          </button>
          <!-- 分享 -->
          <button class="btn btn-light btn-sm">
            <i class="fa fa-share-alt"></i>
          </button>
          <!-- 点赞 -->
          <button class="btn btn-light btn-sm" @click="handleLike">
            <i :class="likeResponseDto.liked ? 'fa fa-heart liked' : 'fa fa-heart-o'"></i>
            {{ likeResponseDto.likeCount }}
          </button>
          <!-- 收藏 -->
          <button class="btn btn-light btn-sm">
            <i class="fa fa-star-o"></i>
          </button>
        </div>

        <!-- 作者信息 -->
        <div class="author-info">
          <!-- 点击作者头像跳转到作者详情页 -->
          <a :href="'/user/profile/' + note.userId">
            <img class="author-avatar" :src="note.avatar ? note.avatar : '/images/rn_avatar.png'" alt="作者头像">
          </a>

          <div>
            <div class="author-name">
              {{ note.username }}
            </div>
            <div class="author-meta">
              已获得 1024 粉丝
            </div>
          </div>
          <div class="author-follow" v-if="me.username != note.username">
            + 关注
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <div class="comments-header">
          <div class="comments-title">
            评论区
          </div>
        </div>

        <!-- 评论输入框 -->
        <div class="comment-input">
          <img class="comment-avatar" src="/images/rn_avatar.png" alt="头像">
          <textarea class="comment-textarea" placeholder="分享你的想法..." v-model="newComment"></textarea>
          <div class="comment-btn" @click="postComment">
            发送
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" id="commentList">
          <!-- 评论列表为空的处理 -->
          <p class="empty-comments" v-if="commentResponseDtoArray.length === 0">
            暂无评论，快来发表你的看法吧
          </p>

          <!-- 评论列表不为空的处理 -->
          <div class="comment-item" v-for="comment in commentResponseDtoArray" :key="comment.commentId">
            <!-- 评论头 -->
            <div class="comment-header">
              <!-- 作者信息 -->
              <!-- 点击用户头像跳转到用户详情页 -->
              <a :href="`/user/profile/${comment.userId}`">
                <img :src="comment.avatar ? comment.avatar : '/images/rn_avatar.png'" alt="用户头像"
                  class="comment-user-avatar">
              </a>
              <div class="comment-user-info">
                <div class="comment-username">{{ comment.username }}</div>
                <div class="comment-time">{{ formatDate(comment.createAt) }}</div>
              </div>

              <!-- 回复评论按钮 -->
              <button class="reply-btn" @click="handleReplyComment(comment)">
                <i class="fa fa-comment-o"></i>
              </button>

              <!-- 删除评论按钮 -->
              <button class="delete-comment" v-if="me.username === comment.username"
                @click="deleteComment(comment.commentId)">
                <i class="fa fa-trash-o"></i>
              </button>
            </div>

            <!-- 评论内容 -->
            <div class="comment-content">
              {{ comment.content }}
            </div>

            <!-- 回复列表 -->
            <div class="reply-list" v-for="reply in comment.replies" :key="reply.commentId">
              <div class="reply-item">
                <div class="reply-header">
                  <!-- 点击用户名跳转到用户详情页 -->
                  <a :href="`/user/profile/${reply.userId}`">
                    <span class="reply-username">{{ reply.username }}</span>
                  </a>
                  <span class="reply-to">»</span>
                  <span class="reply-target">{{ reply.parentCommentUsername ? reply.parentCommentUsername : '评论作者'
                    }}</span>
                  <span class="reply-time">{{ formatDate(reply.createAt) }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
                <!-- 回复回复的按钮-->
                <button class="reply-btn" @click="handleReplyComment(reply)">
                  <i class="fa fa-comment-o"></i>
                </button>
                <!-- 删除回复的按钮-->
                <button class="delete-comment" v-if="me.username === reply.username"
                  @click="deleteReply(reply.commentId)">
                  <i class="fa fa-trash-o"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>

  <!-- 图片预览模态框 -->
  <div class="preview-modal" id="previewModal" ref="previewModalRef" v-show="previewModalVisible">
    <div class="preview-content">
      <img class="preview-image" id="previewImage" :src="note.images[previewCurrentIndex]"
        v-if="note.images && note.images.length > 0" alt="图片预览">
      <div class="preview-close" @click="closePreview">
        <i class="fa fa-times"></i>
      </div>
      <div class="preview-counter" id="previewCounter">
        <span id="previewCurrent">{{ previewCurrentIndex + 1 }}</span> / <span id="previewTotal">{{ note.images?.length
          || 0 }}</span>
      </div>
      <div class="preview-control prev" @click="previewPrev">
        <i class="fa fa-angle-left"></i>
      </div>
      <div class="preview-control next" @click="previewNext">
        <i class="fa fa-angle-right"></i>
      </div>
    </div>
  </div>

  <!-- 回复弹窗 -->
  <div class="modal" id="replyModal" tabindex="-1" aria-labelledby="replyModalLabel" ref="replyModalRef"
    aria-hidden="true">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="replyModalLabel">回复 <span id="replyToUsername">{{
            replyToCommentResponseDto.username }}</span></h5>
        </div>
        <div class="modal-body">
          <div class="reply-to-content"></div>
          <textarea class="form-control" id="replyContent" rows="3" placeholder="写下你的回复..."
            ref="replyContentRef"></textarea>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-light close-model" data-bs-dismiss="modal"
            @click="hideReplyModal">取消</button>
          <button type="button" class="btn btn-danger" id="submitReply" @click="submitReply">提交回复</button>
        </div>
      </div>
    </div>
  </div>
</template>
<style setup>
/* 全局样式 */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f5f5f5;
}

/* 笔记内容区 */
.note-container {
  background-color: white;
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.note-images {
  position: relative;
  background-color: #000;
}

.note-image {
  width: 100%;
  max-height: 60vh;
  object-fit: contain;
}

.note-content {
  padding: 20px;
}

.note-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
}

.note-text {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.note-tags {
  margin-bottom: 20px;
}

.tag {
  display: inline-block;
  background-color: #f0f0f0;
  color: #666;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
  margin-right: 8px;
  margin-bottom: 8px;
}

.note-action-bar {
  margin-bottom: 20px;
}

/* 作者信息 */
.author-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
}

.author-follow {
  margin-left: auto;
  background-color: #ff2442;
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.author-follow.following {
  background-color: #f0f0f0;
  color: #666;
}

/* 评论区（第一部分）*/
.comments-section {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 20px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.comments-title {
  font-size: 18px;
  font-weight: 600;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
}

.comment-input {
  display: flex;
  margin-bottom: 20px;
}

.comment-textarea {
  flex-grow: 1;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  resize: none;
  outline: none;
}

.comment-btn {
  margin-left: 12px;
  background-color: #ff2442;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}


/* 新增轮播和预览样式 */
.carousel-container {
  display: flex;
  transition: transform 0.5s ease;
}

.carousel-item-img {
  min-width: 100%;
  position: relative;
}

.carousel-indicator {
  position: absolute;
  bottom: 15px;
  right: 15px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 12px;
  z-index: 10;
}

.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  color: white;
  font-size: 24px;
  padding: 10px;
  cursor: pointer;
  z-index: 10;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.carousel-control:hover {
  opacity: 1;
}

.carousel-control.prev {
  left: 10px;
}

.carousel-control.next {
  right: 10px;
}

/* 图片预览模态框 */
.preview-modal {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  z-index: 1000;
  justify-content: center;
  align-items: center;
}

.preview-content {
  max-width: 90%;
  max-height: 90%;
  position: relative;
}

.preview-image {
  max-width: 100%;
  max-height: 85vh;
  object-fit: contain;
  cursor: pointer;
}

.preview-close {
  position: absolute;
  top: -40px;
  right: 0;
  color: white;
  font-size: 30px;
  cursor: pointer;
}

.preview-counter {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 14px;
}

.preview-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  color: white;
  font-size: 30px;
  cursor: pointer;
  padding: 20px;
}

.preview-control.prev {
  left: -60px;
}

.preview-control.next {
  right: -60px;
}

/* 去掉下划线 */
a {
  text-decoration: none;
}

/* 点赞按钮样式 */
.liked {
  color: #ff2442;
}

/* 评论区（第二部分）*/
.comment-user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 10px;
}

.comment-user-info {
  flex: 1;
}

.comment-username {
  font-weight: bold;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  margin-left: 42px;
  margin-bottom: 10px;
}


.reply-btn,
.delete-comment {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 12px;
}

.delete-comment {
  margin-left: 10px;
}

.empty-comments {
  color: #999;
  text-align: center;
  padding: 20px 0;
}

/*评论回复*/
.reply-list {
  margin-left: 42px;
  margin-top: 10px;
  padding-left: 10px;
  border-left: 2px solid #f5f5f5;
}

.reply-item {
  margin-bottom: 10px;
}

.reply-header {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.reply-username,
.reply-target {
  font-weight: bold;
  margin-right: 5px;
}

.reply-to {
  margin-right: 5px;
}

.reply-time {
  margin-left: 10px;
  font-size: 12px;
  color: #999;
}

.reply-content {
  margin-top: 5px;
  margin-left: 0;
}

.submit-reply {
  background-color: #ff2442;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}
</style>