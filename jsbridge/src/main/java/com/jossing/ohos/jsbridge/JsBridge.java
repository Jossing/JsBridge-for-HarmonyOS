package com.jossing.ohos.jsbridge;

import com.jossing.ohos.annotaion.MainThread;
import ohos.agp.components.webengine.ResourceRequest;
import ohos.agp.components.webengine.WebAgent;
import ohos.agp.components.webengine.WebView;
import ohos.agp.utils.TextTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 辅助 WebView 接入 JsBridge
 * <p>
 * 基于开源代码修改：com.github.lzyzsd.jsbridge
 *
 * @author jossing
 * @see JsBridge$Default 默认实现
 */
@SuppressWarnings("unused") // Public API
public abstract class JsBridge {

    private static final String CALLBACK_ID_PREFIX = "JAVA_CB_";

    protected final String mBridgeName;
    protected final WebViewInterface mWebView;
    protected final JsBridgeFunction mDefaultHandler;

    private final Map<String, JsBridgeFunction> mNativeFunctions = new HashMap<>();
    private final Map<String, JsBridgeFunction.Callback> mJsFunctionCallbacks = new HashMap<>();
    private final List<JsBridgeMessage> mNativeMessages = new ArrayList<>();

    private final AtomicLong mUniqueCallbackId = new AtomicLong(0);


    /**
     * @param bridgeName     版本名
     * @param webView        实现 WebView 功能接口的对象
     * @param defaultHandler 当 Javascript 调用了不存在的 JsBridge 接口函数时，将使用此默认处理进行响应
     */
    protected JsBridge(@NotNull String bridgeName, @NotNull WebViewInterface webView, @NotNull JsBridgeFunction defaultHandler) {
        mBridgeName = bridgeName;
        mWebView = webView;
        mDefaultHandler = defaultHandler;
    }

    @NotNull
    public final String getBridgeName() {
        return mBridgeName;
    }

    /**
     * 添加 JsBridge 接口函数，这个函数可以从 Javascript 调用
     *
     * @param name     接口名称
     * @param function 接口函数
     * @see #removeFunction(String, JsBridgeFunction)
     * @see #clearFunction()
     */
    @MainThread
    public final void addFunction(@NotNull String name, @NotNull JsBridgeFunction function, boolean allowOverride) {
        final String key = Objects.requireNonNull(name);
        final JsBridgeFunction value = Objects.requireNonNull(function);
        if (allowOverride) {
            mNativeFunctions.put(key, value);
        } else {
            mNativeFunctions.putIfAbsent(key, value);
        }
    }

    /**
     * 删除 JsBridge 接口函数
     *
     * @param name 接口名称
     * @see #addFunction(String, JsBridgeFunction, boolean)
     * @see #clearFunction()
     */
    @MainThread
    public final void removeFunction(@NotNull String name, @NotNull JsBridgeFunction function) {
        final String key = Objects.requireNonNull(name);
        final JsBridgeFunction value = Objects.requireNonNull(function);
        mNativeFunctions.remove(key, value);
    }

    /**
     * 删除全部 JsBridge 接口函数
     *
     * @see #addFunction(String, JsBridgeFunction, boolean)
     * @see #removeFunction(String, JsBridgeFunction)
     */
    @MainThread
    public final void clearFunction() {
        mNativeFunctions.clear();
    }

    /**
     * 调用从 Javascript 注入的 JsBridge 接口函数
     *
     * @param functionName 接口名称
     * @param data         接口参数
     * @param callback     接口回调
     */
    @MainThread
    public final void invokeFunction(
            @NotNull String functionName,
            @Nullable String data,
            @Nullable JsBridgeFunction.Callback callback
    ) {
        final long when = System.nanoTime() / 1_000_000;
        final JsBridgeMessage message = new JsBridgeMessage();
        if (!TextTool.isNullOrEmpty(data)) {
            message.setData(data);
        }
        if (callback != null) {
            final long uniqueId = mUniqueCallbackId.incrementAndGet();
            final String callbackId = CALLBACK_ID_PREFIX + uniqueId + "_" + when;
            mJsFunctionCallbacks.put(callbackId, callback);
            message.setCallbackId(callbackId);
        }
        if (!TextTool.isNullOrEmpty(functionName)) {
            message.setHandlerName(functionName);
        }
        queueNativeMessage(message);
    }

    /**
     * 拦截可能是 JsBridge 通信的伪请求并完成相应处理
     *
     * @param url 请求的链接
     * @return 拦截成功返回 true，否则返回 false
     * @see WebAgent#isNeedLoadUrl(WebView, ResourceRequest)
     */
    @MainThread
    public final boolean interceptJsBridgeRequest(@NotNull String url) {
        if (isRequireFetchJsMessageQueueUrl(url)) {
            fetchJsMessageQueue();
            return true;
        }
        // noinspection RedundantIfStatement
        if (maybeOnFetchJsMessageQueueResult(url)) {
            return true;
        }
        return false;
    }

    /**
     * 检查 {@code url} 是否是 Javascript 请求 Native 拉取和处理消息队列
     *
     * @param url 请求的链接
     */
    @MainThread
    protected abstract boolean isRequireFetchJsMessageQueueUrl(@NotNull String url);

    /**
     * 拉取和处理 Javascript 的消息队列
     */
    @MainThread
    protected abstract void fetchJsMessageQueue();

    /**
     * 安全的对一段文本内容进行 URLDecode，如果失败，则返回它本身
     */
    protected String safeUrlDecode(String content) {
        try {
            return URLDecoder.decode(content, "UTF-8");
        } catch (Throwable tr) {
            return content;
        }
    }

    /**
     * 检查并处理可能是 {@link #fetchJsMessageQueue()} 的结果 {@code url}
     *
     * @param url 请求的链接
     * @return 处理成功返回 true，否则返回 false
     */
    @MainThread
    protected abstract boolean maybeOnFetchJsMessageQueueResult(@NotNull String url);

    /**
     * 处理通过 {@link #fetchJsMessageQueue()} 获得的消息队列
     * <p>
     * 由 {@link #fetchJsMessageQueue()} 或 {@link #maybeOnFetchJsMessageQueueResult(String)} 调用
     *
     * @param messageList 消息队列
     */
    @MainThread
    protected final void onFetchJsMessageQueueResult(@NotNull final List<JsBridgeMessage> messageList) {
        for (final JsBridgeMessage message : messageList) {
            final String responseId = message.getResponseId();
            if (!TextTool.isNullOrEmpty(responseId)) {
                dispatchResponseData(responseId, message.getResponseData());
                continue;
            }
            final String callbackId = message.getCallbackId();
            final String functionName = message.getHandlerName();
            if (!TextTool.isNullOrEmpty(functionName)) {
                dispatchCallbackData(functionName, callbackId, message.getData());
            }
        }
    }

    /**
     * 分发从 Javascript 返回的数据
     *
     * @param responseId   接收这些数据的回调的 callbackId
     * @param responseData 回调数据
     * @see #invokeFunction(String, String, JsBridgeFunction.Callback)
     */
    @MainThread
    private void dispatchResponseData(@NotNull String responseId, String responseData) {
        final JsBridgeFunction.Callback callback = mJsFunctionCallbacks.remove(responseId);
        if (callback != null) {
            final String data = responseData != null ? responseData : "";
            callback.invoke(data);
        }
    }

    /**
     * 分发由 Javascript 调用的 JsBridge 接口数据
     *
     * @param functionName 调用的接口名称
     * @param callbackId   调用接口传入的回调的 callbackId
     * @param callbackData 调用接口传入的数据/参数
     */
    @MainThread
    private void dispatchCallbackData(@NotNull String functionName, String callbackId, String callbackData) {
        final JsBridgeFunction.Callback callbackFunction;
        if (!TextTool.isNullOrEmpty(callbackId)) {
            callbackFunction = new JsBridgeFunction.CallbackImpl(data -> {
                final JsBridgeMessage responseMsg = new JsBridgeMessage();
                responseMsg.setResponseId(callbackId);
                responseMsg.setResponseData(data);
                queueNativeMessage(responseMsg);
            });
        } else {
            // do nothing
            callbackFunction = JsBridgeFunction.Callback.EMPTY;
        }
        // 原生 JsBridge 接口函数 执行
        final String data = callbackData != null ? callbackData : "";
        final JsBridgeFunction function = mNativeFunctions.get(functionName);
        if (function != null) {
            function.invoke(getBridgeName(), data, callbackFunction);
        } else {
            mDefaultHandler.invoke(getBridgeName(), data, callbackFunction);
        }
    }

    /**
     * Native 产生的消息入队待分发
     */
    @MainThread
    private void queueNativeMessage(@NotNull JsBridgeMessage message) {
        if (!mNativeMessages.isEmpty()) {
            mNativeMessages.add(message);
        } else {
            dispatchNativeMessage(message);
        }
    }

    /**
     * 分发 Native 产生的待分发消息
     *
     * @see WebAgent#onPageLoaded(WebView, String)
     */
    @MainThread
    public final void dispatchNativeMessages() {
        for (final JsBridgeMessage message : mNativeMessages) {
            dispatchNativeMessage(message);
        }
        mNativeMessages.clear();
    }

    /**
     * 分发 Native 产生的消息
     */
    @MainThread
    protected abstract void dispatchNativeMessage(@NotNull JsBridgeMessage message);

    /**
     * 重置此 JsBridge，这会清除所有已添加的 JsBridge 接口函数，以及未完成的回调和待分发的消息
     */
    @MainThread
    public final void reset() {
        mNativeFunctions.clear();
        mJsFunctionCallbacks.clear();
        mNativeMessages.clear();
    }


    /**
     * 创建一个默认实现的实例，BridgeName 为 “Default”
     *
     * @see JsBridge#JsBridge(String, WebViewInterface, JsBridgeFunction)
     */
    public static JsBridge newDefault(@NotNull WebViewInterface webView, @NotNull JsBridgeFunction defaultHandler) {
        return new JsBridge$Default(webView, defaultHandler);
    }
}
