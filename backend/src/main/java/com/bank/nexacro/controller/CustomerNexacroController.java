package com.bank.nexacro.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bank.customer.service.CustomerService;
import com.bank.customer.vo.CustomerVO;
import com.bank.nexacro.adapter.BeanDatasets;
import com.bank.nexacro.adapter.NexacroAdapter;
import com.bank.nexacro.adapter.NexacroRequest;
import com.bank.nexacro.adapter.NexacroResponse;

/** 고객 화면용 Nexacro Controller. */
@Controller
@RequestMapping("/nexacro/customer")
public class CustomerNexacroController {

    @Autowired
    private NexacroAdapter adapter;
    @Autowired
    private CustomerService customerService;

    /** in: dsCond(keyword) / out: dsCustomer */
    @RequestMapping(value = "/selectCustomerList.do", method = RequestMethod.POST)
    public void selectCustomerList(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        String keyword = in.getDataset("dsCond").getString("keyword");
        adapter.write(res, NexacroResponse.ok().addDataset(
                BeanDatasets.fromBeans("dsCustomer", CustomerVO.class, customerService.getCustomerList(keyword))));
    }

    /** in: dsCustomer(custNm,phone,email) / out: 변수 custNo */
    @RequestMapping(value = "/registerCustomer.do", method = RequestMethod.POST)
    public void registerCustomer(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NexacroRequest in = adapter.read(req);
        CustomerVO vo = BeanDatasets.toBean(in.getDataset("dsCustomer"), CustomerVO.class);
        String custNo = customerService.registerCustomer(vo);
        adapter.write(res, NexacroResponse.ok().addVariable("custNo", custNo));
    }
}
