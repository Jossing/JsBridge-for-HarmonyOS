package com.jossing.ohos.jsbridge4harmonyos.slice;

import com.jossing.ohos.jsbridge4harmonyos.ResourceTable;
import com.jossing.ohos.jsbridge4harmonyos.WebComponent;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.colors.ColorConverter;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.Component;
import ohos.agp.components.ProgressBar;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.components.webengine.*;
import ohos.agp.utils.Color;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MainAbilitySlice extends AbilitySlice {

    private static final HiLogLabel LOG_LABEL = new HiLogLabel(HiLog.LOG_APP, 0x65536, "MainAbilitySlice");

    private Text mTitleBar;
    private ProgressBar mProgressBar;
    private WebComponent mWebComponent;

    private boolean mAllowReceiveWebTitle = true;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        mTitleBar = (Text) findComponentById(ResourceTable.Id_titlebar);
        mProgressBar = (ProgressBar) findComponentById(ResourceTable.Id_progressbar);
        mWebComponent = (WebComponent) findComponentById(ResourceTable.Id_webview);

        mWebComponent.setWebAgent(mWebAgent);
        mWebComponent.setBrowserAgent(mBrowserAgent);
        mWebComponent.getWebConfig().setJavaScriptPermit(true);
        mWebComponent.getWebConfig().setDataAbilityPermit(true);
        initJsBridgeFunctions();

        mWebComponent.load("dataability://com.jossing.ohos.jsbridge4harmonyos.DataAbility/rawfile/JsBridge.html");
    }

    private void initJsBridgeFunctions() {
        mWebComponent.addJsBridgeFunction("setTitle", (jsBridgeName, data, callback) -> {
            mAllowReceiveWebTitle = false;
            try {
                mTitleBar.setText(URLDecoder.decode(data, "UTF-8"));
                callback.invoke("success");
            } catch (UnsupportedEncodingException e) {
                callback.invoke(e.getMessage());
            }
        });
        mWebComponent.addJsBridgeFunction("setTitleColor", (jsBridgeName, data, callback) -> {
            try {
                final RgbColor rgbColor = RgbColor.fromArgbInt(Color.getIntColor(data));
                rgbColor.setAlpha(0xFF);
                final ShapeElement background = new ShapeElement();
                background.setRgbColor(rgbColor);
                mTitleBar.setBackground(background);
                getWindow().setStatusBarColor(rgbColor.asArgbInt());
                final float lightness = ColorConverter.toHsl(rgbColor).getLightness();
                if (lightness < 60F) {
                    mTitleBar.setTextColor(Color.WHITE);
                } else {
                    mTitleBar.setTextColor(Color.BLACK);
                }
                callback.invoke("success");
            } catch (IllegalArgumentException e) {
                callback.invoke(e.getMessage());
            }
        });
        mWebComponent.addJsBridgeFunction("closeWindow", (jsBridgeName, data, callback) -> {
            terminate();
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onBackPressed() {
        final Navigator navigator = mWebComponent.getNavigator();
        if (navigator.canGoBack()) {
            navigator.goBack();
            return;
        }
        super.onBackPressed();
    }


    public final WebAgent mWebAgent = new WebAgent() {

        @Override
        public boolean isNeedLoadUrl(@NotNull WebView webView, @NotNull ResourceRequest request) {
            return mWebComponent.defaultWebAgent.isNeedLoadUrl(webView, request);
        }

        @Override
        public void onLoadingPage(@NotNull WebView webView, @NotNull String url, @Nullable PixelMap icon) {
            mBrowserAgent.onProgressUpdated(webView, 0);
        }

        @Override
        public void onPageLoaded(@NotNull WebView webView, @NotNull String url) {
            mBrowserAgent.onProgressUpdated(webView, 100);
            mWebComponent.defaultWebAgent.onPageLoaded(webView, url);
        }
    };


    private final BrowserAgent mBrowserAgent = new BrowserAgent(this) {

        @Override
        public void onTitleUpdated(@NotNull WebView webView, String value) {
            if (!mAllowReceiveWebTitle) return;
            mTitleBar.setText(value);
        }

        @Override
        public void onProgressUpdated(@NotNull WebView webView, int newValue) {
            HiLog.info(LOG_LABEL, "onProgressUpdated(%{public}s)", newValue);
            final int progress = newValue + 20;
            final int progressBarVisibility = progress <= 20 || progress >= 120 ? Component.HIDE : Component.VISIBLE;
            mProgressBar.setProgressValue(progress);
            mProgressBar.setVisibility(progressBarVisibility);
        }

    };
}
