package com.example.toyingswipetodismiss

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_item.view.card_context_text

class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

  fun bind(string: String) {
    itemView.card_context_text.text = string
  }

}