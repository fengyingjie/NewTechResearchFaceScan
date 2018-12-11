package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.surfaceCtl;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

public class FaceBlockCtl implements SurfaceHolder.Callback,Runnable{
    /**每30帧刷新一次屏幕**/
    public static final int TIME_IN_FRAME = 30;

    private SurfaceHolder mHolder;
    private Canvas mCanvas;//绘图的画布
    private boolean mIsDrawing;//控制绘画线程的标志位

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        mHolder = holder;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        while (mIsDrawing) {

            /**取得更新之前的时间**/
            long startTime = System.currentTimeMillis();

            draw();

            /**取得更新结束的时间**/
            long endTime = System.currentTimeMillis();

            /**计算出一次更新的毫秒数**/
            int diffTime  = (int)(endTime - startTime);

            /**确保每次更新时间为30帧**/
            while(diffTime <=TIME_IN_FRAME) {
                diffTime = (int)(System.currentTimeMillis() - startTime);
                /**线程等待**/
                Thread.yield();
            }

        }
    }

    //绘图操作
    private void draw() {
        try {
            /**在这里加上线程安全锁**/
            synchronized (mHolder) {
                /**拿到当前画布 然后锁定**/
                mCanvas =mHolder.lockCanvas();

                mCanvas.drawRect(0,1000,1000,1000,new Paint(Color.BLUE));

//                /**绘制结束后解锁显示在屏幕上**/
//                mHolder.unlockCanvasAndPost(mCanvas);
            }

        } catch (Exception e) {
        } finally {
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);//保证每次都将绘图的内容提交
        }
    }
}
