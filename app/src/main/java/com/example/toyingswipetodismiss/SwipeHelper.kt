package com.example.toyingswipetodismiss

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.ItemTouchHelper.Callback
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.graphics.RectF
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import android.view.MotionEvent
import android.view.View

internal enum class ButtonsState {
  GONE,
  RIGHT_VISIBLE
}

class SwipeHelper(private val swipeCallback: SwipeCallback) : Callback() {

  private var swipeBack = false

  private var buttonShowedState = ButtonsState.GONE

  private var currentItemViewHolder: RecyclerView.ViewHolder? = null

  private lateinit var buttonInstance: RectF

  private val buttonWidth = 300f

  override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
    return makeMovementFlags(ACTION_STATE_IDLE, LEFT)
  }

  override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
    // callback for drag-n-drop, so we put false to skip this feature
    return false
  }

  override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
    // callback for swipe to dismiss
  }

  override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
    if (swipeBack) {
      swipeBack = buttonShowedState !== ButtonsState.GONE
      return 0
    }
    return super.convertToAbsoluteDirection(flags, layoutDirection)
  }

  override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int,
    isCurrentlyActive: Boolean) {

    if (actionState == ACTION_STATE_SWIPE) {
      if (buttonShowedState != ButtonsState.GONE) {
        val newDX = Math.min(dX, -300f)
        super.onChildDraw(c, recyclerView, viewHolder, newDX, dY, actionState, isCurrentlyActive)
      } else {
        setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
      }
    }

    if (buttonShowedState == ButtonsState.GONE) {
      super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
    currentItemViewHolder = viewHolder
  }

  private fun setTouchListener(c: Canvas, recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int,
    isCurrentlyActive: Boolean) {
    recyclerView.setOnTouchListener(object : View.OnTouchListener {
      override fun onTouch(v: View, event: MotionEvent): Boolean {
        swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
        if (swipeBack) {
          if (dX < -buttonWidth) {
            buttonShowedState = ButtonsState.RIGHT_VISIBLE
          }

          if (buttonShowedState != ButtonsState.GONE) {
            setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            setItemsClickable(recyclerView, false)
          }
        }
        return false
      }
    })
  }

  private fun setTouchDownListener(c: Canvas, recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int,
    isCurrentlyActive: Boolean) {
    recyclerView.setOnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
      }
      false
    }
  }

  private fun setTouchUpListener(c: Canvas, recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int,
    isCurrentlyActive: Boolean) {
    recyclerView.setOnTouchListener { v, event ->
      if (event.action === MotionEvent.ACTION_UP) {
        super@SwipeHelper.onChildDraw(c, recyclerView, viewHolder, 0f, dY, actionState,
          isCurrentlyActive)
        recyclerView.setOnTouchListener { v, event -> false }
        setItemsClickable(recyclerView, true)
        swipeBack = false

        if (swipeCallback != null && buttonInstance != null && buttonInstance.contains(event.x,
            event.y)) {
          swipeCallback.onRightClicked(viewHolder.adapterPosition)
        }
        buttonShowedState = ButtonsState.GONE
        currentItemViewHolder = null
      }
      false
    }
  }

  private fun setItemsClickable(recyclerView: RecyclerView, isClickable: Boolean) {
    for (i in 0 until recyclerView.childCount) {
      recyclerView.getChildAt(i).isClickable = isClickable
    }
  }

  fun onDraw(c: Canvas) {
    currentItemViewHolder?.let {
      drawButtons(c, currentItemViewHolder!!)
    }
  }

  private fun drawButtons(c: Canvas, viewHolder: RecyclerView.ViewHolder) {

    val itemView = viewHolder.itemView
    val p = Paint()

    val rightButton = RectF(itemView.right.toFloat() - viewHolder.itemView.width, itemView.top.toFloat(),
      itemView.right.toFloat(), itemView.bottom.toFloat())
    p.color = Color.RED
    c.drawRect(rightButton, p)
    drawText("DELETE", c, rightButton, p)

    buttonInstance = RectF(itemView.right.toFloat() - 300f, itemView.top.toFloat(),
      itemView.right.toFloat(), itemView.bottom.toFloat())
  }

  private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
    val padding = 20f
    val textSize = 60f
    p.color = Color.WHITE
    p.textSize = textSize

    val textWidth = p.measureText(text)
    c.drawText(text, c.width - buttonWidth - padding, button.centerY() + textSize / 2, p)
  }

}