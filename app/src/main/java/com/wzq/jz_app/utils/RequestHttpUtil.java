package com.wzq.jz_app.utils;

import android.util.JsonReader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wzq.jz_app.utils.HttpUtils;

public class RequestHttpUtil{

    String baseurl = "https://pycloud.bmob.cn/";
    String url;
    static Long sleepTime = 1000L;
    private static String LatAndLonUrl = "http://api.map.baidu.com/geocoder?output=json&location=";

    /*
    http://api.map.baidu.com/geocoder?output=json&location=34,108.379763&ak=esNPFDwwsXWtsQfw4NMNmur1
     */

    /**
     * 传入经纬度返回地址字符串
     * @param lat
     * @param lon
     * @return address
     * @auther moon
     * t
     */
    public static String getlocateGetByLatAndLon(Long lat, Long lon){
        String getUrl = LatAndLonUrl+lat+","+lon + "&ak=esNPFDwwsXWtsQfw4NMNmur1";
        SonThread t = new SonThread(getUrl);
        Thread thread = new Thread(t);
        thread.start();
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = JSONObject.parseObject(t.res);
        JSONObject result = jsonObject.getJSONObject("result");
        String address = result.getString("formatted_address");
        return address;
    }

    public RequestHttpUtil(String key, String function_name, String params){
        this.url = this.baseurl + key + "/" + function_name;
        if(params != null){
            this.url += "?" + params;
        }
        if(function_name.equals("createFamilyGroup"))
            sleepTime =5000L;
    }
    public String run(){
        SonThread t = new SonThread(this.url);
        Thread thread = new Thread(t);
        thread.start();
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return t.res;
    }
}

class SonThread implements Runnable{
    public String res =null;
    public String url;
    public SonThread(String url){
        this.url = url;
    }
    @Override
    public void run() {
        String re = HttpUtils.request(url);
        res = re;
    }
}
