<template>
  <div>
    <div class="page-header">
      <h2>{{ t('partners.title') }}</h2>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="searchQuery" :placeholder="t('partners.searchPlaceholder')" clearable style="width:260px" />
        <el-select v-model="filterStatus" clearable :placeholder="t('common.status')" style="width:140px">
          <el-option :label="t('common.active')" value="ACTIVE" />
          <el-option :label="t('common.inactive')" value="INACTIVE" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="router.push('/partners/new')" style="margin-left:auto">
          {{ t('partners.newPartner') }}
        </el-button>
      </div>
      <el-table :data="pagedPartners" v-loading="loading" stripe @row-click="goDetail">
        <el-table-column type="index" label="#" width="55" :index="(i: number) => (page - 1) * pageSize + i + 1" />
        <el-table-column prop="groupCode" :label="t('partners.code')" width="150" />
        <el-table-column prop="groupName" :label="t('partners.name')" min-width="180" />
        <el-table-column prop="username" :label="t('partners.username')" width="140" />
        <el-table-column prop="contactEmail" :label="t('partners.email')" min-width="180" />
        <el-table-column prop="memberCount" :label="t('partners.members')" width="90" />
        <el-table-column :label="t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('partners.created')" width="110">
          <template #default="{ row }">{{ row.createdAt?.slice(0, 10) }}</template>
        </el-table-column>
        <el-table-column :label="t('common.actions')" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click.stop="goDetail(row)">{{ t('partners.detail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="filteredPartners.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { listPartners } from '@/api/groups'
import type { Partner } from '@/types'

const { t } = useI18n()
const router = useRouter()
const partners = ref<Partner[]>([])
const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const page = ref(1)
const pageSize = ref(10)

const filteredPartners = computed(() => {
  let result = partners.value
  const q = searchQuery.value.toLowerCase()
  if (q) result = result.filter(p =>
    p.groupCode.toLowerCase().includes(q) ||
    p.groupName.toLowerCase().includes(q) ||
    p.username?.toLowerCase().includes(q) ||
    p.contactEmail?.toLowerCase().includes(q)
  )
  if (filterStatus.value) result = result.filter(p => p.status === filterStatus.value)
  return result
})

const pagedPartners = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredPartners.value.slice(start, start + pageSize.value)
})

watch([searchQuery, filterStatus], () => { page.value = 1 })

async function load() {
  loading.value = true
  try {
    const res = await listPartners()
    if (res.success) partners.value = res.data || []
  } finally { loading.value = false }
}

function goDetail(row: Partner) { router.push('/partners/' + row.groupId) }

onMounted(load)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.toolbar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination-bar { margin-top: 16px; display: flex; justify-content: flex-end; }
:deep(.el-table__row) { cursor: pointer; }
</style>
