package com.lhj.camera;

/**
 * Created by Administrator on 2018/4/9.
 */

public interface OnCameraResults {
    void onSucces();
    void onError(int code,String msg);
}
