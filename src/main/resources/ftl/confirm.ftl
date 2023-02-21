<html lang="en">

<body>
<div id="app">
    <div style="margin: 20px;">
        <div>
            <p style="font-weight: bold;">附件1:确认书</p>
            <p style="font-size: 18px; margin-top: 10px;text-align: center;">确认书</p>
            <p style=" text-align: center;margin-left: 15px;">编号：<span>${contractNo}<span></span></p>
            <div style="font-size: 18px;margin-top: 10px;font-weight: bold;">
                <p>致：<span>常州某个有限公司<span></p>
                <p style="margin-top: 5px;">&nbsp; &nbsp; &nbsp; &nbsp; 我公司确认以下经销商融资信息，纳入我司及贵司签署的编号为<span
                            style="padding: 5px;text-decoration: underline;">${contractNo}</span>号《业务合作协议》内，并保证我司与如下经销商之间的应收账款真实合法。
                </p>
                <table border="1" style="width: 100%;margin-top: 5px;border-collapse:collapse;border-spacing: 0;">
                    <tr>
                        <th style="padding:10px">序号</th>
                        <th style="padding:10px">经销商</th>
                        <th style="padding:10px">协议编号</th>
                        <th style="padding:10px">提货/服务费金额</th>
                        <th style="padding:10px">经销商融资额</th>
                        <th style="padding:10px">应付金额</th>
                        <th style="padding:10px">到期日</th>
                    </tr>
                    <#list list as item>
                        <tr>
                            <td style="padding:10px">${item.number}</td>
                            <td style="padding:10px">${item.name}</td>
                            <td style="padding:10px">${item.contractNo}</td>
                            <td style="padding:10px">${item.servicePrice}</td>
                            <td style="padding:10px">${item.financePrice}</td>
                            <td style="padding:10px">${item.payPrice}</td>
                            <td style="padding:10px">${item.dueDate}</td>
                        </tr>
                    </#list>
                </table>
            </div>
            <div style="margin-top:50px;float:right;text-align: right;font-size: 18px;">
                <p>确认人（盖章）：<span>江苏某个有限公司</span></p>
                <p>${seal}</p>
                <div style="height: 100px;">
                    <p style="margin-top: 5px;">
                        <span style="padding: 10px;"> ${sealDate}</span>
                    </p>
                </div>
            </div>
        </div>
    </div>
</body>

</html>