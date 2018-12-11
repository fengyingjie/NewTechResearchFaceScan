package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Base64;

import com.baidu.aip.face.AipFace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector.FaceData;

public class BaiduFaceClient {

    //设置APPID/AK/SK
    public static final String APP_ID = "14757265";
    public static final String API_KEY = "TZNNBCvRpbtcLoCgu6CMunoA";
    public static final String SECRET_KEY = "mG1ofHNuBZ4jkdUypwfYQj58D3nbedO9";
    private static FaceDetectAPI client;

    public static void init() {
        // 初始化一个AipFace
        client = new FaceDetectAPI(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

//        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

//        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
//        // 也可以直接通过jvm启动参数设置此环境变量
//        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");

    }

    public static ArrayList detect(Bitmap image) {

        ArrayList resultList = new ArrayList();
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        //options.put("face_field", "age");
        //options.put("max_face_num", "2");
        options.put("face_type", "LIVE");

        String imageType = "BASE64";

        // 人脸检测
        JSONObject res = client.detect(bitmapToBase64(image), imageType, options);

        try {
            String error_code = res.getString("error_code");
            if("0".equals(error_code)){
                JSONObject result = res.getJSONObject("result");
                int faceNum = result.getInt("face_num");
                if(faceNum > 0) {
                    JSONArray faceList = result.getJSONArray("face_list");

                    for (int i=0;i<faceNum;i++) {
                        JSONObject face = (JSONObject) faceList.get(i);
                        resultList.add(FaceData.createByDetect(face));
                    }
                }
                return resultList;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static ArrayList search(Bitmap image) {

        ArrayList resultList = new ArrayList();

        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("max_face_num", "1");
        options.put("face_type", "LIVE");

        String imageType = "BASE64";
        String groupidList = "fxs";

        // 人脸检测
        JSONObject res = client.multiSearch(bitmapToBase64(image), imageType, groupidList,options);

//        try {
//
//            String error_code = res.getString("error_code");
//            if("0".equals(error_code)) {
//                JSONObject result = res.getJSONObject("result");
//
//                String faceToken = result.getString("face_token");
//                if (faceToken != null && faceToken.trim().length() > 0) {
//
//                    JSONArray userList = result.getJSONArray("user_list");
//
//                    if (userList != null && userList.length() > 0) {
//
//                        JSONObject user = userList.getJSONObject(0);
//                        if(user.getDouble("score") > 90.0) {
//                            return FaceData.createBySearch(userList.getJSONObject(0));
//                        }
//                    }
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        try {
            String error_code = res.getString("error_code");
            if("0".equals(error_code)){
                JSONObject result = res.getJSONObject("result");
                int faceNum = result.getInt("face_num");
                if(faceNum > 0) {
                    JSONArray faceList = result.getJSONArray("face_list");

                    for (int i=0;i<faceNum;i++) {
                        JSONObject face = (JSONObject) faceList.get(i);
                        resultList.add(FaceData.createBySearch(face));
                    }
                }
                return resultList;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String bitmapToBase64(Bitmap bitmap) {

        // 要返回的字符串
        String reslut = null;

        ByteArrayOutputStream baos = null;

        try {

            if (bitmap != null) {

                baos = new ByteArrayOutputStream();
                /**
                 * 压缩只对保存有效果bitmap还是原来的大小
                 */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);

                baos.flush();
                baos.close();

                // 转换为字节数组
                byte[] byteArray = baos.toByteArray();

                // 转换为字符串
                reslut = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return reslut;
    }
}
