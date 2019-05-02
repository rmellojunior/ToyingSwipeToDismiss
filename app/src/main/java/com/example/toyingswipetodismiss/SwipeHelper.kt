package com.example.toyingswipetodismiss

import androidx.recyclerview.widget.ItemTouchHelper.Callback
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class SwipeHelper : Callback() {

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

  companion object {
    fun setOnRightListener(swipeCallback: SwipeCallback) {

    }
  }

}