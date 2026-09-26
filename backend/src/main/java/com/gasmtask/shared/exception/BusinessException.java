package com.gasmtask.shared.exception;

/** Violação de regra de negócio ou falha esperada, traduzida para Problem Details pelo {@link GlobalExceptionHandler}. */
public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code) {
        this(code, code.defaultMessage());
    }

    public BusinessException(ErrorCode code, String detail) {
        super(detail);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
