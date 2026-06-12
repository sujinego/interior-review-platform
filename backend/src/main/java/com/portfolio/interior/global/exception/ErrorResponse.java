package com.portfolio.interior.global.exception;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Getter
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<ValidationError> errors;

    @Builder
    private ErrorResponse(String code, String message, List<ValidationError> errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(List.of())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(ValidationError.of(bindingResult))
                .build();
    }

    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String value;
        private final String reason;

        public static List<ValidationError> of(BindingResult bindingResult) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            List<ValidationError> result = new ArrayList<>(fieldErrors.size());
            for (FieldError fieldError : fieldErrors) {
                Object rejectedValue = fieldError.getRejectedValue();
                result.add(ValidationError.builder()
                        .field(fieldError.getField())
                        .value(rejectedValue == null ? "" : rejectedValue.toString())
                        .reason(fieldError.getDefaultMessage())
                        .build());
            }
            return result;
        }
    }
}
