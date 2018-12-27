package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
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
                            postDataToServer(name,String.valueOf(System.currentTimeMillis()));
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

    protected void postDataToServer(String id , String time){

        OutputStream os = null;
        HttpURLConnection connection = null;
        StringBuffer body = new StringBuffer("ACTION=FACEIN&ID=");
        try {
            body.append(URLEncoder.encode(id, "UTF-8"));
            body.append("&LOGINTIME=");
            body.append(URLEncoder.encode(time, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] data = body.toString().getBytes();
        URL url = null;
        try {


            //获得URL对象
            url = new URL("http://47.94.142.3/example/");
            //获得HttpURLConnection对象
            connection = (HttpURLConnection) url.openConnection();
            //设置超时时间
            connection.setConnectTimeout(10000);
            //设置读取超时时间
            connection.setReadTimeout(10000);
            // 设置请求方法为post
            connection.setRequestMethod("POST");
            //设置是否从httpUrlConnection读入,默认情况下是true;
            connection.setDoInput(false);
            //设置为true后才能写入参数
            connection.setDoOutput(true);
            //不使用缓存
            connection.setUseCaches(false);

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));

            os = connection.getOutputStream();
            os.write(data);
            os.flush();
            os.close();

            //connection.getInputStream();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d("FaceDetector","HttpURLConnection.HTTP_OK" );
            }else{
                Log.d("FaceDetector","HttpURLConnection.HTTP_NG" );
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            connection.disconnect();
            connection = null;
        }

    }
}