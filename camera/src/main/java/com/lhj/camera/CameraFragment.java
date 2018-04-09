package com.lhj.camera;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;



public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {

    public static final String TAG = CameraFragment.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_mode";
    public static final String PREVIEW_HEIGHT_KEY = "preview_height";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int mCameraID;
    private String mFlashMode;
    private Camera mCamera;
    private SquareCameraPreview mPreviewView;
    private SurfaceHolder mSurfaceHolder;

    private int mDisplayOrientation;
    private int mLayoutOrientation;

    private int mCoverHeight;
    private int mPreviewHeight;

    private CameraOrientationListener mOrientationListener;
    private ImageView camera_back;
    private ImageView camera_glitter,camera_time,camera_change,camera_look_photo,camera_take_photo;
    private SharedPreferences mPreferences;
    private Editor mEditor;
    private int photoScale=1;//1：放大 0：缩小
    
    private Bitmap mBitmap;
    private boolean isCameraOpen=false;
    private boolean isPhoto;
    private int second = 0;

    public static Fragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	mOrientationListener = new CameraOrientationListener(getActivity());
        return inflater.inflate(R.layout.photo_new, container, false);
    }

    @Override
    public void onViewCreated(View view,   Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            mCameraID = getBackCameraID();
            mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashMode = savedInstanceState.getString(CAMERA_FLASH_KEY);
            mPreviewHeight = savedInstanceState.getInt(PREVIEW_HEIGHT_KEY);
        }

        mPreferences=getActivity().getSharedPreferences("pic",getActivity().MODE_PRIVATE);
        mEditor=mPreferences.edit();
        
        mOrientationListener.enable();
        
        mPreviewView = (SquareCameraPreview) view.findViewById(R.id.camera_preview_view);
        mPreviewView.getHolder().addCallback(CameraFragment.this);

        if (mCoverHeight == 0) {
            ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = mPreviewView.getWidth();
                    mPreviewHeight = mPreviewView.getHeight();
//                    mCoverHeight = (mPreviewHeight - width) / 2;
                    mCoverHeight = 0;
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {
        }
        initUi(view);
    }

    private void initUi(View view){
        camera_back = (ImageView) view.findViewById(R.id.camera_back);
        camera_glitter = (ImageView) view.findViewById(R.id.camera_glitter);
        if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
            mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
            camera_glitter.setImageResource(R.mipmap.boldauto);
        } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
            mFlashMode = Camera.Parameters.FLASH_MODE_ON;
            camera_glitter.setImageResource(R.mipmap.bolt);
        } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
            mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
            camera_glitter.setImageResource(R.mipmap.boldclose);
        }
        camera_time = (ImageView) view.findViewById(R.id.camera_time);
        camera_time.setImageResource(R.mipmap.cclose);
        camera_change = (ImageView) view.findViewById(R.id.camera_change);
        camera_look_photo = (ImageView) view.findViewById(R.id.camera_look_photo);
        String path=mPreferences.getString("path", "");
        if(!TextUtils.isEmpty(path)){
            mBitmap = ImageUtility.decodeSampledBitmapFromPath(path, 200, 200);
            camera_look_photo.setImageBitmap(mBitmap);
        }
        camera_take_photo = (ImageView) view.findViewById(R.id.camera_take_photo);
        initListeners();
    }

    private void initListeners(){
        camera_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CameraActivity)getActivity()).finishThis();
            }
        });
        camera_glitter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                    camera_glitter.setImageResource(R.mipmap.bolt);
                } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                    camera_glitter.setImageResource(R.mipmap.boldclose);
                } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                    camera_glitter.setImageResource(R.mipmap.boldauto);
                }
                setupCamera();
            }
        });
        camera_time.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(second==0){
                    second=1;
                    camera_time.setImageResource(R.mipmap.ctime);
                }else if(second==1){
                    second=2;
                    camera_time.setImageResource(R.mipmap.twosecend);
                }else if(second==2){
                    second=3;
                    camera_time.setImageResource(R.mipmap.threesecond);
                }else if(second==3){
                    second=0;
                    camera_time.setImageResource(R.mipmap.cclose);
                }
            }
        });
        camera_change.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraID = getBackCameraID();
                } else {
                    mCameraID = getFrontCameraID();
                }
                restartPreview();
            }
        });
        camera_look_photo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivity(intentFromGallery);
            }
        });
        camera_take_photo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPhoto){
                    takePicture();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putString(CAMERA_FLASH_KEY, mFlashMode);
        outState.putInt(PREVIEW_HEIGHT_KEY, mPreviewHeight);
        super.onSaveInstanceState(outState);
    }

    private void getCamera(int cameraID) {
        Log.d(TAG, "get camera with id " + cameraID);
        try {
        	if(isCameraOpen){
        		return;
        	}
        	isCameraOpen=true;
            mCamera = Camera.open(cameraID);
            mPreviewView.setCamera(mCamera);
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
        }
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
    	try {
        determineDisplayOrientation();
        setupCamera();

            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
//            e.printStackTrace();
        }
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        // Nulls out callbacks, stops face detection
        try {
        	isCameraOpen=false;
			mCamera.stopPreview();
			mPreviewView.setCamera(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // Camera direction
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mDisplayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        mLayoutOrientation = degrees;

        mCamera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
        } else {
        }

        // Lock in the changes
        mCamera.setParameters(parameters);        
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }

    private Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;
        Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private void restartPreview() {
        stopCameraPreview();
        mCamera.release();

        getCamera(mCameraID);
        startCameraPreview();
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * Take a picture
     */
    public void takePicture() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isPhoto||onStop){
                    return;
                }
                try {
                    isPhoto = true;
                    mOrientationListener.rememberOrientation();

                    // Shutter callback occurs after the image is captured. This can
                    // be used to trigger a sound to let the user know that image is taken
                    Camera.ShutterCallback shutterCallback = null;

                    // Raw callback occurs when the raw image data is available
                    Camera.PictureCallback raw = null;

                    // postView callback occurs when a scaled, fully processed
                    // postView image is available.
                    Camera.PictureCallback postView = null;

                    // jpeg callback occurs when the compressed image is available
                    mCamera.takePicture(shutterCallback, raw, postView, CameraFragment.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },second*1000);
    }

    private boolean onStop=false;
    @Override
    public void onStop() {
    	try {
    		onStop=true;
			mOrientationListener.disable();

			// stop the preview
			stopCameraPreview();
			mCamera.release();
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
        super.onStop();
    }

    @Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(onStop){
			onStop=false;
			restartPreview();
		}
	}
    
    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(mBitmap!=null){
    		mBitmap.recycle();
    	}
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;        	
        getCamera(mCameraID);
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
    }

    /**
     * A picture has been taken
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        int rotation = (
                mDisplayOrientation
                        + mOrientationListener.getRememberedNormalOrientation()
                        + mLayoutOrientation
        ) % 360;

        Bitmap bitmap = ImageUtility.rotatePicture(getActivity(), rotation, data);
        File mediaStorageDir = new File(
        		Environment.getExternalStorageDirectory().getPath()+"/DCIM/camera"
        );
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String path =mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
        Uri uri = ImageUtility.savePicture(getActivity(), bitmap,path);
        mEditor.putString("path", path);
        mEditor.commit();
        if(mBitmap!=null){
        	mBitmap.recycle();
        }
        mBitmap = ImageUtility.decodeSampledBitmapFromPath(path, 200, 200);
        camera_look_photo.setImageBitmap(mBitmap);
        mCamera.startPreview();
        isPhoto = false;
    }

    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            return mRememberedNormalOrientation;
        }
    }
}
