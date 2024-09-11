package com.peihua.permissiondemo

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.vr.VrListenerService
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
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
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var curPosition = -1
    private val mAdapter = GreetingAdapter { view, item, position ->
        val permission = item.permission
        val checkPermission = item.hasPermission
        if (checkPermission != null && checkPermission(this)) {
            showToast("已经授予${permission}权")
            notificationPosition(position)
        } else {
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
                        notificationPosition(position)
                    }
                }
            } else {
                startActivity(item)
            }

        }
    }

    private fun notificationPosition(position: Int) {
        mAdapter.notifyItemChanged(position)

    }

    override fun onResume() {
        super.onResume()
        mAdapter.notifyDataSetChanged()
    }

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
            mAdapter.notifyDataSetChanged()
        }
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
        apiWithAsyncCreated<List<PermissionData>> {
            onRequest {
                val normalData = readLocalData()
                val specialData = buildSpecialPermissionData(this@MainActivity)
                val data = specialData + normalData
                data
            }
            onResponse {
                mAdapter.setItems(it)
            }
            onError {
                showToast("获取数据失败")
            }
        }
    }

    private fun readLocalData(): List<PermissionData> {
        assets.open("media_data.json").use { inputStream ->
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val content = String(buffer, Charset.forName("UTF-8"))
            val typeReference: TypeToken<List<PermissionData>> =
                object : TypeToken<List<PermissionData>>() {
                }
            val data: List<PermissionData> =
                GsonUtils.fromJson(content, typeReference.type)
            return data
        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    class GreetingAdapter(val click: (View, PermissionData, Int) -> Unit) :
        RecyclerView.Adapter<ViewHolder1>() {
        val data = mutableListOf<PermissionData>()
        fun setItems(items: List<PermissionData>) {
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
        fun bind(item: PermissionData, position: Int) {
            binding.apply {
                tvName.text = item.name
                val hasPermission = item.hasPermission?.invoke(tvName.context)
                    ?: tvName.checkPermission(item.permission)
                tvStatus.text = if (hasPermission) "已授权" else "未授权"
            }
        }
    }

    fun startActivity(itemData: PermissionData) {
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
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
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

class MyVrService : VrListenerService() {
    // 其他回调方法...
}

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    // 其他回调方法...
}

class NotificationListener : NotificationListenerService() {
    // 其他回调方法...
}