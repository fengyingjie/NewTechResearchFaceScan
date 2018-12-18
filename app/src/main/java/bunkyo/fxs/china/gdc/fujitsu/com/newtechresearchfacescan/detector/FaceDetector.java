package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.BaiduFaceClient;

/**
 * 这个类负责把接收到的图片数据提交到百度进行检测，并返回结果。
 */
public class FaceDetector {

    private ThreadGroup mThreadGroup;
    private volatile ConcurrentLinkedQueue<byte[]> mPhotoQueue;
    private int mMaxThreadCount;

    private class DetectorThread extends Thread{

        public DetectorThread(){
            super(mThreadGroup,"DetectorThread");
        }

        @Override
        public void run() {
            Log.d("FaceDetector","mPhotoQueue.size()：" + mPhotoQueue.size() + ",mThreadGroup.activeCount() :" + mThreadGroup.activeCount() );
            super.run();
            byte[] bitmap = mPhotoQueue.poll();

            while(bitmap != null) {
                Log.d("FaceDetector","mPhotoQueue.size()：" + mPhotoQueue.size() + ",mThreadGroup.activeCount() :" + mThreadGroup.activeCount() );
                ArrayList<FaceData> baiduFaceData = BaiduFaceClient.search(BitmapFactory.decodeByteArray(bitmap,0,bitmap.length));

                if(baiduFaceData!=null && baiduFaceData.size() > 0){
                    for(int i=0;i<baiduFaceData.size();i++){
                        String name = baiduFaceData.get(i).getName();
                        if(!"unknow".equals(name)) {
                            Log.d("FaceDetector", "find:" + name);
                        }
                    }
                }
                bitmap = mPhotoQueue.poll();
            }
        }
    }

    public FaceDetector(int maxThreadCount){

        mMaxThreadCount = maxThreadCount;
        mThreadGroup = new ThreadGroup("FaceDetector");
        mPhotoQueue = new ConcurrentLinkedQueue<byte[]>();
        BaiduFaceClient.init();
    }

    public void detect(byte[] image){

        if(mPhotoQueue.size() < mMaxThreadCount){
            mPhotoQueue.offer(image);
        }else{
            Log.d("FaceDetector","PhotoQueue Count OverFlow" );
        }

        if(mThreadGroup.activeCount() < mMaxThreadCount) {

            for(int i=0; i < mPhotoQueue.size(); i++){
                new DetectorThread().start();
            }
        }
    }
}