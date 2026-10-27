package com.liskovsoft.smartyoutubetv2.common.utils;

import android.graphics.Typeface;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SpanHelperTest {
    private static final int RED = 0xFFFF0000;

    @Test
    public void colorCoversWholeTextOrGivenRange() {
        Spanned whole = (Spanned) SpanHelper.color("hello", RED);
        ForegroundColorSpan wholeSpan = whole.getSpans(0, whole.length(), ForegroundColorSpan.class)[0];
        assertEquals(RED, wholeSpan.getForegroundColor());
        assertEquals(0, whole.getSpanStart(wholeSpan));
        assertEquals(5, whole.getSpanEnd(wholeSpan));

        Spanned range = (Spanned) SpanHelper.color("hello", RED, 1, 3);
        ForegroundColorSpan rangeSpan = range.getSpans(0, range.length(), ForegroundColorSpan.class)[0];
        assertEquals(1, range.getSpanStart(rangeSpan));
        assertEquals(3, range.getSpanEnd(rangeSpan));
    }

    @Test
    public void boldAndItalicApplyStyle() {
        Spanned bold = (Spanned) SpanHelper.bold("text");
        assertEquals(Typeface.BOLD, bold.getSpans(0, bold.length(), StyleSpan.class)[0].getStyle());

        Spanned italic = (Spanned) SpanHelper.italic("text");
        assertEquals(Typeface.ITALIC, italic.getSpans(0, italic.length(), StyleSpan.class)[0].getStyle());
    }

    @Config(sdk = 22)
    @Test
    public void italicIsSkippedOnLollipopMr1() {
        CharSequence source = "text";
        assertSame(source, SpanHelper.italic(source));
    }

    @Test
    public void smallNewLineIsSizedNewLine() {
        Spanned newLine = (Spanned) SpanHelper.createSmallNewLine();
        assertEquals("\n", newLine.toString());
        assertFalse(newLine.getSpans(0, 1, AbsoluteSizeSpan.class).length == 0);
    }

    @Test
    public void nullDrawableIconIsNull() {
        assertNull(SpanHelper.icon(null));
    }
}
