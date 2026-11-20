package org.hachile.runmusic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    static final int BG = 0xff101410, SURFACE = 0xff1c231b, LINE = 0xff343e31;
    static final int TEXT = 0xfff2f5eb, MUTED = 0xffa3af9c, ACCENT = 0xffc8f56b;
    static int dp(Context c, float value) { return Math.round(c.getResources().getDisplayMetrics().density * value); }

    static GradientDrawable shape(int color, int radius, Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(c, radius)); return d;
    }

    static TextView text(Context c, String label, int size, int color) {
        TextView v = new TextView(c); v.setText(label); v.setTextSize(size); v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL); v.setFontFeatureSettings("tnum"); return v;
    }

    static Button button(Context c, String label, Runnable action) {
        Button b = new Button(c); b.setText(label); b.setTextSize(14); b.setTextColor(TEXT); b.setAllCaps(false);
        b.setMinHeight(dp(c, 48)); b.setMinimumHeight(dp(c, 48));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22c8f56b), shape(SURFACE, 14, c), null));
        b.setOnClickListener(v -> action.run()); return b;
    }

    static LinearLayout column(Context c) { LinearLayout v = new LinearLayout(c); v.setOrientation(LinearLayout.VERTICAL); return v; }
    static LinearLayout row(Context c) { LinearLayout v = new LinearLayout(c); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    static LinearLayout.LayoutParams size(Context c, int w, int h) {
        return new LinearLayout.LayoutParams(w < 0 ? w : dp(c, w), h < 0 ? h : dp(c, h));
    }

    static final class Icon extends View {
        String symbol;
        final boolean primary;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Icon(Context c, String symbol, String description, boolean primary, Runnable action) {
            super(c); this.symbol = symbol; this.primary = primary;
            setContentDescription(description); setFocusable(true); setClickable(true);
            setBackground(new RippleDrawable(ColorStateList.valueOf(0x33c8f56b), shape(primary ? ACCENT : SURFACE, primary ? 32 : 16, c), null));
            setOnClickListener(v -> action.run());
        }
        void symbol(String value, String description) { symbol = value; setContentDescription(description); invalidate(); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.save(); canvas.translate(getWidth() / 2f, getHeight() / 2f);
            float unit = dp(getContext(), primary ? 1.1f : .85f); canvas.scale(unit, unit);
            p.setColor(primary ? BG : TEXT); p.setStrokeWidth(2.2f); p.setStrokeCap(Paint.Cap.ROUND); p.setStyle(Paint.Style.STROKE);
            Path path = new Path();
            switch (symbol) {
                case "play":
                    p.setStyle(Paint.Style.FILL); path.moveTo(-6, -11); path.lineTo(11, 0); path.lineTo(-6, 11); path.close(); canvas.drawPath(path, p); break;
                case "pause":
                    p.setStyle(Paint.Style.FILL); canvas.drawRoundRect(-8, -10, -3, 10, 1, 1, p); canvas.drawRoundRect(3, -10, 8, 10, 1, 1, p); break;
                case "prev": case "next":
                    if (symbol.equals("prev")) canvas.scale(-1, 1);
                    p.setStyle(Paint.Style.FILL); path.moveTo(-8, -9); path.lineTo(5, 0); path.lineTo(-8, 9); path.close(); canvas.drawPath(path, p); canvas.drawRect(7, -9, 10, 9, p); break;
                case "settings":
                    for (int y = -8; y <= 8; y += 8) { canvas.drawLine(-11, y, 11, y, p); canvas.drawCircle(y == 0 ? 5 : -4, y, 3, p); } break;
                case "lock":
                    canvas.drawRoundRect(-8, -1, 8, 11, 3, 3, p); canvas.drawArc(-5, -12, 5, 6, 180, 180, false, p); break;
                default:
                    p.setStyle(Paint.Style.FILL); p.setTextSize(16); p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextAlign(Paint.Align.CENTER); canvas.drawText(symbol, 0, 6, p);
            }
            canvas.restore();
        }
        @Override public CharSequence getAccessibilityClassName() { return Button.class.getName(); }
    }
}
