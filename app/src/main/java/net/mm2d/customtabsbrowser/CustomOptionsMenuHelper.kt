/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.app.Activity
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import androidx.annotation.Dimension
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class CustomOptionsMenuHelper(activity: Activity, toolbarId: Int, private val overflowIconId: Int) {
    private val activityReference = WeakReference(activity)
    private val toolbar = activity.findViewById<Toolbar>(toolbarId)
    private val adapter = ArrayAdapter<MenuItem>(activity, android.R.layout.simple_list_item_1)
    private val margin = (MARGIN * activity.resources.displayMetrics.density).roundToInt()
    private val popup = ListPopupWindow(activity).also {
        it.width = (WIDTH * activity.resources.displayMetrics.density).roundToInt()
        it.setDropDownGravity(Gravity.END)
        it.setPromptView(activity.layoutInflater.inflate(R.layout.layout_credit, toolbar, false))
        it.promptPosition = ListPopupWindow.POSITION_PROMPT_BELOW
        it.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        it.setAdapter(adapter)
        it.setOnItemClickListener { _, _, position, _ ->
            activity.onOptionsItemSelected(adapter.getItem(position))
            it.dismiss()
        }
    }
    private var invalidateBySelect = false

    fun onPrepareOptionsMenu(menu: Menu, overflowGroupId: Int): Boolean {
        if (!invalidateBySelect) {
            return true
        }
        invalidateBySelect = false
        adapter.clear()
        menu.forEach {
            if (it.groupId == overflowGroupId) {
                adapter.add(it)
            }
        }
        val anchorView = toolbar.findViewById<View>(overflowIconId) ?: toolbar
        popup.anchorView = anchorView
        popup.verticalOffset = -anchorView.height
        popup.horizontalOffset = -margin
        popup.show()
        return true
    }

    fun onSelectOverflowMenu() {
        invalidateBySelect = true
        activityReference.get()?.invalidateOptionsMenu()
    }

    companion object {
        @Dimension(unit = Dimension.DP)
        private const val MARGIN = 4

        @Dimension(unit = Dimension.DP)
        private const val WIDTH = 200
    }
}
