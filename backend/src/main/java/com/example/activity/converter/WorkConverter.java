package com.example.activity.converter;

import com.example.activity.common.storage.WorkFileUrlResolver;
import com.example.activity.entity.Activity;
import com.example.activity.entity.Work;
import com.example.activity.entity.WorkFile;
import com.example.activity.vo.work.WorkFileVO;
import com.example.activity.vo.work.WorkListItemVO;
import com.example.activity.vo.work.WorkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkConverter {

    private final WorkFileUrlResolver workFileUrlResolver;

    public WorkVO toVO(Work work, Activity activity, List<WorkFile> files) {
        WorkVO vo = new WorkVO();
        vo.setId(work.getId());
        vo.setActivityId(work.getActivityId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setTeacherId(work.getTeacherId());
        vo.setTitle(work.getTitle());
        vo.setCategory(work.getCategory());
        vo.setEquipment(work.getEquipment());
        vo.setDuration(work.getDuration());
        vo.setProvinceName(work.getProvinceName());
        vo.setCityName(work.getCityName());
        vo.setDistrictName(work.getDistrictName());
        vo.setSchoolName(work.getSchoolName());
        vo.setCurrentStep(work.getCurrentStep());
        vo.setCurrentStatus(work.getCurrentStatus());
        vo.setFinalScore(work.getFinalScore());
        vo.setFinalResult(work.getFinalResult());
        vo.setCreatedAt(work.getCreatedAt());
        vo.setUpdatedAt(work.getUpdatedAt());
        if (files != null) {
            vo.setFiles(files.stream().map(this::toFileVO).toList());
        }
        return vo;
    }

    public WorkListItemVO toListItemVO(Work work, Activity activity) {
        WorkListItemVO vo = new WorkListItemVO();
        vo.setId(work.getId());
        vo.setActivityId(work.getActivityId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setTitle(work.getTitle());
        vo.setCategory(work.getCategory());
        vo.setCurrentStep(work.getCurrentStep());
        vo.setCurrentStatus(work.getCurrentStatus());
        vo.setFinalResult(work.getFinalResult());
        vo.setCreatedAt(work.getCreatedAt());
        vo.setUpdatedAt(work.getUpdatedAt());
        return vo;
    }

    public WorkFileVO toFileVO(WorkFile file) {
        WorkFileVO vo = new WorkFileVO();
        vo.setId(file.getId());
        vo.setWorkId(file.getWorkId());
        vo.setFileName(file.getFileName());
        vo.setFileUrl(workFileUrlResolver.resolveForDownload(file.getFileUrl()));
        vo.setFileType(file.getFileType());
        vo.setFileSize(file.getFileSize());
        vo.setCreatedAt(file.getCreatedAt());
        return vo;
    }
}
