<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { WORK_CATEGORY_OPTIONS } from '@/composables/useWorkEnroll'
import type { WorkSavePayload } from '@/types/work'

const props = defineProps<{
  modelValue: WorkSavePayload
  disabled?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: WorkSavePayload]
  submit: []
}>()

const form = reactive<WorkSavePayload>({
  title: props.modelValue.title,
  category: props.modelValue.category,
  equipment: props.modelValue.equipment,
  duration: props.modelValue.duration,
})

watch(
  () => props.modelValue,
  (value) => {
    form.title = value.title
    form.category = value.category
    form.equipment = value.equipment
    form.duration = value.duration
  },
  { deep: true },
)

watch(
  form,
  () => {
    emit('update:modelValue', { ...form })
  },
  { deep: true },
)

const isMusic = computed(() => {
  const category = form.category?.trim().toUpperCase()
  return category === 'MUSIC' || form.category === '音乐'
})

function handleSubmit() {
  emit('submit')
}
</script>

<template>
  <el-form label-width="96px" class="work-form-panel" @submit.prevent="handleSubmit">
    <el-form-item label="作品标题" required>
      <el-input
        v-model="form.title"
        maxlength="200"
        show-word-limit
        placeholder="请输入作品标题"
        :disabled="disabled"
      />
    </el-form-item>
    <el-form-item label="作品类别">
      <el-select
        v-model="form.category"
        placeholder="请选择类别"
        clearable
        :disabled="disabled"
        style="width: 100%"
      >
        <el-option
          v-for="item in WORK_CATEGORY_OPTIONS"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item v-if="isMusic" label="使用器材" required>
      <el-input
        v-model="form.equipment"
        maxlength="200"
        placeholder="音乐类作品请填写使用器材"
        :disabled="disabled"
      />
    </el-form-item>
    <el-form-item label="时长(分钟)" required>
      <el-input-number
        v-model="form.duration"
        :min="1"
        :max="999"
        :disabled="disabled"
        controls-position="right"
        style="width: 160px"
      />
    </el-form-item>
    <el-form-item v-if="!disabled">
      <el-button type="primary" native-type="submit">保存作品信息</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped>
.work-form-panel {
  max-width: 640px;
}
</style>
