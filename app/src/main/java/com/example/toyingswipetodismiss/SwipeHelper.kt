package com.example.toyingswipetodismiss

import android.view.MotionEvent
import android.view.GestureDetector
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper.Callback
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.LinkedList

abstract class SwipeHelper(
  private val context: Context,
  private var recyclerView: RecyclerView
) :  Callback() {

  private val BUTTON_WIDTH = 300f  //WIDTH_FOR_YOUR_BUTTONS

  private lateinit var buttons: LinkedList<UnderlayButton>
  private lateinit var recoverQueue: LinkedList<Int>
  private lateinit var buttonsBuffer: HashMap<Int, LinkedList<UnderlayButton>>
  private var swipedPos = -1
  private var swipeThreshold = 0.5f

  private val gestureLister: GestureDetector.SimpleOnGestureListener =
    object : GestureDetector.SimpleOnGestureListener() {
      override fun onDown(e: MotionEvent): Boolean {
        for (button in buttons) {
          if (button.onClick(e.x, e.y)) {
            break
          }
        }
        buttons.clear()
        return true
      }
    }

  init {
    this.buttons = LinkedList()
    this.recoverQueue = LinkedList()
    this.buttonsBuffer = HashMap()

    setOnTouchListener()
    attachSwipe()
  }

  private fun setOnTouchListener() {
    val touchListener = object : View.OnTouchListener {
      override fun onTouch(view: View, e: MotionEvent): Boolean {
        if (swipedPos < 0) {
          return false
        }
        val point = Point(e.rawX.toInt(), e.rawY.toInt())

        val swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos)
        val swipedItem = swipedViewHolder?.itemView
        val rect = Rect()
        swipedItem?.getGlobalVisibleRect(rect)

        if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
          if (rect.top < point.y && rect.bottom > point.y) {
            GestureDetector(context, gestureLister).onTouchEvent(e)
          } else {
            recoverQueue.add(swipedPos)
            swipedPos = -1
            recoverSwipedItem()
          }
        }
        return false
      }
    }

    recyclerView.setOnTouchListener(touchListener)
  }

  private fun attachSwipe() {
    val itemTouchHelper = ItemTouchHelper(this)
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }

  override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
    return makeMovementFlags(0, LEFT)
  }

  // callback for drag-n-drop, so we put false to skip this feature
  override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder): Boolean {
    return false
  }

  // callback for swipe to dismiss
  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    val pos = viewHolder.adapterPosition

    if (swipedPos != pos) {
      recoverQueue.add(swipedPos)
    }

    swipedPos = pos

    if (buttonsBuffer.containsKey(swipedPos)) {
      buttons = buttonsBuffer.getValue(swipedPos)
    } else {
      buttons.clear()
    }

    buttonsBuffer.clear()
    swipeThreshold = 0.5f * buttons.size * BUTTON_WIDTH
    recoverSwipedItem()
  }

  override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
    return swipeThreshold
  }

  override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
    return 0.1f * defaultValue
  }

  override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
    return 5.0f * defaultValue
  }

  override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
    dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
    val pos = viewHolder.adapterPosition
    var translationX = dX
    val itemView = viewHolder.itemView

    if (pos < 0) {
      swipedPos = pos
      return
    }

    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
      //limit to open the row and show buttons
      if (dX < 0) {
        var buffer: LinkedList<UnderlayButton> = LinkedList()

        if (!buttonsBuffer.containsKey(pos)) {
          instantiateUnderlayButton(viewHolder, buffer)
          buttonsBuffer[pos] = buffer
        } else {
          buffer = buttonsBuffer[pos]!!
        }

        translationX = dX * buffer.size * BUTTON_WIDTH / itemView.width
        drawButtons(c, itemView, buffer, pos, translationX)
      }
//      drawButtons(c, itemView, buffer, pos, translationX)
    }

    super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
  }

  @Synchronized
  private fun recoverSwipedItem() {
    while (!recoverQueue.isEmpty()) {
      val pos = recoverQueue.pop()
      if (pos > -1) {
        recyclerView.adapter?.notifyItemChanged(pos)
      }
    }
  }

  private fun drawButtons(c: Canvas, itemView: View, buffer: List<UnderlayButton>, pos: Int, dX: Float) {
    var right = itemView.right

    // -1, to go left
    // dX width that you want for your button(s)
    // buffer.size, number of buttons
    val dButtonWidth = -1 * dX / buffer.size

    for (button in buffer) {
      val left = right - dButtonWidth
      button.onDraw(
        c,
        RectF(
          left,
          itemView.top.toFloat(),
          right.toFloat(),
          itemView.bottom.toFloat()
        ),
        pos
      )

      right = left.toInt()
    }
  }

  abstract fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder,
    underlayButtons: MutableList<UnderlayButton>)

  class UnderlayButton(
    private val text: String,
    private val imageResId: Int,
    private val color: Int,
    private val textSize: Float,
    private val clickListener: UnderlayButtonClickListener
  ) {

    private var pos: Int = 0
    var clickRegion: RectF? = null

    fun onClick(x: Float, y: Float): Boolean {
      if (clickRegion != null && clickRegion!!.contains(x, y)) {
        clickListener.onClick(pos)
        return true
      }
      return false
    }

    fun onDraw(c: Canvas, rect: RectF, pos: Int) {
      val p = Paint()

      // Draw background
      p.color = color
      c.drawRect(rect, p)

      // Draw Text
      p.color = Color.WHITE
      p.textSize = textSize

      val r = Rect()
      val cHeight = rect.height()
      val cWidth = rect.width()
      p.textAlign = Paint.Align.LEFT
      p.getTextBounds(text, 0, text.length, r)
      val x = cWidth / 2f - r.width() / 2f - r.left
      val y = cHeight / 2f + r.height() / 2f - r.bottom
      c.drawText(text, rect.left + x, rect.top + y, p)

      clickRegion = rect
      this.pos = pos
    }
  }

  interface UnderlayButtonClickListener {
    fun onClick(pos: Int)
  }

}