/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.widget.RemoteViews
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsIntent.*
import androidx.browser.customtabs.CustomTabsSessionToken
import androidx.core.util.getOrDefault
import net.mm2d.customtabsbrowser.extension.*

class CustomTabsIntentReader(intent: Intent) {
    private val extras: Bundle = intent.extras ?: Bundle.EMPTY
    val shouldShowTitle: Boolean =
        extras.getIntSafely(EXTRA_TITLE_VISIBILITY_STATE, NO_TITLE) == SHOW_PAGE_TITLE
    val enableUrlBarHiding: Boolean = extras.getBooleanSafely(EXTRA_ENABLE_URLBAR_HIDING)
    val shouldShowShareMenuItem: Boolean =
        extras.getBooleanSafely(EXTRA_DEFAULT_SHARE_MENU_ITEM)
    private val lightColorSchemeParams: ColorSchemeParams
    private val darkColorSchemeParams: ColorSchemeParams

    val colorScheme: Int = extras.getIntSafely(EXTRA_COLOR_SCHEME, COLOR_SCHEME_SYSTEM)
    val closeIcon: Bitmap? = extras.getParcelableSafely(EXTRA_CLOSE_BUTTON_ICON)
    val callback: CustomTabsCallback? = try {
        CustomTabsSessionToken.getSessionTokenFromIntent(intent)?.callback
    } catch (e: Exception) {
        null
    }
    val clientPackageName: String?
    val hasExitAnimation: Boolean
    val enterAnimationRes: Int
    val exitAnimationRes: Int
    val menuParamsList: List<MenuParams> = makeMenuParamsList(extras)
    val actionButtonParams: ButtonParams?
    val toolbarButtonParamsList: List<ButtonParams>
    val remoteViews: RemoteViews? = extras.getParcelableSafely(EXTRA_REMOTEVIEWS)
    val remoteViewsClickableIDs: IntArray? =
        extras.getIntArraySafely(EXTRA_REMOTEVIEWS_VIEW_IDS)
    val remoteViewsPendingIntent: PendingIntent? =
        extras.getParcelableSafely(EXTRA_REMOTEVIEWS_PENDINGINTENT)

    init {
        val animationBundle = extras.getBundleSafely(EXTRA_EXIT_ANIMATION_BUNDLE) ?: Bundle.EMPTY
        clientPackageName = animationBundle.getStringSafely(BUNDLE_PACKAGE_NAME)
        hasExitAnimation = clientPackageName != null
        enterAnimationRes = animationBundle.getIntSafely(BUNDLE_ENTER_ANIMATION_RESOURCE)
        exitAnimationRes = animationBundle.getIntSafely(BUNDLE_EXIT_ANIMATION_RESOURCE)
        val actionButton = makeActionButtonParams(extras)
        val toolbarList = makeToolbarButtonParamsList(extras, actionButton != null)
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

        val colorSchemeParamsArray =
            extras.getSparseParcelableArraySafely<Bundle>(EXTRA_COLOR_SCHEME_PARAMS)
                ?: SparseArray()
        lightColorSchemeParams = ColorSchemeParams.create(
            colorSchemeParamsArray.getOrDefault(COLOR_SCHEME_LIGHT, Bundle.EMPTY),
            ColorSchemeParams.create(extras, ColorSchemeParams.LIGHT)
        )
        darkColorSchemeParams = ColorSchemeParams.create(
            colorSchemeParamsArray.getOrDefault(COLOR_SCHEME_DARK, Bundle.EMPTY),
            ColorSchemeParams.create(extras, ColorSchemeParams.DARK)
        )
    }

    fun getColorSchemeParams(activity: Activity): ColorSchemeParams =
        if (activity.isNightMode()) darkColorSchemeParams else lightColorSchemeParams

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

    class ColorSchemeParams(
        val toolbarColor: Int,
        val secondaryToolbarColor: Int,
        val navigationBarColor: Int
    ) {
        companion object {
            val LIGHT = ColorSchemeParams(
                DEFAULT_TOOLBAR_COLOR_LIGHT,
                DEFAULT_TOOLBAR_COLOR_LIGHT,
                DEFAULT_NAVIGATION_BAR_COLOR
            )
            val DARK = ColorSchemeParams(
                DEFAULT_TOOLBAR_COLOR_DARK,
                DEFAULT_TOOLBAR_COLOR_DARK,
                DEFAULT_NAVIGATION_BAR_COLOR
            )

            fun create(bundle: Bundle, default: ColorSchemeParams): ColorSchemeParams {
                val toolbarColor = bundle.getIntSafely(EXTRA_TOOLBAR_COLOR, default.toolbarColor)
                return ColorSchemeParams(
                    toolbarColor,
                    bundle.getIntOrNull(EXTRA_SECONDARY_TOOLBAR_COLOR)
                        ?: if (bundle.containsKey(EXTRA_TOOLBAR_COLOR)) toolbarColor else default.secondaryToolbarColor,
                    bundle.getIntSafely(EXTRA_NAVIGATION_BAR_COLOR, default.navigationBarColor)
                )
            }
        }
    }

    companion object {
        private val ANIMATION_BUNDLE_PREFIX =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) "android:activity." else "android:"
        private val BUNDLE_PACKAGE_NAME = ANIMATION_BUNDLE_PREFIX + "packageName"
        private val BUNDLE_ENTER_ANIMATION_RESOURCE = ANIMATION_BUNDLE_PREFIX + "animEnterRes"
        private val BUNDLE_EXIT_ANIMATION_RESOURCE = ANIMATION_BUNDLE_PREFIX + "animExitRes"
        private const val DEFAULT_TOOLBAR_COLOR_LIGHT = Color.WHITE
        private const val DEFAULT_TOOLBAR_COLOR_DARK = Color.BLACK
        private const val DEFAULT_NAVIGATION_BAR_COLOR = Color.BLACK

        private fun makeMenuParamsList(extras: Bundle): List<MenuParams> =
            extras.getParcelableArrayListSafely<Bundle>(EXTRA_MENU_ITEMS)
                ?.mapNotNull { makeMenuParams(it) } ?: emptyList()

        private fun makeMenuParams(bundle: Bundle): MenuParams? {
            val title = bundle.getStringSafelyNonNull(KEY_MENU_ITEM_TITLE)
            if (title.isEmpty()) return null
            return MenuParams(title, bundle.getParcelableSafely(KEY_PENDING_INTENT))
        }

        private fun makeActionButtonParams(extras: Bundle): ButtonParams? =
            extras.getBundleSafely(EXTRA_ACTION_BUTTON_BUNDLE)?.let {
                val id = it.getIntSafely(KEY_ID, TOOLBAR_ACTION_BUTTON_ID)
                val shouldTint = extras.getBooleanSafely(EXTRA_TINT_ACTION_BUTTON, false)
                makeButtonParams(id, shouldTint, it)
            }

        private fun makeToolbarButtonParamsList(
            extras: Bundle,
            existActionButton: Boolean
        ): List<ButtonParams> {
            val idSet =
                if (existActionButton) mutableSetOf(TOOLBAR_ACTION_BUTTON_ID) else mutableSetOf()
            return extras.getParcelableArrayListSafely<Bundle>(EXTRA_TOOLBAR_ITEMS)
                ?.mapNotNull {
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
