/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import androidx.core.math.MathUtils

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */

internal fun Int.isDarkColor(): Boolean {
    return getBrightness() < 128
}

internal fun Int.getBrightness(): Int {
    val r = 0xff and (this ushr 16)
    val g = 0xff and (this ushr 8)
    val b = 0xff and this
    return MathUtils.clamp(Math.round(r * 0.299f + g * 0.587f + b * 0.114f), 0, 255)
}
