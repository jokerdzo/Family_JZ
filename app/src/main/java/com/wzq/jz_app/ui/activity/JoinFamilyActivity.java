package com.wzq.jz_app.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.wzq.jz_app.R;
import com.wzq.jz_app.base.BaseActivity;
import com.wzq.jz_app.model.bean.remote.MyUser;
import com.wzq.jz_app.utils.RequestHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;


/**
 * 加入家庭群组activity
 * 这里复用了activity_create_family.xml
 */
public class JoinFamilyActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private TextView join_family_textview;
    private TextInputEditText join_family_input_tips;
    private TextInputLayout input_invite_code;
    private Button family_confirm_join_button;
    private MyUser currentUser;
    private final String secret_key = "2e9985b26398a3a5";

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        currentUser = BmobUser.getCurrentUser(MyUser.class);
    }

    @Override
    protected void initWidget() {
        super.initWidget();
        toolbar = findViewById(R.id.toolbar);
        input_invite_code = findViewById(R.id.input_family_name);
        family_confirm_join_button = findViewById(R.id.button_confirm_create);
        join_family_textview = findViewById(R.id.create_family_textview);
        join_family_input_tips = findViewById(R.id.create_family_input_tips);
        //初始化Toolbar
        toolbar.setTitle("家庭");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        join_family_textview.setText("加入家庭群组");
        join_family_input_tips.setHint("请输入邀请码");
        family_confirm_join_button.setText("加入");
        input_invite_code.setCounterEnabled(false);
    }

    @Override
    protected void initClick() {
        super.initClick();
        family_confirm_join_button.setOnClickListener(this);
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
                showDialogJoin();
                break;
        }
    }

    private void showDialogJoin(){
        String invite_code = this.input_invite_code.getEditText().getText().toString();
        RequestHttpUtil request_http_join_function =
                new RequestHttpUtil(this.secret_key,
                        "joinFamilyGroup",
                        "inviteCode=" + invite_code + "&userObjectId=" + currentUser.getObjectId());
        String join_result = request_http_join_function.run();
        System.out.println(join_result);
        if(join_result.equals("用户加入家庭成功")){
            new MaterialDialog.Builder(mContext)
                    .title("您已成为家庭的一员")
                    .negativeText("确定")
                    .show();
        }else{
            new MaterialDialog.Builder(mContext)
                    .title("加入家庭失败")
                    .negativeText("确定")
                    .show();
        }

    }
}
