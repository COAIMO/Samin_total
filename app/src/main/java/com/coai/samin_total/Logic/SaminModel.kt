package com.coai.samin_total.Logic

enum class SaminModel(val byte: UByte) {
    GasDock(0x00.toUByte()),
    GasRoom(0x01.toUByte()),
    WasteLiquor(0x02.toUByte()),
    Oxygen(0x03.toUByte()),
    Steamer(0x04.toUByte()),
    Temp_Hum(0x05.toUByte());
    companion object : EnumCodesMap<SaminModel, UByte> by EnumCodesMap({ it.byte })
}
inline fun <reified E : Enum<E>, K> EnumCodesMap(crossinline getKey: (E) -> K) = object : EnumCodesMap<E, K> {
    override val codesMap = enumValues<E>().associate { getKey(it) to it }
}

interface EnumCodesMap<E : Enum<E>, K> {
    val codesMap: Map<K, E>
}