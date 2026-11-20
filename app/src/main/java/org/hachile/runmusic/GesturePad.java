package org.hachile.runmusic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

final class GesturePad extends View {
    interface Listener { void onSpeed(float speed); }
    SpeedConfig config;
    float speed = 1f;
    boolean locked;
    private final Listener listener;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final GestureDetector detector;
    private float downX, downY, initial;
    private boolean tracking, dragging, suppressTouch;
    private long downTime, lastUpdate;
    private final int slop;

    GesturePad(Context context, SpeedConfig config, Listener listener) {
        super(context); this.config = config; this.listener = listener;
        setFocusable(true); setClickable(true);
        slop = ViewConfiguration.get(context).getScaledTouchSlop();
        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onDoubleTap(MotionEvent e) {
                if (!locked && GesturePad.this.config.doubleTapReset) update(GesturePad.this.config.clamp(1f));
                tracking = false; suppressTouch = true; return true;
            }
        });
        describe();
    }

    void setSpeed(float value) { speed = value; describe(); invalidate(); }
    void setLocked(boolean value) { locked = value; tracking = false; describe(); invalidate(); }
    private void describe() {
        String direction = config.vertical ? (config.reverse ? "向下滑动加速，向上减速" : "向上滑动加速，向下减速")
                : (config.reverse ? "向左滑动加速，向右减速" : "向右滑动加速，向左减速");
        setContentDescription(String.format(Locale.CHINA, "调速区域，当前 %.2f 倍。%s", speed,
                locked ? "手势已锁定" : direction + "，可使用辅助功能增减倍速"));
    }

    private void update(float value) {
        if (value == speed) return;
        if (config.haptics && Math.round(value * 20) != Math.round(speed * 20)) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        setSpeed(value); listener.onSpeed(value);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (locked) return true;
        if (e.getPointerCount() > 1) { tracking = false; dragging = false; invalidate(); return true; }
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) suppressTouch = false;
        detector.onTouchEvent(e);
        if (suppressTouch) return true;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                tracking = true; dragging = false; initial = speed;
                downX = e.getX(); downY = e.getY(); downTime = e.getEventTime();
                getParent().requestDisallowInterceptTouchEvent(true); break;
            case MotionEvent.ACTION_MOVE:
                if (!tracking) break;
                float delta = config.vertical ? downY - e.getY() : e.getX() - downX;
                float orthogonal = config.vertical ? e.getX() - downX : e.getY() - downY;
                if (!dragging && Math.abs(orthogonal) > slop && Math.abs(orthogonal) > Math.abs(delta)) { tracking = false; break; }
                if (!dragging) {
                    if (config.holdToDrag && e.getEventTime() - downTime < 200) {
                        if (Math.hypot(e.getX() - downX, e.getY() - downY) > slop) tracking = false;
                        break;
                    }
                    if (Math.abs(delta) <= slop) break;
                    dragging = true;
                }
                if (e.getEventTime() - lastUpdate >= 32) {
                    update(config.drag(initial, delta, config.vertical ? getHeight() : getWidth()));
                    lastUpdate = e.getEventTime();
                }
                invalidate(); break;
            case MotionEvent.ACTION_UP:
                if (tracking && dragging) update(config.drag(initial, config.vertical ? downY - e.getY() : e.getX() - downX,
                        config.vertical ? getHeight() : getWidth()));
                if (tracking && !dragging) performClick();
                tracking = false; dragging = false; invalidate();
                getParent().requestDisallowInterceptTouchEvent(false); break;
            case MotionEvent.ACTION_CANCEL:
                tracking = false; dragging = false; invalidate(); getParent().requestDisallowInterceptTouchEvent(false); break;
        }
        return true;
    }

    @Override public boolean performClick() { super.performClick(); return true; }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.SeekBar");
        info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_FLOAT, config.min, config.max, speed));
        if (!locked) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
        }
    }

    @Override public boolean performAccessibilityAction(int action, Bundle args) {
        if (!locked && (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD || action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
            update(config.clamp(speed + (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD ? .01f : -.01f))); return true;
        }
        return super.performAccessibilityAction(action, args);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight(), d = getResources().getDisplayMetrics().density;
        paint.setTextAlign(Paint.Align.CENTER); paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(Ui.MUTED); paint.setTextSize(12 * d);
        canvas.drawText(locked ? "手势已锁定" : dragging ? "跟着你的步调" : "播放倍速", w / 2, h * .14f, paint);
        paint.setColor(locked ? Ui.MUTED : Ui.ACCENT); paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        paint.setTextSize(Math.min(w * .29f, h * .36f));
        canvas.drawText(String.format(Locale.US, "%.2f", speed), w / 2, h * .49f, paint);
        paint.setTypeface(Typeface.DEFAULT); paint.setTextSize(14 * d); paint.setColor(Ui.MUTED);
        canvas.drawText("×", w / 2, h * .59f, paint);
        float left = 20 * d, right = w - left, y = h * .72f;
        for (int i = 0; i <= 40; i++) {
            float x = left + (right - left) * i / 40;
            paint.setColor(i % 5 == 0 ? Ui.MUTED : Ui.LINE); paint.setStrokeWidth(d);
            canvas.drawLine(x, y, x, y + (i % 5 == 0 ? 15 : 8) * d, paint);
        }
        float x = left + (right - left) * (speed - config.min) / (config.max - config.min);
        paint.setColor(Ui.ACCENT); paint.setStrokeWidth(3 * d);
        canvas.drawLine(x, y - 5 * d, x, y + 20 * d, paint);
        paint.setColor(Ui.MUTED); paint.setTextSize(11 * d);
        paint.setTextAlign(Paint.Align.LEFT); canvas.drawText(String.format(Locale.US, "%.2f×", config.min), left, y + 36 * d, paint);
        paint.setTextAlign(Paint.Align.RIGHT); canvas.drawText(String.format(Locale.US, "%.2f×", config.max), right, y + 36 * d, paint);
        paint.setTextAlign(Paint.Align.CENTER); paint.setTextSize(12 * d);
        String hint = locked ? "点击右下角解锁" : config.vertical ? "上下滑动调速" : "←  左右滑动调速  →";
        if (!locked && config.holdToDrag) hint = "按住片刻，再" + (config.vertical ? "上下" : "左右") + "滑动";
        canvas.drawText(hint, w / 2, h * .96f, paint);
    }
}
