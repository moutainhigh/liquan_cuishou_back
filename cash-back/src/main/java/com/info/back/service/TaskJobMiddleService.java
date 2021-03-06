package com.info.back.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.info.back.dao.IChannelSwitchingDao;
import com.info.constant.Constant;
import com.info.web.pojo.*;
import com.info.web.util.JedisDataClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.info.back.dao.IBackUserDao;
import com.info.back.dao.IMmanLoanCollectionStatusChangeLogDao;
import com.info.back.utils.BackConstant;
import com.info.web.util.DateUtil;

@Service
public class TaskJobMiddleService {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IMmanUserLoanService manUserLoanService;

    @Autowired
    private ICreditLoanPayService creditLoanPayService;

    @Autowired
    private IMmanLoanCollectionOrderService manLoanCollectionOrderService;

    @Autowired
    private IMmanLoanCollectionRecordService mmanLoanCollectionRecordService;

    @Autowired
    private IBackUserDao backUserDao;

    @Autowired
    private IMmanLoanCollectionStatusChangeLogDao mmanLoanCollectionStatusChangeLogDao;

    @Autowired
    private IAlertMsgService sysAlertMsgService;

    @Autowired
    private IChannelSwitchingDao channelSwitchingDao;

    public void dispatchforLoanId(String loanId, String idNumber,String type) {
        logger.error("dispatchforLoanId start,loanId:" + loanId);

        if (StringUtils.isBlank(loanId)) {
            logger.error("dispatchforLoanId loanId is null");
            return;
        }

        MmanUserLoan mmanUserLoan = manUserLoanService.get(loanId);

        if (null == mmanUserLoan) {
            logger.error("dispatchforLoanId mmanUserLoan is null,loanId:" + loanId);
            return;
        }

        if (!"4".equals(mmanUserLoan.getLoanStatus())) {
            logger.error("dispatchforLoanId mmanUserLoan status is not equal 4,loanId:" + loanId);
            return;
        }

        dispatchForLoanId(mmanUserLoan, idNumber,type);

        logger.error("dispatchforLoanId end,loanId:" + loanId);

    }


    /**
     * 分配催收任务，更新催收相关操作(更新催收订单，添加流转日志，更新借款、还款逾期额天数状态等)
     */
    public void dispatchForLoanId(MmanUserLoan mmanUserLoan, String idNumber ,String type) {

        logger.error("TaskJobMiddleService dispatchForLoanId start" + "开始时间 : " + Calendar.getInstance().getTimeInMillis() + "借款id :" + mmanUserLoan.getId());
        //初始化参数
        Date now = new Date();
        String sysName = "系统";
        String sysRemark = "系统派单";
        String sysPromoteRemark = "逾期升级，系统重新派单";
//		String ourCompanyId = "1";
        Calendar clrNow = Calendar.getInstance();
        int yearNow = clrNow.get(Calendar.YEAR);
        int monthNow = clrNow.get(Calendar.MONTH) + 1;
        int dayNow = clrNow.get(Calendar.DAY_OF_MONTH);
        //和处理步骤对应的订单以及催收员列表
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo111List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo111List = new ArrayList<MmanLoanCollectionPerson>();
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo112List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo112List = new ArrayList<MmanLoanCollectionPerson>();
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo113List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo113List = new ArrayList<MmanLoanCollectionPerson>();
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo114List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo114List = new ArrayList<MmanLoanCollectionPerson>();
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo115List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo115List = new ArrayList<MmanLoanCollectionPerson>();
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo12List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo12List = new ArrayList<MmanLoanCollectionPerson>();
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo131List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo131List = new ArrayList<MmanLoanCollectionPerson>();
        List<MmanLoanCollectionOrder> mmanLoanCollectionOrderNo132List = new ArrayList<MmanLoanCollectionOrder>();
        List<MmanLoanCollectionPerson> mmanLoanCollectionPersonNo132List = new ArrayList<MmanLoanCollectionPerson>();

        List<MmanUserLoan> overdueList = new ArrayList<MmanUserLoan>();
        overdueList.add(mmanUserLoan);
        logger.error("overdueList size:" + overdueList.size());

        if (null != overdueList && overdueList.size() > 0) {
            for (MmanUserLoan mmanUserLoanOri : overdueList)
                try {
                    //更新借款
                    CreditLoanPay creditLoanPay = creditLoanPayService.findByLoanId(mmanUserLoanOri.getId());
                    if (null == creditLoanPay) {
                        logger.error("CreditLoanPay is null ,loanId:" + mmanUserLoanOri.getId());
                        continue;
                    }

                    if (creditLoanPay.getRealMoney() == null) {
                        creditLoanPay.setRealMoney(new BigDecimal("0"));
                    }

                    BigDecimal loanMoney = mmanUserLoanOri.getLoanMoney();// 借款本金
//					BigDecimal payedMoney = creditLoanPay.getRealMoney();// 已还款额

                    if (BackConstant.CREDITLOANAPPLY_OVERDUE.equals(mmanUserLoanOri.getLoanStatus())) {
                        // 计算去逾期天数
                        int pday = getOverdueDays(mmanUserLoanOri);
                        // 计算罚息
                        BigDecimal pmoney = getLoanPenalty(mmanUserLoanOri, loanMoney, pday);

                        // 如果滞纳金超过本金金额  则滞纳金金额等于本金且不再增加
                        if (pmoney.compareTo(loanMoney) >= 0) {
                            pmoney = loanMoney;
                        }

                        //对于逾期了多次还款的状况，还够本金则不算新罚息，计算到上次罚息值即可（如借1000，罚息120，上次还了1100，本次补足剩余20即可，罚息依然是120）
                        /*if ((loanMoney.subtract(payedMoney)).compareTo(BigDecimal.valueOf(0.00)) <= 0) {
                            pmoney = null==mmanUserLoanOri.getLoanPenalty()?new BigDecimal("0"):mmanUserLoanOri.getLoanPenalty();
						}*/
//                        loanMoney = loanMoney.add(pmoney).setScale(2, BigDecimal.ROUND_HALF_UP);  // 应还总额
                        mmanUserLoanOri.setLoanPenalty(pmoney);//逾期滞纳金 DecimalFormatUtil.df2Points.format(pmoney.doubleValue())

                        // 外层循环处理时间随着数据量增长会越来越长，防止更新滞纳金（罚息）时覆盖更新已还款的数据

                        MmanUserLoan mmanUserLoanForUpdate = new MmanUserLoan();
                        mmanUserLoanForUpdate.setId(mmanUserLoanOri.getId());
                        mmanUserLoanForUpdate.setLoanPenalty(mmanUserLoanOri.getLoanPenalty());  // 更新借款表滞纳金
                        manUserLoanService.updateMmanUserLoan(mmanUserLoanForUpdate);

                        //更新还款表中剩余应还滞纳金
                        BigDecimal znj = mmanUserLoanOri.getLoanPenalty().subtract(creditLoanPay.getRealgetInterest());  // 剩余应还滞纳金 = 订单滞纳金 - 实收罚息
                        CreditLoanPay np = new CreditLoanPay();
                        np.setId(creditLoanPay.getId());
                        np.setReceivableInterest(znj);  //  剩余应还罚息
                        if (Constant.BIG.equals(type)){
                           BigDecimal receivableMoney = pmoney.add(mmanUserLoanOri.getServiceCharge()).add(mmanUserLoanOri.getLoanMoney());
                            np.setReceivableMoney(receivableMoney);
                        }else{
                            np.setReceivableMoney(mmanUserLoanOri.getLoanMoney().add(mmanUserLoanOri.getLoanPenalty())); // 应还总额
                        }
                        creditLoanPayService.updateCreditLoanPay(np);


                        //1 查询需要处理的订单和该笔订单对应的分组
                        Calendar clrLoanEnd = Calendar.getInstance();
                        clrLoanEnd.setTime(mmanUserLoanOri.getLoanEndTime());
                        int yearLoanEnd = clrLoanEnd.get(Calendar.YEAR);
                        int monthLoanEnd = clrLoanEnd.get(Calendar.MONTH) + 1;

                        int orderUpgradeDay = 0; //  默认逾期升级天数（用一月内,s1组逾期10天，第二天升级至S2组)
                        String overdueUpgradeDays = JedisDataClient.get("OVERDUE_UPGRADE_DAY");
                        orderUpgradeDay = getOrderUpgradeDay(overdueUpgradeDays);

                        if (dayNow == 1) {
                            //1.1 若当前为每月1号，订单和所有分组要分多种情况（注意新订单和跨月订单升级流转日志类型不同）

                            //1.1.1 所有新订单分组为 M1-M2
                            MmanLoanCollectionOrder mmanLoanCollectionOrder = new MmanLoanCollectionOrder();
                            mmanLoanCollectionOrder.setLoanId(mmanUserLoanOri.getId());

                            List<MmanLoanCollectionOrder> mmanLoanCollectionOrderList = manLoanCollectionOrderService.findList(mmanLoanCollectionOrder);
                            if (null == mmanLoanCollectionOrderList || mmanLoanCollectionOrderList.isEmpty()) {

                                logger.error("mmanLoancollectionOrder new dispatch enter 1 ,mmanUserLoanOri id:" + mmanUserLoanOri.getId());

                                MmanLoanCollectionOrder mmanLoanCollectionOrderNo111 = new MmanLoanCollectionOrder();
                                mmanLoanCollectionOrderNo111.setLoanId(mmanUserLoanOri.getId());
                                mmanLoanCollectionOrderNo111.setOrderId(mmanUserLoanOri.getLoanPyId());
                                mmanLoanCollectionOrderNo111.setUserId(mmanUserLoanOri.getUserId());
                                mmanLoanCollectionOrderNo111.setOverdueDays(pday);
                                mmanLoanCollectionOrderNo111.setPayId(creditLoanPay.getId());
                                mmanLoanCollectionOrderNo111.setDispatchName(sysName);
                                mmanLoanCollectionOrderNo111.setDispatchTime(now);
                                mmanLoanCollectionOrderNo111.setOperatorName(sysName);
                                mmanLoanCollectionOrderNo111.setRemark(sysRemark);
                                mmanLoanCollectionOrderNo111.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);
                                mmanLoanCollectionOrderNo111.setIdNumber(idNumber);  // 借款人身份证号
                                mmanLoanCollectionOrderNo111List.add(mmanLoanCollectionOrderNo111);


                                Map<String, String> personMap = new HashMap<String, String>();
                                personMap.put("beginDispatchTime", DateUtil.getDateFormat("yyyy-MM-dd 00:00:00"));
                                personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_M1_M2);
                                personMap.put("userStatus", BackConstant.ON);
                                mmanLoanCollectionPersonNo111List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);

                                if (null == mmanLoanCollectionPersonNo111List || mmanLoanCollectionPersonNo111List.isEmpty()) {
                                    logger.error("mmanLoancollectionOrder new dispatch  no man to dispath,enter 1 ,mmanUserLoanOri id:" + mmanUserLoanOri.getId());
                                    SysAlertMsg alertMsg = new SysAlertMsg();
                                    alertMsg.setTitle("分配催收任务失败");
                                    alertMsg.setContent("所有公司M1-M2组查无可用催收人,请及时添加或启用该组催收员。");
                                    alertMsg.setDealStatus(BackConstant.OFF);
                                    alertMsg.setStatus(BackConstant.OFF);
                                    alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                    sysAlertMsgService.insert(alertMsg);
                                    logger.warn("所有公司M1-M2组查无可用催收人...");
                                    continue;
                                }
                            } else {

                                MmanLoanCollectionOrder mmanLoanCollectionOrderOri = mmanLoanCollectionOrderList.get(0);


                                logger.error("mmanLoancollectionOrder redispatch enter 1 ,id:" + mmanLoanCollectionOrderOri.getId());

                                if (BackConstant.XJX_COLLECTION_ORDER_DELETED.equals(mmanLoanCollectionOrderOri.getRenewStatus())) {
                                    logger.error("mmanLoancollectionOrder renewStatus is deleted,do not dispatch,id:" + mmanLoanCollectionOrder.getId());
                                    continue;
                                }

                                if (BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())) {
                                    logger.error("mmanLoancollectionOrder mmanLoanCollectionOrderOri status is success,do not dispatch,id:" + mmanLoanCollectionOrder.getId());
                                    continue;
                                }

                                mmanLoanCollectionOrderOri.setOverdueDays(pday);
                                mmanLoanCollectionOrderOri.setIdNumber(idNumber);  // 借款人身份证
                                int monthDiff = (yearNow * 12 + monthNow) - (yearLoanEnd * 12 + monthLoanEnd);//跨月次数，monthDiff == 0的是今天派过的不能参与

                                /**
                                 * 订单不是逾期第11天升级的时候需要处理特殊情况（针对这种情况，在派单的时候已做标记isSecondDayFlag）
                                 * 即月底最后一天的单子有可能不经过M1-M2，于下个月直接进入M2-M3组
                                 */
                                if (!"10".equals(overdueUpgradeDays)) {
                                    monthDiff = dealwithSpecialMonthDiff(mmanLoanCollectionOrderOri, monthDiff);
                                }

                                if (monthDiff == 0) {
                                    //原订单不在条件内的只需更新逾期天数
                                    manLoanCollectionOrderService.saveMmanLoanCollectionOrder(mmanLoanCollectionOrderOri);
                                    continue;
                                }

                                if (monthDiff == 1) {
                                    logger.error("mmanLoancollectionOrder redispatch enter 1 monthDiff  1 ,id:" + mmanLoanCollectionOrderOri.getId());

                                    if (!BackConstant.XJX_OVERDUE_LEVEL_M1_M2.equals(mmanLoanCollectionOrderOri.getCurrentOverdueLevel())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_PAYING.equals(mmanLoanCollectionOrderOri.getStatus())) {

                                        //1.1.2 已存在未完成且跨月1次的订单，分组升级为M1-M2
                                        mmanLoanCollectionOrderOri.setDispatchName(sysName);
                                        mmanLoanCollectionOrderOri.setDispatchTime(now);
                                        mmanLoanCollectionOrderOri.setS1Flag(null);   // 订单逾期升级  S1flg置为null
                                        mmanLoanCollectionOrderOri.setOperatorName(sysName);
                                        mmanLoanCollectionOrderOri.setRemark(sysPromoteRemark);
                                        mmanLoanCollectionOrderOri.setLastCollectionUserId(mmanLoanCollectionOrderOri.getCurrentCollectionUserId());//上一催收员

                                        //更新聚信立报告申请审核状态为初始状态，下一催收员要看需要重新申请
                                        mmanLoanCollectionOrderOri.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);

                                        mmanLoanCollectionOrderNo112List.add(mmanLoanCollectionOrderOri);


                                        Map<String, String> personMap = new HashMap<String, String>();
                                        personMap.put("beginDispatchTime", DateUtil.getDateFormat("yyyy-MM-dd 00:00:00"));
                                        personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                        personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_M1_M2);
                                        personMap.put("userStatus", BackConstant.ON);

                                        mmanLoanCollectionPersonNo112List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);

                                        if (null == mmanLoanCollectionPersonNo112List || mmanLoanCollectionPersonNo112List.isEmpty()) {

                                            logger.error("mmanLoancollectionOrder redispatch enter 1 no man monthDiff  1 ,id:" + mmanLoanCollectionOrderOri.getId());

                                            SysAlertMsg alertMsg = new SysAlertMsg();
                                            alertMsg.setTitle("分配催收任务失败");
                                            alertMsg.setContent("所有M1-M2组查无可用催收人,请及时添加或启用该组催收员。");
                                            alertMsg.setDealStatus(BackConstant.OFF);
                                            alertMsg.setStatus(BackConstant.OFF);
                                            alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                            sysAlertMsgService.insert(alertMsg);
                                            logger.warn("所有M1-M2组查无可用催收人...");
                                            continue;
                                        }
                                    } else {
                                        //0 只有新单子和升级单子才会派单，本次同等级派过单的不能再重新派单了！！！
                                        manLoanCollectionOrderService.saveMmanLoanCollectionOrder(mmanLoanCollectionOrderOri);
                                        continue;
                                    }

                                } else if (monthDiff == 2) {
                                    logger.error("mmanLoancollectionOrder redispatch enter 1 monthDiff 2 ,id:" + mmanLoanCollectionOrderOri.getId());

                                    if (!BackConstant.XJX_OVERDUE_LEVEL_M2_M3.equals(mmanLoanCollectionOrderOri.getCurrentOverdueLevel())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_PAYING.equals(mmanLoanCollectionOrderOri.getStatus())) {

                                        //1.1.3 已存在未完成且跨月2次的订单，分组升级为M2-M3
                                        mmanLoanCollectionOrderOri.setDispatchName(sysName);
                                        mmanLoanCollectionOrderOri.setDispatchTime(now);
                                        mmanLoanCollectionOrderOri.setS1Flag(null); // 订单逾期升级  S1flg置为null
                                        mmanLoanCollectionOrderOri.setOperatorName(sysName);
                                        mmanLoanCollectionOrderOri.setRemark(sysPromoteRemark);
                                        mmanLoanCollectionOrderOri.setLastCollectionUserId(mmanLoanCollectionOrderOri.getCurrentCollectionUserId());//上一催收员

                                        //更新聚信立报告申请审核状态为初始状态，下一催收员要看需要重新申请
                                        mmanLoanCollectionOrderOri.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);

                                        mmanLoanCollectionOrderNo113List.add(mmanLoanCollectionOrderOri);

                                        Map<String, String> personMap = new HashMap<String, String>();
                                        personMap.put("beginDispatchTime", DateUtil.getDateFormat("yyyy-MM-dd 00:00:00"));
                                        personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                        personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_M2_M3);
                                        personMap.put("userStatus", BackConstant.ON);

                                        mmanLoanCollectionPersonNo113List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);

                                        if (null == mmanLoanCollectionPersonNo113List || mmanLoanCollectionPersonNo113List.isEmpty()) {

                                            logger.error("mmanLoancollectionOrder redispatch enter 1 no man monthDiff  2,id:" + mmanLoanCollectionOrderOri.getId());
                                            SysAlertMsg alertMsg = new SysAlertMsg();
                                            alertMsg.setTitle("分配催收任务失败");
                                            alertMsg.setContent("所有公司M2-M3组查无可用催收人,请及时添加或启用该组催收员。");
                                            alertMsg.setDealStatus(BackConstant.OFF);
                                            alertMsg.setStatus(BackConstant.OFF);
                                            alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                            sysAlertMsgService.insert(alertMsg);
                                            logger.warn("所有公司M2-M3组查无可用催收人...");
                                            continue;
                                        }
                                    } else {
                                        //0 只有新单子和升级单子才会派单，本次同等级派过单的不能再重新派单了！！！
                                        manLoanCollectionOrderService.saveMmanLoanCollectionOrder(mmanLoanCollectionOrderOri);
                                        continue;
                                    }
                                } else if (monthDiff >= 3 && monthDiff < 6) {

                                    logger.error("mmanLoancollectionOrder redispatch enter 1 monthDiff 3 ,id:" + mmanLoanCollectionOrderOri.getId());

                                    if (!BackConstant.XJX_OVERDUE_LEVEL_M3P.equals(mmanLoanCollectionOrderOri.getCurrentOverdueLevel())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_PAYING.equals(mmanLoanCollectionOrderOri.getStatus())) {

                                        //1.1.4 已存在未完成且跨月3次及以上的订单，分组升级为M3+
                                        mmanLoanCollectionOrderOri.setDispatchName(sysName);
                                        mmanLoanCollectionOrderOri.setDispatchTime(now);
                                        mmanLoanCollectionOrderOri.setS1Flag(null);   // 订单逾期升级  S1flg置为null
                                        mmanLoanCollectionOrderOri.setOperatorName(sysName);
                                        mmanLoanCollectionOrderOri.setRemark(sysPromoteRemark);
                                        mmanLoanCollectionOrderOri.setLastCollectionUserId(mmanLoanCollectionOrderOri.getCurrentCollectionUserId());//上一催收员

                                        //更新聚信立报告申请审核状态为初始状态，下一催收员要看需要重新申请
                                        mmanLoanCollectionOrderOri.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);

                                        mmanLoanCollectionOrderNo114List.add(mmanLoanCollectionOrderOri);

                                        Map<String, String> personMap = new HashMap<String, String>();
                                        personMap.put("beginDispatchTime", DateUtil.getDateFormat("yyyy-MM-dd 00:00:00"));
                                        personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                        personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_M3P);
                                        personMap.put("userStatus", BackConstant.ON);

                                        mmanLoanCollectionPersonNo114List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);

                                        if (null == mmanLoanCollectionPersonNo114List || mmanLoanCollectionPersonNo114List.isEmpty()) {
                                            logger.error("mmanLoancollectionOrder redispatch enter 1 no man monthDiff  3,id:" + mmanLoanCollectionOrderOri.getId());

                                            SysAlertMsg alertMsg = new SysAlertMsg();
                                            alertMsg.setTitle("分配催收任务失败");
                                            alertMsg.setContent("所有公司M3+组查无可用催收人,请及时添加或启用该组催收员。");
                                            alertMsg.setDealStatus(BackConstant.OFF);
                                            alertMsg.setStatus(BackConstant.OFF);
                                            alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                            sysAlertMsgService.insert(alertMsg);
                                            logger.warn("所有M3+组查无可用催收人...");
                                            continue;
                                        }
                                    } else {
                                        //0 只有新单子和升级单子才会派单，本次同等级派过单的不能再重新派单了！！！
                                        manLoanCollectionOrderService.saveMmanLoanCollectionOrder(mmanLoanCollectionOrderOri);
                                        continue;
                                    }
                                } else if (monthDiff >= 6) {
                                    logger.error("mmanLoancollectionOrder redispatch enter 1 monthDiff 6 ,id:" + mmanLoanCollectionOrderOri.getId());
                                    if (!BackConstant.XJX_OVERDUE_LEVEL_M6P.equals(mmanLoanCollectionOrderOri.getCurrentOverdueLevel())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())
                                            && !BackConstant.XJX_COLLECTION_ORDER_STATE_PAYING.equals(mmanLoanCollectionOrderOri.getStatus())) {

                                        //1.1.4 已存在未完成且跨月6次及以上的订单，分组升级为M6+
                                        mmanLoanCollectionOrderOri.setDispatchName(sysName);
                                        mmanLoanCollectionOrderOri.setDispatchTime(now);
                                        mmanLoanCollectionOrderOri.setS1Flag(null);   // 订单逾期升级  将S1flg置为null
                                        mmanLoanCollectionOrderOri.setOperatorName(sysName);
                                        mmanLoanCollectionOrderOri.setRemark(sysPromoteRemark);
                                        mmanLoanCollectionOrderOri.setLastCollectionUserId(mmanLoanCollectionOrderOri.getCurrentCollectionUserId());//上一催收员

                                        //更新聚信立报告申请审核状态为初始状态，下一催收员要看需要重新申请
                                        mmanLoanCollectionOrderOri.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);

                                        mmanLoanCollectionOrderNo115List.add(mmanLoanCollectionOrderOri);

                                        Map<String, String> personMap = new HashMap<String, String>();
                                        personMap.put("beginDispatchTime", DateUtil.getDateFormat("yyyy-MM-dd 00:00:00"));
                                        personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                        personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_M6P);
                                        personMap.put("userStatus", BackConstant.ON);

                                        mmanLoanCollectionPersonNo115List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);

                                        if (null == mmanLoanCollectionPersonNo115List || mmanLoanCollectionPersonNo115List.isEmpty()) {
                                            logger.error("mmanLoancollectionOrder redispatch enter 1 no man monthDiff  6,id:" + mmanLoanCollectionOrderOri.getId());

                                            SysAlertMsg alertMsg = new SysAlertMsg();
                                            alertMsg.setTitle("分配催收任务失败");
                                            alertMsg.setContent("所有公司M6+组查无可用催收人,请及时添加或启用该组催收员。");
                                            alertMsg.setDealStatus(BackConstant.OFF);
                                            alertMsg.setStatus(BackConstant.OFF);
                                            alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                            sysAlertMsgService.insert(alertMsg);
                                            logger.warn("所有M6+组查无可用催收人...");
                                            continue;
                                        }
                                    } else {
                                        //0 只有新单子和升级单子才会派单，本次同等级派过单的不能再重新派单了！！！
                                        manLoanCollectionOrderService.saveMmanLoanCollectionOrder(mmanLoanCollectionOrderOri);
                                        continue;
                                    }
                                }
                            }
                        } else {
                            // if (dayNow > 1 && dayNow < 12)
                            logger.error("mmanLoancollectionOrder redispatch enter 11 mmanUserLoanOri ,id:" + mmanUserLoanOri.getId());

                            MmanLoanCollectionOrder mmanLoanCollectionOrder = new MmanLoanCollectionOrder();
                            mmanLoanCollectionOrder.setLoanId(mmanUserLoanOri.getId());
                            List<MmanLoanCollectionOrder> mmanLoanCollectionOrderList = manLoanCollectionOrderService.findList(mmanLoanCollectionOrder);

                            //1.2 若当前为每月2-11号，所有新订单（这时S1、S2手中没有已存在未完成的订单，因为1号都给M1-M2了），分组为S1、S2均摊，原订单只更新逾期天数
                            if (null == mmanLoanCollectionOrderList || mmanLoanCollectionOrderList.isEmpty()) {

                                logger.error("mmanLoancollectionOrder new dispatch enter 11 mmanUserLoanOri ,id:" + mmanUserLoanOri.getId());

                                MmanLoanCollectionOrder mmanLoanCollectionOrderNo12 = new MmanLoanCollectionOrder();
                                mmanLoanCollectionOrderNo12.setLoanId(mmanUserLoanOri.getId());
                                mmanLoanCollectionOrderNo12.setOrderId(mmanUserLoanOri.getLoanPyId());
                                mmanLoanCollectionOrderNo12.setUserId(mmanUserLoanOri.getUserId());
                                mmanLoanCollectionOrderNo12.setOverdueDays(pday);
                                mmanLoanCollectionOrderNo12.setPayId(creditLoanPay.getId());
                                mmanLoanCollectionOrderNo12.setDispatchName(sysName);
                                mmanLoanCollectionOrderNo12.setDispatchTime(now);
                                mmanLoanCollectionOrderNo12.setOperatorName(sysName);
                                mmanLoanCollectionOrderNo12.setRemark(sysRemark);
                                mmanLoanCollectionOrderNo12.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);
                                mmanLoanCollectionOrderNo12.setS1Flag("S1");
                                mmanLoanCollectionOrderNo12.setIdNumber(idNumber);  // 借款人身份证号码

                                // 如果是2号派单（上月最后一天应还的订单），做上标记以做特殊处理,针对T+1情况，即订单逾期第12天升级。
                                if (dayNow == 2) {
                                    if (!"0".equals(channelSwitchingDao.getChannelValue("order_overdue_rule").getChannelValue())) {
                                        mmanLoanCollectionOrderNo12.setSecondDayFlag("1");
                                    }
                                }
                                mmanLoanCollectionOrderNo12List.add(mmanLoanCollectionOrderNo12);


                                Map<String, String> personMap = new HashMap<String, String>();
                                Calendar cal = Calendar.getInstance();
                                cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 2);
                                Date date = cal.getTime();
                                personMap.put("beginDispatchTime", DateUtil.getDateFormat(date,"yyyy-MM-dd 00:00:00"));
                                personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_S1_OR_S2);
                                personMap.put("userStatus", BackConstant.ON);

                                mmanLoanCollectionPersonNo12List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);

                                if (null == mmanLoanCollectionPersonNo12List || mmanLoanCollectionPersonNo12List.isEmpty()) {

                                    logger.error("mmanLoancollectionOrder redispatch enter 11 no man ,mmanUserLoanOri id:" + mmanUserLoanOri.getId());
                                    SysAlertMsg alertMsg = new SysAlertMsg();
                                    alertMsg.setTitle("分配催收任务失败");
                                    alertMsg.setContent("所有公司S1、S2组查无可用催收人,请及时添加或启用该组催收员。");
                                    alertMsg.setDealStatus(BackConstant.OFF);
                                    alertMsg.setStatus(BackConstant.OFF);
                                    alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                    sysAlertMsgService.insert(alertMsg);
                                    logger.warn("所有公司S1、S2组查无可用催收人...");
                                    continue;
                                }
                            } else {

                                //原订单不在条件内的只需更新逾期天数
                                MmanLoanCollectionOrder mmanLoanCollectionOrderOri = mmanLoanCollectionOrderList.get(0);

                                logger.error("mmanLoancollectionOrder enter 2 ,mmanLoanCollectionOrder id:" + mmanLoanCollectionOrderOri.getId());

                                if (BackConstant.XJX_COLLECTION_ORDER_DELETED.equals(mmanLoanCollectionOrderOri.getRenewStatus())) {
                                    logger.error("mmanLoancollectionOrder renewStatus is deleted,do not dispatch,id:" + mmanLoanCollectionOrder.getId());
                                    continue;
                                }

                                if (BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())) {
                                    logger.error("mmanLoancollectionOrder mmanLoanCollectionOrderOri status is success,do not dispatch,id:" + mmanLoanCollectionOrder.getId());
                                    continue;
                                }

                                mmanLoanCollectionOrderOri.setS1Flag(mmanLoanCollectionOrderOri.getS1Flag());

                                mmanLoanCollectionOrderOri.setOverdueDays(pday);
                                mmanLoanCollectionOrderOri.setS1Flag(mmanLoanCollectionOrderOri.getS1Flag());
                                manLoanCollectionOrderService.saveMmanLoanCollectionOrder(mmanLoanCollectionOrderOri);
                                continue;
                            }
                        }
                        /*
                        else {
                            //1.3 若当前为每月12号-月底，订单和所有分组要分多种情况

                            //1.3.1 所有新订单，分组为S1
                            MmanLoanCollectionOrder mmanLoanCollectionOrder = new MmanLoanCollectionOrder();
                            mmanLoanCollectionOrder.setLoanId(mmanUserLoanOri.getId());
                            List<MmanLoanCollectionOrder> mmanLoanCollectionOrderList = manLoanCollectionOrderService.findList(mmanLoanCollectionOrder);
                            if (null == mmanLoanCollectionOrderList || mmanLoanCollectionOrderList.isEmpty()) {

                                logger.error("mmanLoancollectionOrder enter 13 ,mmanUserLoan id:" + mmanUserLoanOri.getId());

                                MmanLoanCollectionOrder mmanLoanCollectionOrderNo131 = new MmanLoanCollectionOrder();
                                mmanLoanCollectionOrderNo131.setLoanId(mmanUserLoanOri.getId());
                                mmanLoanCollectionOrderNo131.setOrderId(mmanUserLoanOri.getLoanPyId());
                                mmanLoanCollectionOrderNo131.setUserId(mmanUserLoanOri.getUserId());
                                mmanLoanCollectionOrderNo131.setOverdueDays(pday);
                                mmanLoanCollectionOrderNo131.setPayId(creditLoanPay.getId());
                                mmanLoanCollectionOrderNo131.setDispatchName(sysName);
                                mmanLoanCollectionOrderNo131.setDispatchTime(now);
//									mmanLoanCollectionOrderNo131.setStatus(Constants.XJX_COLLECTION_ORDER_STATE_WAIT);
                                mmanLoanCollectionOrderNo131.setOperatorName(sysName);
//									mmanLoanCollectionOrderNo131.setOutsideCompanyId(ourCompanyId);
                                mmanLoanCollectionOrderNo131.setRemark(sysRemark);
                                mmanLoanCollectionOrderNo131.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);
                                mmanLoanCollectionOrderNo131.setIdNumber(idNumber);  // 借款人身份证号码
                                mmanLoanCollectionOrderNo131List.add(mmanLoanCollectionOrderNo131);


                                Map<String, String> personMap = new HashMap<String, String>();
                                personMap.put("beginDispatchTime", DateUtil.getDateFormat("yyyy-MM-dd 00:00:00"));
                                personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_S1);
                                personMap.put("userStatus", BackConstant.ON);

                                mmanLoanCollectionPersonNo131List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);


                                if (null == mmanLoanCollectionPersonNo131List || mmanLoanCollectionPersonNo131List.isEmpty()) {
                                    logger.error("mmanLoancollectionOrder enter 13 no man,mmanUserLoan id:" + mmanUserLoanOri.getId());
                                    SysAlertMsg alertMsg = new SysAlertMsg();
                                    alertMsg.setTitle("分配催收任务失败");
                                    alertMsg.setContent("所有公司S1组查无可用催收人,请及时添加或启用该组催收员。");
                                    alertMsg.setDealStatus(BackConstant.OFF);
                                    alertMsg.setStatus(BackConstant.OFF);
                                    alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                    sysAlertMsgService.insert(alertMsg);
                                    logger.warn("所有公司S1组查无可用催收人...");
                                    continue;
                                }
                            } else {

                                MmanLoanCollectionOrder mmanLoanCollectionOrderOri = mmanLoanCollectionOrderList.get(0);

                                logger.error("mmanLoancollectionOrder enter 13 redispatch,mmanLoanCollectionOrder id:" + mmanLoanCollectionOrder.getId());


                                if (BackConstant.XJX_COLLECTION_ORDER_DELETED.equals(mmanLoanCollectionOrderOri.getRenewStatus())) {
                                    logger.error("mmanLoancollectionOrder renewStatus is deleted,do not dispatch,id:" + mmanLoanCollectionOrder.getId());
                                    continue;
                                }

                                if (BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())) {
                                    logger.error("mmanLoancollectionOrder mmanLoanCollectionOrderOri status is success,do not dispatch,id:" + mmanLoanCollectionOrder.getId());
                                    continue;
                                }
                                mmanLoanCollectionOrderOri.setS1Flag(mmanLoanCollectionOrderOri.getS1Flag());
                                mmanLoanCollectionOrderOri.setOverdueDays(pday);

                                //1.3.2 S1中逾期天数 大于11的已存在未完成订单，分组升级为S2(逾期T+1推送)
                                if (isMeetTheConditions(pday, orderUpgradeDay, mmanLoanCollectionOrderOri)) {

                                    logger.error("mmanLoancollectionOrder enter 13 redispatch overdue>11,mmanLoanCollectionOrder id:" + mmanLoanCollectionOrder.getId());

                                    mmanLoanCollectionOrderOri.setDispatchName(sysName);
                                    mmanLoanCollectionOrderOri.setDispatchTime(now);
                                    mmanLoanCollectionOrderOri.setOperatorName(sysName);
                                    mmanLoanCollectionOrderOri.setRemark(sysPromoteRemark);
                                    mmanLoanCollectionOrderOri.setS1Flag(null); // 订单逾期升级  S1flag置为null
                                    mmanLoanCollectionOrderOri.setLastCollectionUserId(mmanLoanCollectionOrderOri.getCurrentCollectionUserId());//上一催收员

                                    //更新聚信立报告申请审核状态为初始状态，下一催收员要看需要重新申请
                                    mmanLoanCollectionOrderOri.setJxlStatus(BackConstant.XJX_JXL_STATUS_REFUSE);

                                    mmanLoanCollectionOrderNo132List.add(mmanLoanCollectionOrderOri);


                                    Map<String, String> personMap = new HashMap<String, String>();
                                    personMap.put("beginDispatchTime", DateUtil.getDateFormat("yyyy-MM-dd 00:00:00"));
                                    personMap.put("endDispatchTime", DateUtil.getDateFormat((DateUtil.getBeforeOrAfter(new Date(), 1)), "yyyy-MM-dd HH:mm:ss"));
                                    personMap.put("groupLevel", BackConstant.XJX_OVERDUE_LEVEL_S2);
                                    personMap.put("userStatus", BackConstant.ON);

                                    mmanLoanCollectionPersonNo132List = backUserDao.findUnCompleteCollectionOrderByCurrentUnCompleteCountListByMap(personMap);

                                    if (null == mmanLoanCollectionPersonNo132List || mmanLoanCollectionPersonNo132List.isEmpty()) {
                                        logger.error("mmanLoancollectionOrder enter 13 overdue>10 no man,mmanUserLoan id:" + mmanUserLoanOri.getId());
                                        SysAlertMsg alertMsg = new SysAlertMsg();
                                        alertMsg.setTitle("分配催收任务失败");
                                        alertMsg.setContent("所有公司S2组查无可用催收人,请及时添加或启用该组催收员。");
                                        alertMsg.setDealStatus(BackConstant.OFF);
                                        alertMsg.setStatus(BackConstant.OFF);
                                        alertMsg.setType(SysAlertMsg.TYPE_COMMON);
                                        sysAlertMsgService.insert(alertMsg);
                                        logger.warn("所有公司S2组查无可用催收人...");
                                        continue;
                                    }
                                } else {
                                    //原订单不在条件内的只需更新逾期天数
                                    manLoanCollectionOrderService.saveMmanLoanCollectionOrder(mmanLoanCollectionOrderOri);
                                }
                            }
                        }
                        */
                    }
                } catch (Exception e) {
                    logger.error("分配当前催收任务出错，借款ID：" + mmanUserLoanOri.getId(), e);
                }

            //2 将订单派到对应分组催收员
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo111List, mmanLoanCollectionPersonNo111List, now);
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo112List, mmanLoanCollectionPersonNo112List, now);
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo113List, mmanLoanCollectionPersonNo113List, now);
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo114List, mmanLoanCollectionPersonNo114List, now);
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo115List, mmanLoanCollectionPersonNo115List, now);
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo12List, mmanLoanCollectionPersonNo12List, now);
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo131List, mmanLoanCollectionPersonNo131List, now);
            mmanLoanCollectionRecordService.assignCollectionOrderToRelatedGroup(mmanLoanCollectionOrderNo132List, mmanLoanCollectionPersonNo132List, now);

        }
        logger.error("TaskJobMiddleService dispatchForLoanId end" + "结束时间 : " + Calendar.getInstance().getTimeInMillis() + "借款id :" + mmanUserLoan.getId());
    }

    /**
     * 判断订单是否满足逾期升级条件（S1--->S2）
     * 1.订单当前逾期等级是否是S1或S2
     * 2.订单逾期天数是否满足逾期天数升级条件
     * 3.检查日志，判断订单本次是否已经处理（升级）
     * 4.当前订单的状态不是续期或者催收成功
     *
     * @param pday
     * @param orderUpgradeDay
     * @param mmanLoanCollectionOrderOri
     * @return
     */
    private boolean isMeetTheConditions(int pday, int orderUpgradeDay, MmanLoanCollectionOrder mmanLoanCollectionOrderOri) {
        return ((BackConstant.XJX_OVERDUE_LEVEL_S1.equals(mmanLoanCollectionOrderOri.getCurrentOverdueLevel())) || (BackConstant.XJX_OVERDUE_LEVEL_S2.equals(mmanLoanCollectionOrderOri.getCurrentOverdueLevel())))
                && pday > orderUpgradeDay && !checkLog(mmanLoanCollectionOrderOri.getOrderId())
                && !BackConstant.XJX_COLLECTION_ORDER_STATE_SUCCESS.equals(mmanLoanCollectionOrderOri.getStatus())
                && !BackConstant.XJX_COLLECTION_ORDER_STATE_PAYING.equals(mmanLoanCollectionOrderOri.getStatus());
    }

    /**
     * 计算订单当前的逾期天数
     *
     * @param mmanUserLoanOri
     * @return
     */
    private int getOverdueDays(MmanUserLoan mmanUserLoanOri) {
        int pday = 0;
        try {
            pday = DateUtil.daysBetween(mmanUserLoanOri.getLoanEndTime(), new Date());
        } catch (ParseException e) {
            logger.error("parse failed", e);
        }
        return pday;
    }

    /**
     * 计算订单罚息
     *
     * @param mmanUserLoanOri
     * @param loanMoney
     * @param pday
     * @return
     */
    private BigDecimal getLoanPenalty(MmanUserLoan mmanUserLoanOri, BigDecimal loanMoney, int pday) {
        BigDecimal pRate = new BigDecimal(Integer.parseInt(mmanUserLoanOri.getLoanPenaltyRate())).divide(new BigDecimal(10000));
//        BigDecimal pRate = new BigDecimal((Double.parseDouble(mmanUserLoanOri.getLoanPenaltyRate()) / 10000));//罚息率
        BigDecimal paidMoney = mmanUserLoanOri.getPaidMoney();  // 借款本金和服务费之和
//        BigDecimal loanMoney = mmanUserLoanOri.getLoanMoney();
        BigDecimal pmoney = null;
        //TODO 第一版本先按照借款金额算滞纳金，第二版用剩余应还本金算滞纳金
        if(mmanUserLoanOri.getBorrowingType().equals(Constant.BIG)){
            pmoney = loanMoney.multiply(new BigDecimal(pday)).multiply(pRate).setScale(2, BigDecimal.ROUND_HALF_UP);
            return pmoney;
        }

        // 计算订单罚息
        try {
            if (paidMoney != null && paidMoney.compareTo(new BigDecimal("0")) > 0) {
                pmoney = (paidMoney.multiply(pRate).multiply(new BigDecimal(pday))).setScale(2, BigDecimal.ROUND_HALF_UP);//逾期金额(部分还款算全罚息  服务费算罚息)
            } else {
                pmoney = (loanMoney.multiply(pRate).multiply(new BigDecimal(pday))).setScale(2, BigDecimal.ROUND_HALF_UP);//逾期金额（部分还款算全罚息  服务费不算罚息)
            }
        } catch (Exception e) {
            logger.error("calculate Penalty error！ loanid =" + mmanUserLoanOri.getId());
        }
        return pmoney;
    }

    /**
     * 获取当前规则下订单逾期升级的天数
     *
     * @param overdueUpgradeDays
     * @return
     * @throws Exception
     */
    private int getOrderUpgradeDay(String overdueUpgradeDays) throws Exception {
        int orderUpgradeDay;
        if (StringUtils.isNotEmpty(overdueUpgradeDays)) {
            orderUpgradeDay = Integer.valueOf(JedisDataClient.get("OVERDUE_UPGRADE_DAY"));
        } else {
            ChannelSwitching orderOverdueRule = channelSwitchingDao.getChannelValue("order_overdue_rule");
            String channelValue = orderOverdueRule.getChannelValue();
            orderUpgradeDay = Integer.valueOf(channelValue) + 10;
            JedisDataClient.set("OVERDUE_UPGRADE_DAY", String.valueOf(orderUpgradeDay), 60 * 60 * 6);
        }
        return orderUpgradeDay;
    }


    /**
     * 受T+1规则影响，原月底最后一天的单子会在下月不经过M1-M2直接进入M2-M3组(这个不合理)，针对这种特殊情况加以处理。
     *
     * @param mmanLoanCollectionOrderOri
     * @param monthDiff
     * @return
     */
    private int dealwithSpecialMonthDiff(MmanLoanCollectionOrder mmanLoanCollectionOrderOri, int monthDiff) {
        if ("1".equals(mmanLoanCollectionOrderOri.getSecondDayFlag())) {
            monthDiff = monthDiff - 1;
        }
        return monthDiff;
    }


    /**
     * 根据派单订单id,查询是否有系统逾期升级至S2的转派日志
     *
     * @param loanCollectionOrderId
     * @return
     */
    public boolean checkLog(String loanCollectionOrderId) {
        HashMap<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("collectionOrderId", loanCollectionOrderId);
        int count = null == mmanLoanCollectionStatusChangeLogDao.findSystemUpToS2Log(paraMap) ? 0 : mmanLoanCollectionStatusChangeLogDao.findSystemUpToS2Log(paraMap).intValue();

        if (count > 0) {
            return true;
        }
        return false;
    }

    /**
     * 分配催收任务，更新催收相关操作(更新催收订单，添加流转日志，更新借款、还款逾期额天数状态等)
     */
//    public void autoDispatch() {
//        logger.error("TaskJobMiddleService autoDispatch start");
//        //初始化参数
//
//        MmanUserLoan mmanUserLoan = new MmanUserLoan();
//        mmanUserLoan.setLoanStatus(BackConstant.CREDITLOANAPPLY_OVERDUE);
//        //mmanUserLoan.setCreateTime(new Date());
//        List<MmanUserLoan> overdueList = manUserLoanService.findMmanUserLoanList2(mmanUserLoan);
//        logger.error("overdueList size:" + overdueList.size());
//
//        if (null != overdueList && overdueList.size() > 0) {
//            for (MmanUserLoan mmanUserLoanOri : overdueList) {
//                MmanLoanCollectionOrder order = manLoanCollectionOrderService.getOrderByLoanId(mmanUserLoanOri.getId());
//                if (order != null) {
//                    dispatchforLoanId(mmanUserLoanOri.getId(), order.getIdNumber());
//                } else {
//                    logger.error("处理逾期数据失败，原因：借款订单对象为空，借款id" + mmanUserLoanOri.getId());
//                }
//            }
//        }
//        logger.error("TaskJobMiddleService autoDispatch end");
//    }
}

