package com.iqos.debug.callback;

/**
 * Created by iqosjay@gmail.com on 2018/11/8
 */
public interface RequestRootCallback {
    void onRootPermissionGranted();

    void onRootPermissionDenied();
}
