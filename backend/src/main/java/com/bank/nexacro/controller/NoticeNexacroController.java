package com.bank.nexacro.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bank.common.exception.BizException;
import com.bank.nexacro.adapter.BeanDatasets;
import com.bank.nexacro.adapter.NexacroAdapter;
import com.bank.nexacro.adapter.NexacroRequest;
import com.bank.nexacro.adapter.NexacroResponse;
import com.bank.notice.service.NoticeService;
import com.bank.notice.vo.NoticeVO;

/** 공지사항 Nexacro Controller (이 샘플의 프론트 화면에서는 호출하지 않는 백엔드 전용 엔드포인트). */
@Controller
@RequestMapping("/nexacro/notice")
public class NoticeNexacroController {

    @Autowired
    private NexacroAdapter adapter;
    @Autowired
    private NoticeService noticeService;

    /** in: 없음 / out: dsNotice */
    @RequestMapping(value = "/selectNoticeList.do", method = RequestMethod.POST)
    public void selectNoticeList(HttpServletRequest req, HttpServletResponse res) throws IOException {
        adapter.read(req);
        adapter.write(res, NexacroResponse.ok().addDataset(
                BeanDatasets.fromBeans("dsNotice", NoticeVO.class, noticeService.getNoticeList())));
    }

    /** in: dsCond(noticeSeq) / out: dsNotice(1행) - 조회수 +1 */
    @RequestMapping(value = "/readNotice.do", method = RequestMethod.POST)
    public void readNotice(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        String seq = in.getDataset("dsCond").getString("noticeSeq");
        if (seq == null) {
            throw new BizException("E001", "공지 번호는 필수입니다.");
        }
        NoticeVO vo = noticeService.readNotice(Long.parseLong(seq));
        adapter.write(res, NexacroResponse.ok().addDataset(BeanDatasets.fromBean("dsNotice", NoticeVO.class, vo)));
    }

    /** in: dsNotice(title,content,writer) / out: 없음 */
    @RequestMapping(value = "/writeNotice.do", method = RequestMethod.POST)
    public void writeNotice(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        noticeService.writeNotice(BeanDatasets.toBean(in.getDataset("dsNotice"), NoticeVO.class));
        adapter.write(res, NexacroResponse.ok());
    }
}
