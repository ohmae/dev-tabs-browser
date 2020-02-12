/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal fun Intent.getBooleanExtraSafely(key: String, default: Boolean = false): Boolean =
    runCatching { getBooleanExtra(key, default) }.getOrDefault(default)

internal fun Intent.getIntExtraSafely(key: String, default: Int = 0): Int =
    runCatching { getIntExtra(key, default) }.getOrDefault(default)

internal fun Intent.getIntArrayExtraSafely(key: String): IntArray? =
    runCatching { getIntArrayExtra(key) }.getOrNull()

internal fun Intent.getBundleExtraSafely(key: String): Bundle? =
    runCatching { getBundleExtra(key) }.getOrNull()

internal fun <T : Parcelable> Intent.getParcelableExtraSafely(key: String): T? =
    runCatching { getParcelableExtra<T>(key) }.getOrNull()

internal fun <T : Parcelable> Intent.getParcelableArrayListExtraSafely(key: String): ArrayList<T>? =
    runCatching { getParcelableArrayListExtra<T>(key) }.getOrNull()

internal fun Bundle?.getIntSafely(key: String, default: Int = 0): Int {
    this ?: return default
    return runCatching { getInt(key) }.getOrDefault(default)
}

internal fun Bundle?.getStringSafely(key: String, default: String? = null): String? {
    this ?: return default
    return runCatching { getString(key) }.getOrNull() ?: default
}

internal fun Bundle?.getStringSafelyNonNull(key: String, default: String = ""): String {
    this ?: return default
    return runCatching { getString(key) }.getOrNull() ?: default
}

internal fun <T : Parcelable> Bundle?.getParcelableSafely(key: String, default: T? = null): T? {
    this ?: return default
    return runCatching { getParcelable<T>(key) }.getOrNull() ?: default
}
