package com.example.myawesomeapp

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * @author Moneyteee KK
 */
class KeyValueAdapter(
  context: Context, resource: Int
) : ArrayAdapter<Pair<String, String>>(
  context, resource
) {

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val label = super.getView(position, convertView, parent) as TextView
    label.setTextColor(Color.BLACK)
    label.text = getItem(position).second
    return label
  }

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
    val label = super.getDropDownView(position, convertView, parent) as TextView
    label.setTextColor(Color.BLACK)
    label.text = getItem(position).second
    return label
  }
}
