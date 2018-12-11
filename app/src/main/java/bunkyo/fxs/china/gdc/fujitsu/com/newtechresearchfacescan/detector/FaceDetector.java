package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.BaiduFaceClient;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view.FaceBlockView;

/**
 * 这个类负责把接收到的图片数据提交到百度进行检测，并对结果进行计算最后发起View的重绘
 */
public class FaceDetector {

    public static final int MSG_PREVIEW_READY = 10;
    public static final int MSG_FACEBLOCKVIEW_READY = 20;
    public static final int MSG_FACEDATA_READY = 30;

    private ThreadGroup mThreadGroup;
    public FaceBlockView mFaceBlock;
    private volatile Bitmap mPhoto;
    private HandlerThread handlerThread;
    private Handler handler;

    private class DetectorThread extends Thread{

        public DetectorThread(){
            super(mThreadGroup,"DetectorThread");
        }

        @Override
        public void run() {
            super.run();
            ArrayList baiduFaceData = BaiduFaceClient.detect(mPhoto);
            if(mFaceBlock !=null) {
                if(baiduFaceData !=null && ((ArrayList) baiduFaceData).size() >0) {
                    Log.e("tag","!!!!!!DetectorThread if !!!!!!!!");
                    //mFaceBlock.setFaceData((FaceData[]) baiduFaceData.toArray(new FaceData[]{}),
                    //        "activeCount:" + String.valueOf(mThreadGroup.activeCount()));
                }else{
                    Log.e("tag","!!!!!!DetectorThread else!!!!!!!!");
//                    mFaceBlock.setFaceData(null,
//                            "activeCount:" + String.valueOf(mThreadGroup.activeCount()));
                }
            }
            //int[] result = BaiduFaceClient.detect(mPhoto);
            //mFaceBlock.addFaceBlocks(result);
            //mFaceBlock.postInvalidate();
        }
    }

    public FaceDetector(FaceBlockView blockView){
        handlerThread = new HandlerThread("FaceDetector");
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
                        mFaceBlock = (FaceBlockView)(msg.getData().get("ViewObject"));
                        break;
                    case MSG_FACEDATA_READY:
                        break;
                    default:break;
                }
            }
        };

        mThreadGroup = new ThreadGroup("FaceDetector");
        BaiduFaceClient.init();
        mFaceBlock = blockView;
    }

    public void sendMessage(Message message){
        handler.sendMessage(message);
    }
    public void detect(Bitmap photo){
        mPhoto = photo;
        if(mThreadGroup.activeCount() < 500) {
            new DetectorThread().start();
        }
    }
}