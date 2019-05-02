package com.example.toyingswipetodismiss

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

    val swipeController = SwipeHelper.setOnRightListener(object : SwipeCallback {
      override fun onRightClicked(position: Int) {
        adapter.list.removeAt(position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position, adapter.itemCount)
      }
    })
  }
}
