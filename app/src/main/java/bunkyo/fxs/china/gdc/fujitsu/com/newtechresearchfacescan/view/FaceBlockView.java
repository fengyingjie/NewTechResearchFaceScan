package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.BaiduFaceClient;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceData;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceDetector;

import static bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceDetector.*;

public class FaceBlockView extends SurfaceView implements SurfaceHolder.Callback {

    /**面部识别数据**/
//    private volatile FaceData[] mFaceData = null;
//    private volatile String mExtraData = null;

    private HandlerThread handlerThread;
    private Handler handler;

    /**画布对象**/
    private Canvas mCanvas = null;

    private Paint mTextPaint = null;
    private Paint mRectPaint = null;

    private FaceDetector mFaceDetector = null;

    private SurfaceHolder mHolder = null;

    private int mPreviewWidth;
    private float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight;
    private float mHeightScaleFactor = 1.0f;

    //private Bitmap mPhoto = null;
    public FaceBlockView(Context context) {
        super(context);
        init();
    }

    public FaceBlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceBlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FaceBlockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT); // 设置为透明
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.YELLOW);
        mTextPaint.setStyle(Paint.Style.FILL);
        mRectPaint = new Paint();
        mRectPaint.setColor(Color.BLUE);
        mRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRectPaint.setStrokeWidth(5);
        setZOrderOnTop(true);// 设置为顶端
        BaiduFaceClient.init();

        handlerThread = new HandlerThread("FaceBlockView");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_PREVIEW_READY:
                        //msg.getData();
                        break;
                    case MSG_FACEBLOCKVIEW_READY:
                        break;
                    case MSG_FACERECT_READY:
                        Log.i("FaceBlockView","MSG_FACERECT_READY start At:"+String.valueOf(System.currentTimeMillis()));

                        ArrayList<Rect> faces = (ArrayList<Rect>)(msg.getData().get("FACEDATA"));
                        int PIC_WIDTH = (int) msg.getData().get("PIC_WIDTH");
                        int PIC_HEIGHT = (int) msg.getData().get("PIC_HEIGHT");

                        try{
                            /**在这里加上线程安全锁**/
                            synchronized (mHolder) {

                                /**拿到当前画布 然后锁定**/
                                mCanvas = mHolder.lockCanvas();
                                mCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);

                                mWidthScaleFactor = (float)mCanvas.getWidth() / (float)PIC_WIDTH;
                                mHeightScaleFactor = (float)mCanvas.getHeight() / (float)PIC_HEIGHT;

                                for(Rect face : faces) {
                                    int left = transformX(face.left);
                                    int right = transformX(face.right);
                                    int top = transformY(face.top);
                                    int bottom = transformY(face.bottom);

                                    //canvas.drawRect(left,top,right,bottom,mRectPaint);

                                    mCanvas.drawLine(left, top, right, top, mRectPaint);
                                    mCanvas.drawLine(left, bottom, right, bottom, mRectPaint);
                                    mCanvas.drawLine(left, top, left, bottom, mRectPaint);
                                    mCanvas.drawLine(right, top, right, bottom, mRectPaint);
                                }

                                /**计算出一次更新的毫秒数**/
                                int diffTime = (int) (System.currentTimeMillis() - msg.getWhen());
                                int fps = 1000;
                                if(diffTime > 1) {
                                    fps = 1000 / diffTime;
                                }

                                String debugText = "fps:" + String.valueOf(fps);
                                mTextPaint.setTextSize(50);
                                mCanvas.drawText(debugText, 0, 100, mTextPaint);
                            }
                        } catch (Exception e) {
                        } finally {
                            if (mCanvas != null)
                                mHolder.unlockCanvasAndPost(mCanvas);//保证每次都将绘图的内容提交
                            mCanvas = null;
                        }

                        Log.i("FaceBlockView","MSG_FACERECT_READY end At:"+String.valueOf(System.currentTimeMillis()));
                        break;
                    case MSG_FACEDATA_READY:
                        Log.i("FaceBlockView","MSG_FACEDATA_READY start At:"+String.valueOf(System.currentTimeMillis()));

                        byte[] bytes = (byte[])(msg.getData().get("FACEDATA"));
                        BitmapFactory.Options bitmapOption = new BitmapFactory.Options();
                        //图片的参数(这个参数要有，不然找不到人脸)
                        //bitmapOption.inPreferredConfig = Bitmap.Config.RGB_565;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,bitmapOption);
                        ArrayList facesData = BaiduFaceClient.search(bitmap);
//                        ArrayList facesData = BaiduFaceClient.detect(bitmap);
//                        if(facesData!=null && facesData.size() ==1){
//                            FaceData faceData = BaiduFaceClient.search(bitmap);
//                            FaceData faceRectData = (FaceData)facesData.get(0);
//                            if(faceData !=null) {
//                                faceRectData.setId(faceData.getId());
//                                faceRectData.setName(faceData.getName());
//                            }else{
//                                faceRectData.setId("i don't know");
//                                faceRectData.setName("i don't know");
//                            }
//                        }

                        //ArrayList<FaceData> facesData = new ArrayList<FaceData>();
                        //facesData.add(new FaceData(new Rect(0,0,bitmap.getWidth()-100,bitmap.getHeight()-100)));
                        //msg.getData().clear();
                        try{
                            /**在这里加上线程安全锁**/
                            synchronized (mHolder) {

                                /**拿到当前画布 然后锁定**/
                                mCanvas = mHolder.lockCanvas();
                                mCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);

                                mWidthScaleFactor = (float)mCanvas.getWidth() / (float)bitmap.getWidth();
                                mHeightScaleFactor = (float)mCanvas.getHeight() / (float)bitmap.getHeight();

                                drawFaceRect(facesData);

                                /**计算出一次更新的毫秒数**/
                                int diffTime = (int) (System.currentTimeMillis() - msg.getWhen());
                                int fps = 1000;
                                if(diffTime > 1) {
                                    fps = 1000 / diffTime;
                                }

                                String debugText = "fps:" + String.valueOf(fps);
                                mTextPaint.setTextSize(50);
                                mCanvas.drawText(debugText, 0, 100, mTextPaint);
                            }
                        } catch (Exception e) {
                        } finally {
                            if (mCanvas != null)
                                mHolder.unlockCanvasAndPost(mCanvas);//保证每次都将绘图的内容提交
                                mCanvas = null;
                        }

                        Log.i("FaceBlockView","MSG_FACEDATA_READY end At:"+String.valueOf(System.currentTimeMillis()));
                        break;
                    default:break;
                }
            }
        };


    }

    public void sendMessage(Message msg) {
        if(!this.handler.hasMessages(msg.what)){
            this.handler.sendMessage(msg);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mFaceDetector != null){
            mFaceDetector.mFaceBlock = this;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void drawFaceRect(ArrayList faces) {
        //mCanvas
        if(faces == null){
            return;
        }

        for(int i=0; i<faces.size();i++){
            FaceData faceData = (FaceData) faces.get(i);
            int left = transformX(faceData.getFaceRect().left);
            int right = transformX(faceData.getFaceRect().right);
            int top = transformY(faceData.getFaceRect().top);
            int bottom = transformY(faceData.getFaceRect().bottom);

            //canvas.drawRect(left,top,right,bottom,mRectPaint);

            mCanvas.drawLine(left,top,right,top,mRectPaint);
            mCanvas.drawLine(left,bottom,right,bottom,mRectPaint);
            mCanvas.drawLine(left,top,left,bottom,mRectPaint);
            mCanvas.drawLine(right,top,right,bottom,mRectPaint);

            //mTextPaint.setTextSize((bottom-top)/3);
            if(faceData.getId() !=null && faceData.getId().trim().length() > 0) {
                mTextPaint.setTextSize(30);
                mCanvas.drawText("id:" + faceData.getId(), left, bottom, mTextPaint);
            }
            Log.i("FaceBlockView", "Face Founnd");
        }
    }

    private int transformX(int x){

        int newX;
        //平移
        //缩放
        newX = (int) (x * mWidthScaleFactor);
        return newX;
    }

    private int transformY(int y){
        int newY;
        //平移
        //缩放
        newY = (int) (y * mHeightScaleFactor);
        return newY;
    }
}
