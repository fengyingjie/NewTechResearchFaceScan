package com.example.android.camera2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.android.camera2.FaceCamera;
import com.example.android.camera2.FaceCamera.OnFaceAvailableListener;

/**
 * TODO: document your custom view class.
 */
public class FaceBlockView extends SurfaceView implements SurfaceHolder.Callback, OnFaceAvailableListener {

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "FaceBlockView";

    private SurfaceHolder mHolder;
    private Canvas mCanvas = null;

    private Paint mTextPaint = null;
    private Paint mRectPaint = null;
    private Matrix mMatrix = null;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    public FaceBlockView(Context context) {
        super(context);
        init(null, 0);
    }

    public FaceBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FaceBlockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mMatrix = new Matrix();

        mHolder.setFormat(PixelFormat.TRANSPARENT); // 设置为透明
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.YELLOW);
        mTextPaint.setStyle(Paint.Style.FILL);
        mRectPaint = new Paint();
        mRectPaint.setColor(Color.BLUE);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(5);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mHolder.setKeepScreenOn(true);
        startBackgroundThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        RectF destRect = new RectF(0,0,width,height);
//        boolean result = mMatrix.setRectToRect(destRect, destRect, Matrix.ScaleToFit.FILL);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopBackgroundThread();
    }

    public void drawRect(){

    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("FacePreviewViewThread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper()){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                mCanvas = mHolder.lockCanvas();
                mCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
                if (msg.what==1){



                    //mCanvas.setMatrix(mMatrix);
                    RectF rect = msg.getData().getParcelable("faceRect");
                    mCanvas.drawRect(rect,mRectPaint);
                    //mCanvas.drawRect(0,0,100,100,mRectPaint);


                }
                mHolder.unlockCanvasAndPost(mCanvas);
                mCanvas = null;
            }
        };
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFaceAvailable(Size output, RectF faceRect) {
        Message msg = new Message();
        msg.what = 1;
        Bundle bul =  new Bundle();
        RectF mapedRect = new RectF();
        msg.setData(bul);
        mMatrix.setRectToRect(new RectF(0,0,output.getWidth(),output.getHeight()),
            new RectF(0,0,getWidth(),getHeight()),Matrix.ScaleToFit.FILL);
        mMatrix.mapRect(mapedRect,faceRect);
        bul.putParcelable("faceRect",mapedRect);
        mBackgroundHandler.sendMessage(msg);
    }
}
