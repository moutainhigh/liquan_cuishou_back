package com.info.back.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.info.back.result.JsonResult;
import com.info.web.pojo.*;
import com.info.web.util.PageConfig;

public interface IMmanLoanCollectionRecordService {

	/**
	 * 催款记录分页
	 * 
	 * @param params
	 * @return
	 */
	 PageConfig<MmanLoanCollectionRecord> findPage(
			HashMap<String, Object> params);

	/**
	 * 根据主键查询一条催收记录
	 * 
	 * @return 查询到的催收记录对象
	 */
	 MmanLoanCollectionRecord getOne(HashMap<String, Object> params);

	/**
	 * 添加一条催收记录
	 * 
	 * @param record
	 *            要添加的记录对象
	 */
	 void insert(MmanLoanCollectionRecord record);

	/**
	 * 更新一条催收记录
	 * 
	 * @param record
	 *            要更新的记录对象
	 */
	 void update(MmanLoanCollectionRecord record);

	/**
	 * 根据订单号查询历史
	 * 
	 * @return
	 */
	 List<MmanLoanCollectionRecord> findListRecord(HashMap<String,Object> param);

	 void assignCollectionOrderToRelatedGroup(
			List<MmanLoanCollectionOrder> mmanLoanCollectionOrderList,
			List<MmanLoanCollectionPerson> mmanLoanCollectionPersonList,
			Date date);
 
	 JsonResult saveCollection(Map<String, String> params,
			BackUser backUser);

	/**
	 * 发起代扣
	 * 
	 * @param params
	 * @return
	 */
	 JsonResult xjxWithholding(Map<String, String> params);

	/**
	 * 转派
	 * 
	 * @param user
	 * @param mmanLoanCollectionOrder
	 * @return
	 */
	 JsonResult batchDispatch(BackUser user,
			MmanLoanCollectionOrder mmanLoanCollectionOrder);

	/**
	 * 根据订单号查询代扣记录
	 * 
	 * @param id
	 * @return
	 */
	 List<CollectionWithholdingRecord> findWithholdRecord(String id);

	/**
	 *  创建分期还款业务
	 * @param list
	 * @param mmanLoanCollectionOrderOri
     * @return
     */
	 JsonResult insertInstallmentPayRecord(List<InstallmentPayInfoVo> list,MmanLoanCollectionOrder mmanLoanCollectionOrderOri);


	/**
	 * 根据订单ID查分期记录
	 * @param id
	 * @return
     */
	 List<InstallmentPayRecord> findInstallmentList(String id);

	/**
	 * 处理分期付款
	 * @param params
	 * @return
     */
	 JsonResult fqWithholding(Map<String, String> params);

    void saveCollectionRecord(HashMap<String, Object> params, BackUser user);
}
