package com.example.activity.common.exception;

import com.example.activity.common.result.Result;
import com.example.activity.dto.query.ActivityPageQuery;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void handleBindException_shouldReturnUnifiedResult() {
        ActivityPageQuery query = new ActivityPageQuery();
        query.setPage(0);

        BindException bindException = new BindException(query, "query");
        validator.validate(query).forEach(v ->
                bindException.rejectValue(v.getPropertyPath().toString(), v.getMessageTemplate(), v.getMessage()));

        ResponseEntity<Result<Void>> response = handler.handleBindingException(bindException);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(40007, response.getBody().getCode());
        assertEquals("页码不能小于1", response.getBody().getMessage());
    }

    @Test
    void handleBindException_shouldValidateEventTimeRange() {
        ActivityPageQuery query = new ActivityPageQuery();
        query.setEventStartFrom(java.time.LocalDateTime.of(2026, 12, 31, 0, 0));
        query.setEventStartTo(java.time.LocalDateTime.of(2026, 1, 1, 0, 0));

        BindException bindException = new BindException(query, "query");
        Set<ConstraintViolation<ActivityPageQuery>> violations = validator.validate(query);
        violations.forEach(v ->
                bindException.rejectValue(v.getPropertyPath().toString(), v.getMessageTemplate(), v.getMessage()));

        ResponseEntity<Result<Void>> response = handler.handleBindingException(bindException);

        assertEquals(40007, response.getBody().getCode());
        assertEquals("活动开始筛选时间不能晚于结束时间", response.getBody().getMessage());
    }
}
