package com.example.toyingswipetodismiss

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.recycler_view

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

    val swipeHelper = object : SwipeHelper(this, recycler_view) {
      override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder, underlayButtons: MutableList<UnderlayButton>) {
        underlayButtons.add(
          SwipeHelper.UnderlayButton(
            "Delete",
            0,
            Color.parseColor("#FF3C30"),
            20f,
            object : SwipeHelper.UnderlayButtonClickListener {
              override fun onClick(pos: Int) {
                adapter.list.removeAt(pos)
                adapter.notifyItemRemoved(pos)
                adapter.notifyItemRangeChanged(pos, adapter.itemCount)
              }
            }
          )
        )
      }
    }
  }
}
