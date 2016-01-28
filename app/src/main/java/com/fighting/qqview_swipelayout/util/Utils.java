package com.fighting.qqview_swipelayout.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 描述：
 * 作者 mjd
 * 日期：2016/1/28 22:18
 */
public class Utils {
    private static Toast toast;

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        toast.setText(msg);
        toast.show();
    }
}
