import type { CachedRevisionFeedback } from '@/types/admin-work-review'
import type { WorkStep } from '@/types/work'

const CACHE_KEY = 'work_revision_feedback_cache'

function readCache(): CachedRevisionFeedback[] {
  const raw = sessionStorage.getItem(CACHE_KEY)
  if (!raw) {
    return []
  }
  try {
    return JSON.parse(raw) as CachedRevisionFeedback[]
  } catch {
    return []
  }
}

function writeCache(entries: CachedRevisionFeedback[]) {
  sessionStorage.setItem(CACHE_KEY, JSON.stringify(entries))
}

export function useRevisionFeedbackCache() {
  function appendFeedback(
    workId: number,
    reviewStep: WorkStep,
    feedback: string,
    reviewerId?: number,
  ) {
    const entries = readCache()
    const sameWorkStep = entries.filter(
      (item) => item.workId === workId && item.reviewStep === reviewStep,
    )
    const nextRound = sameWorkStep.length + 1
    const entry: CachedRevisionFeedback = {
      workId,
      reviewStep,
      roundNo: nextRound,
      feedback,
      reviewerId,
      createdAt: new Date().toISOString(),
    }
    writeCache([entry, ...entries])
    return entry
  }

  function listByWork(workId: number, reviewStep?: WorkStep): CachedRevisionFeedback[] {
    return readCache()
      .filter((item) => item.workId === workId && (!reviewStep || item.reviewStep === reviewStep))
      .sort((a, b) => b.roundNo - a.roundNo)
  }

  return {
    appendFeedback,
    listByWork,
  }
}
