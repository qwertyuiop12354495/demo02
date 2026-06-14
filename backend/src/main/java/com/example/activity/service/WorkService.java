package com.example.activity.service;

import com.example.activity.common.result.PageResult;
import com.example.activity.dto.query.WorkMinePageQuery;
import com.example.activity.dto.request.work.WorkCreateDraftRequest;
import com.example.activity.dto.request.work.WorkSaveRequest;
import com.example.activity.vo.work.WorkListItemVO;
import com.example.activity.vo.work.WorkVO;

public interface WorkService {

    WorkVO createDraft(WorkCreateDraftRequest request);

    WorkVO saveWork(Long workId, WorkSaveRequest request);

    WorkVO submitWork(Long workId);

    PageResult<WorkListItemVO> pageMyWorks(WorkMinePageQuery query);

    WorkVO getMyWorkDetail(Long workId);
}
