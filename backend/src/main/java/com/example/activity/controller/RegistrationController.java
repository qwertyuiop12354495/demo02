package com.example.activity.controller;

import com.example.activity.common.auth.UserContext;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.RegistrationMinePageQuery;
import com.example.activity.dto.request.registration.RegistrationCreateRequest;
import com.example.activity.service.RegistrationService;
import com.example.activity.vo.registration.RegistrationCancelVO;
import com.example.activity.vo.registration.RegistrationListItemVO;
import com.example.activity.vo.registration.RegistrationVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
@Validated
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public Result<RegistrationVO> register(@Valid @RequestBody RegistrationCreateRequest request) {
        return Result.success(registrationService.register(request, UserContext.requireUserId()));
    }

    @PatchMapping("/{id}/cancel")
    public Result<RegistrationCancelVO> cancel(
            @PathVariable @Positive(message = "报名ID必须大于0") Long id) {
        return Result.success(registrationService.cancel(id, UserContext.requireUserId()));
    }

    @GetMapping("/mine")
    public Result<PageResult<RegistrationListItemVO>> pageMine(@Valid RegistrationMinePageQuery query) {
        return Result.success(registrationService.pageMine(query, UserContext.requireUserId()));
    }
}
