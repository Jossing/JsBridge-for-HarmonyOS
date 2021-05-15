package com.jossing.jsbridge;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wonders.libs.android.support.utils.KLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * data of bridge
 *
 * @author haoqing
 */
public final class JsBridgeMessage {

    private final static String CALLBACK_ID_STR = "callbackId";
    private final static String RESPONSE_ID_STR = "responseId";
    private final static String RESPONSE_DATA_STR = "responseData";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";

    private String mCallbackId = null;
    private String mResponseId = null;
    private String mResponseData = null;
    private String mData = null;
    private String mHandlerName = null;

    public JsBridgeMessage() {
    }

    JsBridgeMessage(@NonNull final JSONObject jsonObject) {
        mCallbackId = jsonObject.optString(CALLBACK_ID_STR, null);
        mResponseId = jsonObject.optString(RESPONSE_ID_STR, null);
        mResponseData = jsonObject.optString(RESPONSE_DATA_STR, null);
        mData = jsonObject.optString(DATA_STR, null);
        mHandlerName = jsonObject.optString(HANDLER_NAME_STR, null);
    }

    @Nullable
    public String getResponseId() {
        return mResponseId;
    }

    public void setResponseId(@Nullable String responseId) {
        this.mResponseId = responseId;
    }

    @Nullable
    public String getResponseData() {
        return mResponseData;
    }

    public void setResponseData(@Nullable String responseData) {
        this.mResponseData = responseData;
    }

    @Nullable
    public String getCallbackId() {
        return mCallbackId;
    }

    public void setCallbackId(@Nullable String callbackId) {
        this.mCallbackId = callbackId;
    }

    @Nullable
    public String getData() {
        return mData;
    }

    public void setData(@Nullable String data) {
        this.mData = data;
    }

    @Nullable
    public String getHandlerName() {
        return mHandlerName;
    }

    public void setHandlerName(@Nullable String handlerName) {
        this.mHandlerName = handlerName;
    }

    @Nullable
    public String toJson() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(CALLBACK_ID_STR, getCallbackId());
            jsonObject.put(DATA_STR, getData());
            jsonObject.put(HANDLER_NAME_STR, getHandlerName());
            final String data = getResponseData();
            if (TextUtils.isEmpty(data)) {
                jsonObject.put(RESPONSE_DATA_STR, data);
            } else {
                jsonObject.put(RESPONSE_DATA_STR, new JSONTokener(data).nextValue());
            }
            jsonObject.put(RESPONSE_DATA_STR, getResponseData());
            jsonObject.put(RESPONSE_ID_STR, getResponseId());
            return jsonObject.toString();
        } catch (JSONException e) {
            KLog.w(e);
        }
        return null;
    }

    @Nullable
    public static JsBridgeMessage formJson(String jsonStr) {
        try {
            final JSONObject jsonObject = new JSONObject(jsonStr);
            return new JsBridgeMessage(jsonObject);
        } catch (JSONException e) {
            KLog.w((Object) jsonStr, e);
        }
        return null;
    }

    @NonNull
    public static List<JsBridgeMessage> formJsonArray(@NonNull String jsonStr) {
        final List<JsBridgeMessage> list = new ArrayList<>();
        final JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonStr);
        } catch (JSONException e) {
            KLog.w((Object) jsonStr, e);
            return list;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject;
            try {
                jsonObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                KLog.w(e);
                continue;
            }
            list.add(new JsBridgeMessage(jsonObject));
        }
        return list;
    }
}
