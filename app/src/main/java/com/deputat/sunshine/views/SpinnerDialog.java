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

    private Dialog alertDialog;

    public SpinnerDialog(Activity activity) {
        initView(activity);
    }

    @SuppressLint("InflateParams")
    private void initView(Activity activity) {
        final LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = Objects.requireNonNull(inflater)
                .inflate(R.layout.progress_dialog, null, false);

        alertDialog = new Dialog(activity);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.setContentView(layout);

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    public void show() {
        if (alertDialog != null && alertDialog.isShowing()) {
            hide();
        }
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    private void hide() {
        alertDialog.dismiss();
    }

    public boolean isShowing() {
        return alertDialog.isShowing();
    }
}
