package com.iqos.debug;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iqos.debug.callback.ExecuteCmdResultCallback;
import com.iqos.debug.callback.RequestRootCallback;
import com.iqos.debug.util.CmdManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout mRlProgressing, mRlNotRootTip;
    private TextView mTvResult;
    private CmdManager mCmdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initViews();
        this.initData();
    }

    private void initViews() {
        mRlProgressing = findViewById(R.id.app_main_rl_progressing);
        mRlNotRootTip = findViewById(R.id.app_main_rl_tip_miss_root);
        mTvResult = findViewById(R.id.app_tv_exec_cmd_result);
    }

    private void initData() {
        if (null == mCmdManager) mCmdManager = new CmdManager();
        this.requestRoot();
    }

    private void requestRoot() {
        try {
            mCmdManager.requestRootPermission(getPackageCodePath(), new RequestRootCallback() {
                @Override
                public void onRootPermissionGranted() {
                    mRlProgressing.setVisibility(View.GONE);
                    mRlNotRootTip.setVisibility(View.GONE);
                }

                @Override
                public void onRootPermissionDenied() {
                    mRlNotRootTip.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUsbDebug() {
        try {
            mCmdManager.exeCmd(new String[]{"setprop service.adb.tcp.port 5555", "stop adbd", "start adbd"}, new ExecuteCmdResult() {
                @Override
                public void onExecuteSuccess(String successMsg) {
                    showIpAdd();
                }

                @Override
                public void onExecuteFail(String errInfo) {
                    mTvResult.setText(String.format(getResources().getString(R.string.app_tip_exec_cmd_success), errInfo));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showIpAdd() {
        try {
            mCmdManager.exeCmd(new String[]{"ifconfig wlan0"}, new ExecuteCmdResult() {
                @Override
                public void onExecuteSuccess(String successMsg) {
                    mTvResult.setText(String.format(getResources().getString(R.string.app_tip_exec_cmd_success), successMsg));
                }

                @Override
                public void onExecuteFail(String errInfo) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private abstract class ExecuteCmdResult implements ExecuteCmdResultCallback {
        @Override
        public void onProgressing() {
            mRlProgressing.setVisibility(View.VISIBLE);
        }

        @Override
        public void onExecuteComplete() {
            mRlProgressing.setVisibility(View.GONE);
        }
    }

    private void closeUsbDebug() {
        try {
            mCmdManager.exeCmd(new String[]{"setprop service.adb.tcp.port -1", "stop adbd", "start adbd"}, new ExecuteCmdResult() {
                @Override
                public void onExecuteSuccess(String successMsg) {
                    mTvResult.setText(String.format(getResources().getString(R.string.app_tip_exec_cmd_success), getResources().getString(R.string.app_tip_wifi_has_closed)));
                }

                @Override
                public void onExecuteFail(String errInfo) {
                    mTvResult.setText(String.format(getResources().getString(R.string.app_tip_exec_cmd_success), errInfo));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_main_open_usb_debug:
                this.openUsbDebug();
                break;
            case R.id.app_main_close_usb_debug:
                this.closeUsbDebug();
                break;
            case R.id.app_main_tv_not_root_tip:
                this.requestRoot();
                break;
        }
    }
}
