/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.customtabsbrowser

import android.app.Activity
import android.view.*
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import androidx.annotation.Dimension
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class PopupMenuHelper(private val activity: Activity, toolbarId: Int) {
    private val toolbar = activity.findViewById<Toolbar>(toolbarId)
    private val adapter = ArrayAdapter<MenuItem>(activity, android.R.layout.simple_list_item_1)
    private val window = ListPopupWindow(activity)
    private val density = activity.resources.displayMetrics.density
    private var bySelect = false

    init {
        window.setPromptView(activity.layoutInflater.inflate(R.layout.layout_credit, toolbar, false))
        window.width = Math.round(WIDTH * density)
        window.setDropDownGravity(Gravity.END)
        window.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        window.promptPosition = ListPopupWindow.POSITION_PROMPT_BELOW
        window.setAdapter(adapter)
        window.setOnItemClickListener { _, _, position, _ ->
            activity.onOptionsItemSelected(adapter.getItem(position))
            window.dismiss()
        }
    }

    private fun findOverflowButton(view: View): View {
        if (view is ViewGroup) {
            view.forEach {
                if (it is ActionMenuView) {
                    return it
                }
            }
        }
        return view
    }

    fun onPrepareOptionsMenu(menu: Menu, overflowGroupId: Int) {
        menu.setGroupVisible(overflowGroupId, false)
        adapter.clear()
        menu.forEach {
            if (it.groupId == overflowGroupId) {
                adapter.add(it)
            }
        }
        if (bySelect) {
            bySelect = false
            show()
        }
    }

    private fun show() {
        val anchorView = findOverflowButton(toolbar)
        window.anchorView = anchorView
        window.verticalOffset = Math.round(-anchorView.height + MARGIN * density)
        window.horizontalOffset = Math.round(-MARGIN * density)
        window.show()
    }

    fun onSelectOverflowMenu() {
        bySelect = true
        activity.invalidateOptionsMenu()
    }

    companion object {
        @Dimension(unit = Dimension.DP)
        private const val MARGIN = 5
        @Dimension(unit = Dimension.DP)
        private const val WIDTH = 200
    }
}
