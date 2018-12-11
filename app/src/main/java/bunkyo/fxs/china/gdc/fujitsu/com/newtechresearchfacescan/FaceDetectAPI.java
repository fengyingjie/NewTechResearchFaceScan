package bunkyo.fxs.china.gdc.fujitsu.com.newtechresearchfacescan;

import com.baidu.aip.client.BaseClient;
import com.baidu.aip.error.AipError;
import com.baidu.aip.face.FaceVerifyRequest;
import com.baidu.aip.face.MatchRequest;
import com.baidu.aip.http.AipRequest;
import com.baidu.aip.http.EBodyFormat;
import com.baidu.aip.http.HttpMethodName;
import com.baidu.aip.util.Base64Util;
import com.baidu.aip.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FaceDetectAPI extends BaseClient {

    public FaceDetectAPI(String appId, String apiKey, String secretKey) {
        super(appId, apiKey, secretKey);

    }

    @Override
    protected void preOperation(AipRequest request) {
        if (this.needAuth()) {
            this.getAccessToken(this.config);
        }
        request.setHttpMethod(HttpMethodName.POST);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("accept", "*/*");
        request.setConfig(this.config);
    }


    public JSONObject detect(String image, String imageType, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("image", image);
        request.addBody("image_type", imageType);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/detect");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject search(String image, String imageType, String groupIdList, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("image", image);
        request.addBody("image_type", imageType);
        request.addBody("group_id_list", groupIdList);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/search");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject multiSearch(String image, String imageType, String groupIdList, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("image", image);
        request.addBody("image_type", imageType);
        request.addBody("group_id_list", groupIdList);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/multi-search");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject addUser(String image, String imageType, String groupId, String userId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("image", image);
        request.addBody("image_type", imageType);
        request.addBody("group_id", groupId);
        request.addBody("user_id", userId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject updateUser(String image, String imageType, String groupId, String userId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("image", image);
        request.addBody("image_type", imageType);
        request.addBody("group_id", groupId);
        request.addBody("user_id", userId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/update");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject faceDelete(String userId, String groupId, String faceToken, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("user_id", userId);
        request.addBody("group_id", groupId);
        request.addBody("face_token", faceToken);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/face/delete");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject getUser(String userId, String groupId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("user_id", userId);
        request.addBody("group_id", groupId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/get");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject faceGetlist(String userId, String groupId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("user_id", userId);
        request.addBody("group_id", groupId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/face/getlist");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject getGroupUsers(String groupId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("group_id", groupId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/group/getusers");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject userCopy(String userId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("user_id", userId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/copy");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject deleteUser(String groupId, String userId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("group_id", groupId);
        request.addBody("user_id", userId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/delete");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject groupAdd(String groupId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("group_id", groupId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/group/add");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject groupDelete(String groupId, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("group_id", groupId);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/group/delete");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject getGroupList(HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceset/group/getlist");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject personVerify(String image, String imageType, String idCardNumber, String name, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("image", image);
        request.addBody("image_type", imageType);
        request.addBody("id_card_number", idCardNumber);
        request.addBody("name", name);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/person/verify");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject videoSessioncode(HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v1/faceliveness/sessioncode");
        request.setBodyFormat(EBodyFormat.RAW_JSON);
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject videoFaceliveness(String sessionId, byte[] videoBase64, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        request.addBody("session_id", sessionId);
        String base64Content = Base64Util.encode(videoBase64);
        request.addBody("video_base64", base64Content);
        if (options != null) {
            request.addBody(options);
        }

        request.setUri("https://aip.baidubce.com/rest/2.0/face/v1/faceliveness/verify");
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject videoFaceliveness(String sessionId, String videoBase64, HashMap<String, String> options) {
        try {
            byte[] data = Util.readFileByBytes(videoBase64);
            return this.videoFaceliveness(sessionId, data, options);
        } catch (IOException var5) {
            var5.printStackTrace();
            return AipError.IMAGE_READ_ERROR.toJsonResult();
        }
    }

    public JSONObject match(List<MatchRequest> input) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        JSONArray arr = new JSONArray();
        Iterator i$ = input.iterator();

        while(i$.hasNext()) {
            MatchRequest req = (MatchRequest)i$.next();
            arr.put(req.toJsonObject());
        }

        request.addBody("body", arr.toString());
        request.setBodyFormat(EBodyFormat.RAW_JSON_ARRAY);
        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/match");
        this.postOperation(request);
        return this.requestServer(request);
    }

    public JSONObject faceverify(List<FaceVerifyRequest> input) {
        AipRequest request = new AipRequest();
        this.preOperation(request);
        JSONArray arr = new JSONArray();
        Iterator i$ = input.iterator();

        while(i$.hasNext()) {
            FaceVerifyRequest req = (FaceVerifyRequest)i$.next();
            arr.put(req.toJsonObject());
        }

        request.addBody("body", arr.toString());
        request.setBodyFormat(EBodyFormat.RAW_JSON_ARRAY);
        request.setUri("https://aip.baidubce.com/rest/2.0/face/v3/faceverify");
        this.postOperation(request);
        return this.requestServer(request);
    }

}
