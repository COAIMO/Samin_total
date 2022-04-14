package com.coai.samin_total.Logic

import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration() : RecyclerView.ItemDecoration() {
    private var top_space: Int = 0
    private var right_space: Int = 0
    private var bottom_space: Int = 0
    private var left_space: Int = 0

    constructor(top: Int, right: Int, bottom: Int, left: Int) : this() {
        this.top_space = top
        this.right_space = right
        this.bottom_space = bottom
        this.left_space = left
    }
    constructor(space:Int) : this() {
        this.top_space = space
        this.right_space = space
        this.bottom_space = space
        this.left_space = space
    }

    fun changeSpace(top: Int, right: Int, bottom: Int, left: Int) {
        this.top_space = top
        this.right_space = right
        this.bottom_space = bottom
        this.left_space = left
    }
    fun changeAllSpace(space:Int) {
        this.top_space = space
        this.right_space = space
        this.bottom_space = space
        this.left_space = space
    }
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = top_space
        outRect.right = right_space
        outRect.bottom = bottom_space
        outRect.left = left_space
//        if (parent.getChildLayoutPosition(view) == 0){
//            outRect.top = vertical_space
//        }else{
//            outRect.top = 0
//        }
//        super.getItemOffsets(outRect, view, parent, state)
    }
}