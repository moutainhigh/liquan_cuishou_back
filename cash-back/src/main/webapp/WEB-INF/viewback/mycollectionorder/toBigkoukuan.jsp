<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    String path = request.getContextPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>大额订单催收代扣</title>
</head>
<body>
<div class="pageContent">
    <form id="frm" method="post" enctype="multipart/form-data" action="collectionOrder/kokuan" onsubmit="return validateCallback(this, dialogAjaxDone);"
          class="pageForm required-validate">
        <input type="hidden" name="parentId" value="${params.parentId}"/>
        <input type="hidden" name="id" id="id" value="${params.id }">
        <input type="hidden" name="borrowingType" id="borrowingType" value="1">
        <div class="pageFormContent" layoutH="65" style="overflow: auto;">
            <dl>
                <dt style="width: 80px;">
                    <label>
                        代扣金额:
                    </label>
                </dt>
                <dd>
                    <input type="text" name="payMoney" readonly="readonly" maxlength="16" class="number" value="${totalPayMonery}">
                </dd>
            </dl>
        </div>
        <div><span style="color: #cd0a0a"><span style="color: #1b8d0f">温馨提示：</span>大额代扣暂时不支持部分扣款，部分扣款功能开发中...</span></div>
        <div class="formBar">
            <ul>
                <li>
                    <div class="buttonActive">
                        <div class="buttonContent">
                            <button type="submit">
                                扣款
                            </button>
                        </div>
                    </div>
                </li>
                <li>
                    <div class="button">
                        <div class="buttonContent">
                            <button type="button" class="close">
                                取消
                            </button>
                        </div>
                    </div>
                </li>
            </ul>
        </div>
    </form>
</div>
</body>
</html>