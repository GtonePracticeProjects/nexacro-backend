package com.bank.nexacro.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bank.common.exception.BizException;
import com.bank.loan.service.LoanService;
import com.bank.loan.vo.LoanVO;
import com.bank.nexacro.adapter.BeanDatasets;
import com.bank.nexacro.adapter.NexacroAdapter;
import com.bank.nexacro.adapter.NexacroRequest;
import com.bank.nexacro.adapter.NexacroResponse;

/** 대출 화면용 Nexacro Controller. */
@Controller
@RequestMapping("/nexacro/loan")
public class LoanNexacroController {

    @Autowired
    private NexacroAdapter adapter;
    @Autowired
    private LoanService loanService;

    /** in: dsCond(statusCd) / out: dsLoan */
    @RequestMapping(value = "/selectLoanList.do", method = RequestMethod.POST)
    public void selectLoanList(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        String statusCd = in.getDataset("dsCond").getString("statusCd");
        adapter.write(res, NexacroResponse.ok().addDataset(
                BeanDatasets.fromBeans("dsLoan", LoanVO.class, loanService.getLoanList(statusCd))));
    }

    /** in: dsApply(acctNo,principal,termMonths) / out: 없음 */
    @RequestMapping(value = "/applyLoan.do", method = RequestMethod.POST)
    public void applyLoan(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        LoanVO vo = BeanDatasets.toBean(in.getDataset("dsApply"), LoanVO.class);
        if (vo.getAcctNo() == null) {
            throw new BizException("E001", "계좌번호는 필수입니다.");
        }
        loanService.applyLoan(vo.getAcctNo(), vo.getPrincipal(), vo.getTermMonths());
        adapter.write(res, NexacroResponse.ok());
    }

    /** in: dsDecide(loanNo) / out: 없음 (승인) */
    @RequestMapping(value = "/approveLoan.do", method = RequestMethod.POST)
    public void approveLoan(HttpServletRequest req, HttpServletResponse res) throws IOException {
        decide(req, res, true);
    }

    /** in: dsDecide(loanNo) / out: 없음 (거절) */
    @RequestMapping(value = "/rejectLoan.do", method = RequestMethod.POST)
    public void rejectLoan(HttpServletRequest req, HttpServletResponse res) throws IOException {
        decide(req, res, false);
    }

    private void decide(HttpServletRequest req, HttpServletResponse res, boolean approve) throws IOException {
        NexacroRequest in = adapter.read(req);
        LoanVO vo = BeanDatasets.toBean(in.getDataset("dsDecide"), LoanVO.class);
        loanService.decideLoan(vo.getLoanNo(), approve);
        adapter.write(res, NexacroResponse.ok());
    }
}
