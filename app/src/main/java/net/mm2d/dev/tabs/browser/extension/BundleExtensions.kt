/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dev.tabs.browser.extension

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.core.os.BundleCompat

internal fun Bundle.getBooleanSafely(key: String, default: Boolean = false): Boolean =
    runCatching { getBoolean(key, default) }.getOrDefault(default)

internal fun Bundle.getIntSafely(key: String, default: Int = 0): Int =
    runCatching { getInt(key, default) }.getOrDefault(default)

internal fun Bundle.getIntOrNull(key: String): Int? =
    runCatching { if (containsKey(key)) getInt(key) else null }.getOrNull()

internal fun Bundle.getIntArraySafely(key: String): IntArray? =
    runCatching { getIntArray(key) }.getOrNull()

internal fun Bundle.getStringSafely(key: String, default: String? = null): String? =
    runCatching { getString(key) }.getOrNull() ?: default

internal fun Bundle.getStringSafelyNonNull(key: String, default: String = ""): String =
    runCatching { getString(key) }.getOrNull() ?: default

internal fun Bundle.getBundleSafely(key: String): Bundle? =
    runCatching { getBundle(key) }.getOrNull()

internal inline fun <reified T : Parcelable> Bundle.getParcelableSafely(
    key: String,
    default: T? = null,
): T? =
    runCatching { BundleCompat.getParcelable(this, key, T::class.java) }.getOrNull() ?: default

internal inline fun <reified T : Parcelable> Bundle.getParcelableArrayListSafely(key: String): ArrayList<T>? =
    runCatching { BundleCompat.getParcelableArrayList(this, key, T::class.java) }.getOrNull()

internal inline fun <reified T : Parcelable> Bundle.getSparseParcelableArraySafely(key: String?): SparseArray<T>? =
    runCatching { BundleCompat.getSparseParcelableArray(this, key, T::class.java) }.getOrNull()
