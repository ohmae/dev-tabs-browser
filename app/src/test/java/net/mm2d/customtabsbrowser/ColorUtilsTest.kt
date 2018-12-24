/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.graphics.Color
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
@RunWith(RobolectricTestRunner::class)
class ColorUtilsTest {
    @Test
    fun getBrightness() {
        p("#CE93D8")
        p("#BA68C8")
        p("#9FA8DA")
        p("#7986CB")
        p("#26A69A")
        p("#009688")
    }

    fun p(string: String) {
        val c = Color.parseColor(string)
        println("$string ${c.getBrightness()}")
    }
}
