<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    String path = request.getContextPath();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=7"/>
    <title>还款二维码</title>

</head>
<body>

<div class="pageContent">
    <form id="frm" method="post" enctype="multipart/form-data" action="" onsubmit="return validateCallback(this, dialogAjaxDone);">
        <input type="hidden" name="parentId" value="${params.myId}"/>
        <div class="pageFormContent" layoutH="50" style="overflow: auto;">
            <span style="font-size: 14px; color: red ">&nbsp;&nbsp;&nbsp;借款人： ${loanUserName}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;借款平台：${merchantNo}</span>
            <img height="300" width="300" id="qrCode" src="collectionOrder/getQrCode?payId=${payId}" alt="该订单可能正在还款中，请稍等">
        </div>
        <div class="formBar">
            <ul>
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
<script type="text/javascript">

    $(function () {
        $.pdialog.resizeDialog({style: {width: 330, height: 420}}, $.pdialog.getCurrent(), "");
    });

</script>
