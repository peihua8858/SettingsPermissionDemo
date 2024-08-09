package com.peihua.permissiondemo

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.vr.VrListenerService
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fz.common.permissions.requestPermissionsDsl
import com.fz.common.utils.ActivityResultLauncher
import com.fz.common.utils.apiWithAsyncCreated
import com.fz.common.view.utils.getItemView
import com.fz.gson.GsonUtils
import com.google.gson.reflect.TypeToken
import com.peihua.permissiondemo.databinding.ActivityMainBinding
import com.peihua.permissiondemo.databinding.ItemGreetingBinding
import com.peihua.permissiondemo.ui.theme.PermissionDemoTheme
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var curPosition = -1
    private val mAdapter = GreetingAdapter { view, item, position ->
        val permission = item.permission
        val action = item.action
        if (permission.isNotEmpty() && !hasPermission(permission)) {
            requestPermissionsDsl(permission) {
                onDenied {
                    startActivity(item)
                }
                onNeverAskAgain {
                    startActivity(item)
                }
                onGranted {
                    showToast("已授权")
                    refreshPermissionStatus(item, position, true)
                }
            }
        } else {
            startActivity(item)
        }
    }

//    fun hasAccess(context: Context, str: String?): Boolean {
//        return (context.getSystemService<NotificationManager>(NotificationManager::class.java) as NotificationManager).isNotificationPolicyAccessGrantedForPackage(
//            str
//        )
//    }
//
//    fun setAccess(context: Context, str: String?, z: Boolean) {
//        logSpecialPermissionChange(z, str, context)
//        (context.getSystemService<NotificationManager>(NotificationManager::class.java) as NotificationManager).setNotificationPolicyAccessGranted(
//            str,
//            z
//        )
//    }

    protected val intentResultContract = object : ActivityResultContract<Intent, ActivityResult>() {

        override fun createIntent(context: Context, input: Intent): Intent = input

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): ActivityResult = ActivityResult(resultCode, intent)
    }

    fun registerForActivityResult(callback: (ActivityResult) -> Unit): ActivityResultLauncher<Intent> {
        return ActivityResultLauncher(registerForActivityResult(intentResultContract, callback))
    }

    private val launcher = registerForActivityResult {
        if (it.resultCode == Activity.RESULT_OK) {
            val item = mAdapter.data[curPosition]
            val permission = item.permission
            item.status = hasPermission(permission)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun refreshPermissionStatus(item: ItemData, position: Int, status: Boolean) {
        item.status = status
        mAdapter.data[position] = item
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = mAdapter
        }
        requestData()
    }

    private fun requestData() {
        apiWithAsyncCreated<List<ItemData>> {
            onRequest {

                return@onRequest readData()
//                readLocalData()
            }
            onResponse {
                mAdapter.setItems(it)
            }
            onError {
                showToast("获取数据失败")
            }
        }
    }

    private fun readData(): List<ItemData> {
        val result = mutableListOf<ItemData>()
        result.add(
            ItemData(
                "使用情况访问",
                Settings.ACTION_USAGE_ACCESS_SETTINGS,
                Manifest.permission.PACKAGE_USAGE_STATS,
                false
            )
        )
        result.add(
            ItemData(
                "安装未知应用",
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Manifest.permission.PACKAGE_USAGE_STATS,
                false
            )
        )
        result.add(
            ItemData(
                "修改系统设置",
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Manifest.permission.PACKAGE_USAGE_STATS,
                false
            )
        )
        result.add(
            ItemData(
                "显示在其他应用的上层",
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                "android.settings.MANAGE_APP_OVERLAY_PERMISSION",
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                false
            )
        )
        result.add(
            ItemData(
                "管理APP所有文件访问权限",
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                false
            )
        )
        result.add(
            ItemData(
                "管理媒体应用",
                Settings.ACTION_REQUEST_MANAGE_MEDIA,
                Manifest.permission.MANAGE_MEDIA,
                false
            )
        )
        result.add(
            ItemData(
                "闹钟和提醒",
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                false
            )
        )
        result.add(
            ItemData(
                "WLAN 控制",
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Manifest.permission.CHANGE_WIFI_STATE,
                false
            )
        )
        result.add(
            ItemData(
                "画中画",
//                Settings.ACTION_PICTURE_IN_PICTURE_SETTINGS,
                "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                "",
                false
            )
        )


        result.add(
            ItemData(
                "设备管理应用",
                "",
                "",
                false
            ) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(
                        this,
                        MyDeviceAdminReceiver::class.java
                    )
                );
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "需要启用设备管理权限")
                startActivityForResult(intent, 11)
            }
        )
        result.add(
            ItemData(
                "全屏通知",
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Manifest.permission.USE_FULL_SCREEN_INTENT,
                false
            )
        )
        result.add(
            ItemData(
                "开启屏幕",
                "",
                Manifest.permission.WAKE_LOCK,
                false
            )
        )

        result.add(
            ItemData(
                "勿扰权限",
//                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
                "android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS",
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                //isNotificationPolicyAccessGrantedForPackage 判断权限的方法
//                "android.permission.MANAGE_NOTIFICATIONS",
                //android.permission.MANAGE_NOTIFICATIONS
                false
            )
        )

        result.add(
            ItemData(
                "设备和应用通知",
//                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
                Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                false
            )
        )


        result.add(
            ItemData(
                "VR 助手服务",
                Settings.ACTION_VR_LISTENER_SETTINGS,
                Manifest.permission.BIND_VR_LISTENER_SERVICE,
                false
            )
        )
        result.add(
            ItemData(
                "不受流量限制",
                Settings.ACTION_DATA_ROAMING_SETTINGS,
                "",
                false
            )
        )
val manager=getSystemService("") as NotificationManager
        manager.setNotificationListenerAccessGranted()
        return result
    }

    private fun readLocalData(): List<ItemData> {
        assets.open("media_data.json").use { inputStream ->
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val content = String(buffer, Charset.forName("UTF-8"))
            val typeReference: TypeToken<List<ItemData>> =
                object : TypeToken<List<ItemData>>() {
                }
            val data: List<ItemData> =
                GsonUtils.fromJson(content, typeReference.type)
            return data
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    class GreetingAdapter(val click: (View, ItemData, Int) -> Unit) :
        RecyclerView.Adapter<ViewHolder1>() {
        val data = mutableListOf<ItemData>()
        fun setItems(items: List<ItemData>) {
            data.clear()
            data.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder1 {
            return ViewHolder1(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder1, position: Int) {
            holder.bind(data[position], position)
            holder.itemView.setOnClickListener {
                click(holder.itemView, data[position], position)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    class ViewHolder1(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.getItemView(R.layout.item_greeting)) {
        val binding by lazy { ItemGreetingBinding.bind(itemView) }
        fun bind(item: ItemData, position: Int) {
            binding.apply {
                tvName.text = item.name
                binding.tvStatus.text = if (item.status) "已授权" else "未授权"
            }
        }
    }

    class ItemData(
        val name: String,
        val action: String,
        val permission: String,
        var status: Boolean,
        val jumpIntent: (() -> Unit)? = null
    )

    fun startActivity(itemData: ItemData) {
        val callback = itemData.jumpIntent
        if (callback != null) {
            try {
                callback()
            } catch (e: Exception) {
                showToast(e.message ?: "启动失败")
                startAppSettings()
            }
        } else {
            startActivity(itemData.action)
        }
    }

    private fun startAppSettings() {
        val intent =Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    fun startActivity(action: String) {
        val intent = Intent(action)
        intent.data = Uri.parse("package:$packageName")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showToast(e.message ?: "启动失败")
            startAppSettings()
        }
    }
}

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
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, click: (String, String) -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Text(text = "全屏通知", modifier = modifier.clickable {
                click(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Manifest.permission.USE_FULL_SCREEN_INTENT
                )
            })
            Text(text = "", modifier = modifier.clickable {
                click(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Manifest.permission.USE_FULL_SCREEN_INTENT
                )
            })
        }

        Text(text = "画中画", modifier = modifier.clickable {
//            click(Settings.ACTION_PICTURE_IN_PICTURE_SETTINGS)
            click("android.settings.PICTURE_IN_PICTURE_SETTINGS", "")
        })
        Text(text = "勿扰权限", modifier = modifier.clickable {
//            click(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS)
            click(
                "android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS",
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            )
        })
        Text(text = "设备和应用通知", modifier = modifier.clickable {
            click(Settings.ACTION_APP_NOTIFICATION_SETTINGS, "")
        })
        Text(text = "使用情况访问", modifier = modifier.clickable {
            click(Settings.ACTION_USAGE_ACCESS_SETTINGS, "")
        })
        Text(text = "安装未知应用", modifier = modifier.clickable {
            click(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "")
        })
        Text(text = "修改系统设置", modifier = modifier.clickable {
            click(Settings.ACTION_MANAGE_WRITE_SETTINGS, "")
        })
//        Text(text = "显示在其他应用的上层（系统级）", modifier = modifier.clickable {
////            click(Settings.ACTION_MANAGE_APP_OVERLAY_PERMISSION)
//            click("android.settings.MANAGE_APP_OVERLAY_PERMISSION")
//        })
        Text(text = "显示在其他应用的上层（应用级）", modifier = modifier.clickable {
            click(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "")
        })
        Text(text = "管理APP所有文件访问权限", modifier = modifier.clickable {
            click(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, "")
        })
        Text(text = "管理媒体应用", modifier = modifier.clickable {
            click(Settings.ACTION_REQUEST_MANAGE_MEDIA, "")
        })

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PermissionDemoTheme {
        Greeting("Android") { z, c ->

        }
    }
}


class MyVrService : VrListenerService() {
    // 其他回调方法...
}

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    // 其他回调方法...
}