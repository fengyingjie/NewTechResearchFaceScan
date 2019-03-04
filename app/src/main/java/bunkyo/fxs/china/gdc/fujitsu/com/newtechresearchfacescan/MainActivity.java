package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan;

import android.graphics.ImageFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.example.android.camera2.FaceCamera;
import com.example.android.camera2.view.FacePreviewView;
import com.example.android.camera2.view.FaceBlockView;

public class MainActivity extends AppCompatActivity {

//    private TextView mTextMessage;

    private WebView mWebView;
    private FaceCamera mFaceCamera;
    private FacePreviewView mFacePreviewView;
    private FaceBlockView mFaceBlockView;
    //private FaceDetector mFacedetector;
//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mFacePreviewView = findViewById(R.id.previewSurface);
        mFaceBlockView = findViewById(R.id.blockFaceSurface);
        mFaceCamera = new FaceCamera(ImageFormat.JPEG, mFacePreviewView, mFaceBlockView);
        mFacePreviewView.setFaceCamera(mFaceCamera);
//        mWebView = (WebView)findViewById(R.id.webview);
//        mWebView.setBackgroundColor(0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mWebView.getSettings().setSafeBrowsingEnabled(false);
//        }
//
//
//        mWebView.setWebViewClient(new WebViewClient() {
//            //设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//        });
//        mWebView.getSettings().setJavaScriptEnabled(true);  //设置WebView属性,运行执行js脚本
//        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); //设置WebView属性,不用缓存
//        mWebView.loadUrl("http://www.fengyunxi.com/");          //调用loadUrl方法为WebView加入链接


//        mTextMessage = (TextView) findViewById(R.id.message);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
