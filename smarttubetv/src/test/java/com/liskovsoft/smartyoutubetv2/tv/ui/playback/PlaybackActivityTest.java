package com.liskovsoft.smartyoutubetv2.tv.ui.playback;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class PlaybackActivityTest {
    @Test
    public void mouseWheelIsHandledAsVolume() {
        PlaybackActivity activity = Robolectric.buildActivity(PlaybackActivity.class).get();

        assertTrue(activity.onGenericMotionEvent(createEvent(InputDevice.SOURCE_MOUSE, MotionEvent.ACTION_SCROLL, MotionEvent.AXIS_VSCROLL, 1f)));
    }

    @Test
    public void releasedGamepadTriggerIsNotConsumed() {
        PlaybackActivity activity = Robolectric.buildActivity(PlaybackActivity.class).get();

        assertFalse(activity.onGenericMotionEvent(createEvent(InputDevice.SOURCE_JOYSTICK, MotionEvent.ACTION_MOVE, MotionEvent.AXIS_LTRIGGER, 0f)));
    }

    private static MotionEvent createEvent(int source, int action, int axis, float value) {
        PointerProperties properties = new PointerProperties();
        properties.id = 0;
        PointerCoords coords = new PointerCoords();
        coords.setAxisValue(axis, value);

        return MotionEvent.obtain(0, 0, action, 1, new PointerProperties[] {properties}, new PointerCoords[] {coords},
                0, 0, 1f, 1f, 0, 0, source, 0);
    }
}
