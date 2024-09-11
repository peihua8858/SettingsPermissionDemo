package com.peihua.permissiondemo

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.fz.common.array.isNonEmpty
import java.util.Objects

//fun Context.checkPermission(permission: String): Boolean {
//    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
//}
fun View.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this.context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasPermission(permission: String): Boolean {
    return checkPermission(arrayOf(permission)).isPermissible
}

fun Context.checkPermission(permission: Array<String>): PermissionState {
    val packageInfo = packageManager.getPackageInfo(
        packageName,
        PackageManager.GET_PERMISSIONS
    )
    // Check static permission state (whatever that is declared in package manifest)
    val requestedPermissions = packageInfo.requestedPermissions
    val permissionFlags = packageInfo.requestedPermissionsFlags?: IntArray(0)
    val permissionState = PermissionState(packageInfo, packageName)
    Log.d("PermissionState", "Requested permissions: ${requestedPermissions.contentToString()}")
    Log.d("PermissionState", "Requested permissions: ${permissionFlags.contentToString()}")
    if (requestedPermissions.isNonEmpty()) {
        for ((index, item) in requestedPermissions.withIndex()) {
            if (doesAnyPermissionMatch(item, permission)) {
                permissionState.permissionDeclared = true
                if ((permissionFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    permissionState.staticPermissionGranted = true
                    break
                }
            }
        }
    }
    return permissionState
}

fun Context.checkPermission(permission: String): Boolean {
    val packageName = Objects.requireNonNull(packageName)
    val uid = applicationInfo.uid
    val appOps = getSystemService(AppOpsManager::class.java)
    val opMode =
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.permissionToOp(permission) ?: "",
            uid,
            packageName
        )
    Log.d("PermissionState", "AppOpsManager,permission:$permission, mode: $opMode")
    return when (opMode) {
        AppOpsManager.MODE_DEFAULT -> (PackageManager.PERMISSION_GRANTED
                == checkPermission(permission, Process.myPid(), uid))

        AppOpsManager.MODE_ALLOWED -> true
        AppOpsManager.MODE_ERRORED, AppOpsManager.MODE_IGNORED -> false
        else -> throw IllegalStateException("Unknown AppOpsManager mode $opMode")
    }
}

fun Context.checkPermissionByOps(opsStr: String): Boolean {
    val packageName = Objects.requireNonNull(packageName)
    val uid = applicationInfo.uid

    val appOps = getSystemService(AppOpsManager::class.java)
    val opMode =
        appOps.unsafeCheckOpNoThrow(opsStr, uid, packageName)

    return when (opMode) {
        AppOpsManager.MODE_DEFAULT -> false

        AppOpsManager.MODE_ALLOWED -> true
        AppOpsManager.MODE_ERRORED, AppOpsManager.MODE_IGNORED -> false
        else -> throw IllegalStateException("Unknown AppOpsManager mode $opMode")
    }
}

private fun doesAnyPermissionMatch(permissionToMatch: String, permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (permissionToMatch == permission) {
            return true
        }
    }
    return false
}

class PermissionState(val packInfo: PackageInfo, val packageName: String) {
    var staticPermissionGranted: Boolean = false
    var permissionDeclared: Boolean = false
    var appOpMode: Int = AppOpsManager.MODE_DEFAULT

    val isPermissible: Boolean
        get() {
            // defining the default behavior as permissible as long as the package requested this
            // permission (this means pre-M gets approval during install time; M apps gets approval
            // during runtime).
            if (appOpMode == AppOpsManager.MODE_DEFAULT) {
                return staticPermissionGranted
            }
            return appOpMode == AppOpsManager.MODE_ALLOWED
        }
}