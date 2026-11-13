package com.bank.nexacro.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bank.account.service.AccountService;
import com.bank.account.vo.AccountVO;
import com.bank.account.vo.TransHistVO;
import com.bank.account.vo.TransferReqVO;
import com.bank.common.exception.BizException;
import com.bank.nexacro.adapter.BeanDatasets;
import com.bank.nexacro.adapter.NexacroAdapter;
import com.bank.nexacro.adapter.NexacroRequest;
import com.bank.nexacro.adapter.NexacroResponse;

/** 계좌 화면용 Nexacro Controller. Dataset 변환만 하고 로직은 AccountService 에 위임한다. */
@Controller
@RequestMapping("/nexacro/account")
public class AccountNexacroController {

    @Autowired
    private NexacroAdapter adapter;
    @Autowired
    private AccountService accountService;

    /** in: 없음 / out: dsAccount */
    @RequestMapping(value = "/selectAccountList.do", method = RequestMethod.POST)
    public void selectAccountList(HttpServletRequest req, HttpServletResponse res) throws IOException {
        adapter.read(req);
        adapter.write(res, NexacroResponse.ok().addDataset(
                BeanDatasets.fromBeans("dsAccount", AccountVO.class, accountService.getAccountList())));
    }

    /** in: dsCond(acctNo) / out: dsAccount(1행), dsHist */
    @RequestMapping(value = "/selectAccount.do", method = RequestMethod.POST)
    public void selectAccount(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        String acctNo = in.getDataset("dsCond").getString("acctNo");
        if (acctNo == null) {
            throw new BizException("E001", "계좌번호는 필수입니다.");
        }
        AccountVO acct = accountService.getAccount(acctNo);
        adapter.write(res, NexacroResponse.ok()
                .addDataset(BeanDatasets.fromBean("dsAccount", AccountVO.class, acct))
                .addDataset(BeanDatasets.fromBeans("dsHist", TransHistVO.class,
                        accountService.getTransHistList(acctNo))));
    }

    /** in: dsTransfer(fromAcctNo,toAcctNo,amount,memo) / out: 없음 */
    @RequestMapping(value = "/transfer.do", method = RequestMethod.POST)
    public void transfer(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        TransferReqVO vo = BeanDatasets.toBean(in.getDataset("dsTransfer"), TransferReqVO.class);
        accountService.transfer(vo);
        adapter.write(res, NexacroResponse.ok());
    }
}
