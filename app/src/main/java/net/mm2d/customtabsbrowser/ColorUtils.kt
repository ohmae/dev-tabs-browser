/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.graphics.Color

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */

internal fun Int.isDarkColor(): Boolean {
    return getBrightness() < 0.5f
}

internal fun Int.getBrightness(): Float {
    val r = Color.red(this) / 255f
    val g = Color.green(this) / 255f
    val b = Color.blue(this) / 255f
    return r * 0.2126f + g * 0.7152f + b * 0.0722f
}
