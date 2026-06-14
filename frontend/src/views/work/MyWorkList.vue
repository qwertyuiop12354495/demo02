<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MyWorkTable from '@/components/work/MyWorkTable.vue'
import PortalPageHeader from '@/components/portal/PortalPageHeader.vue'
import { pageMyWorks } from '@/api/work'
import type { WorkListItem } from '@/types/work'

const router = useRouter()

const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<WorkListItem[]>([])

const isEmpty = computed(() => !loading.value && list.value.length === 0)

async function fetchList() {
  loading.value = true
  try {
    const data = await pageMyWorks({
      page: page.value,
      pageSize: pageSize.value,
    })
    list.value = data.list
    total.value = data.total
    page.value = data.page
    pageSize.value = data.pageSize
  } finally {
    loading.value = false
  }
}

function handlePageChange(nextPage: number) {
  page.value = nextPage
  fetchList()
}

function goHome() {
  router.push('/')
}

onMounted(fetchList)
</script>

<template>
  <div class="portal-page portal-page--narrow my-work-list">
    <PortalPageHeader
      title="我的作品"
      description="查看各活动的报名进度与审核状态，可继续编辑草稿或修改后重新提交。"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">去报名</el-button>
      </template>
    </PortalPageHeader>

    <MyWorkTable :list="list" :loading="loading" />

    <el-empty v-if="isEmpty" description="您还没有作品报名记录">
      <el-button type="primary" @click="goHome">浏览可报名活动</el-button>
    </el-empty>

    <div v-if="total > pageSize" class="my-work-list__pager">
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
.my-work-list__pager {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}
</style>
