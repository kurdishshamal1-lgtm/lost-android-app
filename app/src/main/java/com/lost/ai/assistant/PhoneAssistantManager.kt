package com.lost.ai.assistant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.lost.ai.service.LostAccessibilityService

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class AppNotInstalled(val appName: String, val packageName: String) : ActionResult()
    data class PermissionRequired(val permissionName: String, val reason: String) : ActionResult()
    data class SensitiveActionRequiresConfirmation(val title: String, val details: String, val onConfirm: () -> Unit) : ActionResult()
    data class Error(val errorMessage: String) : ActionResult()
}

class PhoneAssistantManager(private val context: Context) {

    fun launchApp(appName: String, packageName: String): ActionResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)

        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult.Success("Opening ${appName}...")
        } else {
            ActionResult.AppNotInstalled(appName, packageName)
        }
    }

    fun performSystemAction(action: String): ActionResult {
        return when (action) {
            "HOME" -> {
                if (LostAccessibilityService.performHomeAction()) {
                    ActionResult.Success("Navigated to Home Screen")
                } else {
                    ActionResult.PermissionRequired(
                        "Accessibility Service",
                        "LOST needs Accessibility permission to return to the home screen."
                    )
                }
            }
            "BACK" -> {
                if (LostAccessibilityService.performBackAction()) {
                    ActionResult.Success("Performed Back Action")
                } else {
                    ActionResult.PermissionRequired(
                        "Accessibility Service",
                        "LOST needs Accessibility permission to go back."
                    )
                }
            }
            "RECENTS" -> {
                if (LostAccessibilityService.performRecentsAction()) {
                    ActionResult.Success("Opened Recent Apps")
                } else {
                    ActionResult.PermissionRequired(
                        "Accessibility Service",
                        "LOST needs Accessibility permission to open recent apps."
                    )
                }
            }
            "SETTINGS" -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ActionResult.Success("Opened Android Settings")
            }
            "ACCESSIBILITY_SETTINGS" -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ActionResult.Success("Opened Accessibility Settings")
            }
            else -> ActionResult.Error("Unknown system action: ${action}")
        }
    }
}