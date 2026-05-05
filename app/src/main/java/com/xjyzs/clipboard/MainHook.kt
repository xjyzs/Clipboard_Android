package com.xjyzs.clipboard

import android.content.ClipData
import android.content.Context
import android.content.Intent
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "android") {
            // 复制后推送给应用
            XposedHelpers.findAndHookMethod(
                "com.android.server.clipboard.ClipboardService",
                lpparam.classLoader,
                "setPrimaryClipInternalLocked",
                ClipData::class.java,  // 剪贴板数据
                Int::class.javaPrimitiveType,  // 调用者UID
                Int::class.javaPrimitiveType,  // deviceId
                String::class.java, // 来源
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val clip = param.args[0] as ClipData?
                        val sourcePkg = param.args[3] as String?
                        if (clip == null || clip.itemCount == 0) return
                        val text = clip.getItemAt(0).text ?: return
                        val copiedString = text.toString()
                        val systemContext =
                            XposedHelpers.callMethod(param.thisObject, "getContext") as Context
                        Thread {
                            try {
                                val intent = Intent("com.xjyzs.clipboard.CLIPBOARD_RECEIVER")
                                intent.putExtra("txt", copiedString)
                                intent.putExtra("sourcePackage", sourcePkg)
                                systemContext.sendBroadcast(intent)
                            } catch (e: Exception) {
                                XposedBridge.log(e)
                            }
                        }.start()
                    }
                }
            )


            // 允许后台读写剪贴板
            val cbServiceClass = XposedHelpers.findClass(
                "com.android.server.clipboard.ClipboardService",
                lpparam.classLoader
            )
            for (method in cbServiceClass.declaredMethods) {
                if (method.name == "clipboardAccessAllowed") {
                    XposedBridge.hookMethod(method, object : XC_MethodHook() {
                        @Throws(Throwable::class)
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            for (arg in param.args) {
                                if (arg is String && "com.xjyzs.clipboard" == arg) {
                                    param.result = true
                                    return
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}