package com.wzq.jz_app;

import android.app.Application;
import android.content.Context;

import com.wzq.jz_app.model.bean.remote.MyUser;
import com.wzq.jz_app.net.ApiService;
import com.wzq.jz_app.net.RetrofitHelper;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

/**
 * 作者：wzq
 * 邮箱：wang_love152@163.com
 */
public class MyApplication extends Application {

    public static MyApplication application;
    private static Context context;
    private static MyUser currentUser;
    private ApiService apiService;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        context = getApplicationContext();
        //初始化Bmob后端云
        Bmob.initialize(getContext(),"63561b8d321d84de6be0c17f2faab05e");
//        Bmob.initialize(getContext(),"696f47420555481bd4650182b8ad71ba");

          currentUser = BmobUser.getCurrentUser(MyUser.class);
    }
    public ApiService getApiService(){
        if (apiService == null) {
            apiService = new RetrofitHelper().getApiService();
        }
        return apiService;
    }

    /**
     * 获取上下文
     * @return
     */
    public static Context getContext() {
        return context;
    }

    /**
     * 获取用户id
     * @return
     */
    public static String getCurrentUserId() {
        currentUser = BmobUser.getCurrentUser(MyUser.class);
        if (currentUser == null)
            return null;
        return BmobUser.getCurrentUser(MyUser.class).getObjectId();
    }
}
