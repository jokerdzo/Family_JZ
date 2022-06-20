package com.wzq.jz_app.utils;

import com.wzq.jz_app.utils.HttpUtils;

public class RequestHttpUtil{

    String baseurl = "https://pycloud.bmob.cn/";
    String url;

    public RequestHttpUtil(String key, String function_name, String params){
        this.url = this.baseurl + key + "/" + function_name;
        if(params != null){
            this.url += "?" + params;
        }
    }
    public String run(){
        SonThread t = new SonThread(this.url);
        Thread thread = new Thread(t);
        thread.start();
        try {
            Thread.sleep(1000);
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
