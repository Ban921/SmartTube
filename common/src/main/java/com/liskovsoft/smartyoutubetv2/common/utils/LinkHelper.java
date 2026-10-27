package com.liskovsoft.smartyoutubetv2.common.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.SplashPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.WebBrowserPresenter;

public class LinkHelper {
    private static final String TAG = LinkHelper.class.getSimpleName();
    private static final String QR_CODE_URL_TEMPLATE = "https://api.qrserver.com/v1/create-qr-code/?data=%s";

    public static void displayShareVideoDialog(Context context, String videoId) {
        displayShareVideoDialog(context, videoId, 0);
    }

    public static void displayShareVideoDialog(Context context, String videoId, int posSec) {
        Uri videoUrl = convertToFullVideoUrl(videoId, posSec);
        showMultiChooser(context, videoUrl);
    }

    public static void displayShareEmbedVideoDialog(Context context, String videoId) {
        displayShareEmbedVideoDialog(context, videoId, 0);
    }

    public static void displayShareEmbedVideoDialog(Context context, String videoId, int posSec) {
        Uri videoUrl = convertToEmbedVideoUrl(videoId, posSec);
        showMultiChooser(context, videoUrl);
    }

    public static void displayShareChannelDialog(Context context, String channelId) {
        Uri channelUrl = convertToFullChannelUrl(channelId);
        showMultiChooser(context, channelUrl);
    }

    public static void displaySharePlaylistDialog(Context context, String playlistId) {
        Uri playlistUrl = convertToPlaylistUrl(playlistId);
        showMultiChooser(context, playlistUrl);
    }

    public static void openUrlInternally(Context context, Uri url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(url);
        intent.setPackage(context.getPackageName());
        PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            SplashPresenter.instance(context).applyNewIntent(intent);
        } else {
            // Fallback to the chooser dialog
            showMultiChooser(context, url);
        }
    }

    public static void showMultiChooser(Context context, Uri url) {
        Intent primaryIntent = new Intent(Intent.ACTION_VIEW);
        Intent secondaryIntent = new Intent(Intent.ACTION_SEND);
        primaryIntent.setData(url);
        secondaryIntent.putExtra(Intent.EXTRA_TEXT, url.toString());
        secondaryIntent.setType("text/plain");
        Intent chooserIntent = Intent.createChooser(primaryIntent, context.getResources().getText(R.string.share_link));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { secondaryIntent });
        chooserIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        try {
            context.startActivity(chooserIntent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Chooser intent not found", e);
        }
    }

    /**
     * https://youtu.be/nragduYePsQ?t=193<br/>
     * https://www.youtube.com/watch?v=nragduYePsQ&t=193
     */
    public static Uri convertToFullVideoUrl(String videoId, int posSec) {
        String url = String.format("https://youtu.be/%s?t=%s", videoId, posSec);
        return Uri.parse(url);
    }

    /**
     * https://www.youtube.com/embed/nragduYePsQ?start=193
     */
    public static Uri convertToEmbedVideoUrl(String videoId, int posSec) {
        String url = String.format("https://www.youtube.com/embed/%s?start=%s", videoId, posSec);
        return Uri.parse(url);
    }

    public static Uri convertToFullChannelUrl(String channelId) {
        String url = String.format("https://www.youtube.com/channel/%s", channelId);
        return Uri.parse(url);
    }

    public static Uri convertToPlaylistUrl(String playlistId) {
        String url = String.format("https://www.youtube.com/playlist?list=%s", playlistId);
        return Uri.parse(url);
    }

    public static String toQrCodeLink(String data) {
        return String.format(QR_CODE_URL_TEMPLATE, data);
    }

    public static void openLink(Context context, String url) {
        try {
            WebBrowserPresenter.instance(context).loadUrl(url);
        } catch (Exception e) {
            // WebView not found. Use alt method.
            openLinkExt(context, url);
        }
    }

    public static void openLinkExt(Context context, String url) {
        try {
            openLinkInTabs(context, url);
        } catch (Exception e) {
            // Permission Denial on Android 9 (SecurityException)
            // Chrome Tabs not found (ActivityNotFoundException)
            Helpers.openLink(context, url); // revert to simple in-browser page
        }
    }

    /**
     * <a href="https://developer.chrome.com/docs/android/custom-tabs/integration-guide/">Chrome custom tabs</a>
     */
    private static void openLinkInTabs(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
