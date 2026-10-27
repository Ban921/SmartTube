package com.liskovsoft.smartyoutubetv2.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.manager.PlayerManager;

public class VolumeHelper {
    private static final String TAG = VolumeHelper.class.getSimpleName();
    private static final int GLOBAL_VOLUME_TYPE = AudioManager.STREAM_MUSIC;
    private static final String GLOBAL_VOLUME_SERVICE = Context.AUDIO_SERVICE;
    private static Boolean sGlobalVolumeFixed;
    private static int sCurrentVolume = -1;

    /**
     * Volume: 0 - 100
     */
    private static void setGlobalVolume(Context context, int volume) {
        if (context != null) {
            AudioManager audioManager = (AudioManager) context.getSystemService(GLOBAL_VOLUME_SERVICE);
            if (audioManager != null) {
                int streamMaxVolume = audioManager.getStreamMaxVolume(GLOBAL_VOLUME_TYPE);
                double newVolume = streamMaxVolume / 100d * volume;
                if (sCurrentVolume < volume) {
                    newVolume = Math.ceil(newVolume);
                } else {
                    newVolume = Math.floor(newVolume);
                }
                try {
                    audioManager.setStreamVolume(GLOBAL_VOLUME_TYPE, (int) newVolume, 0);
                    sCurrentVolume = volume;
                } catch (SecurityException | IllegalArgumentException e) {
                    // Not allowed to change Do Not Disturb state
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Volume: 0 - 100
     */
    private static int getGlobalVolume(Context context) {
        if (context != null) {
            AudioManager audioManager = (AudioManager) context.getSystemService(GLOBAL_VOLUME_SERVICE);
            if (audioManager != null) {
                int streamMaxVolume = audioManager.getStreamMaxVolume(GLOBAL_VOLUME_TYPE);
                int streamVolume = audioManager.getStreamVolume(GLOBAL_VOLUME_TYPE);

                int volume = (int) Math.ceil(streamVolume / (streamMaxVolume / 100f));
                return Math.abs(sCurrentVolume - volume) == 1 ? sCurrentVolume : volume; // fix small steps (1). volume should be precise.
            }
        }

        return 100;
    }

    private static void initVolume(Context context) {
        if (sGlobalVolumeFixed != null) {
            return;
        }

        if ("D2150 (Nebula-Cosmos-Max)".equals(Helpers.getDeviceName())) { // A projector
            sGlobalVolumeFixed = true;
            return;
        }

        int volume = getGlobalVolume(context);
        setGlobalVolume(context, volume > 50 ? volume - 10 : volume + 10);
        sGlobalVolumeFixed = volume == getGlobalVolume(context);
        setGlobalVolume(context, volume);
    }

    /**
     * Global volume may not be supported (see FireTV Stick)
     */
    private static boolean isGlobalVolumeFixed(Context context) {
        if (sGlobalVolumeFixed != null) {
            return sGlobalVolumeFixed;
        }

        initVolume(context);
        return sGlobalVolumeFixed;
    }

    /**
     * Volume: 0 - 100
     */
    public static int getVolume(Context context, PlayerManager player) {
        if (context != null) {
            if (isGlobalVolumeFixed(context)) {
                return getPlayerVolume(player);
            } else {
                try {
                    return getGlobalVolume(context);
                } catch (SecurityException e) {
                    // Permission denial: writing to settings requires:android.permission.WRITE_SECURE_SETTINGS
                    return getPlayerVolume(player);
                }
            }
        }

        return 100;
    }

    private static int getPlayerVolume(PlayerManager player) {
        if (player == null) {
            return -1;
        }
        return (int)(player.getVolume() * 100);
    }

    /**
     * Volume: 0 - 100
     */
    public static void setVolume(Context context, PlayerManager player, int volume) {
        Log.d(TAG, "setVolume: %s", volume);

        if (volume < 0) {
            volume = 0;
        }

        if (context != null) {
            if (isGlobalVolumeFixed(context)) {
                setPlayerVolume(context, player, volume);
            } else {
                try {
                    setGlobalVolume(context, volume);
                    showSystemVolumeUI(context);
                } catch (SecurityException e) {
                    // Permission denial: writing to settings requires:android.permission.WRITE_SECURE_SETTINGS
                    setPlayerVolume(context, player, volume);
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private static void setPlayerVolume(Context context, PlayerManager player, int volume) {
        if (player == null) {
            return;
        }
        player.setVolume(volume / 100f);
        MessageHelpers.showMessage(context, context.getString(R.string.volume, getPlayerVolume(player)));
    }

    public static void volumeUp(Context context, PlayerManager player, boolean up) {
        if (player != null) {
            int volume = getVolume(context, player);
            final int delta = 1; // volume step

            if (up) {
                setVolume(context, player, Math.min(volume + delta, 100));
            } else {
                setVolume(context, player, Math.max(volume - delta, 0));
            }
        }
    }

    private static void showSystemVolumeUI(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            // Show the system volume bar without changing the volume
            audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC, // Target the music stream
                    AudioManager.ADJUST_SAME, // No actual adjustment
                    AudioManager.FLAG_SHOW_UI // This flag displays the volume UI
            );
        }
    }

    public static void registerAudioObserver(Context context, ContentObserver observer) {
        context.getApplicationContext().getContentResolver()
                .registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, observer);
    }

    public static void unregisterAudioObserver(Context context, ContentObserver observer) {
        context.getApplicationContext().getContentResolver().unregisterContentObserver(observer);
    }
}
