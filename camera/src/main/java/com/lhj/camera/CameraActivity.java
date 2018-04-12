package com.lhj.camera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;


public class CameraActivity extends Activity {

    private CameraFragment mFCameraFragment;
    private OnBackListeners onBackListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_camera);
//        if (savedInstanceState == null) {
        	mFCameraFragment= new CameraFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mFCameraFragment)
                    .commit();
//        }
        CameraBuidler.setActivity(this);
    }

    public void onCancel(View view) {
        getFragmentManager().popBackStack();
    }

    public void takePicture(){
    	mFCameraFragment.takePicture();
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==event.KEYCODE_BACK){
            finishThis();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFCameraFragment = null;
        onBackListeners = null;
        CameraBuidler.setActivity(null);
    }

    public void finishThis(){
        if(onBackListeners!=null){
            onBackListeners.onBack();
        }
    }

    public interface OnBackListeners{
        void onBack();
    }

    public void setOnBackListeners(OnBackListeners onBackListeners) {
        this.onBackListeners = onBackListeners;
    }
}


