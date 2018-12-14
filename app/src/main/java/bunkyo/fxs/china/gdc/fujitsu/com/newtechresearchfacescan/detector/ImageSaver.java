package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector;

import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Saves a JPEG {@link Image} into the specified {@link File}.
 */
public class ImageSaver implements Runnable {

    /**
     * The JPEG image
     */
    private byte[] mImage;
    /**
     * The file we save the image into.
     */
    private File mFile;

    public ImageSaver(byte[] image, File file) {
        mImage = new byte[image.length];
        System.arraycopy(image, 0, mImage, 0, image.length);
        mFile = file;
    }

    @Override
    public void run() {
//            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
//            byte[] bytes = new byte[buffer.remaining()];
//            buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mFile);
            output.write(mImage);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage = null;
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}