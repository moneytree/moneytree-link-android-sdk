package com.example.myawesomeapp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class GenericSpinnerAdapter<T : Any>(
  context: Context,
  itemResource: Int = android.R.layout.simple_spinner_item,
  dropDownResource: Int = android.R.layout.simple_spinner_dropdown_item,
  data: List<T>,
  private val itemText: (T?) -> String?,
) : ArrayAdapter<T>(context, itemResource) {

  init {
    addAll(data)
    setDropDownViewResource(dropDownResource)
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val label = super.getView(position, convertView, parent) as TextView
    label.text = itemText(getItem(position))
    return label
  }

  override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
    val label = super.getDropDownView(position, convertView, parent) as TextView
    label.text = itemText(getItem(position))
    return label
  }
}
