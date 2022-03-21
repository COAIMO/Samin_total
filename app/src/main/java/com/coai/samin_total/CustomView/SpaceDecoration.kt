package com.coai.samin_total.CustomView

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceDecoration(private val h_size: Int, private val v_size:Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.right += h_size
        outRect.top += v_size
        outRect.left += h_size
        outRect.bottom += v_size
        if (parent.getChildAdapterPosition(view) == 0) {
        }
    }
}