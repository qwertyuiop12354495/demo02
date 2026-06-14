<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ActivityHomeCard from '@/components/work/ActivityHomeCard.vue'
import PortalPageHeader from '@/components/portal/PortalPageHeader.vue'
import { listPublishedForHome } from '@/api/activity'
import type { ActivityHomeListItem } from '@/types/activity-home'

const router = useRouter()

const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const list = ref<ActivityHomeListItem[]>([])

async function fetchList() {
  loading.value = true
  try {
    const data = await listPublishedForHome({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
    })
    list.value = data.list
    total.value = data.total
    page.value = data.page
    pageSize.value = data.pageSize
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  keyword.value = ''
  page.value = 1
  fetchList()
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  fetchList()
}

function goEnroll(activityId: number) {
  router.push(`/works/enroll/${activityId}`)
}

function goContinueEdit(activityId: number, _workId: number) {
  router.push(`/works/enroll/${activityId}`)
}

onMounted(() => {
  fetchList().catch(() => {
    ElMessage.error('加载活动列表失败')
  })
})
</script>

<template>
  <div class="portal-page portal-page--narrow teacher-home">
    <PortalPageHeader
      title="可报名活动"
      description="以下为当前处于报名期内的活动，您可创建作品草稿并提交报名。"
    >
      <template #extra>
        <el-button link type="primary" @click="router.push('/works')">我的作品</el-button>
      </template>
    </PortalPageHeader>

    <div class="teacher-home__toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索活动名称…"
        clearable
        style="max-width: 280px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div v-loading="loading" class="teacher-home__grid">
      <ActivityHomeCard
        v-for="item in list"
        :key="item.id"
        :activity="item"
        @enroll="goEnroll"
        @continue-edit="goContinueEdit"
      />
    </div>

    <el-empty v-if="!loading && list.length === 0" description="当前暂无处于报名期的活动" />

    <div v-if="total > pageSize" class="teacher-home__pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.teacher-home__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.teacher-home__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-4);
  min-height: 120px;
}

.teacher-home__pager {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}
</style>
