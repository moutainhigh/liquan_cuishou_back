package com.info.web.pojo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 类描述：
 * 创建人：yyf
 * 创建时间：2018/4/19 0019上午 10:28
 */

public class PersonStatistics {
    private int id;
    private int backUserId;//催收员id
    private String roleId;//角色id
    private String companyId;//公司id
    private String groupLevel; //催收组
    private String backUserName;//催收员姓名
    private BigDecimal totalPrincipal;//总本金
    private BigDecimal realgetTotalPrincipal;//已还本金
    private BigDecimal remainPrincipal; //剩余本金
    private BigDecimal repaymentProbability;//本金还款率
    private BigDecimal totalPenalty;//总滞纳金
    private BigDecimal realgetTotalPenalty;//已还滞纳金
    private BigDecimal remainPenalty;//剩余滞纳金
    private BigDecimal penaltyProbability;//滞纳金还款率
    private int totalOrderCount;//总订单数
    private int undoneOrderCount;//未还订单数
    private int doneOrderCount;//完成订单数
    private BigDecimal orderProbability;//订单完成率
    private Date createDate;
    private String companyName;
    private String merchantNo;
    private int cleanOrderCount;
    private BigDecimal cleanPrincipal; //结清本金
    private BigDecimal cleanPenalty; //结清滞纳金
    private BigDecimal cleanPrincipalProbability; // 本金结清率
    private BigDecimal cleanPenaltyProbability; //滞纳金结清率

    public BigDecimal getCleanPrincipalProbability() {
        return cleanPrincipalProbability;
    }

    public void setCleanPrincipalProbability(BigDecimal cleanPrincipalProbability) {
        this.cleanPrincipalProbability = cleanPrincipalProbability;
    }

    public BigDecimal getCleanPenaltyProbability() {
        return cleanPenaltyProbability;
    }

    public void setCleanPenaltyProbability(BigDecimal cleanPenaltyProbability) {
        this.cleanPenaltyProbability = cleanPenaltyProbability;
    }

    public int getCleanOrderCount() {
        return cleanOrderCount;
    }

    public void setCleanOrderCount(int cleanOrderCount) {
        this.cleanOrderCount = cleanOrderCount;
    }

    public BigDecimal getCleanPrincipal() {
        return cleanPrincipal;
    }

    public void setCleanPrincipal(BigDecimal cleanPrincipal) {
        this.cleanPrincipal = cleanPrincipal;
    }

    public BigDecimal getCleanPenalty() {
        return cleanPenalty;
    }

    public void setCleanPenalty(BigDecimal cleanPenalty) {
        this.cleanPenalty = cleanPenalty;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBackUserId() {
        return backUserId;
    }

    public void setBackUserId(int backUserId) {
        this.backUserId = backUserId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getBackUserName() {
        return backUserName;
    }

    public void setBackUserName(String backUserName) {
        this.backUserName = backUserName;
    }

    public BigDecimal getTotalPrincipal() {
        return totalPrincipal;
    }

    public void setTotalPrincipal(BigDecimal totalPrincipal) {
        this.totalPrincipal = totalPrincipal;
    }

    public BigDecimal getRealgetTotalPrincipal() {
        return realgetTotalPrincipal;
    }

    public void setRealgetTotalPrincipal(BigDecimal realgetTotalPrincipal) {
        this.realgetTotalPrincipal = realgetTotalPrincipal;
    }

    public BigDecimal getRemainPrincipal() {
        return remainPrincipal;
    }

    public void setRemainPrincipal(BigDecimal remainPrincipal) {
        this.remainPrincipal = remainPrincipal;
    }

    public BigDecimal getRepaymentProbability() {
        return repaymentProbability;
    }

    public void setRepaymentProbability(BigDecimal repaymentProbability) {
        this.repaymentProbability = repaymentProbability;
    }

    public BigDecimal getTotalPenalty() {
        return totalPenalty;
    }

    public void setTotalPenalty(BigDecimal totalPenalty) {
        this.totalPenalty = totalPenalty;
    }

    public BigDecimal getRealgetTotalPenalty() {
        return realgetTotalPenalty;
    }

    public void setRealgetTotalPenalty(BigDecimal realgetTotalPenalty) {
        this.realgetTotalPenalty = realgetTotalPenalty;
    }

    public BigDecimal getRemainPenalty() {
        return remainPenalty;
    }

    public void setRemainPenalty(BigDecimal remainPenalty) {
        this.remainPenalty = remainPenalty;
    }

    public BigDecimal getPenaltyProbability() {
        return penaltyProbability;
    }

    public void setPenaltyProbability(BigDecimal penaltyProbability) {
        this.penaltyProbability = penaltyProbability;
    }

    public int getTotalOrderCount() {
        return totalOrderCount;
    }

    public void setTotalOrderCount(int totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }

    public int getUndoneOrderCount() {
        return undoneOrderCount;
    }

    public void setUndoneOrderCount(int undoneOrderCount) {
        this.undoneOrderCount = undoneOrderCount;
    }

    public int getDoneOrderCount() {
        return doneOrderCount;
    }

    public void setDoneOrderCount(int doneOrderCount) {
        this.doneOrderCount = doneOrderCount;
    }

    public BigDecimal getOrderProbability() {
        return orderProbability;
    }

    public void setOrderProbability(BigDecimal orderProbability) {
        this.orderProbability = orderProbability;
    }

    public String getGroupLevel() {
        return groupLevel;
    }

    public void setGroupLevel(String groupLevel) {
        this.groupLevel = groupLevel;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
