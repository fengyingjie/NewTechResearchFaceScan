package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceDetector;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.surfaceCtl.FaceBlockCtl;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view.CameraPreviewView;
import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.view.FaceBlockView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    //private FaceDetector mFacedetector;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(new FaceBlockView(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FaceBlockView faceBlockView = (FaceBlockView)findViewById(R.id.faceBlock);
//        faceBlockView.getHolder().addCallback(new FaceBlockCtl());
//
        CameraPreviewView prView = (CameraPreviewView)findViewById(R.id.previewSurface);
//        prView.getHolder().addCallback(new FaceBlockCtl());

        //mFacedetector = new FaceDetector(faceBlockView);
        //prView.setPhotoDetector(mFacedetector);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
