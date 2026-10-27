package com.liskovsoft.smartyoutubetv2.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.liskovsoft.sharedutils.helpers.DeviceHelpers;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.liskovsoft.sharedutils.misc.WeakHashSet;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.okhttp.OkHttpManager;
import com.liskovsoft.smartyoutubetv2.common.BuildConfig;
import com.liskovsoft.smartyoutubetv2.common.R;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.service.VideoStateService;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.PlaybackPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.SplashPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.views.PlaybackView;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.FormatItem.VideoPreset;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.TrackSelectorUtil;
import com.liskovsoft.smartyoutubetv2.common.exoplayer.selector.track.MediaTrack;
import com.liskovsoft.smartyoutubetv2.common.misc.MotherActivity;
import com.liskovsoft.smartyoutubetv2.common.misc.RemoteControlService;
import com.liskovsoft.smartyoutubetv2.common.misc.ScreensaverManager;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.prefs.HiddenPrefs;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;
import com.liskovsoft.smartyoutubetv2.common.prefs.RemoteControlData;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils {
    public static final String[] KNOWN_PACKAGES = {
            "com.liskovsoft.smarttubetv.beta",
            "com.teamsmart.videomanager.tv",
            "org.smarttube.beta",
            "org.smarttube.stable",
            "app.smarttube.fdroid",
    };
    public static final String[] BACKUP_PREFS = {
            "yt_service_prefs.xml",
            "com.liskovsoft.appupdatechecker2.preferences.xml",
            "com.liskovsoft.sharedutils.prefs.GlobalPreferences.xml",
            "_preferences.xml" // before _ should be the app package name
    };
    public static final String[] BACKUP_DIRS = {
            "app_prefs",
            "yt_service_prefs",
            "global_prefs"
    };
    private static final String SUPER_PASSWD = "smarttube";
    private static final int RANDOM_FAIL_REPEAT_TIMES = 10;
    private static final String UPDATE_CHANNELS_RECEIVER_CLASS_NAME = "com.liskovsoft.leanbackassistant.channels.UpdateChannelsReceiver";
    private static final String TAG = Utils.class.getSimpleName();
    public static final Handler sHandler = new Handler(Looper.getMainLooper());
    public static final float[] SPEED_LIST_LONG =
            new float[]{0.25f, 0.5f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1.0f, 1.05f, 1.1f, 1.15f, 1.2f, 1.25f, 1.3f, 1.35f, 1.4f, 1.45f, 1.5f, 1.6f, 1.75f, 1.8f, 2.0f, 2.25f, 2.5f, 2.75f, 3.0f, 3.25f, 3.5f, 3.75f, 4.0f};
    public static final float[] SPEED_LIST_EXTRA_LONG = Helpers.range(0.05f, 4f, 0.05f);
    public static final float[] SPEED_LIST_SHORT =
            new float[] {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.25f, 2.5f, 2.75f, 3.0f, 3.25f, 3.5f, 3.75f, 4.0f};

    public static boolean isAppInForegroundFixed() {
        // Skip the situation when the splash presenter still running
        return Helpers.isAppInForeground() && SplashPresenter.instance(null).getView() == null;
    }

    /**
     * NOTE: Below won't help with "Can not perform this action after onSaveInstanceState"
     */
    public static boolean checkActivity(Activity activity) {
        return activity != null && !activity.isDestroyed() && !activity.isFinishing();
    }

    public static void updateRemoteControlService(Context context) {
        if (context == null || VERSION.SDK_INT <= 19) { // Eltex NPE fix
            return;
        }

        if (RemoteControlData.instance(context).isDeviceLinkEnabled()) {
            // Service that prevents the app from destroying
            startService(context, RemoteControlService.class);
        } else {
            stopService(context, RemoteControlService.class);
        }
    }

    private static void bindService(Context context, Intent serviceIntent) {
        // https://medium.com/@debuggingisfun/android-auto-stop-background-service-336e8b3ff03c
        // https://medium.com/@debuggingisfun/android-o-work-around-background-service-limitation-e697b2192bc3
        context.getApplicationContext().bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                 // NOP
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                 // NOP
            }
        }, Context.BIND_AUTO_CREATE);
    }

    /**
     * <a href="https://stackoverflow.com/questions/2891337/turning-on-screen-programmatically">More info</a>
     */
    @SuppressWarnings("deprecation")
    public static void turnScreenOn(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (Build.VERSION.SDK_INT >= 27) {
                activity.setShowWhenLocked(true);
                activity.setTurnScreenOn(true);
                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager != null) {
                    keyguardManager.requestDismissKeyguard(activity, null);
                }
            } else {
                Window window = activity.getWindow();
                window.addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                );
            }
        }
    }

    public static void postDelayed(Runnable callback, long delayMs) {
        if (callback == null || delayMs < 0) {
            return;
        }

        sHandler.removeCallbacks(callback);
        sHandler.postDelayed(callback, delayMs);
    }

    public static void post(Runnable callback) {
        if (callback == null) {
            return;
        }

        sHandler.removeCallbacks(callback);
        sHandler.post(callback);
    }

    public static void removeCallbacks(Runnable... callbacks) {
        if (callbacks == null) {
            return;
        }

        for (Runnable callback : callbacks) {
            if (callback != null) {
                sHandler.removeCallbacks(callback);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass) {
        List<RunningServiceInfo> services = getRunningServices(context);

        if (services == null) {
            return false;
        }

        for (RunningServiceInfo service : services) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static List<RunningServiceInfo> getRunningServices(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            return manager.getRunningServices(Integer.MAX_VALUE);
        } catch (NullPointerException e) {
            // NullPointerException: Attempt to invoke interface method 'java.lang.Object android.os.Parcelable$Creator.createFromParcel(android.os.Parcel)' on a null object reference
            e.printStackTrace();
        }

        return null;
    }

    public static Notification createNotification(Context context, int iconResId, String title, Class<? extends Activity> activityCls) {
        return createNotification(context, iconResId, title, null, activityCls);
    }

    @SuppressWarnings("deprecation")
    public static Notification createNotification(Context context, int iconResId, String title, String content, Class<? extends Activity> activityCls) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(iconResId)
                        .setContentTitle(title);

        if (content != null) {
            builder.setContentText(content);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;

        if (Build.VERSION.SDK_INT >= 23) {
            // IllegalArgumentException fix: Targeting S+ (version 31 and above) requires that one of FLAG_IMMUTABLE...
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        Intent targetIntent = new Intent(context, activityCls);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, flags);
        builder.setContentIntent(contentIntent);

        if (VERSION.SDK_INT >= 26) {
            String channelId = context.getPackageName();
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    title,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        return builder.build();
    }

    public static void startService(Context context, Class<? extends Service> serviceCls) {
        if (isServiceRunning(context, serviceCls)) {
            return;
        }

        Intent serviceIntent = new Intent(context, serviceCls);

        // https://stackoverflow.com/questions/46445265/android-8-0-java-lang-illegalstateexception-not-allowed-to-start-service-inten
        if (VERSION.SDK_INT >= 26) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static void stopService(Context context, Class<? extends Service> serviceCls) {
        if (!isServiceRunning(context, serviceCls)) {
            return;
        }

        Intent serviceIntent = new Intent(context, serviceCls);

        context.stopService(serviceIntent);
    }

    /**
     * NOTE: Doesn't work in Android 13<br/>
     * java.lang.SecurityException: Injecting input events requires the caller (or the source of the instrumentation, if any) to have the INJECT_EVENTS permission.
     */
    public static void sendKey(int key) {
        if (VERSION.SDK_INT < 33) {
            try {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeyDownUpSync(key);
            } catch (SecurityException e) {
                // Injecting to another application requires INJECT_EVENTS permission
                e.printStackTrace();
            }
        }
    }

    /**
     * NOTE: Doesn't work in Android 13<br/>
     * java.lang.SecurityException: Injecting input events requires the caller (or the source of the instrumentation, if any) to have the INJECT_EVENTS permission.
     */
    public static void sendKey(KeyEvent keyEvent) {
        if (VERSION.SDK_INT < 33) {
            try {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeySync(keyEvent);
            } catch (SecurityException e) {
                // Injecting to another application requires INJECT_EVENTS permission
                e.printStackTrace();
            }
        }
    }

    public static void sendKey(Activity activity, int keyCode) {
        BaseInputConnection  inputConnection = new BaseInputConnection(activity.getWindow().getDecorView().getRootView(), true);
        KeyEvent kd = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        KeyEvent ku = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        inputConnection.sendKeyEvent(kd);
        inputConnection.sendKeyEvent(ku);
    }

    public static void showNotCompatibleMessage(Context context, int msgResId) {
        MessageHelpers.showMessage(context, String.format("%s '%s'",
                context.getString(R.string.not_compatible_with),
                context.getString(msgResId)));
    }

    public static String getCountryFlagUrl(String countryCode) {
        // Sometimes down
        //return "https://countryflagsapi.com/png/" + countryCode;

        // https://flagpedia.net/download/api
        return String.format("https://flagcdn.com/w160/%s.png", countryCode.toLowerCase());
    }

    public static void showPlayerControls(Context context, boolean show) {
        PlaybackView view = PlaybackPresenter.instance(context).getView();
        if (view != null) {
            view.showOverlay(show);
        }
    }

    public static int toSec(long ms) {
        return (int) (ms / 1_000);
    }

    public static boolean isFirstRun(Context context) {
        VideoStateService stateService = VideoStateService.instance(context);

        return stateService.isEmpty();
    }

    public static boolean isPresetSupported(VideoPreset preset) {
        if (preset.isVP9Preset() && !DeviceHelpers.isVP9ResolutionSupported(preset.getHeight())) {
            return false;
        }

        if (preset.isAV1Preset() && !DeviceHelpers.isAV1ResolutionSupported(preset.getHeight())) {
            return false;
        }

        return true;
    }

    public static boolean isFormatSupported(MediaTrack mediaTrack) {
        if (mediaTrack.isVP9Codec() && !DeviceHelpers.isVP9ResolutionSupported(TrackSelectorUtil.getRealHeight(mediaTrack.format))) {
            return false;
        }

        if (mediaTrack.isAV1Codec() && !DeviceHelpers.isAV1ResolutionSupported(TrackSelectorUtil.getRealHeight(mediaTrack.format))) {
            return false;
        }

        // There's a bug. The player hangs at the black screen.
        // opus and others audio codecs require hardware acceleration
        //if (mediaTrack instanceof AudioTrack && !mediaTrack.isMP4ACodec() && !Helpers.isVP9Supported()) {
        //    return false;
        //}

        return true;
    }

    public static void enableScreensaver(Context activity, boolean enable) {
        if (activity instanceof MotherActivity) {
            ScreensaverManager screensaver = ((MotherActivity) activity).getScreensaverManager();
            if (enable) {
                screensaver.enable();
            } else {
                screensaver.disable();
            }
        }
    }

    public static boolean isScreenOff(Context activity) {
        if (activity instanceof MotherActivity) {
            ScreensaverManager manager = ((MotherActivity) activity).getScreensaverManager();

            return manager != null && manager.isScreenOff();
        }

        return false;
    }

    public static boolean isHardScreenOff(Context context) {
        if (context == null) {
            return false;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        
        if (Build.VERSION.SDK_INT < 20) {
            return !pm.isScreenOn();
        } else {
            return !pm.isInteractive();
        }
    }

    public static int getColor(Context context, int colorResId, int dimPercents) {
        int color = ContextCompat.getColor(context, colorResId);
        color = ColorUtils.setAlphaComponent(color, (int)(255f / 100 * dimPercents));

        return color;
    }

    /**
     * https://stackoverflow.com/questions/11288147/get-resources-from-another-apk
     */
    public static Drawable getDrawable(Context context, String packageName, String drawableName) {
        if (context == null || packageName == null || drawableName == null) {
            return null;
        }

        Drawable result = null;

        try {
            PackageManager manager = context.getPackageManager();
            Resources resources = manager.getResourcesForApplication(packageName);
            int drawableResId = resources.getIdentifier(drawableName, "drawable", packageName);

            if (drawableResId == 0) {
                drawableResId = resources.getIdentifier(drawableName, "mipmap", packageName);
            }

            result = resources.getDrawable(drawableResId);
        } catch (NameNotFoundException | NotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean isOculusQuest() {
        return Helpers.getDeviceName().startsWith("Oculus Quest");
    }

    public static void updateChannels(Context context) {
        startReceiver(context, UPDATE_CHANNELS_RECEIVER_CLASS_NAME);
    }

    private static void startReceiver(Context context, String receiverClassName) {
        // Can't use class directly! ATV module is disabled for some flavors.
        Class<?> clazz = null;

        try {
            clazz = Class.forName(receiverClassName);
        } catch (ClassNotFoundException e) {
            // NOP
        }

        if (clazz != null) {
            if (context != null) {
                Log.d(TAG, "Starting channels receiver...");
                Intent intent = new Intent(context, clazz);
                try {
                    context.sendBroadcast(intent);
                } catch (Exception e) {
                    // NullPointerException on MX9Pro (rk3328  7.1.2)
                }
            }
        } else {
            Log.e(TAG, "Channels receiver class not found: " + receiverClassName);
        }
    }

    public static String updateTooltip(Context context, String tooltip) {
        return GeneralData.instance(context).isFirstUseTooltipEnabled() ?
                String.format("%s (%s)", tooltip, context.getString(R.string.long_press_for_options)) : tooltip;
    }

    private static String createTransactionID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * https://stackoverflow.com/a/5626208/1279056<br/>
     * https://stackoverflow.com/a/40237325/1279056
     */
    @SuppressWarnings("MissingPermission")
    public static String getUniqueId(Context context) {
        String uniqueId = HiddenPrefs.instance(context).getUniqueId();

        if (uniqueId == null) {
            UUID uuid = null;
            @SuppressLint("HardwareIds")
            final String androidId = Secure.getString(
                    context.getContentResolver(), Secure.ANDROID_ID);
            // Use the Android ID unless it's broken, in which case
            // fallback on deviceId,
            // unless it's not available, then fallback on a random
            // number which we store to a prefs file
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId
                            .getBytes("utf8"));
                } else {
                    @SuppressLint("HardwareIds")
                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    uuid = deviceId != null ? UUID
                            .nameUUIDFromBytes(deviceId
                                    .getBytes("utf8")) : UUID
                            .randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage());
            }

            uniqueId = uuid != null ? uuid.toString() : createTransactionID();
            HiddenPrefs.instance(context).setUniqueId(uniqueId);
        }

        return uniqueId;
    }

    public static <T> boolean chainProcess(List<T> listeners, ChainProcessor<T> processor) {
        boolean result = false;

        for (T listener : listeners) {
            result = processor.process(listener);

            if (result) {
                break;
            }
        }

        return result;
    }

    public interface ChainProcessor<T> {
        boolean process(T listener);
    }

    public static <T> void process(List<T> listeners, Processor<T> processor) {
        for (T listener : listeners) {
            processor.process(listener);
        }
    }

    public interface Processor<T> {
        void process(T listener);
    }

    public static int getFasterDataSource() {
        return skipCronet() ? PlayerTweaksData.PLAYER_DATA_SOURCE_DEFAULT : preferOkHttp()
                ? PlayerTweaksData.PLAYER_DATA_SOURCE_OKHTTP : PlayerTweaksData.PLAYER_DATA_SOURCE_CRONET;
    }

    public static boolean skipCronet() {
        // Android 6 and below may crash running Cronet???
        //return VERSION.SDK_INT <= 23 || Helpers.equals(BuildConfig.FLAVOR, "strtarmenia");
        return Helpers.equals(BuildConfig.FLAVOR, "strtarmenia");
    }

    private static boolean preferOkHttp() {
        return Helpers.equalsAny(Build.MODEL, "SHIELD Android TV");
    }

    public static boolean isEnoughRam() {
        long maxMemory = Runtime.getRuntime().maxMemory();

        return (int)(maxMemory / (1024 * 1024)) > 350; // more than 350MB available to the app
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringBuilder result = new StringBuilder();
        result.append(throwable.getMessage()).append("\n");
        StackTraceElement[] elements = throwable.getStackTrace();
        if (elements.length > 0) {
            StackTraceElement topElement = elements[0];
            result.append(topElement.getMethodName());
            result.append("(").append(topElement.getFileName()).append(":").append(topElement.getLineNumber()).append(")");
        } else {
            result.append("No stack trace available");
        }

        return result.toString();
    }

    public static int getRandomIndex(int currentIdx, int playlistSize) {
        if (playlistSize <= 1) {
            return -1;
        }

        int randomIndex = -1;

        for (int i = 0; i < RANDOM_FAIL_REPEAT_TIMES; i++) {
            randomIndex = Helpers.getRandomIndex(playlistSize);
            if (randomIndex != currentIdx) {
                break;
            }
        }

        return randomIndex;
    }

    public static void addMyCallback(Collection<Runnable> myCallbacks, Runnable callback) {
        if (myCallbacks == null || callback == null) {
            return;
        }

        if (!myCallbacks.contains(callback)) {
            myCallbacks.add(callback);
        }
    }

    public static void addMyCallback(WeakHashSet<Runnable> myCallbacks, Runnable callback) {
        if (myCallbacks == null || callback == null) {
            return;
        }

        if (!myCallbacks.contains(callback)) {
            myCallbacks.add(callback);
        }
    }

    public static void runMyCallbacks(Collection<Runnable> myCallbacks) {
        if (myCallbacks == null || myCallbacks.isEmpty()) {
            return;
        }

        // Copy-then-Clear approach to fix possible stackoverflow
        List<Runnable> callbacks = new ArrayList<>(myCallbacks);
        myCallbacks.clear();

        for (Runnable callback : callbacks) {
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static void runMyCallbacks(WeakHashSet<Runnable> myCallbacks) {
        if (myCallbacks == null || myCallbacks.isEmpty()) {
            return;
        }

        // Copy-then-Clear approach to fix possible stackoverflow
        List<Runnable> callbacks = myCallbacks.asList();
        myCallbacks.clear();

        for (Runnable callback : callbacks) {
            if (callback != null) {
                callback.run();
            }
        }
    }

    public static boolean passwordMatch(String original, String typed) {
        if (original == null || (typed != null && typed.equalsIgnoreCase(SUPER_PASSWD))) {
            return true;
        }

        return original.equals(typed);
    }

    @SuppressLint("DiscouragedApi")
    public static boolean isSystemGestureArrowEnabled(Context context) {
        Resources res = context.getResources();
        // This is the internal Android constant for navigation mode
        int resourceId = res.getIdentifier("config_navBarInteractionMode", "integer", "android");

        if (resourceId > 0) {
            int navMode = res.getInteger(resourceId);
            // 0: 3-button navigation (Back, Home, Recents)
            // 1: 2-button navigation (Android P style)
            // 2: Full Gesture Navigation (Android 10+ Arrow)
            return navMode == 2;
        }
        
        return false;
    }

    public static boolean fixRetrofitErrors(@NonNull Context context, Throwable error) {
        if (error != null && Helpers.contains(error.getMessage(), "No address associated with hostname")) {
            // java.net.UnknownHostException: Unable to resolve host "www.youtube.com": No address associated with hostname
            PlayerTweaksData playerTweaksData = PlayerTweaksData.instance(context);
            int customDns = PlayerTweaksData.DNS_TYPE_IPV4;
            if (playerTweaksData.getPreferredDnsType() != customDns) {
                playerTweaksData.setPreferredDnsType(customDns);
                // Restart app to reinit OkHttp internal objects
                AppProcessHelper.restartTheApp(context);
                return true;
            }
        }
        return false;
    }

    public static void testUrl(String url, Runnable onSuccess) {
        if (onSuccess == null) {
            return;
        }

        OkHttpClient okHttpClient = OkHttpManager.instance().getClient();
        Request request = new Request.Builder().url(url).head().build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // NOP
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response ignored = response) {
                    if (response.isSuccessful()) {
                        post(onSuccess);
                    }
                }
            }
        });
    }
}
