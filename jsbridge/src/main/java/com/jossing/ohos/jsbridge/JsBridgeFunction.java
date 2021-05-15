package com.jossing.ohos.jsbridge;

import com.jossing.ohos.annotaion.MainThread;
import org.jetbrains.annotations.NotNull;

/**
 * JsBridge 接口函数
 *
 * @author jossing
 */
@FunctionalInterface
public interface JsBridgeFunction {

    /**
     * 调用此接口函数
     *
     * @param jsBridgeName 标记调用来自哪个版本的 JsBridge，也可以看看 {@link JsBridge#getBridgeName()}
     * @param data         数据/参数
     * @param callback     接口回调
     */
    @MainThread
    void invoke(@NotNull String jsBridgeName, @NotNull String data, @NotNull Callback callback);


    /**
     * JsBridge 接口函数 回调接口
     * <p>
     * <b>不建议直接实现使用，请使用 {@link CallbackImpl} 代替</b>
     */
    @FunctionalInterface
    interface Callback {

        Callback EMPTY = data -> {
        };

        /**
         * 调用此回调接口
         *
         * @param data 回调数据/参数
         */
        @MainThread
        void invoke(@NotNull String data);
    }


    /**
     * {@link Callback} 主要子类，限制了 {@link #invoke(String)} 方法只有第一次调用有效。
     */
    final class CallbackImpl implements Callback {

        private final Callback mDelegate;

        /**
         * 限制只允许调用一次
         */
        private boolean isCalled = false;


        public CallbackImpl(@NotNull Callback delegate) {
            mDelegate = delegate;
        }

        @Override
        public void invoke(@NotNull String data) {
            if (isCalled) {
                return;
            }
            isCalled = true;
            mDelegate.invoke(data);
        }
    }
}
