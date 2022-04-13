package com.coai.samin_total.Logic

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration() : RecyclerView.ItemDecoration() {
    private var horizontal_space:Int = 0
    private var vertical_space:Int = 0

    constructor(horizontal: Int, vertical: Int) : this() {
        this.horizontal_space = horizontal
        this.vertical_space = vertical
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = horizontal_space
        outRect.right = horizontal_space
        outRect.bottom = vertical_space
        if (parent.getChildLayoutPosition(view) == 0){
            outRect.top = vertical_space
        }else{
            outRect.top = 0
        }
        super.getItemOffsets(outRect, view, parent, state)
    }
}