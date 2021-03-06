package com.info.back.controller;

import com.alibaba.fastjson.JSON;
import com.info.back.service.IMmanLoanCollectionOrderService;
import com.info.back.service.IMmanUserInfoService;
import com.info.back.service.IMmanUserRelaService;
import com.info.back.utils.BackConstant;
import com.info.web.pojo.MmanLoanCollectionOrder;
import com.info.web.pojo.MmanUserInfo;
import com.info.web.pojo.MmanUserRela;
import com.info.web.util.PageConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/mmanUserRela")
public class MmanUserRelaController extends BaseController {
    private static Logger logger = Logger.getLogger(MmanUserRelaController.class);

    @Autowired
    private IMmanUserRelaService mmanUserRelaService;
    @Autowired
    private IMmanUserInfoService mmanUserInfoService;

    @Autowired
    private IMmanLoanCollectionOrderService mmanLoanCollectionOrderService;

    @RequestMapping("/getMmanUserRelaPage")
    @ResponseBody
    public String getMmanUserRelaPage(HttpServletRequest request) {
        HashMap<String, Object> params = this.getParametersO(request);
        String orderId = params.get("id").toString();
        List<MmanUserRela> list = null;
        try {
            MmanLoanCollectionOrder order = mmanLoanCollectionOrderService.getOrderById(orderId);
            if (order != null) {
                params.put("userId", order.getUserId());
//                params.put("userId", order.getUserId());
//                int overdueDays = order.getOverdueDays();//逾期天数
//                params.put("overdueDays", overdueDays);
                if (!BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(order.getStatus())) {
                    MmanUserInfo userInfo = mmanUserInfoService.getUserInfoById(order.getUserId());

                    list = mmanUserRelaService.getList(params);
                    List<MmanUserRela> mmanUserRelaList = list;
                    MmanUserRela mmanUserRelaOne = new MmanUserRela();
                    mmanUserRelaOne.setUserId(order.getUserId());
                    mmanUserRelaOne.setRealName(userInfo.getRealname());
                    mmanUserRelaOne.setContactsKey(userInfo.getFristContactRelation().toString());
                    mmanUserRelaOne.setRelaKey(userInfo.getFristContactRelation().toString());
                    mmanUserRelaOne.setInfoName(userInfo.getFirstContactName());
                    mmanUserRelaOne.setInfoValue(userInfo.getFirstContactPhone());
                    MmanUserRela mmanUserRelaTwo = new MmanUserRela();

                    mmanUserRelaTwo.setRealName(userInfo.getRealname());
                    mmanUserRelaTwo.setUserId(order.getUserId());
                    mmanUserRelaTwo.setContactsKey(userInfo.getSecondContactRelation().toString());
                    mmanUserRelaTwo.setRelaKey(userInfo.getSecondContactRelation().toString());
                    mmanUserRelaTwo.setInfoName(userInfo.getSecondContactName());
                    mmanUserRelaTwo.setInfoValue(userInfo.getSecondContactPhone());
                    mmanUserRelaList.add(0, mmanUserRelaOne);
                    mmanUserRelaList.add(1, mmanUserRelaTwo);
                    // 为了美观 对里面（特殊数据）过长做截取处理（名字留8位字符，电话留25位字符）
                    getResultStr(list);
                }
            }
        } catch (Exception e) {
            logger.error("getMmanUserRelaPage error", e);
        }
        return JSON.toJSONString(list);
    }

    // 对里面（特殊数据）过长做截取处理（名字留8位字符，电话留25位字符）
    private void getResultStr(List<MmanUserRela> list) {
        for (MmanUserRela info : list) {
            String name = info.getInfoName();
            String phone = info.getInfoValue();
            if (StringUtils.isNotEmpty(name)) {
                if (name.length() > 8) {
                    info.setInfoName(name.substring(0, 7));
                }
            }
            if (StringUtils.isNotEmpty(phone)) {
                if (phone.length() > 25) {
                    info.setInfoValue(phone.substring(0, 23));
                }
            }
        }
    }


    @RequestMapping("getMmanUserRelaCountPage")
    public String getMmanUserRelaCountPage(HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            HashMap<String, Object> params = this.getParametersO(request);
            String orderId = params.get("id").toString();
            MmanLoanCollectionOrder order = mmanLoanCollectionOrderService.getOrderById(orderId);
            params.put("userId", order.getUserId());
            List<MmanUserRela> list = mmanUserRelaService.getList(params);
            model.addAttribute("list", list);
            PageConfig<MmanUserRela> page = mmanUserRelaService.findAllPage(params);
            model.addAttribute("pm", page);
            model.addAttribute("params", params);
        } catch (Exception e) {
            logger.error("params error", e);
            model.addAttribute(MESSAGE, "服务器异常，请稍后重试！");
        }
        return "mycollectionorder/relaCountPage";
    }

}
