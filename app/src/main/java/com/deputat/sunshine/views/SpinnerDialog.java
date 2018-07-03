package com.deputat.sunshine.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import com.deputat.sunshine.R;

import java.util.Objects;

public class SpinnerDialog {

    private Dialog mAlertDialog;

    public SpinnerDialog(Activity activity) {
        initView(activity);
    }

    @SuppressLint("InflateParams")
    private void initView(Activity activity) {
        final LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = Objects.requireNonNull(inflater)
                .inflate(R.layout.progress_dialog, null, false);

        mAlertDialog = new Dialog(activity);
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setCancelable(false);
        mAlertDialog.setContentView(layout);

        if (mAlertDialog.getWindow() != null) {
            mAlertDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    public void show() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            hide();
        }
        if (mAlertDialog != null && !mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    private void hide() {
        mAlertDialog.dismiss();
    }

    public boolean isShowing() {
        return mAlertDialog.isShowing();
    }
}
