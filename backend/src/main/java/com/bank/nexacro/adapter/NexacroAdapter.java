package com.bank.nexacro.adapter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Nexacro 통신 포맷(Dataset) 과 서버 코드 사이의 유일한 접점.
 * Controller 는 이 인터페이스만 사용하므로, 벤더 라이브러리(xapi / uiadapter)로 교체하려면
 * 이 인터페이스의 구현체 하나만 새로 만들어 Bean 으로 등록하면 된다.
 */
public interface NexacroAdapter {

    /** HTTP 요청 본문(Dataset/Variable) 을 읽는다. */
    NexacroRequest read(HttpServletRequest request) throws IOException;

    /** 응답(Dataset/Variable + ErrorCode/ErrorMsg) 을 Nexacro 가 이해하는 포맷으로 쓴다. */
    void write(HttpServletResponse response, NexacroResponse result) throws IOException;
}
