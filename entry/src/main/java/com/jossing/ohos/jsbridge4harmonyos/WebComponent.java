package com.jossing.ohos.jsbridge4harmonyos;

import com.jossing.ohos.jsbridge.JsBridge;
import com.jossing.ohos.jsbridge.JsBridgeFunction;
import com.jossing.ohos.jsbridge.WebViewInterface;
import ohos.agp.components.AttrSet;
import ohos.agp.components.webengine.AsyncCallback;
import ohos.agp.components.webengine.ResourceRequest;
import ohos.agp.components.webengine.WebAgent;
import ohos.agp.components.webengine.WebView;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WebComponent extends WebView implements WebViewInterface {

    private static final HiLogLabel LOG_LABEL = new HiLogLabel(HiLog.LOG_APP, 0x65536, "WebComponent");

    private final JsBridge mJsBridge = JsBridge.newDefault(this, DEFAULT_JS_BRIDGE_FUNCTION);

    public WebComponent(@NotNull Context context) {
        super(context);
        init(context);
    }

    public WebComponent(@NotNull Context context, @Nullable AttrSet attrSet) {
        super(context, attrSet);
        init(context);
    }

    private void init(@NotNull Context context) {
        setWebAgent(defaultWebAgent);
    }

    @Override
    @NotNull
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void load(@NotNull String url) {
        super.load(url);
    }

    @Override
    public void executeJs(@NotNull String jsContent, @Nullable AsyncCallback<String> callback) {
        super.executeJs(jsContent, callback);
    }


    /**
     * @see WebAgent#isNeedLoadUrl
     * @see WebAgent#onPageLoaded
     */
    public final WebAgent defaultWebAgent = new WebAgent() {

        @Override
        public boolean isNeedLoadUrl(@NotNull WebView webView, @NotNull ResourceRequest request) {
            final String url = request.getRequestUrl().toString();
            if (mJsBridge.interceptJsBridgeRequest(url)) {
                return false;
            }
            HiLog.debug(LOG_LABEL, "isNeedLoadUrl(%{public}s)", url);
            return true;
        }

        @Override
        public void onPageLoaded(@NotNull WebView webView, @NotNull String url) {
            mJsBridge.dispatchNativeMessages();
        }
    };


    private static final JsBridgeFunction DEFAULT_JS_BRIDGE_FUNCTION = (jsBridgeName, data, callback) -> {
        HiLog.debug(LOG_LABEL, "[%s]defaultJsBridgeFunction(%{public}s)", jsBridgeName, data);
        callback.invoke("This is defaultJsBridgeFunction.");
    };
}
