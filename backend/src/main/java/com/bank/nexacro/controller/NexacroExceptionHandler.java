package com.bank.nexacro.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.bank.common.exception.BizException;
import com.bank.nexacro.adapter.NexacroAdapter;
import com.bank.nexacro.adapter.NexacroResponse;

/**
 * /nexacro/** Controller 의 예외를 Dataset 응답의 ErrorCode / ErrorMsg 로 변환한다.
 * HTTP 상태는 항상 200 (Nexacro 는 ErrorCode 로 성공/실패를 판단해 콜백을 호출한다).
 * 업무 예외: ErrorCode = -1, 업무 에러코드(E###) 는 변수 BizCode 로 전달. 시스템 예외: ErrorCode = -999.
 */
@ControllerAdvice(basePackages = "com.bank.nexacro")
public class NexacroExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(NexacroExceptionHandler.class);

    @Autowired
    private NexacroAdapter adapter;

    @ExceptionHandler(BizException.class)
    public void handleBiz(BizException e, HttpServletResponse res) throws IOException {
        log.warn("업무 오류 [{}] {}", e.getErrorCode(), e.getMessage());
        adapter.write(res, NexacroResponse.error(-1, e.getMessage()).addVariable("BizCode", e.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public void handleEtc(Exception e, HttpServletResponse res) throws IOException {
        log.error("시스템 오류", e);
        adapter.write(res, NexacroResponse.error(-999, "시스템 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
}
