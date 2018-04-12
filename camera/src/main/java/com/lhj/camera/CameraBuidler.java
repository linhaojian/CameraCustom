package com.lhj.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.v4.content.ContextCompat;

/**
 * Created by Administrator on 2018/4/9.
 */

public class CameraBuidler {
    private static OnCameraResults onCameraResults;
    public static final int CAMERA_ERROR = 0x100;
    public static final int STORAGE_ERROR = 0x101;
    private static CameraActivity activity;

    public CameraBuidler(){

    }

    public static void setActivity(CameraActivity cameraActivity){
        activity = cameraActivity;
        activity.setOnBackListeners(new CameraActivity.OnBackListeners() {
            @Override
            public void onBack() {
                if(onCameraResults!=null){
                    onCameraResults.onBack(activity);
                }
            }
        });
    }

    public CameraBuidler setOnCameraResults(OnCameraResults onCameraResults){
        this.onCameraResults = onCameraResults;
        return this;
    }

    public CameraBuidler buidler(Activity activity){
        if(jugdePermissions(activity)){
            Intent intent = new Intent(activity,CameraActivity.class);
            activity.startActivity(intent);
        }
        return this;
    }

    private boolean jugdePermissions(Activity activity){
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            //没有打开拍照权限
            if(onCameraResults!=null) {
                ;onCameraResults.onError(CAMERA_ERROR,"camera permission unable");
            }
            return false;
        }else{
            if((ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)||(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)){
                //没有打开文件存储权限
                if(onCameraResults!=null) {
                    ;onCameraResults.onError(STORAGE_ERROR,"storage permission unable");
                }
                return false;
            }
        }
        if(onCameraResults!=null) {
            ;onCameraResults.onSucces();
        }
        return true;
    }

    public void takePicture(){
        if(activity!=null){
            activity.takePicture();
        }
    }

    public void finish(){
        if(activity!=null){
            activity.finish();
        }
    }

    public void release(){
        activity=null;
        onCameraResults=null;
    }

}
