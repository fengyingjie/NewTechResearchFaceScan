package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view.FaceBlockView;

public class BlockOverlay {

    private FaceBlockView mBlockView;
    private volatile Bitmap mFaceData;

    public BlockOverlay(FaceBlockView blockView){
        mBlockView = blockView;
    }

    public void draw(Canvas canvas){
        // Confirm that the face and its features are still visible
        // before drawing any graphics over it.
        if (mFaceData == null) {
            return;
        }

//        int[] block = BaiduFaceClient.detect(mFaceData);
//        Paint pen = new Paint();
//        pen.setAlpha(1);
//        pen.setColor(Color.GREEN);
//        canvas.drawRect(block[0],block[1],block[0]+block[2],block[1]+block[3],pen);
    }

    /**
     * Adjusts a horizontal value of the supplied value from the preview scale to the view
     * scale.
     */
    public float scaleX(float horizontal) {
        return horizontal;// * mBlockView.mWidthScaleFactor;
    }

    /**
     * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
     */
    public float scaleY(float vertical) {

        return vertical;// * mBlockView.mHeightScaleFactor;
    }

    /**
     * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateX(float x) {
//        if (mBlockView.mFacing == CameraSource.CAMERA_FACING_FRONT) {
//            return mBlockView.getWidth() - scaleX(x);
//        } else {
//            return scaleX(x);
//        }
        return scaleX(x);
    }

    /**
     * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateY(float y) {
        return scaleY(y);
    }

    public void postInvalidate() {
        mBlockView.postInvalidate();
    }


    void update(Bitmap  faceData) {
        mFaceData  = faceData;
        postInvalidate(); // Trigger a redraw of the graphic (i.e. cause draw() to be called).
    }
}
