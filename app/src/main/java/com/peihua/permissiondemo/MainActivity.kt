package com.peihua.permissiondemo

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.peihua.permissiondemo.ui.theme.PermissionDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PermissionDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier
                            .padding(innerPadding)
                            .clickable {
                                //ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS
                                //ACTION_MANAGE_APPLICATIONS_SETTINGS
                                //ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION 管理所有文件访问权限
                                //ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION 管理APP所有文件访问权限
                                //Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS
                                //ACTION_MANAGE_OVERLAY_PERMISSION 管理显示在其他应用的上层
                                //ACTION_MANAGE_APP_OVERLAY_PERMISSION 管理APP显示在其他应用的上层
                                //ACTION_MANAGE_WRITE_SETTINGS  管理修改系统设置的权限
                                //ACTION_APPLICATION_DETAILS_SETTINGS
                                //ACTION_ZEN_MODE_PRIORITY_SETTINGS
                                //ACTION_BATTERY_SAVER_SETTINGS
                                //ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                                //ACTION_NOTIFICATION_SETTINGS
                                //ACTION_APP_NOTIFICATION_SETTINGS
                                //ACTION_CHANNEL_NOTIFICATION_SETTINGS
                                //ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS
                                //ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT 显示屏幕，用于控制应用是否可以发送全屏 Intent。
                                //Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES//管理安装未知应用的权限
                                //Settings.ACTION_USAGE_ACCESS_SETTINGS //管理使用情况访问的权限
                                //Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                //Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS // 忽略电量优化设置的权限
                                //Settings.ACTION_VR_LISTENER_SETTINGS
                                //ACTION_PICTURE_IN_PICTURE_SETTINGS //画中画设置
                                //Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS//管理设备和应用通知权限
                                //android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS 管理勿扰权限
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//                                val intent =Intent("android.settings.MMS_MESSAGE_SETTING")

                                intent.data = Uri.parse("package:$packageName")
                                try {
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Toast
                                        .makeText(this, e.message, Toast.LENGTH_LONG)
                                        .show()
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PermissionDemoTheme {
        Greeting("Android")
    }
}