package com.jossing.jsbridge;

import android.content.Context;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * WebView 功能接口
 *
 * @author ZhengAn
 * @date 2019-07-01
 */
public interface WebViewInterface {

    /**
     * @see WebView#getContext()
     */
    @NonNull
    Context getContext();

    /**
     * @see WebView#loadUrl(String)
     */
    void loadUrl(@NonNull String url);

    /**
     * @see WebView#evaluateJavascript(String, ValueCallback)
     */
    void evaluateJavascript(@NonNull String javascript, @Nullable ValueCallback<String> callback);
}
