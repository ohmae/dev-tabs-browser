/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSessionToken

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsIntentReader(private val intent: Intent) {
    val shouldShowTitle = intent.getIntExtra(
        CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE,
        CustomTabsIntent.NO_TITLE
    ) == CustomTabsIntent.SHOW_PAGE_TITLE
    val enableUrlBarHiding = intent.getBooleanExtra(CustomTabsIntent.EXTRA_ENABLE_URLBAR_HIDING, false)
    val shouldShowShareMenuItem = intent.getBooleanExtra(CustomTabsIntent.EXTRA_DEFAULT_SHARE_MENU_ITEM, false)
    val toolbarColor = intent.getIntExtra(CustomTabsIntent.EXTRA_TOOLBAR_COLOR, Color.WHITE)
    val secondaryToolbarColor = intent.getIntExtra(CustomTabsIntent.EXTRA_SECONDARY_TOOLBAR_COLOR, toolbarColor)
    val closeIcon: Bitmap? = intent.getParcelableExtra(CustomTabsIntent.EXTRA_CLOSE_BUTTON_ICON)
    val callback: CustomTabsCallback? = if (intent.hasExtra(CustomTabsIntent.EXTRA_SESSION))
        CustomTabsSessionToken.getSessionTokenFromIntent(intent)?.callback else null
    val clientPackageName: String?
    val hasExitAnimation: Boolean
    val enterAnimationRes: Int
    val exitAnimationRes: Int
    val menuParamList: List<MenuParams> =
        intent.getParcelableArrayListExtra<Bundle>(CustomTabsIntent.EXTRA_MENU_ITEMS)?.map {
            MenuParams(
                it.getString(CustomTabsIntent.KEY_MENU_ITEM_TITLE, ""),
                it.getParcelable(CustomTabsIntent.KEY_PENDING_INTENT)
            )
        } ?: emptyList()
    val actionButtonParams: ButtonParams?
    val toolbarButtonParamsList: List<ButtonParams>

    init {
        val animationBundle = intent.getBundleExtra(CustomTabsIntent.EXTRA_EXIT_ANIMATION_BUNDLE)
        clientPackageName = animationBundle?.getString(BUNDLE_PACKAGE_NAME)
        if (clientPackageName != null) {
            hasExitAnimation = true
            enterAnimationRes = animationBundle.getInt(BUNDLE_ENTER_ANIMATION_RESOURCE)
            exitAnimationRes = animationBundle.getInt(BUNDLE_EXIT_ANIMATION_RESOURCE)
        } else {
            hasExitAnimation = false
            enterAnimationRes = 0
            exitAnimationRes = 0
        }
        val actionButton = intent.getBundleExtra(CustomTabsIntent.EXTRA_ACTION_BUTTON_BUNDLE)?.let {
            val icon: Bitmap = it.getParcelable(CustomTabsIntent.KEY_ICON) ?: return@let null
            ButtonParams(
                it.getInt(CustomTabsIntent.KEY_ID, CustomTabsIntent.TOOLBAR_ACTION_BUTTON_ID),
                icon,
                intent.getBooleanExtra(CustomTabsIntent.EXTRA_TINT_ACTION_BUTTON, false),
                it.getString(CustomTabsIntent.KEY_DESCRIPTION, ""),
                it.getParcelable(CustomTabsIntent.KEY_PENDING_INTENT)
            )
        }
        val idSet = mutableSetOf<Int>()
        val toolbarList = intent.getParcelableArrayListExtra<Bundle>(CustomTabsIntent.EXTRA_TOOLBAR_ITEMS)?.mapNotNull {
            val icon: Bitmap = it.getParcelable(CustomTabsIntent.KEY_ICON) ?: return@mapNotNull null
            val id = it.getInt(CustomTabsIntent.KEY_ID, CustomTabsIntent.TOOLBAR_ACTION_BUTTON_ID)
            if (idSet.contains(id)) return@mapNotNull null
            idSet.add(id)
            ButtonParams(
                it.getInt(CustomTabsIntent.KEY_ID, CustomTabsIntent.TOOLBAR_ACTION_BUTTON_ID),
                icon,
                false,
                it.getString(CustomTabsIntent.KEY_DESCRIPTION, ""),
                it.getParcelable(CustomTabsIntent.KEY_PENDING_INTENT)
            )
        } ?: emptyList()
        if (actionButton != null) {
            actionButtonParams = actionButton
            toolbarButtonParamsList = toolbarList
        } else {
            actionButtonParams = toolbarList.find { it.id == CustomTabsIntent.TOOLBAR_ACTION_BUTTON_ID }
            if (actionButtonParams == null) {
                toolbarButtonParamsList = toolbarList
            } else {
                toolbarButtonParamsList = toolbarList.filter { it.id != CustomTabsIntent.TOOLBAR_ACTION_BUTTON_ID }
            }
        }
    }

    class MenuParams(
        val label: String,
        val pendingIntent: PendingIntent?
    )

    class ButtonParams(
        val id: Int,
        val icon: Bitmap?,
        val shouldTint: Boolean,
        val description: String,
        val pendingIntent: PendingIntent?
    )

    companion object {
        private val ANIMATION_BUNDLE_PREFIX =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) "android:activity." else "android:"
        private val BUNDLE_PACKAGE_NAME = ANIMATION_BUNDLE_PREFIX + "packageName"
        private val BUNDLE_ENTER_ANIMATION_RESOURCE = ANIMATION_BUNDLE_PREFIX + "animEnterRes"
        private val BUNDLE_EXIT_ANIMATION_RESOURCE = ANIMATION_BUNDLE_PREFIX + "animExitRes"
    }
}
