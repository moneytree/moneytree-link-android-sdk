package com.example.myawesomeapp

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

  private val mainActivity
    get() = requireActivity() as MainActivity

  protected fun showMessage(message: String) {
    mainActivity.setDebug(message)
  }

  protected fun showError(message: String) {
    mainActivity.setDebug(message, true)
  }

  protected fun hideKeyboard() {
    WindowCompat.getInsetsController(requireActivity().window, requireView())
      .hide(WindowInsetsCompat.Type.ime())
  }
}
