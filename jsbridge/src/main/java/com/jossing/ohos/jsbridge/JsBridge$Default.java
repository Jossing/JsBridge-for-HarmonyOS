package com.jossing.ohos.jsbridge;

import com.jossing.ohos.annotaion.UiThread;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 这个版本相对于开源代码几乎没有修改
 * <p>
 * 特别说明：只允许使用 {@link JsBridge#newDefault(WebViewInterface, JsBridgeFunction)} 获取此类的实例。
 *
 * @author jossing
 */
final class JsBridge$Default extends JsBridge {

    private static final HiLogLabel LOG_LABEL = new HiLogLabel(HiLog.LOG_APP, 0x65536, "JsBridge$Default");

    private static final String JS_BRIDGE_OBJECT = "WebViewJavascriptBridge";
    private static final String SCHEME = "yy://";
    private static final String URL_FETCH_MESSAGE_QUEUE_RESULT = SCHEME + "return/_fetchQueue/";
    private static final String URL_REQUIRE_FETCH_MESSAGE_QUEUE = SCHEME + "__QUEUE_MESSAGE__/";


    public JsBridge$Default(@NotNull WebViewInterface webView, @NotNull JsBridgeFunction defaultHandler) {
        super("Default", webView, defaultHandler);
    }

    @Override
    protected boolean isRequireFetchJsMessageQueueUrl(@NotNull String url) {
        return url.equals(URL_REQUIRE_FETCH_MESSAGE_QUEUE);
    }

    @Override
    protected void fetchJsMessageQueue() {
        final String fetchCommand = "javascript:" + JS_BRIDGE_OBJECT + "._fetchQueue();";
        mWebView.load(fetchCommand);
    }

    @Override
    protected boolean maybeOnFetchJsMessageQueueResult(@NotNull String url) {
        // url = yy://return/_fetchQueue/[{"responseId":"JAVA_CB_2_3957","responseData":"Javascript Says Right back aka!"}]
        final int resultUrlMinLength = URL_FETCH_MESSAGE_QUEUE_RESULT.length();
        if (!url.startsWith(URL_FETCH_MESSAGE_QUEUE_RESULT) || url.length() <= resultUrlMinLength) {
            return false;
        }
        // data = [{"responseId":"JAVA_CB_2_3957","responseData":"Javascript Says Right back aka!"}]
        final String data = url.substring(resultUrlMinLength);
        final List<JsBridgeMessage> jsBridgeMessageList = JsBridgeMessage.formJsonArray(data);
        onFetchJsMessageQueueResult(jsBridgeMessageList);
        return true;
    }

    @UiThread
    @Override
    protected void dispatchNativeMessage(@NotNull JsBridgeMessage message) {
        String messageJson = message.toJson();
        if (messageJson == null) {
            return;
        }
        // 为 json 字符串转义特殊字符
        try {
            messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
            messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        } catch (Throwable tr) {
            HiLog.error(LOG_LABEL, "dispatchNativeMessage failed. \n%s", HiLog.getStackTrace(tr));
            return;
        }
        final String dispatchCommand = "javascript:" + JS_BRIDGE_OBJECT + "._handleMessageFromNative('" + messageJson + "');";
        mWebView.load(dispatchCommand);
    }
}
