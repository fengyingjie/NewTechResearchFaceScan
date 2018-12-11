package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.BaiduFaceClient;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.MainActivity;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.R;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceDetector;

import static android.content.ContentValues.TAG;
import static android.os.Looper.getMainLooper;
import static bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceDetector.MSG_FACEDATA_READY;

public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback ,Runnable {

    public static final int REQUEST_CAMERA_CODE = 100;

    //private FaceDetector.Face[] mFaces = null;//new FaceDetector.Face[50];
    //private FaceDetector mFaceDetector = null;//new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 50);

    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler2,childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private Thread mWorkThread;
    private boolean mWorkingFlg;
    private boolean mTakingPicFlg;
    private FaceDetector mFaceDetector;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private FaceBlockView mBlockView;

    static

    {

        ORIENTATIONS.append(Surface.ROTATION_0, 90);

        ORIENTATIONS.append(Surface.ROTATION_90, 180);

        ORIENTATIONS.append(Surface.ROTATION_180, 270);

        ORIENTATIONS.append(Surface.ROTATION_270, 0);

    }

    private SurfaceHolder mHolder = null;
    private boolean isOpen = false;// 相机是否打开

    public CameraPreviewView(Context context) {
        super(context);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mHolder = getHolder();
        mHolder.addCallback(this);
        //mWorkThread = new Thread(this);
        mWorkingFlg = false;

//        mHolder.setFormat(PixelFormat.TRANSPARENT); // 设置为透明
//        setZOrderOnTop(true);// 设置为顶端

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera2();

        mBlockView = (FaceBlockView) this.getRootView().findViewById(R.id.faceBlock);
//        mWorkingFlg = true;
//        if(!mWorkThread.isAlive()){
//            mWorkThread.start();
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        mWorkingFlg = false;
//        try {
//            mWorkThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 初始化Camera2
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        HandlerThread handlerThreadFindFace = new HandlerThread("FindFace");
        handlerThreadFindFace.start();
        childHandler = new Handler(handlerThread.getLooper());
        childHandler2 = new Handler(handlerThreadFindFace.getLooper());
        mainHandler = new Handler(getMainLooper());

        mImageReader = ImageReader.newInstance(this.getWidth(), this.getHeight(), ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {

                Log.i("onImageAvailable","onImageAvailable Start At:"+String.valueOf(System.currentTimeMillis()));
                //mTakingPicFlg = true;
                //mCameraDevice.close();
                // 拿到拍照照片数据
                //synchronized (reader) {
                Image image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                if(mBlockView !=null) {
                    //mFaceDetector.mFaceBlock = (FaceBlockView) findViewById(R.id.faceBlock);
                    Message msg = Message.obtain();
                    msg.what = MSG_FACEDATA_READY;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("FACEDATA",bytes);
                    msg.setData(bundle);
                    Log.i("onImageAvailable","mBlockView.sendMessage At:"+String.valueOf(System.currentTimeMillis()));
                    mBlockView.sendMessage(msg);
                }

                image.close();
                image = null;
                buffer = null;
                bytes = null;
            }
        }, childHandler);

        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;//前摄像头

        //获取摄像头管理
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                return;
            } else {
                //打开摄像头
                mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(mHolder.getSurface());
            previewRequestBuilder.addTarget(mImageReader.getSurface());
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mHolder.getSurface(),mImageReader.getSurface()), new CameraCaptureSession.StateCallback() // ③
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    // 自动对焦
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    // 打开闪光灯
                    previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    // 显示预览
                    // 获取手机方向
                    int rotation = new Float(getRootView().getRotation()).intValue();
                    // 根据设备方向计算设置照片的方向
                    previewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

                    CaptureRequest previewRequest = previewRequestBuilder.build();

                    final CameraCaptureSession.CaptureCallback capCallback = new CameraCaptureSession.CaptureCallback() {

                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            Log.e("ee", "onCaptureCompleted");

                        }
                    };

                    try {
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, capCallback, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getContext(), "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        Log.e("tag","!!!!!!takePicture!!!!!!!!");
        if (mCameraDevice == null) return;
        // 创建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
            int rotation = new Float(getRootView().getRotation()).intValue();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, childHandler);
            //mCameraCaptureSession.capture(mCaptureRequest, null, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 摄像头创建监听
     */
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//打开摄像头
            mCameraDevice = camera;
            //开启预览
            takePreview();
            //takePicture();
            mWorkingFlg = true;

//            if(mWorkThread!=null && !mWorkThread.isAlive()){
//                mWorkThread.start();
//            }

        }

        @Override
        public void onDisconnected(CameraDevice camera) {//关闭摄像头
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
//                mWorkingFlg = false;
//                if(mWorkThread!=null && mWorkThread.isAlive()){
//                    try {
//                        mWorkThread.join();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
//                mWorkingFlg = false;
//                if(mWorkThread!=null && mWorkThread.isAlive()){
//                    try {
//                        mWorkThread.join();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
            Toast.makeText(getContext(), "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void run() {

//        while(true){
//            if(mCameraDevice !=null) {
//                synchronized (mCameraDevice) {
//
//                        //takePicture();
//
//                    //AppCompatActivity
//
////                FaceBlockView blockView = (FaceBlockView)findViewById(R.id.faceBlock);
////                if(blockView!=null) {
////                    blockView.drawFaceBlock(0, 0, 1000, 1000);
////                }
//
////                    try {
////                        Thread.sleep(100);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
//                }
//            }
//        }
    }
}

