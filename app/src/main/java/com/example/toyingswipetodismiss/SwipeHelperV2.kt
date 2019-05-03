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
import java.util.LinkedList

abstract class SwipeHelperV2(context: Context, private var recyclerView: RecyclerView) :  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){

  public val BUTTON_WIDTH = 300f
  private lateinit var buttons: LinkedList<UnderlayButton>
  private lateinit var gestureDetector: GestureDetector
  private var swipedPos = -1
  private var swipeThreshold = 0.5f
  private lateinit var buttonsBuffer: HashMap<Int, LinkedList<UnderlayButton>>
  private lateinit var recoverQueue: LinkedList<Int>

  private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
      for (button in buttons) {
        if (button.onClick(e.x, e.y))
          break
      }

      return true
    }
  }

  init {
    this.buttons = LinkedList()
    this.recoverQueue = LinkedList()
    this.gestureDetector = GestureDetector(context, gestureListener)
    this.recyclerView.setOnTouchListener(object : View.OnTouchListener {
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
          if (rect.top < point.y && rect.bottom > point.y)
            gestureDetector.onTouchEvent(e)
          else {
            recoverQueue.add(swipedPos)
            swipedPos = -1
            recoverSwipedItem()
          }
        }
        return false
      }
    })
    this.buttonsBuffer = HashMap()
    attachSwipe()
  }

  override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder): Boolean {
    return false
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    val pos = viewHolder.adapterPosition

    if (swipedPos !== pos)
      recoverQueue.add(swipedPos)

    swipedPos = pos

    if (buttonsBuffer.containsKey(swipedPos))
      buttons = buttonsBuffer.getValue(swipedPos)
    else
      buttons.clear()

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

  private fun attachSwipe() {
    val itemTouchHelper = ItemTouchHelper(this)
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }

  abstract fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder,
    underlayButtons: MutableList<UnderlayButton>)

  class UnderlayButton(private val text: String, private val imageResId: Int,
    private val color: Int, private val clickListener: UnderlayButtonClickListener) {
    private var pos: Int = 0
    private var clickRegion: RectF? = null

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
      p.textSize = 20f

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