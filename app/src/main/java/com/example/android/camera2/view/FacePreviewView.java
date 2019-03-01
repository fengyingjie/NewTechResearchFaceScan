package com.example.android.camera2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.android.camera2.FaceCamera;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceDetector;

/**
 * TODO: document your custom view class.
 */
public class FacePreviewView extends SurfaceView implements SurfaceHolder.Callback {

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "FacePreviewView";

    private SurfaceHolder mHolder;

    private FaceCamera mFaceCamera;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * 图片数据的格式(JPG,RAW等)
     */
    private int mImageFormat;

    public FacePreviewView(Context context) {
        super(context);
        init(null, 0);
    }

    public FacePreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FacePreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        //从设定文件中去除图像类型参数，如没有设定，则默认设定问JPEG
        int imageFormat = attrs.getAttributeIntValue("", "ImageFormat", ImageFormat.JPEG);
        mHolder = getHolder();
        mHolder.addCallback(this);
        //mFaceCamera = new FaceCamera(imageFormat,this);
        //mFaceDetector = new FaceDetector(10);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mHolder.setKeepScreenOn(true);
        mFaceCamera.openCamera(getWidth(), getHeight());
//        //打开相机
//        openCamera(this.getWidth(), this.getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mImageFormat = format;
        mHolder.setFormat(mImageFormat);
//        configureTransform(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mFaceCamera.closeCamera();
    }

    public void setFaceCamera(FaceCamera faceCamera){
        mFaceCamera = faceCamera;
    }
}
