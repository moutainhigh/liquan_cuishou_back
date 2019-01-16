package com.info.back.utils;

import com.info.config.PayContents;

/**
 * 类描述：
 * 创建人：yyf
 * 创建时间：2018/10/23 0023下午 05:32
 */
public class MerchantNoUtils {

    public static String getMerchantName(){
        String merchantName = "";
        String merchantNo = PayContents.MERCHANT_NUMBER.toString();
        if ("cjxjx".equals(merchantNo)){
            merchantName = "现金侠催收系统";
        }else if ("jxx".equals(merchantNo)){
            merchantName = "金小侠催收系统";
        }else if ("jyb".equals(merchantNo)){
            merchantName = "急用帮催收系统";
        }else if("jqb".equals(merchantNo)){
            merchantName = "极速币下催收系统";
        }
        return merchantName;
    }

    public static int getMerchantNum(){
        int merchantNum = 0;
        String merchantNo = PayContents.MERCHANT_NUMBER.toString();
        if ("cjxjx".equals(merchantNo)){
            merchantNum = 0;
        }else if ("jxx".equals(merchantNo)){
            merchantNum = 1001;
        }else if ("jyb".equals(merchantNo)){
            merchantNum = 1000;
        }else if("jqb".equals(merchantNo)){
            merchantNum = 1002;
        }
        return merchantNum;
    }

    public static String getMerchantNo(String merchantNo){
        String merchantNoTemp = "cjxjx";
        if ("0".equals(merchantNo)){
            merchantNoTemp = "cjxjx";
        }else if ("1".equals(merchantNo)){
            merchantNoTemp = "kjqb";
        }else if ("2".equals(merchantNo)){
            merchantNoTemp = "jxyp";
        }else if("1000".equals(merchantNo)){
            merchantNoTemp = "jyb";
        }else if("1001".equals(merchantNo)){
            merchantNoTemp = "jxx";
        }else if("1002".equals(merchantNo)){
            merchantNoTemp = "jqb";
        }else if("1005".equals(merchantNo)){
            merchantNoTemp = "ymjk";
        }else if("1006".equals(merchantNo)){
            merchantNoTemp = "txlc";
        }
        return merchantNoTemp;
    }

    public static String getMerchantNoByMerchantName(String merchantName){
        String merchantNoTemp = "0";
        merchantName = merchantName.trim();
        if ("极速现金侠".equals(merchantName) || "极速金掌柜".equals(merchantName)){
            merchantNoTemp = "0";
        }else if ("快借钱包".equals(merchantName)){
            merchantNoTemp = "1";
        }else if ("匠心有品".equals(merchantName)){
            merchantNoTemp = "2";
        }else if("急用帮".equals(merchantName)){
            merchantNoTemp = "1000";
        }else if("金小侠".equals(merchantName)){
            merchantNoTemp = "1001";
        }else if("借钱呗".equals(merchantName) || "极速币下".equals(merchantName)){
            merchantNoTemp = "1002";
        }else if("易秒借款".equals(merchantName)){
            merchantNoTemp = "1005";
        }else if("天下粮仓".equals(merchantName)){
            merchantNoTemp = "1006";
        }
        return merchantNoTemp;
    }

    public static String getMerchantName2(String merchantNo){
        String merchantName = "";
        if ("0".equals(merchantNo) || "xianjinxia".equals(merchantNo)  || "cjxjx".equals(merchantNo)  || "null".equals(merchantNo)){
            merchantName = "极速金掌柜";
        }else if ("1".equals(merchantNo)){
            merchantName = "快借钱包";
        }
        else if ("2".equals(merchantNo)){
            merchantName = "匠心有品";
        }else if ("1000".equals(merchantNo)){
            merchantName = "急用帮";
        }else if ("1001".equals(merchantNo)){
            merchantName = "金小侠";
        }else if("1002".equals(merchantNo)){
            merchantName = "极速币下";
        }else if("1005".equals(merchantNo)){
            merchantName = "易秒借款";
        }else if("1006".equals(merchantNo)){
            merchantName = "天下粮仓";
        }
        return merchantName;
    }
}
