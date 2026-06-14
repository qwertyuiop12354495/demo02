package com.example.activity.service;

import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Activity;
import com.example.activity.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 活动名额并发控制。
 * <p>
 * 推荐方案：行级锁（SELECT FOR UPDATE）+ 条件更新（UPDATE ... WHERE current_count &lt; max_count）
 * <ul>
 *   <li>FOR UPDATE：同一活动的并发报名在 InnoDB 下行级串行化</li>
 *   <li>条件 UPDATE：在数据库层原子判断并占用名额，避免 current_count 超过 max_count</li>
 *   <li>先占位、后插入报名记录：占位失败则不写报名行，事务回滚时占位自动撤销</li>
 * </ul>
 * 必须在 {@code @Transactional} 事务内调用。
 */
@Service
@RequiredArgsConstructor
public class ActivityQuotaService {

    private final ActivityMapper activityMapper;

    /**
     * 锁定活动行，供后续业务校验使用。不修改名额。
     */
    public Activity lockActivity(Long activityId) {
        return activityMapper.selectByIdForUpdate(activityId);
    }

    /**
     * 在已持有行锁的前提下，原子预占一个名额。
     *
     * @return 预占后的活动快照（含最新 current_count）
     */
    public Activity reserveQuota(Long activityId) {
        int affected = activityMapper.reserveQuota(activityId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.QUOTA_FULL);
        }
        return reloadActivity(activityId);
    }

    /**
     * 释放一个已占用的名额（取消报名时调用）。
     */
    public void releaseQuota(Long activityId) {
        int affected = activityMapper.releaseQuota(activityId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "人数统计异常，请稍后重试");
        }
    }

    private Activity reloadActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        return activity;
    }
}
