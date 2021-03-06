package com.wzq.jz_app.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import com.wzq.jz_app.ui.adapter.MainFragmentPagerAdapter;
import com.wzq.jz_app.utils.Base64BitmapUtils;
import com.wzq.jz_app.utils.ExcelUtil;
import com.wzq.jz_app.utils.FileProvider7;
import com.wzq.jz_app.utils.FileUtil;
import com.wzq.jz_app.utils.HttpUtils;
import com.wzq.jz_app.utils.ImageUtils;
import com.wzq.jz_app.utils.LocationUtils;
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
 * ?????????wzq on 2019/4/2.
 * ?????????wang_love152@163.com
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
    // ????????????????????????
    private int type = 1;//"1"?????? "2"??????
    //????????????????????????
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    //????????????????????????
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 104;
    //????????????
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
        //?????? EventBus
        EventBus.getDefault().register(this);

        //??????????????????????????????????????????????????????
        if (SharedPUtils.isFirstStart(mContext)) {
            Log.i(TAG, "??????????????????????????????????????????????????????");
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


        editor = getViewById(R.id.nav_edit);//????????????
        snyc = getViewById(R.id.nav_sync);//????????????
        setting = getViewById(R.id.nav_setting);//??????
        theme = getViewById(R.id.nav_theme);//??????
        countClass = getViewById(R.id.nav_class);//????????????
        nav_outexcle = getViewById(R.id.nav_outexcle);//????????????
        about = getViewById(R.id.nav_about);//??????
        nav_family = getViewById(R.id.nav_family);//??????
        home_title = getViewById(R.id.home_title);

        //??????????????????
        setDrawerHeaderAccount();
        if (currentUser != null) {//????????????
            getHeadimg();
            Log.e("123", "??????: " + currentUser.getImage());
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initClick() {
        super.initClick();

        //??????????????????????????????
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

        //?????????????????????
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
            case R.id.nav_edit://????????????
                currentUser = BmobUser.getCurrentUser(MyUser.class);
                //??????????????????
                if (currentUser == null)
//                    SnackbarUtils.show(mContext, "????????????");
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                else
                    startActivityForResult(new Intent(mContext, UserInfoActivity.class), USERINFOACTIVITY_CODE);
                break;

            case R.id.nav_class://????????????
                startActivity(new Intent(mContext, SortActivity.class));
                break;
            case R.id.nav_outexcle://????????????
                ActivityCompat.requestPermissions(getActivity(), new String[]{android
                        .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                //???????????????
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && getAvailableStorage(getContext()) > 1000000) {
                    Toast.makeText(getContext(), "SD????????????", Toast.LENGTH_LONG).show();
                    return;
                }
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndroidwzqExcelDemo/";
                File file = new File(filePath);
                if (!file.exists()) {
                    file.mkdirs();
                    if (file.mkdirs()) {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                String excleCountName = "/wzq.xls";
                String[] title = {"??????id", "????????????id", "??????", "??????", "??????id", "????????????", "??????", "????????????", "????????????", "????????????", "????????????", "??????"};
                String sheetName = "demoSheetName";
                List<BBill> bBills = new ArrayList<>();
                bBills = LocalRepository.getInstance().getBBills();
                filePath = file.getAbsolutePath() + "/" + excleCountName;
                ExcelUtil.initExcel(filePath, title);
                ExcelUtil.writeObjListToExcel(bBills, filePath, getApplicationContext());

                break;

            case R.id.nav_sync://????????????
                if (currentUser == null)
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                else {
                    /*
                     *???????????????????????????????????????
                     */
                    if (Constants.is_current_user_flag)
                        BmobRepository.getInstance().syncBill(currentUser.getObjectId());
                    else {
                        Toast.makeText(getApplicationContext(), "????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.nav_setting://??????

                startActivity(new Intent(mContext, SettingActivity.class));
                break;
            case R.id.nav_theme://??????
                showUpdateThemeDialog();
                break;
            case R.id.nav_about://??????
                getGPS();
                startActivity(new Intent(mContext, AboutActivity.class));
                break;
            /**
             * ????????????
             */
            case R.id.nav_family://??????
                currentUser = BmobUser.getCurrentUser(MyUser.class);
                //??????????????????
                if (currentUser == null)
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                else
                    showFamilyGroup();
                break;
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void showFamilyGroup() {
        RequestHttpUtil request_http =
                new RequestHttpUtil(this.secret_key, "findFamilyGroup", "user_object_id=" + currentUser.getObjectId());
        String result = request_http.run();
        System.out.println("result:" + result);

        /**
         * ???????????????
         */
        AlertDialog.Builder group_item_list = new AlertDialog.Builder(mContext);
        group_item_list.setTitle("????????????");
        /**
         * ???????????????????????????????????????????????????,???????????????
         */
        if (result != null) {
            String[] family_group = result.split(",");
            String[] family_group_name = new String[family_group.length];
            String[] family_group_uni_id = new String[family_group.length];
            for (int i = 0; i < family_group.length; i++) {
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
            group_item_list.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (select_group_index[0] != -1)
                        showFamilyGroupMember(family_group_name[select_group_index[0]],
                                family_group_uni_id[select_group_index[0]]);
                }
            });

        }
        group_item_list.setNeutralButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] join_create_list = {"??????????????????????????????", "??????????????????????????????"};
                AlertDialog.Builder join_create_dialog = new AlertDialog.Builder(mContext);
                join_create_dialog.setTitle("????????????");
                final int[] select_way_index = new int[1];
                join_create_dialog.setSingleChoiceItems(join_create_list, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        select_way_index[0] = which;
                    }
                });

                join_create_dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println(select_way_index[0]);
                        switch (select_way_index[0]) {
                            case 0:
                                //??????????????????
                                showCreateFamilyView();
                                break;
                            case 1:
                                //??????????????????
                                showJoinFamilyView();
                                break;
                        }
                    }
                });
                join_create_dialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        group_item_list.show();
                    }
                });
                join_create_dialog.show();
            }
        });
        group_item_list.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        group_item_list.create().show();
    }

    /**
     * ??????????????????????????????
     */
    private void showCreateFamilyView() {
        startActivity(new Intent(mContext, CreateFamilyActivity.class));
    }

    private void showJoinFamilyView() {
        startActivity(new Intent(mContext, JoinFamilyActivity.class));
    }


    /**
     * ????????????????????????
     */
    private void showFamilyGroupMember(String group_name, String group_uni_id) {

        //???????????????findFamilyGroupMember
        String params = "userObjId=" + currentUser.getObjectId() + "&UniID=" + group_uni_id;
        String function_name = "findFamilyGroupMember";
        RequestHttpUtil request_http_findFamilyGroupMember_function
                = new RequestHttpUtil(this.secret_key, function_name, params);
        String result = request_http_findFamilyGroupMember_function.run();
        System.out.println("findFamilyGroupMember:" + result);
        if (result.equals("????????????Group????????????????????????Group???")) {

        } else {
            String[] member_info_list = result.split(",");
            String[] member_name_list = new String[member_info_list.length];
            String[] member_obj_id_list = new String[member_info_list.length];
            for (int i = 0; i < member_info_list.length; i++) {
                member_name_list[i] = member_info_list[i].split(":")[1];
                member_obj_id_list[i] = member_info_list[i].split(":")[0];
            }

            AlertDialog.Builder member_item_list = new AlertDialog.Builder(mContext);

            String quit_text = "??????";

            // ?????????????????????
            if (currentUser.getObjectId().equals(member_obj_id_list[0])) {

                // ???????????????
                String get_invite_result = getInviteCode(member_obj_id_list[0], group_uni_id);
                member_item_list.setTitle(group_name + ":" + get_invite_result);
                quit_text = "??????";
            } else member_item_list.setTitle(group_name);

            final int[] select_member_index = {-1};
            member_item_list.setSingleChoiceItems(member_name_list, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    select_member_index[0] = which;
                }
            });
            member_item_list.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Constants.is_current_user_flag =
                            member_obj_id_list[select_member_index[0]].equals(currentUser.getObjectId());
                    switchMemberAccount(member_obj_id_list[select_member_index[0]],
                            member_name_list[select_member_index[0]]);
                }
            });
            member_item_list.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            String finalQuit_text = quit_text;
            member_item_list.setNeutralButton(quit_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String quit_result = quitFamily(currentUser.getObjectId(), group_uni_id);
                    Toast.makeText(getActivity(), "???" + finalQuit_text + "??????", Toast.LENGTH_SHORT).show();
                }
            });
            member_item_list.show();
        }


    }

    /**
     * ??????????????????????????????
     * @param user_obj_id
     */
    private void switchMemberAccount(String user_obj_id, String user_name) {
        BmobRepository bmobRepository = BmobRepository.getInstance();
        LocalRepository.getInstance().deleteAllBills();
        bmobRepository.syncBill(user_obj_id);
        Constants.check_user_name = user_name;
        Toast.makeText(getActivity(), "????????????", Toast.LENGTH_SHORT).show();
    }

    /**
     * ??????????????????
     * @param user_obj_id
     * @param group_uni_id
     */
    private String quitFamily(String user_obj_id, String group_uni_id) {
        String function_name = "quitFamilyGroup";
        String params = "user_objectId=" + user_obj_id + "&uniId=" + group_uni_id;
        RequestHttpUtil request_get_invite_code_function =
                new RequestHttpUtil(this.secret_key, function_name, params);
        String result = request_get_invite_code_function.run();
        System.out.println(result);
        return result;
    }


    /**
     * ?????????????????????????????????
     * @param user_obj_id
     * @param group_uni_id
     * @return
     */
    private String getInviteCode(String user_obj_id, String group_uni_id) {
        String function_name = "getFamilyGroupInviteCode";
        String params = "userObjId=" + user_obj_id + "&UniID=" + group_uni_id;
        RequestHttpUtil request_get_invite_code_function =
                new RequestHttpUtil(this.secret_key, function_name, params);
        String result = request_get_invite_code_function.run();
        System.out.println(result);
        return result;
    }

    /**
     * ??????GPS????????????
     */
    private void getGPS() {
//        System.out.println("run gps");
        if (Build.VERSION.SDK_INT >= 23) {// android6 ?????????????????????
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                Log.e(TAG, "???????????????");
                LocationUtils.getInstance(getActivity()).setAddressCallback(new LocationUtils.AddressCallback() {
                    @Override
                    public void onGetLocation(double lat, double lng) {
                        System.out.println(lat + " " + lng);
                    }
                });
            }
        } else {

        }


    }

    /**
     * ????????????????????? Dialog
     */
    private void showUpdateThemeDialog() {
        String[] themes = ThemeManager.getInstance().getThemes();
        new MaterialDialog.Builder(mContext)
                .title("????????????")
                .titleGravity(GravityEnum.CENTER)
                .items(themes)
//                .titleColorRes(R.color.material_red_500)
                .contentColor(Color.BLACK) // notice no 'res' postfix for literal color
                .linkColorAttr(R.attr.aboutPageHeaderTextColor)  // notice attr is used instead of none or res for attribute resolving
                .dividerColorRes(R.color.colorMainDateBg)
//                .backgroundColorRes(R.drawable.dialog_backgroud)//?????????
//                .positiveColorRes(R.color.material_red_500)
                .neutralColorRes(R.color.colorControlNormal)
//                .negativeColorRes(R.color.material_red_500)
//                .widgetColorRes(R.color.colorControlNormal)//????????????
//                .buttonRippleColorRes(R.color.colorControlNormal)
                .negativeText("??????")
//                .customView(R.layout.activity_dialog,true)
                .itemsCallbackSingleChoice(0, (dialog, itemView, position, text) -> {
                    ThemeManager.getInstance().setTheme(mActivity, themes[position]);
                    dialog.dismiss();
                    return false;
                }).show();
    }


    /**
     * ?????????????????????????????????
     */
    private void toChooseImg() {
        if (dialogView1 == null) {
            dialogView1 = new ButtomDialogView1(getActivity());
            dialogView1.setOnDialogClickListener(new ButtomDialogView1.OnDialogClickListener() {
                @Override
                public void onclick1() {//??????
                    selectFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()+"jz_app.jpg");
                    SelectphotoUtils.takePicture(getActivity(), MineFragment.this, selectFile);
                    dialogView1.dismiss();
                }

                @Override
                public void onclick2() {//????????????
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
     * ??????SD????????????
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
     * ??????Activity?????????
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHOOSE_PICTURE:// ?????????????????????
                try {
                    selectFile  = new File(Environment.getExternalStorageDirectory(), "jz_app.jpg");
                    SelectphotoUtils.startPhotoZoom(data.getData(), MineFragment.this, selectFile);
                } catch (NullPointerException e) {
                    e.printStackTrace();// ????????????????????????
                }
                break;
            case TAKE_PICTURE:// ??????????????????
                if (isExistSd()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //?????????7.0???????????? ?????? ?????????uri??????
                        Uri fileUri = FileProvider7.getUriForFile(getActivity(), selectFile);
                        if (selectFile.exists()) {
                            //?????????????????????????????????
                            SelectphotoUtils.startPhotoZoom(fileUri, MineFragment.this, selectFile);
                        }
                    } else {
                        if (selectFile.exists()) {
                            //?????????????????????????????????
                            SelectphotoUtils.startPhotoZoom(Uri.fromFile(selectFile), MineFragment.this, selectFile);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "??????????????????", Toast.LENGTH_SHORT).show();
                }
                break;
            case CROP_SMALL_PICTURE://????????????????????????
                if (data != null) {
                    setPicToView(data);
                }
                ;
                break;
        }
    }

    /**
     * ??????????????????SD???????????????
     */
    private boolean isExistSd() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    /**
     * ?????????????????????????????????
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
            //??????????????????
            File filepath = SelectphotoUtils.saveFile(bitmap, Environment.getExternalStorageDirectory().toString(), "jz_app.jpg");
            //?????????????????????bitMap?????????????????????????????????
            Toast.makeText(getActivity(), "?????????", Toast.LENGTH_SHORT).show();
            uploadPic(bitmap);
        } catch (Exception e) {

        }
    }


    /**
     * ??????DrawerHeader???????????????
     */
    public void setDrawerHeaderAccount() {
        currentUser = BmobUser.getCurrentUser(MyUser.class);
        //??????????????????
        if (currentUser != null) {
            drawerTvAccount.setText(currentUser.getUsername());
            drawerTvMail.setText(currentUser.getEmail());

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.icon_head_error);
//            Glide.with(this).load(currentUser.getImage()).apply(requestOptions).into(drawerIv);

        } else {
            drawerTvAccount.setText("??????");
            drawerTvMail.setText("????????????");
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
     * ??????????????????????????????
     *
     * @param bitmap
     */
    private void uploadPic(Bitmap bitmap) {
        // ??????????????????
        // ??????????????????Bitmap?????????file???????????????file???url????????????????????????
        // ???????????????????????????????????????????????????
        // bitmap??????????????????????????????????????????????????????
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
                                    Toast.makeText(getActivity(), "????????????", Toast.LENGTH_SHORT).show();
                                    drawerIv.setImageBitmap(bitmap);
                                    final String img = Base64BitmapUtils.bitmapToBase64(bitmap);
                                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Bill/head/img/";
                                    FileUtil.writeToFile(filePath, "data:image/png;base64," + img);
                                } else {
                                    Toast.makeText(getActivity(), "????????????," + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "????????????," + e.toString(), Toast.LENGTH_SHORT).show();
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
