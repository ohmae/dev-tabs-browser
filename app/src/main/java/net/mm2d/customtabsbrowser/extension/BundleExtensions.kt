/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser.extension

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray

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

internal fun <T : Parcelable> Bundle.getParcelableSafely(key: String, default: T? = null): T? =
    runCatching { getParcelable<T>(key) }.getOrNull() ?: default

internal fun <T : Parcelable> Bundle.getParcelableArrayListSafely(key: String): ArrayList<T>? =
    runCatching { getParcelableArrayList<T>(key) }.getOrNull()

internal fun <T : Parcelable> Bundle.getSparseParcelableArraySafely(key: String?): SparseArray<T>? =
    runCatching { getSparseParcelableArray<T>(key) }.getOrNull()
