package com.wzq.jz_app.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.wzq.jz_app.R;
import com.wzq.jz_app.base.BaseActivity;
import com.wzq.jz_app.model.bean.remote.MyUser;
import com.wzq.jz_app.utils.DataClearUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;

public class CreateFamilyActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextInputLayout family_input_name;
    private Button family_confirm_create_button;
    private MyUser currentUser;

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        currentUser = BmobUser.getCurrentUser(MyUser.class);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        toolbar = findViewById(R.id.toolbar);
        family_input_name = findViewById(R.id.input_family_name);
        family_confirm_create_button = findViewById(R.id.button_confirm_create);
        //初始化Toolbar
        toolbar.setTitle("家庭");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void initClick() {
        super.initClick();
        family_confirm_create_button.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_family;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_confirm_create:
                showDialogCreate();
                break;
        }
    }

    private void showDialogCreate(){
        String family_name = this.family_input_name.getEditText().getText().toString();
        if(family_name.length() <= 1){
            new MaterialDialog.Builder(mContext)
                    .title("创建失败,群组名长度需大于2")
                    .negativeText("确定")
                    .show();
        }else{

            new MaterialDialog.Builder(mContext)
                    .title("是否确定创建家庭群组")
                    .positiveText("确定")
                    .onPositive((dialog, which) -> {
                        AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
                        JSONObject params = new JSONObject();
                        try {
                            params.put("hostname", currentUser.getUsername());
                            params.put("groupName",family_name);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        System.out.println(params);
                        ace.callEndpoint("createFamilyGroup", params, new CloudCodeListener() {
                            @Override
                            public void done(Object object, BmobException e) {
                                if (e == null) {
                                    String result = object.toString();
                                    System.out.println(result);
                                } else {
                                    Log.e(TAG, " " + e.getMessage());
                                }
                            }
                        });
                    })
                    .negativeText("取消")
                    .show();
        }

    }
}
