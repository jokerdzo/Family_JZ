package com.wzq.jz_app.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.wzq.jz_app.R;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

/**
 * 作者：wzq on 2019/4/10.
 * 邮箱：wang_love152@163.com
 * 启动页
 */

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launch);

        View target=findViewById(R.id.launch);
        //第一个参数--target：你要对那个View绑定动画，今天我们要对ImageView绑定动画
        //第二个参数---propertyName:你要执行什么动画---动画的属性名称
        //缩放动画：scaleX
        //渐变动画：alpha
        //第三个参数--动画变化范围（例如：缩放动画变化范围0.0-1.0之间）
        ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(target,"alpha",0.5f,1.0f);
//        //设置动画执行的时间（企业级开发标准：执行时间一般情况2-3秒）
        objectAnimator.setDuration(1000);
//        //启动动画
        objectAnimator.start();

        //扩展知识点---设计模式---适配器模式
        //项目开发需要定义很多的接口
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startActivity(new Intent(LaunchActivity.this,MainActivity1.class));
                finish();
            }
        });

        /**
         * 在登录的过程中直接和bmob建立连接，之后的其他调用云函数操作就可以不用连接了
         */
        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
         //第一个参数是云函数的方法名称，第二个参数是上传到云函数的参数列表（JSONObject cloudCodeParams），第三个参数是回调类
        ace.callEndpoint("keepConnected", null, new CloudCodeListener() {
            @Override
            public void done(Object object, BmobException e) {
                if (e == null) {
                    String result = object.toString();
                    System.out.println(result);
                } else {
                    //Log.e(TAG, " " + e.getMessage());
                    System.out.println("错误信息如下：");
                    System.out.println(e.getMessage());
                }
            }
        });




    }
}
