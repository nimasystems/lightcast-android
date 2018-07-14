package com.nimasystems.lightcast.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.nimasystems.lightcast.exceptions.SystemException;

public class SysUtils {

    private static final String TAG = "SysUtils";

    // cache the deviceId
    private static String mDeviceId;

    public static synchronized String getDeviceId(Context context) {
        return getDeviceId(context, true);
    }

    // returns true if current Android OS on device is >= verCode
    public static boolean androidSupportsSDK(int verCode) {
        if (android.os.Build.VERSION.RELEASE.startsWith("1.0"))
            return verCode == 1;
        else if (android.os.Build.VERSION.RELEASE.startsWith("1.1")) {
            return verCode <= 2;
        } else if (android.os.Build.VERSION.RELEASE.startsWith("1.5")) {
            return verCode <= 3;
        } else {
            return android.os.Build.VERSION.SDK_INT >= verCode;
        }
    }

    /**
     * Get Device Id
     *
     * @param context the current context
     * @return device id. If there is not available (tablets and etc.) it
     * returns custom device id.
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static synchronized String getDeviceId(Context context, boolean useTelephone) {

        if (!StringUtils.isNullOrEmpty(mDeviceId)) {
            return mDeviceId;
        }

        String deviceId = null;

        try {
            // deprecated
            /*if (useTelephone) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    deviceId = tm.getDeviceId();
                    if (deviceId == null || deviceId.equals("")) {
                        deviceId = getCustomDeviceID();
                    } else {
                        deviceId = getCustomDeviceID();
                    }
                } else {
                    deviceId = getCustomDeviceID();
                }
            } else {
                deviceId = getCustomDeviceID();
            }*/

            mDeviceId = getCustomDeviceID();

        } catch (Exception e) {
            e.printStackTrace();

            try {
                deviceId = getCustomDeviceID();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        return deviceId;
    }

    /*public static boolean isRemoteProcess(Context context) {
        long myPid = (long) Process.myPid();

        String packageNameD = AppContextUtils.getPackageName(context) + ":remote";

        ActivityManager manager = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));

        if (manager == null) {
            return false;
        }

        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();

        boolean ret = false;

        if (runningAppProcesses != null && runningAppProcesses.size() != 0) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                if (((long) runningAppProcessInfo.pid) == myPid && packageNameD.equals(runningAppProcessInfo.processName)) {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }*/

    public static String getPackageVersion(Context context) {
        String clientApiVersion = null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(),
                    0);
            clientApiVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return clientApiVersion;
    }

    /**
     * Get custom device id
     *
     * @return custom device id.
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private static String getCustomDeviceID() {

        String cpuAbi;

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            String[] sabis = Build.SUPPORTED_ABIS;
            cpuAbi = (sabis != null && sabis.length > 0 ? sabis[0] : null);
        } else {
            cpuAbi = Build.CPU_ABI;
        }

        cpuAbi = (cpuAbi == null ? "" : cpuAbi);

        return "35" + Build.BOARD.length() % 10
                + Build.BRAND.length() % 10 + cpuAbi.length() % 10
                + Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10
                + Build.HOST.length() % 10 + Build.ID.length() % 10
                + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10
                + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10
                + Build.TYPE.length() % 10 + Build.USER.length() % 10;
    }

    public static void checkThrowOnMainThread() throws SystemException {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new SystemException(
                    "Operation can be ran only on the main thread!");
        }
    }

    public static void doRestart(Context c) {
        try {
            // check if the context is given
            if (c != null) {
                // fetch the packagemanager so we can get the default launch
                // activity
                // (you can replace this intent with any other activity if you
                // want
                PackageManager pm = c.getPackageManager();
                // check if we got the PackageManager
                if (pm != null) {
                    // create the intent with the default start activity for
                    // your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(c
                            .getPackageName());
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        // create a pending intent so the application is
                        // restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId,
                                        mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c
                                .getSystemService(Context.ALARM_SERVICE);

                        if (mgr != null) {
                            mgr.set(AlarmManager.RTC,
                                    System.currentTimeMillis() + 100,
                                    mPendingIntent);
                        }

                        // kill the application
                        System.exit(0);
                    } else {
                        Log.e(TAG,
                                "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }
}
