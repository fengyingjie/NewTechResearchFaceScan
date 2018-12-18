package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan.detector;

import android.graphics.Point;
import android.graphics.Rect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FaceData {
    private String mId;
    private String mName;
    private Point mCenterPoint;
    private Rect mFaceRect;

    public static FaceData createBySearch(JSONObject jsonObj){
//        if(jsonObj == null){
//            return null;
//        }
//        FaceData result = new FaceData();
//        try {
//            result.setId(jsonObj.getString("user_id"));
//            result.setName(jsonObj.getString("user_info"));
//            return result;
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return null;

        if(jsonObj == null){
            return null;
        }
        JSONObject location = null;
        try {
            location = jsonObj.getJSONObject("location");
            double left = location.getDouble("left");
            double top = location.getDouble("top");
            double width = location.getDouble("width");
            double height = location.getDouble("height");

            Rect faceRect = new Rect(new Double(left).intValue(),
                    new Double(top).intValue(),
                    new Double(left).intValue() + new Double(width).intValue(),
                    new Double(top).intValue() + new Double(height).intValue());

            FaceData result = new FaceData();
            result.setFaceRect(faceRect);

            result.setId("unknow");
            result.setName("unknow");
            JSONArray userList = jsonObj.getJSONArray("user_list");
            if(userList !=null && userList.length() >0) {
                JSONObject user = userList.getJSONObject(0);
                double score = user.getDouble("score");

                if(score > 70.0f) {
                    result.setId(user.getString("user_id"));
                    result.setName(user.getString("user_id"));
                }
            }

            return result;

        } catch (JSONException e) {
        }
        return null;
    }

    public static FaceData createByDetect(JSONObject jsonObj){
        //JSONObject face = (JSONObject) jsonObj.get(i);
        if(jsonObj == null){
            return null;
        }
        JSONObject location = null;
        try {
            location = jsonObj.getJSONObject("location");
            double left = location.getDouble("left");
            double top = location.getDouble("top");
            double width = location.getDouble("width");
            double height = location.getDouble("height");

            Rect faceRect = new Rect(new Double(left).intValue(),
                    new Double(top).intValue(),
                    new Double(left).intValue() + new Double(width).intValue(),
                    new Double(top).intValue() + new Double(height).intValue());
            FaceData result = new FaceData();
            result.setId(jsonObj.getString("user_id"));
            result.setName(jsonObj.getString("user_info"));
            result.setFaceRect(faceRect);
            return result;

        } catch (JSONException e) {
        }
        return null;
    }

    public  FaceData(Rect rect){
        mFaceRect = rect;
    }
    private FaceData(){
        mCenterPoint = new Point();
        mFaceRect = new Rect();
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Point getCenterPoint() {
        return mCenterPoint;
    }

    public void setCenterPoint(Point mCenterPoint) {
        this.mCenterPoint = mCenterPoint;
    }

    public void setCenterPoint(int x, int y) {
        this.mCenterPoint.x=x;
        this.mCenterPoint.y=y;
    }

    public Rect getFaceRect() {
        return mFaceRect;
    }

    public void setFaceRect(Rect mFaceRect) {
        this.mFaceRect = mFaceRect;
    }

    public void setFaceRect(int left, int top, int right, int bottom) {
        this.mFaceRect.left = left;
        this.mFaceRect.top = top;
        this.mFaceRect.right = right;
        this.mFaceRect.bottom = bottom;
    }
}
