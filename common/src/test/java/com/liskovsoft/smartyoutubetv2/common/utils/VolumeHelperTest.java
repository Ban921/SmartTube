package com.liskovsoft.smartyoutubetv2.common.utils;

import android.app.Application;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import com.liskovsoft.smartyoutubetv2.common.app.models.playback.manager.PlayerManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class VolumeHelperTest {
    private static final int STREAM = AudioManager.STREAM_MUSIC;
    private static final float DELTA = 0.001f;

    private Application mContext;
    private AudioManager mAudioManager;

    @Before
    public void setUp() throws Exception {
        resetVolumeState(null);
        mContext = RuntimeEnvironment.getApplication();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    @After
    public void tearDown() throws Exception {
        resetVolumeState(null);
    }

    @Test
    public void setVolumeScalesPercentToSystemStream() {
        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM);
        mAudioManager.setStreamVolume(STREAM, maxVolume / 2, 0);
        float[] playerVolume = {1.0f};

        VolumeHelper.setVolume(mContext, createPlayer(playerVolume), 80);

        assertEquals((int) Math.ceil(maxVolume * 0.8), mAudioManager.getStreamVolume(STREAM));
        assertEquals(80, VolumeHelper.getVolume(mContext, createPlayer(playerVolume)));
        assertEquals("Player volume is untouched when the system stream works", 1.0f, playerVolume[0], DELTA);
    }

    @Test
    public void fixedGlobalVolumeFallsBackToPlayer() throws Exception {
        resetVolumeState(true);
        int streamVolume = mAudioManager.getStreamVolume(STREAM);
        float[] playerVolume = {1.0f};

        VolumeHelper.setVolume(mContext, createPlayer(playerVolume), 30);

        assertEquals(0.3f, playerVolume[0], DELTA);
        assertEquals(30, VolumeHelper.getVolume(mContext, createPlayer(playerVolume)));
        assertEquals("System stream is untouched when global volume is fixed", streamVolume, mAudioManager.getStreamVolume(STREAM));
    }

    @Test
    public void negativeVolumeIsClampedToZero() throws Exception {
        resetVolumeState(true);
        float[] playerVolume = {0.5f};

        VolumeHelper.setVolume(mContext, createPlayer(playerVolume), -20);

        assertEquals(0f, playerVolume[0], DELTA);
    }

    @Test
    public void audioObserverRegistrationIsReversible() {
        ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {};

        VolumeHelper.registerAudioObserver(mContext, observer);
        assertEquals(1, shadowOf(mContext.getContentResolver()).getContentObservers(Settings.System.CONTENT_URI).size());

        VolumeHelper.unregisterAudioObserver(mContext, observer);
        assertEquals(0, shadowOf(mContext.getContentResolver()).getContentObservers(Settings.System.CONTENT_URI).size());
    }

    /**
     * Only the volume accessors are exercised by {@link VolumeHelper}.
     */
    private static PlayerManager createPlayer(float[] volume) {
        return (PlayerManager) Proxy.newProxyInstance(PlayerManager.class.getClassLoader(), new Class<?>[] {PlayerManager.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getVolume":
                            return volume[0];
                        case "setVolume":
                            volume[0] = (float) args[0];
                            return null;
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private static void resetVolumeState(Boolean globalVolumeFixed) throws Exception {
        Field fixedField = VolumeHelper.class.getDeclaredField("sGlobalVolumeFixed");
        fixedField.setAccessible(true);
        fixedField.set(null, globalVolumeFixed);

        Field currentField = VolumeHelper.class.getDeclaredField("sCurrentVolume");
        currentField.setAccessible(true);
        currentField.setInt(null, -1);
    }
}
