package com.coai.samin_total.Logic

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax(private val min: String, private val max: String) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length)).toInt()
            if (input in min.toInt()..max.toInt())
                return null
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        }
        return ""
    }
}