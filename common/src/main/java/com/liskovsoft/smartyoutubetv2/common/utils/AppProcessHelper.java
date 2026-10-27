package com.liskovsoft.smartyoutubetv2.common.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService;
import com.liskovsoft.smartyoutubetv2.common.app.views.ViewManager;
import com.liskovsoft.smartyoutubetv2.common.prefs.BlockedChannelData;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.prefs.MainUIData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;
import com.liskovsoft.youtubeapi.service.internal.MediaServiceData;

public class AppProcessHelper {
    private static final String BOOTSTRAP_ACTIVITY_CLASS_NAME = "com.liskovsoft.smartyoutubetv2.tv.ui.main.SplashActivity";
    private static final Runnable sForceFinishTheApp = () -> Runtime.getRuntime().exit(0);

    /**
     * Finish the app but remain running services
     */
    public static void properlyFinishTheApp(Context context) {
        ViewManager.instance(context).properlyFinishTheApp(context);
    }

    /**
     * Simply kills the app.
     */
    public static void forceFinishTheApp(Context context) {
        persistData(context);
        Utils.postDelayed(sForceFinishTheApp, 1_000);
    }

    public static void cancelFinishTheApp(Context context) {
        ViewManager.instance(context).cancelOnFinish();
        Utils.removeCallbacks(sForceFinishTheApp);
    }

    public static void restartTheApp(Context context) {
        persistData(context);
        Utils.postDelayed(() -> restartTheAppInt(context), 1_000);
    }

    private static void restartTheAppInt(Context context) {
        try {
            Intent intent = new Intent(context, Class.forName(BOOTSTRAP_ACTIVITY_CLASS_NAME));
            intent.putExtra(IntentExtractor.RESTART_INTENT, true);
            ProcessPhoenix.triggerRebirth(context, intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void restartTheApp(Context context, Video video, long posMs) {
        persistData(context);
        Utils.postDelayed(() -> restartTheAppInt(context, video, posMs), 1_000);
    }

    private static void restartTheAppInt(Context context, Video video, long posMs) {
        if (video == null || !video.hasVideo()) {
            return;
        }

        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(String.format("https://www.youtube.com/watch?v=%s&t=%ss", video.videoId, posMs / 1_000)),
                    context,
                    Class.forName(BOOTSTRAP_ACTIVITY_CLASS_NAME)
            );
            intent.putExtra(IntentExtractor.RESTART_INTENT, true);
            intent.putExtra(IntentExtractor.INCOGNITO_INTENT, video.incognito);
            ProcessPhoenix.triggerRebirth(context, intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void persistData(Context context) {
        VideoStateService.instance(context).persistNow();
        PlayerData.instance(context).persistNow();
        PlayerTweaksData.instance(context).persistNow();
        MainUIData.instance(context).persistNow();
        GeneralData.instance(context).persistNow();
        MediaServiceData mediaServiceData = MediaServiceData.instance();
        mediaServiceData.persistNow();
        BlockedChannelData.instance(context).persistNow();
    }
}
