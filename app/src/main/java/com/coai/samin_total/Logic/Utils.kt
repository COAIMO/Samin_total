package com.coai.samin_total.Logic

import android.util.Range

class Utils {
    companion object {
        /**
         * 숫자 배열 -> Range
         */
        fun ToIntRange(lstvalue : List<Int>, size: Int) : List<Range<Int>>? {
            if (lstvalue.isEmpty()) {
                return null
            }
            val refreshList = ArrayList<Range<Int>>()
            var start: Int = -1
            var end: Int = -1

            for (i in 0..size) {
                if (lstvalue.contains(i)) {
                    if (start == -1) {
                        start = i
                    }
                    if (end < i) {
                        end = i
                    }
                } else {
                    if (start != -1) {
                        refreshList.add(Range(start, end))

                        start = -1
                        end = -1
                    }
                }
            }

            return refreshList
        }
    }
}