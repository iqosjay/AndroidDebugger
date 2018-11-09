package com.iqos.debug.util;

import android.os.Handler;
import android.os.Message;

import com.iqos.debug.callback.ExecuteCmdResultCallback;
import com.iqos.debug.callback.RequestRootCallback;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

/**
 * Created by iqosjay@gmail.com on 2018/11/8
 */
public final class CmdManager {
    private final MyHandler mHandler = new MyHandler(this);
    private final String mCommandExit = "exit\n";
    private final String mCommandLineEnd = "\n";
    private ExecuteCmdResultCallback mExecuteCmdResultCallback;
    private RequestRootCallback mRequestRootCallback;

    public void requestRootPermission(final String packageCodePath, final RequestRootCallback callback) throws Exception {
        if (null == callback) {
            throw new Exception("The callback can not be null!");
        }
        this.mRequestRootCallback = callback;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                DataOutputStream out = null;
                try {
                    String cmd = "chmod 777 " + packageCodePath;
                    process = Runtime.getRuntime().exec("su");
                    if (null == process) {
                        callback.onRootPermissionDenied();
                        return;
                    }
                    out = new DataOutputStream(process.getOutputStream());
                    out.writeBytes(cmd + mCommandLineEnd);
                    out.writeBytes(mCommandExit);
                    out.flush();
                    int result = process.waitFor();
                    if (0 == result) {
                        mHandler.sendEmptyMessage(0);
                    } else {
                        mHandler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != process) {
                        process.destroy();
                    }
                    if (null != out) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private void sendMessage(int what, String msg) {
        Message message = new Message();
        message.what = what;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    public void exeCmd(final String[] commands, final ExecuteCmdResultCallback callback) throws Exception {
        if (null == callback) {
            throw new Exception("The callback can not be null.");
        }
        mExecuteCmdResultCallback = callback;
        mExecuteCmdResultCallback.onProgressing();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result;
                if (commands == null || commands.length == 0) {
                    mExecuteCmdResultCallback.onProgressing();
                    return;
                }
                Process process = null;
                BufferedReader successResult = null;
                BufferedReader errorResult = null;
                DataOutputStream os = null;
                try {
                    process = Runtime.getRuntime().exec("su");
                    os = new DataOutputStream(process.getOutputStream());
                    for (String command : commands) {
                        if (command == null) continue;
                        os.write(command.getBytes());
                        os.writeBytes(mCommandLineEnd);
                        os.flush();
                    }
                    os.writeBytes(mCommandExit);
                    os.flush();
                    result = process.waitFor();
                    // -----------------get command result------------------//
                    StringBuilder successMsg = new StringBuilder();
                    StringBuilder errorMsg = new StringBuilder();
                    successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while (successResult.readLine() != null) {
                        successMsg.append(successResult.readLine()).append(mCommandLineEnd);
                    }
                    while (errorResult.readLine() != null) {
                        errorMsg.append(errorResult.readLine()).append(mCommandLineEnd);
                    }
                    if (0 == result) {
                        sendMessage(2, successMsg.toString());
                    } else {
                        sendMessage(3, errorMsg.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mHandler.sendEmptyMessage(4);
                    if (null != process) process.destroy();
                    try {
                        if (null != os) os.close();
                        if (null != successResult) successResult.close();
                        if (null != errorResult) errorResult.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<CmdManager> mCmdManagerWeakReference;

        MyHandler(CmdManager cmdManager) {
            mCmdManagerWeakReference = new WeakReference<>(cmdManager);
        }

        @Override
        public void handleMessage(Message msg) {
            CmdManager cmdManager = mCmdManagerWeakReference.get();
            if (null == cmdManager) return;
            switch (msg.what) {
                case 0:
                    cmdManager.mRequestRootCallback.onRootPermissionGranted();
                    break;
                case 1:
                    cmdManager.mRequestRootCallback.onRootPermissionDenied();
                    break;
                case 2:
                    cmdManager.mExecuteCmdResultCallback.onExecuteSuccess(msg.obj.toString());
                    break;
                case 3:
                    cmdManager.mExecuteCmdResultCallback.onExecuteFail(msg.obj.toString());
                    break;
                case 4:
                    cmdManager.mExecuteCmdResultCallback.onExecuteComplete();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
