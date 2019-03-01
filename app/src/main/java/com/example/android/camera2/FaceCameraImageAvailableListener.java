package com.example.android.camera2;

import android.media.Image;
import android.media.ImageReader;

import java.nio.ByteBuffer;

public class FaceCameraImageAvailableListener implements ImageReader.OnImageAvailableListener {

    public FaceCameraImageAvailableListener() {
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
//        Image mImage = reader.acquireNextImage();
//        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
//        byte[] bytes = new byte[buffer.remaining()];
//        buffer.get(bytes);
//
////            Log.d(TAG,"onImageAvailable mFaceDetector.detect");
////            mFaceDetector.detect(bytes);
//
////            Log.d(TAG,"onImageAvailable ImageSaver");
////            File file = new File(getContext().getExternalFilesDir(DIRECTORY_PICTURES), "pic_"+String.valueOf(System.currentTimeMillis())+".jpg");
////            mBackgroundHandler.post(new ImageSaver(bytes,file));
//
//        bytes = null;
//        mImage.close();
    }
}
