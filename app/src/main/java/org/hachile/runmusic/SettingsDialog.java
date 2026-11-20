package org.hachile.runmusic;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Locale;

final class SettingsDialog {
    static void show(Context context, SpeedConfig c, java.util.function.Consumer<SpeedConfig> save) {
        LinearLayout form = Ui.column(context);
        int pad = Ui.dp(context, 24); form.setPadding(pad, 0, pad, pad);
        TextView intro = Ui.text(context, "按你的手感设置，保存后立即生效。", 13, Ui.MUTED);
        form.addView(intro, Ui.size(context, -1, 40));
        EditText min = number(context, form, "倍速下限（0.25–3.00）", c.min);
        EditText max = number(context, form, "倍速上限（0.25–3.00）", c.max);
        TextView sensitivity = Ui.text(context, "", 14, Ui.TEXT); form.addView(sensitivity, Ui.size(context, -1, 48));
        SeekBar slider = new SeekBar(context); slider.setMax(190); slider.setProgress(Math.round(c.sensitivity * 100) - 10);
        form.addView(slider, Ui.size(context, -1, 48));
        sensitivity.setText(String.format(Locale.CHINA, "灵敏度：滑满区域变化 %.2f×", c.sensitivity));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                sensitivity.setText(String.format(Locale.CHINA, "灵敏度：滑满区域变化 %.2f×", (value + 10) / 100f));
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        Switch vertical = toggle(context, form, "使用上下滑动（默认左右）", c.vertical);
        Switch reverse = toggle(context, form, "反转加速方向", c.reverse);
        Switch hold = toggle(context, form, "先按住 0.2 秒再滑动", c.holdToDrag);
        Switch doubleTap = toggle(context, form, "双击调速区域恢复 1.00×", c.doubleTapReset);
        Switch pitch = toggle(context, form, "变速时保持原音调", c.preservePitch);
        Switch haptics = toggle(context, form, "调速时轻微震动", c.haptics);
        Switch screen = toggle(context, form, "停留在播放器时保持亮屏", c.keepScreenOn);
        TextView foot = Ui.text(context, "锁屏后仍可播放；屏幕关闭时无法接收滑动手势。", 12, Ui.MUTED);
        form.addView(foot, Ui.size(context, -1, 64));
        ScrollView scroll = new ScrollView(context); scroll.addView(form);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("调速设置").setView(scroll)
                .setNegativeButton("取消", null).setNeutralButton("恢复默认", null).setPositiveButton("保存", null).create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> { save.accept(new SpeedConfig()); dialog.dismiss(); });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                try {
                    SpeedConfig updated = new SpeedConfig();
                    updated.min = Float.parseFloat(min.getText().toString().trim());
                    updated.max = Float.parseFloat(max.getText().toString().trim());
                    updated.sensitivity = (slider.getProgress() + 10) / 100f;
                    updated.vertical = vertical.isChecked(); updated.reverse = reverse.isChecked();
                    updated.holdToDrag = hold.isChecked(); updated.doubleTapReset = doubleTap.isChecked();
                    updated.preservePitch = pitch.isChecked(); updated.haptics = haptics.isChecked(); updated.keepScreenOn = screen.isChecked();
                    updated.validate(); save.accept(updated); dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    max.setError(e instanceof NumberFormatException ? "请输入有效数字" : e.getMessage());
                }
            });
        });
        dialog.show();
    }

    private static EditText number(Context context, LinearLayout parent, String label, float value) {
        TextView title = Ui.text(context, label, 13, Ui.MUTED); parent.addView(title, Ui.size(context, -1, 32));
        EditText field = new EditText(context); field.setText(String.format(Locale.US, "%.2f", value)); field.setTextColor(Ui.TEXT);
        field.setTextSize(20); field.setSingleLine(true); field.setContentDescription(label);
        field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        parent.addView(field, Ui.size(context, -1, 52)); return field;
    }

    private static Switch toggle(Context c, LinearLayout parent, String label, boolean checked) {
        Switch toggle = new Switch(c); toggle.setText(label); toggle.setTextSize(14); toggle.setTextColor(Ui.TEXT); toggle.setChecked(checked);
        parent.addView(toggle, Ui.size(c, -1, 56)); return toggle;
    }
}
