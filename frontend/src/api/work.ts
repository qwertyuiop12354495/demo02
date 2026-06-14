import { request } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type {
  WorkCreateDraftPayload,
  WorkFile,
  WorkFileRegisterPayload,
  WorkListItem,
  WorkMinePageQuery,
  WorkSavePayload,
  WorkVO,
} from '@/types/work'

export function createWorkDraft(payload: WorkCreateDraftPayload) {
  return request<WorkVO>({
    url: '/works/draft',
    method: 'POST',
    data: payload,
  })
}

export function saveWork(workId: number, payload: WorkSavePayload) {
  return request<WorkVO>({
    url: `/works/${workId}`,
    method: 'PUT',
    data: payload,
  })
}

export function submitWork(workId: number) {
  return request<WorkVO>({
    url: `/works/${workId}/submit`,
    method: 'POST',
  })
}

export function pageMyWorks(params?: WorkMinePageQuery) {
  return request<PageResult<WorkListItem>>({
    url: '/works/mine',
    method: 'GET',
    params,
  })
}

export function getWorkDetail(workId: number) {
  return request<WorkVO>({
    url: `/works/${workId}`,
    method: 'GET',
  })
}

export function listWorkFiles(workId: number) {
  return request<WorkFile[]>({
    url: `/works/${workId}/files`,
    method: 'GET',
  })
}

export function uploadWorkFile(workId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request<WorkFile>({
    url: `/works/${workId}/files/upload`,
    method: 'POST',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function registerWorkFile(workId: number, payload: WorkFileRegisterPayload) {
  return request<WorkFile>({
    url: `/works/${workId}/files/register`,
    method: 'POST',
    data: payload,
  })
}

export function deleteWorkFile(workId: number, fileId: number) {
  return request<void>({
    url: `/works/${workId}/files/${fileId}`,
    method: 'DELETE',
  })
}
