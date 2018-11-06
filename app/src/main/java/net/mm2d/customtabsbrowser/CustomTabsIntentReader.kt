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
import android.widget.RemoteViews
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsIntent.*
import androidx.browser.customtabs.CustomTabsSessionToken

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomTabsIntentReader(intent: Intent) {
    val shouldShowTitle: Boolean =
        intent.getIntExtraSafely(EXTRA_TITLE_VISIBILITY_STATE, NO_TITLE) == SHOW_PAGE_TITLE
    val enableUrlBarHiding: Boolean = intent.getBooleanExtraSafely(EXTRA_ENABLE_URLBAR_HIDING)
    val shouldShowShareMenuItem: Boolean =
        intent.getBooleanExtraSafely(EXTRA_DEFAULT_SHARE_MENU_ITEM)
    val toolbarColor: Int = intent.getIntExtraSafely(EXTRA_TOOLBAR_COLOR, Color.WHITE)
    val secondaryToolbarColor: Int =
        intent.getIntExtraSafely(EXTRA_SECONDARY_TOOLBAR_COLOR, toolbarColor)
    val closeIcon: Bitmap? = intent.getParcelableExtraSafely(EXTRA_CLOSE_BUTTON_ICON)
    val callback: CustomTabsCallback? = try {
        CustomTabsSessionToken.getSessionTokenFromIntent(intent)?.callback
    } catch (e: Exception) {
        null
    }
    val clientPackageName: String?
    val hasExitAnimation: Boolean
    val enterAnimationRes: Int
    val exitAnimationRes: Int
    val menuParamsList: List<MenuParams> = makeMenuParamsList(intent)
    val actionButtonParams: ButtonParams?
    val toolbarButtonParamsList: List<ButtonParams>
    val remoteViews: RemoteViews? = intent.getParcelableExtraSafely(EXTRA_REMOTEVIEWS)
    val remoteViewsClickableIDs: IntArray? =
        intent.getIntArrayExtraSafely(EXTRA_REMOTEVIEWS_VIEW_IDS)
    val remoteViewsPendingIntent: PendingIntent? =
        intent.getParcelableExtraSafely(EXTRA_REMOTEVIEWS_PENDINGINTENT)

    init {
        val animationBundle = intent.getBundleExtraSafely(EXTRA_EXIT_ANIMATION_BUNDLE)
        clientPackageName = animationBundle.getStringSafely(BUNDLE_PACKAGE_NAME)
        hasExitAnimation = clientPackageName != null
        enterAnimationRes = animationBundle.getIntSafely(BUNDLE_ENTER_ANIMATION_RESOURCE)
        exitAnimationRes = animationBundle.getIntSafely(BUNDLE_EXIT_ANIMATION_RESOURCE)
        val actionButton = makeActionButtonParams(intent)
        val toolbarList = makeToolbarButtonParamsList(intent, actionButton != null)
        if (actionButton != null) {
            actionButtonParams = actionButton
            toolbarButtonParamsList = toolbarList
        } else {
            actionButtonParams = toolbarList.find { it.id == TOOLBAR_ACTION_BUTTON_ID }
            toolbarButtonParamsList = if (actionButtonParams == null) {
                toolbarList
            } else {
                toolbarList.filter { it.id != TOOLBAR_ACTION_BUTTON_ID }
            }
        }
    }

    class MenuParams(
        val title: String,
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

        private fun makeMenuParamsList(intent: Intent): List<MenuParams> {
            return intent.getParcelableArrayListExtraSafely<Bundle>(EXTRA_MENU_ITEMS)
                ?.mapNotNull { makeMenuParams(it) } ?: emptyList()
        }

        private fun makeMenuParams(bundle: Bundle): MenuParams? {
            val title = bundle.getStringSafelyNonNull(KEY_MENU_ITEM_TITLE)
            if (title.isEmpty()) return null
            return MenuParams(title, bundle.getParcelableSafely(KEY_PENDING_INTENT))
        }

        private fun makeActionButtonParams(intent: Intent): ButtonParams? {
            return intent.getBundleExtraSafely(EXTRA_ACTION_BUTTON_BUNDLE)?.let {
                val id = it.getIntSafely(KEY_ID, TOOLBAR_ACTION_BUTTON_ID)
                val shouldTint = intent.getBooleanExtraSafely(EXTRA_TINT_ACTION_BUTTON, false)
                makeButtonParams(id, shouldTint, it)
            }
        }

        private fun makeToolbarButtonParamsList(
            intent: Intent,
            existActionButton: Boolean
        ): List<ButtonParams> {
            val idSet =
                if (existActionButton) mutableSetOf(TOOLBAR_ACTION_BUTTON_ID) else mutableSetOf()
            return intent.getParcelableArrayListExtraSafely<Bundle>(EXTRA_TOOLBAR_ITEMS)?.mapNotNull {
                val id = it.getIntSafely(KEY_ID, TOOLBAR_ACTION_BUTTON_ID)
                if (idSet.contains(id)) return@mapNotNull null
                idSet.add(id)
                makeButtonParams(id, false, it)
            } ?: emptyList()
        }

        private fun makeButtonParams(id: Int, shouldTint: Boolean, bundle: Bundle): ButtonParams? {
            val icon: Bitmap = bundle.getParcelableSafely(KEY_ICON) ?: return null
            return ButtonParams(
                id,
                icon,
                shouldTint,
                bundle.getStringSafelyNonNull(KEY_DESCRIPTION),
                bundle.getParcelableSafely(KEY_PENDING_INTENT)
            )
        }
    }
}
