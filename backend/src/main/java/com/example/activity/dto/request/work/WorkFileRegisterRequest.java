package com.example.activity.dto.request.work;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkFileRegisterRequest {

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255字")
    private String fileName;

    @NotBlank(message = "文件地址不能为空")
    @Size(max = 500, message = "文件地址不能超过500字")
    private String fileUrl;

    @NotBlank(message = "文件类型不能为空")
    @Size(max = 50, message = "文件类型不能超过50字")
    private String fileType;

    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须大于0")
    private Long fileSize;
}
