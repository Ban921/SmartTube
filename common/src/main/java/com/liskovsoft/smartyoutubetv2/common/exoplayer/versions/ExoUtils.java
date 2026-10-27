package com.liskovsoft.smartyoutubetv2.common.exoplayer.versions;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;

public class ExoUtils {
    private static String sVideoDecoderName;

    public static boolean isPlaying(ExoPlayer player) {
        if (player == null) {
            return false;
        }

        // Exo 2.9
        //return player.getPlayWhenReady() && player.getPlaybackState() == Player.STATE_READY;

        // Exo 2.10 and up
        return player.isPlaying();
    }

    public static boolean isLoading(ExoPlayer player) {
        if (player == null) {
            return false;
        }

        return player.isLoading();
    }

    public static boolean isBuffering(ExoPlayer player) {
        if (player == null) {
            return false;
        }

        return player.getPlaybackState() == Player.STATE_BUFFERING;
    }

    public static void updateVideoDecoderInfo(MediaCodecInfo codecInfo) {
        if (codecInfo == null) {
            return;
        }

        sVideoDecoderName = codecInfo.name;
    }

    public static String getVideoDecoderName() {
        return sVideoDecoderName;
    }
}
