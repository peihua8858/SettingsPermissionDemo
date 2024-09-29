package com.peihua.permissiondemo

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import com.fz.common.utils.toBoolean

data class PermissionData(
    val name: String,
    val action: String,
    val permission: String,
    val isSpecial:Boolean = false,
    val isJumpDetail:Boolean = true,
    val hasPermission: ((Context) -> Boolean)? = null,
    val jumpIntent: (() -> Unit)? = null
) {

}

fun buildSpecialPermissionData(context: Activity): List<PermissionData> {
    val result = mutableListOf<PermissionData>()
    result.add(
        PermissionData(
            "管理APP所有文件访问权限",
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            true,true,
            hasPermission ={ context ->
                return@PermissionData Environment.isExternalStorageManager()
            }
        )
    )
    result.add(
        PermissionData(
            "设备管理应用",
            //设置代码
            //DevicePolicyManager.setActiveAdmin(mDeviceAdmin.getComponent(), mRefreshing);
            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN,
            "",
            true,true,
            hasPermission = { context ->
                return@PermissionData isDeviceAdminActive(context)
            }
        ) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(
                    context,
                    MyDeviceAdminReceiver::class.java
                )
            );
            intent.data = Uri.parse("package:${context.packageName}")
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "需要启用设备管理权限")
            context.startActivityForResult(intent, 11)
        }
    )
    result.add(
        PermissionData(
            "显示在其他应用的上层",
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                "android.settings.MANAGE_APP_OVERLAY_PERMISSION",
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            true,true,
            hasPermission =  { context ->
                return@PermissionData Settings.canDrawOverlays(context)
            }
        )
    )
    result.add(
        PermissionData(
            "“勿扰”权限",
            //设置代码
            // final NotificationManager mgr = context.getSystemService(NotificationManager.class);
            //        mgr.setNotificationPolicyAccessGranted(pkg, access);
                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
//            "android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS",
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            //isNotificationPolicyAccessGrantedForPackage 判断权限的方法
//                "android.permission.MANAGE_NOTIFICATIONS",
            //android.permission.MANAGE_NOTIFICATIONS
            true,true,
            hasPermission = { context ->
                return@PermissionData isDndPermissionGranted(context)
            }
        )
    )
    result.add(
        PermissionData(
            "管理媒体应用",
            Settings.ACTION_REQUEST_MANAGE_MEDIA,
            Manifest.permission.MANAGE_MEDIA,
            true,true,
            hasPermission = { context ->
                return@PermissionData context.checkPermission(Manifest.permission.MANAGE_MEDIA)
            }
        )
    )
    result.add(
        PermissionData(
            "修改系统设置",
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Manifest.permission.PACKAGE_USAGE_STATS,
            true,
            hasPermission = { context ->
                return@PermissionData Settings.System.canWrite(context)
            }
        )
    )
    result.add(
        PermissionData(
            "设备和应用通知",
            //设置代码
            //mNfl ：NotificationListenerFilter
            //mNm.setListenerFilter->INotificationManager.setListenerFilter
            // public boolean onPreferenceChange(Preference preference, Object newValue) {
            //        if (preference instanceof CheckBoxPreference) {
            //            String packageName = preference.getKey().substring(0, preference.getKey().indexOf("|"));
            //            int uid = Integer.parseInt(preference.getKey().substring(
            //                    preference.getKey().indexOf("|") + 1));
            //            boolean allowlisted = newValue == Boolean.TRUE;
            //            mNlf = mNm.getListenerFilter(mCn, mUserId);
            //            if (allowlisted) {
            //                mNlf.removePackage(new VersionedPackage(packageName, uid));
            //            } else {
            //                mNlf.addPackage(new VersionedPackage(packageName, uid));
            //            }
            //            mNm.setListenerFilter(mCn, mUserId, mNlf);
            //            return true;
            //        }
            //        return false;
            //    }
            Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS,
//                Settings.ACTION_APP_NOTIFICATION_SETTINGS,
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
            true,true,
            hasPermission = { context ->
                return@PermissionData isNotificationListenerEnabled(context)
            }
        )
    )
    result.add(
        PermissionData(
            "画中画",
//                Settings.ACTION_PICTURE_IN_PICTURE_SETTINGS,
            "android.settings.PICTURE_IN_PICTURE_SETTINGS",
            "",
            true,
           hasPermission =  { context ->
                return@PermissionData context.checkPermissionByOps(AppOpsManager.OPSTR_PICTURE_IN_PICTURE)
            }
        )
    )
    result.add(
        PermissionData(
            "付费短信权限",
            //设置代码
            //最终是调用 NetworkPolicyManager.setUidPolicy 方法
            "android.settings.PREMIUM_SMS_SETTINGS",
            "",
            true,
            hasPermission = {context ->
                return@PermissionData false
            }
        )
    )
    result.add(
        PermissionData(
            "不受流量限制",
            //设置代码
            //最终是调用 NetworkPolicyManager.setUidPolicy 方法
            Settings.ACTION_DATA_ROAMING_SETTINGS,
            "",
            true,
            hasPermission =  {context ->
                return@PermissionData false
            }
        )
    )
    result.add(
        PermissionData(
            "安装未知应用",
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Manifest.permission.PACKAGE_USAGE_STATS,
            true,
            hasPermission = { context ->
                return@PermissionData context.packageManager.canRequestPackageInstalls()
            }
        )
    )
    result.add(
        PermissionData(
            "闹钟和提醒",
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            true,
          hasPermission = { context ->
                return@PermissionData context.checkPermissionByOps("android:schedule_exact_alarm")
            },
        )
    )
    result.add(
        PermissionData(
            "使用情况访问",
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Manifest.permission.PACKAGE_USAGE_STATS,
            //Manifest.permission.LOADER_USAGE_STATS,
            true,
            hasPermission = { context ->
                return@PermissionData context.checkPermissionByOps(AppOpsManager.OPSTR_GET_USAGE_STATS)
            }
        )
    )
    result.add(
        PermissionData(
            "VR 助手服务",
            //设置代码
            //将包名设置到Settings.Secure.ENABLED_VR_LISTENERS中存储
            Settings.ACTION_VR_LISTENER_SETTINGS,
            Manifest.permission.BIND_VR_LISTENER_SERVICE,
            true,
            hasPermission =  { context ->
                return@PermissionData isVrListenerEnabled(context)
            }
        )
    )
    result.add(
        PermissionData(
            "WLAN 控制",
//            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "android.settings.action.MANAGE_WRITE_SETTINGS",
            Manifest.permission.CHANGE_WIFI_STATE,
            true,
            hasPermission =  { context ->
                return@PermissionData context.checkPermissionByOps(/*AppOpsManager.OPSTR_CHANGE_WIFI_STATE*/"android:change_wifi_state")
            }
        )
    )
    result.add(
        PermissionData(
            "开启屏幕",
            "android.settings.TURN_SCREEN_ON_SETTINGS",
            Manifest.permission.TURN_SCREEN_ON,
            true,
            hasPermission =   { context ->
                return@PermissionData context.checkPermissionByOps(/*AppOpsManager.OPSTR_TURN_SCREEN_ON*/"android:turn_screen_on")
            }
        )
    )



    result.add(
        PermissionData(
            "全屏通知",
            //设置代码
            //permissionManager: PermissionManager
            //appOpsManager :AppOpsManager
            //packageManager: PackageManager
            // private fun isPermissionGranted(): Boolean {
            //        val attributionSource = AttributionSource.Builder(uid).setPackageName(packageName).build()
            //
            //        val permissionResult =
            //            permissionManager.checkPermissionForPreflight(USE_FULL_SCREEN_INTENT, attributionSource)
            //
            //        return (permissionResult == PermissionManager.PERMISSION_GRANTED)
            //    }
            //
            //    private fun setPermissionGranted(allowed: Boolean) {
            //        val mode = if (allowed) AppOpsManager.MODE_ALLOWED else AppOpsManager.MODE_ERRORED
            //        appOpsManager.setUidMode(OP_USE_FULL_SCREEN_INTENT, uid, mode)
            //        packageManager.updatePermissionFlags(
            //            USE_FULL_SCREEN_INTENT,
            //            packageName,
            //            FLAG_PERMISSION_USER_SET,
            //            FLAG_PERMISSION_USER_SET,
            //            userHandle
            //        )
            //    }
            Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
            Manifest.permission.USE_FULL_SCREEN_INTENT,
            true,
            hasPermission = { context ->
                return@PermissionData context.checkPermission(Manifest.permission.USE_FULL_SCREEN_INTENT)
            }
        )
    )
    result.add(
        PermissionData(
            "打开文件夹列表",
            Intent.ACTION_OPEN_DOCUMENT_TREE,
//                "android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS",
            "",
            //isNotificationPolicyAccessGrantedForPackage 判断权限的方法
//                "android.permission.MANAGE_NOTIFICATIONS",
            //android.permission.MANAGE_NOTIFICATIONS
            false,false,
        ){
            val uri= Uri.parse(Environment.getExternalStorageState()+"/Documents")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent. putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
            context.startActivity(intent)
        }
    )
//    result.add(
//        ItemData(
//            "勿扰权限（列表）",
//            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
////                "android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS",
//            Manifest.permission.ACCESS_NOTIFICATION_POLICY,
//            //isNotificationPolicyAccessGrantedForPackage 判断权限的方法
////                "android.permission.MANAGE_NOTIFICATIONS",
//            //android.permission.MANAGE_NOTIFICATIONS
//            false,
//            { context ->
//                return@ItemData isDndPermissionGranted(context)
//            }
//        )
//    )







    return result
}

private fun isDndPermissionGranted(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
}

private fun isDeviceAdminActive(context: Context): Boolean {
    val deviceAdminComponent = ComponentName(context, MyDeviceAdminReceiver::class.java)
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    return devicePolicyManager.isAdminActive(deviceAdminComponent)
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationListenerAccessGranted(ComponentName(context, NotificationListener::class.java))
}

fun isVrListenerEnabled(context: Context): Boolean {
    return context.isContainPermission("enabled_vr_listeners")
}
fun Context.isContainPermission(key:String): Boolean {
    val pkgName = packageName
    val enabledListeners = Settings.Secure.getString(contentResolver, key)
    return enabledListeners != null && enabledListeners.contains(pkgName)
}

