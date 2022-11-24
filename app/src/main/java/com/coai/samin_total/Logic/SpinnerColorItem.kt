package com.coai.samin_total.Logic

class SpinnerColorItem {
    private val colorName:String
    private val boxColor:Int
    constructor(colorName: String, boxColor:Int){
        this.colorName = colorName
        this.boxColor = boxColor
    }

    fun getSpinnerColorName():String{
        return colorName.toString()
    }

    fun getColor():Int{
        return boxColor
    }
}