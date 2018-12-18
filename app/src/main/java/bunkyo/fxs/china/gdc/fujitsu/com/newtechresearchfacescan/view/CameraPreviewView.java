package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.R;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceDetector;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.ImageSaver;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Looper.getMainLooper;
import static bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view.FaceBlockView.MSG_FACEDATA_READY;
import static bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view.FaceBlockView.MSG_FACERECT_READY;

public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback {

    private static String LOG_TAG = "CameraPreviewView";
    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler2,childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private boolean mTakingPicFlg;
    private FaceDetector mFaceDetector;
    private FaceBlockView mBlockView;
    private SurfaceHolder mHolder;
    private SparseIntArray ORIENTATIONS;
    private boolean mFaceDetectSupported;
    private Integer mFaceDetectMode;

    private static final SparseIntArray ORIENTATIONS_BACK = new SparseIntArray();
    static
    {
        //1.绕原点逆时针旋转90°，2，绕y轴反转180°
        ORIENTATIONS_BACK.append(Surface.ROTATION_0, 90);
        ORIENTATIONS_BACK.append(Surface.ROTATION_90, 0);
        ORIENTATIONS_BACK.append(Surface.ROTATION_180, 270);
        ORIENTATIONS_BACK.append(Surface.ROTATION_270, 180);
    }
    private static final SparseIntArray ORIENTATIONS_FRONT = new SparseIntArray();
    static
    {
        ORIENTATIONS_FRONT.append(Surface.ROTATION_0, 270);
        ORIENTATIONS_FRONT.append(Surface.ROTATION_90, 0);
        ORIENTATIONS_FRONT.append(Surface.ROTATION_180, 90);
        ORIENTATIONS_FRONT.append(Surface.ROTATION_270, 180);
    }

    private Size mPreviewSize;
    private Size mPixelSize;
    private boolean mHaveFace;

    // 为Size定义一个比较器Comparator
    static class CompareSizesByArea implements Comparator<Size>
    {
        @Override
        public int compare(Size lhs, Size rhs)
        {
            // 强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }


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
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera2();
        mBlockView = (FaceBlockView) this.getRootView().findViewById(R.id.faceBlock);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 计算合适的预览尺寸
     */
    private static Size chooseOptimalSize(Size[] choices
            , int width, int height, Size aspectRatio)
    {
        // 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices)
        {
            //当高宽比例相等(无变型) 并且 大于等于预览高度和宽度时加入备选列表
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height)
            {
                bigEnough.add(option);
            }
        }
        // 如果找到多个预览尺寸，获取其中面积最小的
        if (bigEnough.size() > 0)
        {
            return Collections.min(bigEnough, new CompareSizesByArea());
        }
        else
        {
            Log.e(LOG_TAG,"找不到合适的预览尺寸");
            return choices[0];
        }
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

        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;//前摄像头

        //获取摄像头管理
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                return;
            } else {

                //取得相机一览
                String[] cameraIDList = mCameraManager.getCameraIdList();
                if(cameraIDList == null){
                    return;
                }

                //遍历全部的相机
                for (int i=0; i<cameraIDList.length;i++){

                    String cameraID = cameraIDList[i];
                    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraID);

                    //检查相机是否支持Camera2接口(完全支持非必须条件)
                    int supporedLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL != supporedLevel){
                        Log.i(LOG_TAG,"相机不完全兼容Camera2接口");
                    }

                    int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    int a = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    if(lensFacing == CameraCharacteristics.LENS_FACING_BACK){
                        Log.i(LOG_TAG,"LENS_FACING_BACK Founed");
                        mCameraID = cameraID;
                        ORIENTATIONS = ORIENTATIONS_BACK;
                        //float[] b = characteristics.get(CameraCharacteristics.LENS_POSE_ROTATION);
                    }

                    StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);


                    //Rect cRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);//获取成像区域
                    mPixelSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);//获取成像尺寸，同上

                    Size largest = Collections.max(
                            Arrays.asList(sizes),
                            new CompareSizesByArea());
                    //设置预览大小
                    mPreviewSize = chooseOptimalSize(sizes,this.getWidth(),this.getHeight(),largest);

                    //获取人脸检测参数
                    int[] FD =characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
                    int maxFD=characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);

                    if (FD.length>0) {
                        List<Integer> fdList = new ArrayList<>();
                        for (int FaceD : FD ) {
                            fdList.add(FaceD);
                            Log.e(LOG_TAG, "setUpCameraOutputs: FD type:" + Integer.toString(FaceD));
                        }
                        Log.e(LOG_TAG, "setUpCameraOutputs: FD count" + Integer.toString(maxFD));

                        if (maxFD > 0) {
                            mFaceDetectSupported = true;
                            mFaceDetectMode = Collections.max(fdList);
                        }
                    }
                }

                //打开摄像头
                mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {

                Log.e("onImageAvailable","onImageAvailable Start At:"+String.valueOf(System.currentTimeMillis()));
                //mTakingPicFlg = true;
                //mCameraDevice.close();
                // 拿到拍照照片数据
                //synchronized (reader) {

                Image image = reader.acquireLatestImage();

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                if(mBlockView !=null && mHaveFace) {
//                    File mFile = new File(getContext().getExternalFilesDir(DIRECTORY_PICTURES), "pic_"+String.valueOf(System.currentTimeMillis())+".jpg");
//                    childHandler2.post(new ImageSaver(bytes, mFile));
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

                    previewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);
                    // 显示预览
                    // 获取手机方向
                    int rotation = new Float(getRootView().getRotation()).intValue();
                    // 根据设备方向计算设置照片的方向
                    previewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

                    CaptureRequest previewRequest = previewRequestBuilder.build();

                    final CameraCaptureSession.CaptureCallback capCallback = new CameraCaptureSession.CaptureCallback() {

                        /**
                         * 对摄像头返回的结果进行处理,并获取人脸数据
                         * @param result 摄像头数据
                         */
                        private void process(CaptureResult result) {

                            //获得Face类
                            Face faces[]=result.get(CaptureResult.STATISTICS_FACES);

                            //如果有人脸的话
                            if (faces.length>0 ){

                                mHaveFace = true;

                                Log.e(LOG_TAG, "face detected " + Integer.toString(faces.length));

                                //获取人脸矩形框
                                ArrayList<Rect> FACEDATA = new ArrayList<Rect>();

                                for(Face face : faces){
                                    RectF boundf = new RectF(face.getBounds());
                                    RectF viewRectF = new RectF();
                                    Rect viewRect = new Rect();

                                    // 构造一个matrix
                                    Matrix matrix = new Matrix();
                                    matrix.setRotate(90,mPixelSize.getWidth()/2,mPixelSize.getHeight()/2);
                                    matrix.mapRect(viewRectF,boundf);

                                    viewRectF.roundOut(viewRect);
                                    FACEDATA.add(viewRect);
                                }
                                //Log.e(LOG_TAG, FACEDATA.toString());

                                if(mBlockView !=null && FACEDATA.size() > 0) {
                                    //mFaceDetector.mFaceBlock = (FaceBlockView) findViewById(R.id.faceBlock);
                                    Message msg = new Message();
                                    msg.what = MSG_FACERECT_READY;
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("FACEDATA",FACEDATA);
                                    //图片转了90°所以高和宽要互换
                                    bundle.putSerializable("PIC_WIDTH",mPixelSize.getHeight());
                                    bundle.putSerializable("PIC_HEIGHT",mPixelSize.getWidth());
                                    msg.setData(bundle);
                                    mBlockView.sendMessage(msg);
                                }
                            }else{
                                mHaveFace = false;
                            }
                        }

                        //@Override
                        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureProgressed(session, request, result);
                            Log.e(LOG_TAG,"onCaptureProgressed");
                            process(result);
                        }

                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            Log.e("ee", "onCaptureCompleted");
                            process(result);
                        }
                    };

                    try {
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, capCallback, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                    try {
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, capCallback, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                };

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
        }

        @Override
        public void onDisconnected(CameraDevice camera) {//关闭摄像头
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            Toast.makeText(getContext(), "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };
}

