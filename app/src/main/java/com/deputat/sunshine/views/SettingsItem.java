package com.deputat.sunshine.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.deputat.sunshine.R;

public class SettingsItem extends LinearLayout {

  private static final int DEFAULT_TITLE_COLOR = Color.parseColor("#000000");
  private static final int DEFAULT_SUBTITLE_COLOR = Color.parseColor("#7e7c7c");

  private TextView textViewSubtitle;
  private TextView textViewTitle;
  private Switch switcher;

  private String key;
  private String defaultValue;

  public SettingsItem(Context context) {
    super(context);
    inflate(context, null);
  }

  public SettingsItem(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    inflate(context, attrs);
  }

  public SettingsItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    inflate(context, attrs);
  }

  private void inflate(Context context, @Nullable AttributeSet attrs) {
    inflate(context, R.layout.settings_item, this);

    init();
    set(context, attrs);
  }

  private void init() {
    textViewTitle = findViewById(R.id.title);
    textViewSubtitle = findViewById(R.id.subtitle);
    switcher = findViewById(R.id.switch_item);
  }

  private void set(Context context, @Nullable AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingsItem,
        0, 0);

    final String titleText = a.getString(R.styleable.SettingsItem_titleText);
    int titleColor = a.getColor(R.styleable.SettingsItem_titleColor, DEFAULT_TITLE_COLOR);
    float titleSize = a.getDimension(R.styleable.SettingsItem_titleSize,
        getResources().getDimension(R.dimen.text_size_16));

    textViewTitle.setText(titleText);
    textViewTitle.setTextColor(titleColor);
    textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);

    int subtitleVisibility = a.getInt(R.styleable.SettingsItem_subtitleVisibility, 2);
    final String subtitleText = a.getString(R.styleable.SettingsItem_subtitleText);
    int subtitleColor = a.getColor(R.styleable.SettingsItem_subtitleColor, DEFAULT_SUBTITLE_COLOR);
    float subtitleSize = a.getDimension(R.styleable.SettingsItem_subtitleSize,
        getResources().getDimension(R.dimen.text_size_14));

    textViewSubtitle.setVisibility(subtitleVisibility == 0 ? VISIBLE :
        (subtitleVisibility == 1 ? INVISIBLE : GONE));
    textViewSubtitle.setText(subtitleText);
    textViewSubtitle.setTextColor(subtitleColor);
    textViewSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, subtitleSize);

    int switchVisibility = a.getInt(R.styleable.SettingsItem_switchVisibility, 2);
    switcher.setVisibility(switchVisibility == 0 ? VISIBLE :
        (switchVisibility == 1 ? INVISIBLE : GONE));

    key = a.getString(R.styleable.SettingsItem_key);
    defaultValue = a.getString(R.styleable.SettingsItem_defaultValue);

    a.recycle();
  }

  public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
    switcher.setOnCheckedChangeListener(listener);
  }

  @SuppressWarnings("unused")
  public void setSwitchVisibility(int visibility) {
    switcher.setVisibility(visibility);
  }

  @SuppressWarnings("unused")
  public void setSubtitleVisibility(int visibility) {
    textViewSubtitle.setVisibility(visibility);
  }

  @SuppressWarnings("unused")
  public void setSubtitleText(@StringRes int text) {
    textViewSubtitle.setText(text);
  }

  public void setSubtitleText(String text) {
    textViewSubtitle.setText(text);
  }

  @SuppressWarnings("unused")
  public void setTitleText(@StringRes int text) {
    textViewTitle.setText(text);
  }

  @SuppressWarnings("unused")
  public void setTitleText(String text) {
    textViewTitle.setText(text);
  }

  public boolean isSwitchUnchecked() {
    return !switcher.isChecked();
  }

  public void setSwitchChecked(boolean isChecked) {
    switcher.setChecked(isChecked);
  }

  public String getKey() {
    return key;
  }

  public String getDefaultValue() {
    return defaultValue;
  }
}
