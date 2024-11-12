package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import java.io.InputStreamReader
import java.util.Vector
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

typealias DotVector = Pair<Float, Float>

class PieChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        // Sizes of main circle
        private const val MAIN_X_LEFT = 100f
        private const val MAIN_Y_TOP = 100f
        private const val MAIN_X_RIGHT = 900f
        private const val MAIN_Y_BOTTOM = 900f

        // Sizes of inner white circle
        private const val INNER_X_LEFT = 300f
        private const val INNER_Y_TOP = 300f
        private const val INNER_X_RIGHT = 700f
        private const val INNER_Y_BOTTOM = 700f

        // Center circle coordinates
        private const val X_CENTER = 500f
        private const val Y_CENTER = 500f

        // Radius
        private const val OUTER_RADIUS = 400f
        private const val INNER_RADIUS = 200f

        // Base vector - from center to right edge of circle - zero angle
        private val BASE_VECTOR = Pair(400f, 0f)

        // Colors for pie bar
        private val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW)
    }

    private val paint = Paint()
    private val whitePaint = Paint().apply { color = Color.WHITE }

    private val rect = RectF()
    private val rectInner = RectF()

    private val products: List<Product>

    init {
        products = Gson().fromJson(
            readFromJson(),
            Array<Product>::class.java
        ).toList()

        val amountToPercent = 360f / products.sumBy { it.amount }

        var _angleStart = 0f
        products.onEach { product ->
            product.apply {
                angleStart = _angleStart
                angle = round(amountToPercent * amount)
                color = colors[id % colors.size]
            }
            _angleStart += product.angle
        }

        Log.d("PRODUCTS", products.toString())
    }

    private fun readFromJson(): String {
        val inputStream = resources.openRawResource(R.raw.payload)
        val reader = InputStreamReader(inputStream)
        return reader.readText().also {
            reader.close()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val vMode = MeasureSpec.getMode(widthMeasureSpec)
        val vSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(vSize, hSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rect.set(MAIN_X_LEFT, MAIN_Y_TOP, MAIN_X_RIGHT, MAIN_Y_BOTTOM)
        products.forEach {
            canvas.drawArc(
                rect,
                it.angleStart,
                it.angle,
                true,
                paint.apply { color = it.color }
            )
        }

        rectInner.set(INNER_X_LEFT, INNER_Y_TOP, INNER_X_RIGHT, INNER_Y_BOTTOM)
        canvas.drawArc(rectInner, 0f, 360f, true, whitePaint)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                val touchPoint = PointF(event.x, event.y)

                val radius = calculateDotRadius(touchPoint)
                val radiusCorrect = checkDotInsideRadius(radius)

                if (!radiusCorrect) return true

                val angle = calculateAngle(
                    calculateVectorsProduct(
                        touchPoint
                    )
                ).let {
                    if (touchPoint.y > X_CENTER) it
                    else 360f - it
                }

                val selectedProduct = defineProduct(angle)

                makeToast(selectedProduct.name + " - " + selectedProduct.category)
            }
        }

        return true
    }

    private fun defineProduct(angle: Float): Product {
        return products.first { angle > it.angleStart && angle < (it.angleStart + it.angle) }
    }

    private fun calculateDotRadius(touchPoint: PointF): Float {
        val deltaX = touchPoint.x - X_CENTER
        val deltaY = touchPoint.y - Y_CENTER
        return sqrt(deltaX.pow(2) + deltaY.pow(2))
    }

    private fun checkDotInsideRadius(radius: Float): Boolean {
        return radius > INNER_RADIUS && radius < OUTER_RADIUS
    }

    private fun calculateAngle(dotProduct: Float): Float {
        return acos(dotProduct) * 180 / Math.PI.toFloat()
    }

    private fun calculateVectorsProduct(touchPoint: PointF): Float {
        // Calculate the vector
        val touchVector = defineDotVector(touchPoint)

        val dotProductTop = touchVector.first * BASE_VECTOR.first + touchVector.second * BASE_VECTOR.second
        val dotProductBottomLeft = sqrt(touchVector.first.pow(2) + touchVector.second.pow(2))
        val dotProductBottomRight = sqrt(BASE_VECTOR.first.pow(2) + BASE_VECTOR.second.pow(2))

        // Vector product is the cos of needed angle
        return dotProductTop / (dotProductBottomLeft * dotProductBottomRight)
    }

    private fun defineDotVector(touchPoint: PointF): DotVector {
        return Pair(touchPoint.x - X_CENTER, touchPoint.y - Y_CENTER)
    }

    private fun makeToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}