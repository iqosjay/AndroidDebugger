package com.iqos.debug.callback;

/**
 * Created by iqosjay@gmail.com on 2018/11/8
 */
public interface ExecuteCmdResultCallback {
    void onExecuteSuccess(String successMsg);

    void onExecuteFail(String errInfo);

    void onProgressing();

    void onExecuteComplete();
}
