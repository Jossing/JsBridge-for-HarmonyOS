package com.jossing.ohos.jsbridge;

import ohos.agp.components.webengine.AsyncCallback;
import ohos.agp.components.webengine.WebView;
import ohos.app.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * WebView 功能接口
 *
 * @author ZhengAn
 * @date 2019-07-01
 */
@SuppressWarnings("unused") // Public API
public interface WebViewInterface {

    /**
     * @see WebView#getContext()
     */
    @NotNull
    Context getContext();

    /**
     * @see WebView#load(String)
     */
    void load(@NotNull String url);

    /**
     * @see WebView#executeJs(String, AsyncCallback)
     */
    void executeJs(@NotNull String javascript, @Nullable AsyncCallback<String> callback);
}
