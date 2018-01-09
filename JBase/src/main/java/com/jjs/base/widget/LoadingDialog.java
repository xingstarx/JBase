package com.jjs.base.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.jjs.base.R;


/**
 * 本页：dialog弹窗，在activity的onCreate和onResume调用init初始化，在onDestroy中关闭dialog
 * Created by jjs on 2017-03-25.
 * Email:994462623@qq.com
 */

public class LoadingDialog {
    private static Dialog mDialog;
    private static Context mContext;

    /**
     * 初始化dialog数据，可动态设置显示view和style
     */
    public static void init(Context context) {
        mContext = context;
    }

    public static void show() {
        show(null);
    }

    public static void show(View view) {
        show(view, -1);
    }

    public static void show(View view, int style) {
        if (mDialog == null) {
            if (style == -1)
                style = R.style.Dialog_notBG;
            mDialog = new Dialog(mContext, style);
            mDialog.setCancelable(true);//按返回键消失
            mDialog.setCanceledOnTouchOutside(false);//但点击dialog范围外不消失
            if (view == null) {
                view = View.inflate(mContext, R.layout.dialog_loadingview, null);
            }
            mDialog.setContentView(view);

        }
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public static void hide() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.hide();
        }
    }

    public static void dissmiss() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
