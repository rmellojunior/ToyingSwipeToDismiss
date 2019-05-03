package com.example.toyingswipetodismiss

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.recycler_view
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.State
import com.example.toyingswipetodismiss.SwipeHelperV2.UnderlayButton
import java.util.LinkedList
import com.example.toyingswipetodismiss.SwipeHelperV2.UnderlayButtonClickListener



class MainActivity : AppCompatActivity() {

  private lateinit var adapter: MyAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    setupAdapter()
    setupRecyclerView()
  }

  private fun setupAdapter() {
    adapter = MyAdapter(
      arrayListOf(
        "Item1",
        "Item2",
        "Item3",
        "Item4",
        "Item5",
        "Item6"
      )
    )
  }

  private fun setupRecyclerView() {
    recycler_view.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    recycler_view.adapter = adapter

//    val swipeController = SwipeHelper(object : SwipeCallback {
////      override fun onRightClicked(position: Int) {
////        adapter.list.removeAt(position)
////        adapter.notifyItemRemoved(position)
////        adapter.notifyItemRangeChanged(position, adapter.itemCount)
////      }
////    })
////
////    val itemTouchHelper = ItemTouchHelper(swipeController)
////    itemTouchHelper.attachToRecyclerView(recycler_view)
////
////    recycler_view.addItemDecoration(object : RecyclerView.ItemDecoration() {
////      override fun onDraw(c: Canvas, parent: RecyclerView, state: State) {
////        swipeController.onDraw(c)
////      }
////    })

    val swipeHelper = object : SwipeHelperV2(this, recycler_view) {
      override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder,
        underlayButtons: MutableList<UnderlayButton>) {
        underlayButtons.add(SwipeHelperV2.UnderlayButton(
          "Delete",
          0,
          Color.parseColor("#FF3C30"),
          object : SwipeHelperV2.UnderlayButtonClickListener {
            override fun onClick(pos: Int) {
              // TODO: onDelete
            }
          }
        ))
      }
    }
  }
}
