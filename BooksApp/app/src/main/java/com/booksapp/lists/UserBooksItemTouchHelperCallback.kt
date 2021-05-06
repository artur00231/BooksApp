package com.booksapp.lists

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.booksapp.R
import java.util.*
import kotlin.math.round

class UserBooksItemTouchHelperCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    private val background = ColorDrawable()
    private val deleteColor = Color.parseColor("#f44336")
    private val addColor = Color.parseColor("#2ef26c")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    private val upIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_keyboard_arrow_up)
    private val downIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_keyboard_arrow_down)

    private var margin = 10 //dp

    fun interface OnSwipeListener : EventListener {
        fun swapped(viewHolder: RecyclerView.ViewHolder, direction : Int)
    }

    private var eventListener : OnSwipeListener? = null
    private var isSwappableFunc: (Int) -> Int = { 2 }

    init {
        val r = context.resources
        margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            margin.toFloat(),
            r.displayMetrics).toInt()
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        eventListener?.swapped(viewHolder, direction)
    }

    override fun getSwipeDirs (recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return when (isSwappableFunc(viewHolder.adapterPosition)) {
            0 -> 0
            1 -> 8
            2 -> 4
            else -> super.getSwipeDirs(recyclerView, viewHolder)
        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val isCanceled = dX == 0f && !isCurrentlyActive

            if (isCanceled) {
                canvas.drawRect(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), clearPaint)
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }

            //Background
            if (dX < 0) {
                background.color = addColor
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top + margin,
                    itemView.right,
                    itemView.bottom - margin
                )
            } else {
                background.color = deleteColor
                background.setBounds(
                    itemView.left,
                    itemView.top + margin,
                    itemView.left + dX.toInt(),
                    itemView.bottom - margin
                )
            }
            background.draw(canvas)

            //Icon
            val iconSize = round(0.6 * itemHeight).toInt();

            val iconTop = itemView.top + (itemHeight - iconSize) / 2
            val iconMargin = (itemHeight - iconSize) / 2
            val iconBottom = iconTop + iconSize

            if (dX < 0) {
                val iconLeft = itemView.right - iconMargin - iconSize
                val iconRight = itemView.right - iconMargin

                downIcon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                downIcon.draw(canvas)
            } else {
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + iconSize

                upIcon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                upIcon.draw(canvas)
            }

        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    fun setOnSwipeListener(listener: OnSwipeListener) {
        eventListener = listener
    }

    /**
     * @function returns 0 - non swappable; 1 - only right; 2 - only left; 3 - both directions
     */
    fun setIsSwappable(function: (Int) -> Int) {
        isSwappableFunc = function
    }
}
