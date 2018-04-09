package com.lhj.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lhj.camera.CameraActivity;
import com.lhj.camera.CameraBuidler;
import com.lhj.camera.OnCameraResults;

public class MainActivity extends AppCompatActivity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraBuidler cameraBuidler = new CameraBuidler().setOnCameraResults(new OnCameraResults() {
                    @Override
                    public void onSucces() {
                        Log.w("linhaojian","onSucces");
                    }

                    @Override
                    public void onError(int code, String msg) {
                        Log.e("linhaojian","code : "+code+"       msg : "+msg);
                    }
                }).buidler(MainActivity.this);
            }
        });
    }
}
