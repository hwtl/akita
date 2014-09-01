package org.akita.taobao;

import org.akita.annotation.AkAPI;
import org.akita.annotation.AkGET;
import org.akita.annotation.AkParam;
import org.akita.annotation.AkSignature;
import org.akita.exception.AkInvokeException;
import org.akita.exception.AkServerStatusException;

/**
 * Created with IntelliJ IDEA.
 * User: justinyang
 * Date: 13-2-17
 * Time: PM3:32
 *
 * eg.
 * http://gw.api.taobao.com/router/rest?sign=719E9C5DCF2E1423702EC66822DE22E4
 * &timestamp=2013-02-17+15%3A44%3A42
 * &v=2.0
 * &app_key=12129701
 * &method=taobao.item.get
 * &partner_id=top-apitools
 * &format=json
 * &num_iid=16788418057
 * &fields=detail_url,num_iid,title
 *
 */
public interface MTopAPI {
    @AkGET
    @AkSignature(using = MTopAPISignature.class)
    @AkAPI(url="http://api.m.taobao.com/rest/api3.do")
    String mtop_production(
            @AkParam("ecode") String ecode,
            @AkParam("appSecret") String appSecret,
            @AkParam("appKey") String appKey,
            @AkParam("appVersion") String appVersion,
            @AkParam("api") String api,
            @AkParam("v") String v,
            @AkParam("ttid") String ttid,
            @AkParam("imsi") String imsi,
            @AkParam("imei") String imei,
            @AkParam("t") long t,
            @AkParam(value = "data", encode = "utf8") String data,
            @AkParam(value = "ext", encode = "utf8") String ext,
            @AkParam("sid") String sid,
            @AkParam("authType") String authType
    ) throws AkInvokeException,  AkServerStatusException;

    @AkGET
    @AkSignature(using = MTopAPISignature.class)
    @AkAPI(url="http://api.wapa.taobao.com/rest/api3.do")
    String mtop_predeploy(
            @AkParam("ecode") String ecode,
            @AkParam("appSecret") String appSecret,
            @AkParam("appKey") String appKey,
            @AkParam("appVersion") String appVersion,
            @AkParam("api") String api,
            @AkParam("v") String v,
            @AkParam("ttid") String ttid,
            @AkParam("imsi") String imsi,
            @AkParam("imei") String imei,
            @AkParam("t") long t,
            @AkParam(value = "data", encode = "utf8") String data,
            @AkParam(value = "ext", encode = "utf8") String ext,
            @AkParam("sid") String sid,
            @AkParam("authType") String authType
    ) throws AkInvokeException,  AkServerStatusException;

    @AkGET
    @AkSignature(using = MTopAPISignature.class)
    //@AkAPI(url="http://10.232.127.67/rest/api3.do")
    @AkAPI(url="http://api.waptest.taobao.com/rest/api3.do")
    String mtop_daily(
            @AkParam("ecode") String ecode,
            @AkParam("appSecret") String appSecret,
            @AkParam("appKey") String appKey,
            @AkParam("appVersion") String appVersion,
            @AkParam("api") String api,
            @AkParam("v") String v,
            @AkParam("ttid") String ttid,
            @AkParam("imsi") String imsi,
            @AkParam("imei") String imei,
            @AkParam("t") long t,
            @AkParam(value = "data", encode = "utf8") String data,
            @AkParam(value = "ext", encode = "utf8") String ext,
            @AkParam("sid") String sid,
            @AkParam("authType") String authType
    ) throws AkInvokeException,  AkServerStatusException;
}
