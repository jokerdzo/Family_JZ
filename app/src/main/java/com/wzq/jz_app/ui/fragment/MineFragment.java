package com.wzq.jz_app.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.wzq.jz_app.R;
import com.wzq.jz_app.base.BaseFragment;
import com.wzq.jz_app.common.Constants;
import com.wzq.jz_app.model.bean.local.BBill;
import com.wzq.jz_app.model.bean.local.BSort;
import com.wzq.jz_app.model.bean.local.NoteBean;
import com.wzq.jz_app.model.bean.remote.MyUser;
import com.wzq.jz_app.model.repository.BmobRepository;
import com.wzq.jz_app.model.repository.LocalRepository;
import com.wzq.jz_app.ui.activity.AboutActivity;
import com.wzq.jz_app.ui.activity.CreateFamilyActivity;
import com.wzq.jz_app.ui.activity.JoinFamilyActivity;
import com.wzq.jz_app.ui.activity.LoginActivity;
import com.wzq.jz_app.ui.activity.MainActivity1;
import com.wzq.jz_app.ui.activity.SettingActivity;
import com.wzq.jz_app.ui.activity.SortActivity;
import com.wzq.jz_app.ui.activity.UserInfoActivity;
import com.wzq.jz_app.ui.adapter.FamilyAdapter;
import com.wzq.jz_app.ui.adapter.MainFragmentPagerAdapter;
import com.wzq.jz_app.utils.Base64BitmapUtils;
import com.wzq.jz_app.utils.ExcelUtil;
import com.wzq.jz_app.utils.FileProvider7;
import com.wzq.jz_app.utils.FileUtil;
import com.wzq.jz_app.utils.HttpUtils;
import com.wzq.jz_app.utils.ImageUtils;
import com.wzq.jz_app.utils.OSUtil;
import com.wzq.jz_app.utils.RequestHttpUtil;
import com.wzq.jz_app.utils.SelectphotoUtils;
import com.wzq.jz_app.utils.SharedPUtils;
import com.wzq.jz_app.utils.ThemeManager;
import com.wzq.jz_app.widget.ButtomDialogView1;
import com.wzq.jz_app.widget.CircleImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static android.app.Activity.RESULT_OK;
import static cn.bmob.v3.Bmob.getApplicationContext;
import static com.wzq.jz_app.utils.SelectphotoUtils.getRealFilePathFromUri;


/**
 * 作者：wzq on 2019/4/2.
 * 邮箱：wang_love152@163.com
 */

public class MineFragment extends BaseFragment implements View.OnClickListener {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView tOutcome;
    private TextView tIncome;
    private TextView tTotal;

    private View drawerHeader;
    private CircleImageView drawerIv;
    private TextView drawerTvAccount, drawerTvMail;

    protected static final int USERINFOACTIVITY_CODE = 0;
    protected static final int LOGINACTIVITY_CODE = 1;

    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    protected static final int CROP_SMALL_PICTURE = 2;

    private final String secret_key = "2e9985b26398a3a5";
    // 方形还是圆形截图
    private int type = 1;//"1"圆形 "2"方形
    //请求访问外部存储
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    //请求写入外部存储
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 104;
    //图片路径
    protected static Uri tempUri = null;
    public final int SIZE = 2 * 1024;


    // Tab
    private FragmentManager mFragmentManager;
    private MainFragmentPagerAdapter mFragmentPagerAdapter;
    private HomeFragment homeFragment;
    private ChartFragment chartFragment;

    private File selectFile;
    private ButtomDialogView1 dialogView1;
    private MyUser currentUser;
    private RelativeLayout editor;
    private RelativeLayout snyc;
    private RelativeLayout setting;
    private RelativeLayout theme;
    private RelativeLayout about;
    private RelativeLayout countClass;
    private RelativeLayout nav_outexcle;
    private RelativeLayout nav_family;
    private TextView home_title;


    /***************************************************************************/
    @Override
    protected int getLayoutId() {
        return R.layout.main_fragment_mine;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        //注册 EventBus
        EventBus.getDefault().register(this);

        //第一次进入将默认账单分类添加到数据库
        if (SharedPUtils.isFirstStart(mContext)) {
            Log.i(TAG, "第一次进入将默认账单分类添加到数据库");
            NoteBean note = new Gson().fromJson(Constants.BILL_NOTE, NoteBean.class);
            List<BSort> sorts = note.getOutSortlis();
            sorts.addAll(note.getInSortlis());
            LocalRepository.getInstance().saveBsorts(sorts);
            LocalRepository.getInstance().saveBPays(note.getPayinfo());
        }

        homeFragment = new HomeFragment();
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        super.initWidget(savedInstanceState);


        drawerHeader = getViewById(R.id.drawer_header);
        drawerIv = getViewById(R.id.drawer_iv);
        drawerTvAccount = getViewById(R.id.drawer_tv_name);
        drawerTvMail = getViewById(R.id.drawer_tv_email);


        editor = getViewById(R.id.nav_edit);//编辑资料
        snyc = getViewById(R.id.nav_sync);//同步数据
        setting = getViewById(R.id.nav_setting);//设置
        theme = getViewById(R.id.nav_theme);//主题
        countClass = getViewById(R.id.nav_class);//账单分类
        nav_outexcle = getViewById(R.id.nav_outexcle);//导出账单
        about = getViewById(R.id.nav_about);//关于
        nav_family = getViewById(R.id.nav_family);//家庭
        home_title = getViewById(R.id.home_title);

        //设置头部账户
        setDrawerHeaderAccount();
        if (currentUser != null) {//登录状态
            getHeadimg();
            Log.e("123", "头像: " + currentUser.getImage());
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initClick() {
        super.initClick();

        //监听菜单头部点击事件
        drawerHeader.setOnClickListener(v -> {
            if (currentUser == null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.putExtra("exit", "1");
                startActivity(intent);
                EventBus.getDefault().post("finish");
            } else {
                toChooseImg();
            }
        });

        //监听设置主题等
        editor.setOnClickListener(this);
        snyc.setOnClickListener(this);
        setting.setOnClickListener(this);
        theme.setOnClickListener(this);
        about.setOnClickListener(this);
        countClass.setOnClickListener(this);
        nav_outexcle.setOnClickListener(this);
        nav_family.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_edit://编辑资料
                currentUser = BmobUser.getCurrentUser(MyUser.class);
                //获取当前用户
                if (currentUser == null)
//                    SnackbarUtils.show(mContext, "请先登陆");
                    Toast.makeText(getApplicationContext(), "请先登录", Toast.LENGTH_SHORT).show();
                else
                    startActivityForResult(new Intent(mContext, UserInfoActivity.class), USERINFOACTIVITY_CODE);
                break;

            case R.id.nav_class://账单分类
                startActivity(new Intent(mContext, SortActivity.class));
                break;
            case R.id.nav_outexcle://导出账单
                ActivityCompat.requestPermissions(getActivity(), new String[]{android
                        .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                //创建文件夹
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && getAvailableStorage(getContext()) > 1000000) {
                    Toast.makeText(getContext(), "SD卡不可用", Toast.LENGTH_LONG).show();
                    return;
                }
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndroidwzqExcelDemo/";
                File file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                    if (file.mkdirs()) {
                        Toast.makeText(getApplicationContext(), "创建目录成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "创建目录失败", Toast.LENGTH_SHORT).show();
                    }
                }
                String excleCountName = "/wzq.xls";
                String[] title = {"本地id", "服务器端id", "金额", "内容", "用户id", "支付方式", "图标", "账单分类", "分类图标", "创建时间", "收入支出", "版本"};
                String sheetName = "demoSheetName";
                List<BBill> bBills = new ArrayList<>();
                bBills = LocalRepository.getInstance().getBBills();
                filePath = file.getAbsolutePath() + "/" + excleCountName;
                ExcelUtil.initExcel(filePath, title);
                ExcelUtil.writeObjListToExcel(bBills, filePath, getApplicationContext());

                break;

            case R.id.nav_sync://同步数据
                if (currentUser == null)
                    Toast.makeText(getApplicationContext(), "请先登录", Toast.LENGTH_SHORT).show();
                else {
                    /*
                     *判断是否查看的是自己的账户
                     */
                    if(Constants.is_current_user_flag)
                        BmobRepository.getInstance().syncBill(currentUser.getObjectId());
                    else {
                        Toast.makeText(getApplicationContext(), "请切回自己的账户", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.nav_setting://设置

                startActivity(new Intent(mContext, SettingActivity.class));
                break;
            case R.id.nav_theme://主题
                showUpdateThemeDialog();
                break;
            case R.id.nav_about://关于
                startActivity(new Intent(mContext, AboutActivity.class));
                break;
            /**
             * 家庭按钮
             */
            case R.id.nav_family://家庭
                showFamilyGroup();
                break;
        }
    }

    /**
     * 显示用户所属的家庭群组
     */
    private void showFamilyGroup(){
        RequestHttpUtil request_http =
                new RequestHttpUtil(this.secret_key, "findFamilyGroup", "user_object_id=" + currentUser.getObjectId());
        String result = request_http.run();
        System.out.println("result:" + result);

        /**
         * 显示对话框
         */
        AlertDialog.Builder group_item_list = new AlertDialog.Builder(mContext);
        group_item_list.setTitle("家庭群组");
        /**
         * 返回结果不为空，说明已加入家庭群组,初始化群组
         */
        if(result != null){
            String[] family_group = result.split(",");
            String[] family_group_name = new String[family_group.length];
            String[] family_group_uni_id = new String[family_group.length];
            for(int i = 0; i < family_group.length; i++){
                family_group_name[i] = family_group[i].split(":")[0];
                family_group_uni_id[i] = family_group[i].split(":")[1];
            }
            System.out.println(family_group_name);
            System.out.println(family_group_uni_id);

            final int[] select_group_index = {-1};
            group_item_list.setSingleChoiceItems(family_group_name, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    select_group_index[0] = which;
                }
            });
            group_item_list.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(select_group_index[0] != -1)
                        showFamilyGroupMember(family_group_name[select_group_index[0]],
                                            family_group_uni_id[select_group_index[0]]);
                }
            });

        }
        group_item_list.setNeutralButton("新增", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] join_create_list = {"我要创建一个家庭群组", "我要加入一个家庭群组"};
                AlertDialog.Builder join_create_dialog = new AlertDialog.Builder(mContext);
                join_create_dialog.setTitle("选择方式");
                final int[] select_way_index = new int[1];
                join_create_dialog.setSingleChoiceItems(join_create_list, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        select_way_index[0] = which;
                    }
                });

                join_create_dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println(select_way_index[0]);
                        switch (select_way_index[0]) {
                            case 0:
                                //创建家庭群组
                                showCreateFamilyView();
                                break;
                            case 1:
                                //加入家庭群组
                                showJoinFamilyView();
                                break;
                        }
                    }
                });
                join_create_dialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        group_item_list.show();
                    }
                });
                join_create_dialog.show();
            }
        });
        group_item_list.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        group_item_list.create().show();
    }

    /**
     * 查看创建家庭群组界面
     */
    private void showCreateFamilyView(){
        startActivity(new Intent(mContext, CreateFamilyActivity.class));
    }

    private void showJoinFamilyView(){
        startActivity(new Intent(mContext, JoinFamilyActivity.class));
    }

    /**
     * 查看群组成员列表
     */
    private void showFamilyGroupMember(String group_name, String group_uni_id){

        //调用云函数findFamilyGroupMember
        String params = "userObjId=" + currentUser.getObjectId() + "&UniID=" + group_uni_id;
        String function_name = "findFamilyGroupMember";
        RequestHttpUtil request_http_findFamilyGroupMember_function
                = new RequestHttpUtil(this.secret_key, function_name, params);
        String result = request_http_findFamilyGroupMember_function.run();
        System.out.println("findFamilyGroupMember:" + result);
        if(result.equals("您所处的Group中没有您所查找的Group！")){

        }else {
            String[] member_info_list = result.split(",");
            String[] member_name_list = new String[member_info_list.length];
            String[] member_obj_id_list = new String[member_info_list.length];
            for (int i = 0; i < member_info_list.length; i++) {
                member_name_list[i] = member_info_list[i].split(":")[1];
                member_obj_id_list[i] = member_info_list[i].split(":")[0];
            }

            AlertDialog.Builder member_item_list = new AlertDialog.Builder(mContext);

            String quit_text = "退出";

            // 判断是否为群主
            if(currentUser.getObjectId().equals(member_obj_id_list[0])){

                // 获取邀请码
                String get_invite_result = getInviteCode(member_obj_id_list[0], group_uni_id);
                member_item_list.setTitle(group_name + ":" + get_invite_result);
                quit_text = "解散";
            }
            else member_item_list.setTitle(group_name);

            final int[] select_member_index = {-1};
            member_item_list.setSingleChoiceItems(member_name_list, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    select_member_index[0] = which;
                }
            });
            member_item_list.setPositiveButton("查看", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Constants.is_current_user_flag =
                            member_obj_id_list[select_member_index[0]].equals(currentUser.getObjectId());
                    switchMemberAccount(member_obj_id_list[select_member_index[0]],
                                        member_name_list[select_member_index[0]]);
                }
            });
            member_item_list.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            String finalQuit_text = quit_text;
            member_item_list.setNeutralButton(quit_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String quit_result = quitFamily(currentUser.getObjectId(), group_uni_id);
                    Toast.makeText(getActivity(), "已"  + finalQuit_text + "群组", Toast.LENGTH_SHORT).show();
                }
            });
            member_item_list.show();
        }


    }

    /**
     * 切换到目标用户的账单
     * @param user_obj_id
     */
    private void switchMemberAccount(String user_obj_id, String user_name){
        BmobRepository bmobRepository = BmobRepository.getInstance();
        LocalRepository.getInstance().deleteAllBills();
        bmobRepository.syncBill(user_obj_id);
        Constants.check_user_name = user_name;
    }

    /**
     * 退出指定群组
     * @param user_objet_id
     * @param group_uni_id
     */
    private String quitFamily(String user_obj_id, String group_uni_id){
        String function_name = "quitFamilyGroup";
        String params = "user_objectId=" + user_obj_id + "&uniId=" + group_uni_id;
        RequestHttpUtil request_get_invite_code_function =
                new RequestHttpUtil(this.secret_key, function_name, params);
        String result = request_get_invite_code_function.run();
        System.out.println(result);
        return result;
    }


    /**
     * 调用获取邀请码的云函数
     * @param user_obj_id
     * @param group_uni_id
     * @return
     */
    private String getInviteCode(String user_obj_id, String group_uni_id){
        String function_name = "getFamilyGroupInviteCode";
        String params = "userObjId=" + user_obj_id + "&UniID=" + group_uni_id;
        RequestHttpUtil request_get_invite_code_function =
                new RequestHttpUtil(this.secret_key, function_name, params);
        String result = request_get_invite_code_function.run();
        System.out.println(result);
        return result;
    }

    /**
     * 显示修改主题色 Dialog
     */
    private void showUpdateThemeDialog() {
        String[] themes = ThemeManager.getInstance().getThemes();
        new MaterialDialog.Builder(mContext)
                .title("选择主题")
                .titleGravity(GravityEnum.CENTER)
                .items(themes)
//                .titleColorRes(R.color.material_red_500)
                .contentColor(Color.BLACK) // notice no 'res' postfix for literal color
                .linkColorAttr(R.attr.aboutPageHeaderTextColor)  // notice attr is used instead of none or res for attribute resolving
                .dividerColorRes(R.color.colorMainDateBg)
//                .backgroundColorRes(R.drawable.dialog_backgroud)//背景色
//                .positiveColorRes(R.color.material_red_500)
                .neutralColorRes(R.color.colorControlNormal)
//                .negativeColorRes(R.color.material_red_500)
//                .widgetColorRes(R.color.colorControlNormal)//选中颜色
//                .buttonRippleColorRes(R.color.colorControlNormal)
                .negativeText("取消")
//                .customView(R.layout.activity_dialog,true)
                .itemsCallbackSingleChoice(0, (dialog, itemView, position, text) -> {
                    ThemeManager.getInstance().setTheme(mActivity, themes[position]);
                    dialog.dismiss();
                    return false;
                }).show();
    }


    /**
     * 显示选择头像来源对话框
     */
    private void toChooseImg() {
        if (dialogView1 == null) {
            dialogView1 = new ButtomDialogView1(getActivity());
            dialogView1.setOnDialogClickListener(new ButtomDialogView1.OnDialogClickListener() {
                @Override
                public void onclick1() {//相机
                    selectFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()+"jz_app.jpg");
                    SelectphotoUtils.takePicture(getActivity(), MineFragment.this, selectFile);
                    dialogView1.dismiss();
                }

                @Override
                public void onclick2() {//打开相册
                    Intent openAlbumIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                    dialogView1.dismiss();
                }

                @Override
                public void onclick3() {
                    dialogView1.dismiss();
                }
            });
        }
        dialogView1.show();
    }


    /**
     * 获取SD可用容量
     */
    private static long getAvailableStorage(Context context) {
        String root = context.getExternalFilesDir(null).getPath();
        StatFs statFs = new StatFs(root);
        long blockSize = statFs.getBlockSize();
        long availableBlocks = statFs.getAvailableBlocks();
        long availableSize = blockSize * availableBlocks;
        // Formatter.formatFileSize(context, availableSize);
        return availableSize;
    }

    /**
     * 监听Activity返回值
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHOOSE_PICTURE:// 直接从相册获取
                try {
                    selectFile  = new File(Environment.getExternalStorageDirectory(), "jz_app.jpg");
                    SelectphotoUtils.startPhotoZoom(data.getData(), MineFragment.this, selectFile);
                } catch (NullPointerException e) {
                    e.printStackTrace();// 用户点击取消操作
                }
                break;
            case TAKE_PICTURE:// 调用相机拍照
                if (isExistSd()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //如果是7.0剪裁图片 同理 需要把uri包装
                        Uri fileUri = FileProvider7.getUriForFile(getActivity(), selectFile);
                        if (selectFile.exists()) {
                            //对相机拍照照片进行裁剪
                            SelectphotoUtils.startPhotoZoom(fileUri, MineFragment.this, selectFile);
                        }
                    } else {
                        if (selectFile.exists()) {
                            //对相机拍照照片进行裁剪
                            SelectphotoUtils.startPhotoZoom(Uri.fromFile(selectFile), MineFragment.this, selectFile);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "图片保存失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case CROP_SMALL_PICTURE://取得裁剪后的图片
                if (data != null) {
                    setPicToView(data);
                }
                ;
                break;
        }
    }

    /**
     * 定义方法判断SD卡的存在性
     */
    private boolean isExistSd() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    protected void setPicToView(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap bitmap = null;
        try {
            if (OSUtil.isMIUI()) {
                Uri uritempFile = Uri.parse("file://" + "/" + selectFile);
                try {
                    bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uritempFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                if (extras != null) {
                    bitmap = extras.getParcelable("data");

                }
            }
            //获得图片路径
            File filepath = SelectphotoUtils.saveFile(bitmap, Environment.getExternalStorageDirectory().toString(), "jz_app.jpg");
            //此处后面可以将bitMap转为二进制上传后台网络
            Toast.makeText(getActivity(), "上传中", Toast.LENGTH_SHORT).show();
            uploadPic(bitmap);
        } catch (Exception e) {

        }
    }


    /**
     * 设置DrawerHeader的用户信息
     */
    public void setDrawerHeaderAccount() {
        currentUser = BmobUser.getCurrentUser(MyUser.class);
        //获取当前用户
        if (currentUser != null) {
            drawerTvAccount.setText(currentUser.getUsername());
            drawerTvMail.setText(currentUser.getEmail());

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.icon_head_error);
//            Glide.with(this).load(currentUser.getImage()).apply(requestOptions).into(drawerIv);

        } else {
            drawerTvAccount.setText("账号");
            drawerTvMail.setText("点我登录");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                selectFile = new File(Environment.getExternalStorageDirectory(), "jz_app.jpg");
                SelectphotoUtils.takePicture(getActivity(), MineFragment.this, selectFile);
                break;

            case 101:
                selectFile = new File(Environment.getExternalStorageDirectory(), "jz_app.jpg");
                SelectphotoUtils.takePicture(getActivity(), MineFragment.this, selectFile);
                break;
            default:
        }
    }

    /**
     * 保存头像并上传服务器
     *
     * @param bitmap
     */
    private void uploadPic(Bitmap bitmap) {
        // 上传至服务器
        // 可以在这里把Bitmap转换成file，然后得到file的url，做文件上传操作
        // 注意这里得到的图片已经是圆形图片了
        // bitmap是没有做个圆形处理的，但已经被裁剪了
        String imagename = currentUser.getObjectId() + "_" + String.valueOf(System.currentTimeMillis());
        String imagePath = ImageUtils.savePhoto(bitmap, Environment
                .getExternalStorageDirectory().getAbsolutePath(), imagename + ".png");
        if (imagePath != null) {
            final BmobFile bmobFile = new BmobFile(new File(imagePath));
            bmobFile.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        MyUser newUser = new MyUser();
                        newUser.setImage(bmobFile.getFileUrl());
                        newUser.update(currentUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Toast.makeText(getActivity(), "上传成功", Toast.LENGTH_SHORT).show();
                                    drawerIv.setImageBitmap(bitmap);
                                    final String img = Base64BitmapUtils.bitmapToBase64(bitmap);
                                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bill/head/img/";
                                    FileUtil.writeToFile(filePath, "data:image/png;base64," + img);
                                } else {
                                    Toast.makeText(getActivity(), "上传失败," + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "上传失败," + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private void getHeadimg() {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bill/head/img/";
        String img = FileUtil.readFile(filePath);
        if (!TextUtils.isEmpty(img)) {
            Bitmap bitmap = Base64BitmapUtils.stringToBitmap(img);
            drawerIv.setImageBitmap(bitmap);
        }
    }


    //EvenBus
    @Subscribe
    public void eventBusListener(String tag) {
        if (TextUtils.equals("1", tag)) {
            processLogic();
        }
    }
}
