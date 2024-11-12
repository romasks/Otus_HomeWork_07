package otus.homework.customview

import android.graphics.Color

data class Product(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long,
) {
    var angleStart: Float = 0f
    var angle: Float = 0f
    var color: Int = Color.BLACK

    override fun toString(): String {
        return "Product: $name, angleStart: $angleStart, angleEnd: ${angleStart + angle}"
    }
}
