package com.jossing.ohos.jsbridge;

import com.jossing.ohos.orgjson.JSONArray;
import com.jossing.ohos.orgjson.JSONException;
import com.jossing.ohos.orgjson.JSONObject;
import com.jossing.ohos.orgjson.JSONTokener;
import ohos.agp.utils.TextTool;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * data of bridge
 *
 * @author haoqing
 */
@SuppressWarnings("unused") // Public API
public final class JsBridgeMessage {

    private static final HiLogLabel LOG_LABEL = new HiLogLabel(HiLog.LOG_APP, 0x65536, "JsBridgeMessage");

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

    JsBridgeMessage(@NotNull final JSONObject jsonObject) {
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

    /**
     * @noinspection ConstantConditions
     */
    @Nullable
    public String toJson() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(CALLBACK_ID_STR, getCallbackId());
            jsonObject.put(DATA_STR, getData());
            jsonObject.put(HANDLER_NAME_STR, getHandlerName());
            final String data = getResponseData();
            if (TextTool.isNullOrEmpty(data)) {
                jsonObject.put(RESPONSE_DATA_STR, data);
            } else {
                jsonObject.put(RESPONSE_DATA_STR, new JSONTokener(data).nextValue());
            }
            jsonObject.put(RESPONSE_DATA_STR, getResponseData());
            jsonObject.put(RESPONSE_ID_STR, getResponseId());
            return jsonObject.toString();
        } catch (JSONException e) {
            HiLog.error(LOG_LABEL, "toJson\n%s", HiLog.getStackTrace(e));
        }
        return null;
    }

    /**
     * @noinspection ConstantConditions
     */
    @Nullable
    public static JsBridgeMessage formJson(String jsonStr) {
        try {
            final JSONObject jsonObject = new JSONObject(jsonStr);
            return new JsBridgeMessage(jsonObject);
        } catch (JSONException e) {
            HiLog.error(LOG_LABEL, "formJson\n%s", HiLog.getStackTrace(e));
        }
        return null;
    }

    @NotNull
    public static List<JsBridgeMessage> formJsonArray(@NotNull String jsonStr) {
        final List<JsBridgeMessage> list = new ArrayList<>();
        final JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonStr);
        } catch (JSONException e) {
            HiLog.error(LOG_LABEL, "formJsonArray\n%s", HiLog.getStackTrace(e));
            return list;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject;
            try {
                jsonObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                HiLog.error(LOG_LABEL, "formJsonArray\n%s", HiLog.getStackTrace(e));
                continue;
            }
            list.add(new JsBridgeMessage(jsonObject));
        }
        return list;
    }
}
