package com.example.activity.controller.admin;

import com.example.activity.common.auth.RequireAdmin;
import com.example.activity.common.auth.UserContext;
import com.example.activity.common.result.Result;
import com.example.activity.dto.request.registration.RegistrationAuditRequest;
import com.example.activity.service.RegistrationService;
import com.example.activity.vo.registration.RegistrationVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/registrations")
@RequireAdmin
@Validated
@RequiredArgsConstructor
public class AdminRegistrationController {

    private final RegistrationService registrationService;

    @PatchMapping("/{id}/audit")
    public Result<RegistrationVO> audit(
            @PathVariable @Positive(message = "报名ID必须大于0") Long id,
            @Valid @RequestBody RegistrationAuditRequest request) {
        return Result.success(registrationService.audit(id, request, UserContext.requireUserId()));
    }
}
