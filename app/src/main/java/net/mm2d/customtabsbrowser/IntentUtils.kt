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
internal fun Intent.getBooleanExtraSafely(key: String, default: Boolean = false): Boolean {
    return try {
        getBooleanExtra(key, default)
    } catch (e: Exception) {
        default
    }
}

internal fun Intent.getIntExtraSafely(key: String, default: Int = 0): Int {
    return try {
        getIntExtra(key, default)
    } catch (e: Exception) {
        default
    }
}

internal fun Intent.getIntArrayExtraSafely(key: String): IntArray? {
    return try {
        getIntArrayExtra(key)
    } catch (e: Exception) {
        null
    }
}

internal fun Intent.getBundleExtraSafely(key: String): Bundle? {
    return try {
        getBundleExtra(key)
    } catch (e: Exception) {
        null
    }
}

internal fun <T : Parcelable> Intent.getParcelableExtraSafely(key: String): T? {
    return try {
        getParcelableExtra(key)
    } catch (e: Exception) {
        null
    }
}

internal fun <T : Parcelable> Intent.getParcelableArrayListExtraSafely(key: String): ArrayList<T>? {
    return try {
        getParcelableArrayListExtra(key)
    } catch (e: Exception) {
        null
    }
}

internal fun Bundle?.getIntSafely(key: String, default: Int = 0): Int {
    this ?: return default
    return try {
        getInt(key)
    } catch (e: Exception) {
        default
    }
}

internal fun Bundle?.getStringSafely(key: String, default: String? = null): String? {
    this ?: return default
    return try {
        getString(key) ?: default
    } catch (e: Exception) {
        default
    }
}

internal fun Bundle?.getStringSafelyNonNull(key: String, default: String = ""): String {
    this ?: return default
    return try {
        getString(key) ?: default
    } catch (e: Exception) {
        default
    }
}

internal fun <T : Parcelable> Bundle?.getParcelableSafely(key: String, default: T? = null): T? {
    this ?: return default
    return try {
        getParcelable(key)
    } catch (e: Exception) {
        null
    }
}
