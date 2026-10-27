package com.liskovsoft.smartyoutubetv2.common.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;

import androidx.core.content.ContextCompat;

public class SpanHelper {
    public static CharSequence color(CharSequence string, int color, int start, int end) {
        SpannableString spannable = new SpannableString(string);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);
        spannable.setSpan(foregroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    public static CharSequence color(CharSequence string, int color) {
        SpannableString spannable = new SpannableString(string);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(color);
        spannable.setSpan(foregroundColorSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    /**
     * NOTE: Android 5.1: Italic cause crashes with Arabic fonts
     */
    public static CharSequence italic(CharSequence string) {
        if (Build.VERSION.SDK_INT <= 22) {
            return string;
        }

        SpannableString spannable = new SpannableString(string);
        spannable.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static CharSequence bold(CharSequence string) {
        SpannableString spannable = new SpannableString(string);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static CharSequence createSmallNewLine() {
        SpannableString spannable = new SpannableString("\n");
        // Make this line smaller by setting a smaller font size
        spannable.setSpan(new AbsoluteSizeSpan(6, true), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public static CharSequence icon(Context context, int resId, int lineHeight) {
        SpannableString spannable = new SpannableString(" ");
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(0, 0, lineHeight, lineHeight);
        ImageSpan imageSpan = new ImageSpan(drawable);
        spannable.setSpan(imageSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }

    public static CharSequence icon(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        SpannableString spannable = new SpannableString(" ");
        CenteredImageSpan imageSpan = new CenteredImageSpan(drawable);
        spannable.setSpan(imageSpan, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }
}
