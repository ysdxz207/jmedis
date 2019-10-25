package win.hupubao.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import win.hupubao.views.LoadingFragment

object Loading {

    val loading = LoadingFragment()
    private var isShowing = false


    fun show() {
        if (isShowing) {
            return
        }
        isShowing = true
        GlobalScope.launch(Dispatchers.JavaFx) {
            loading.show()
        }
    }

    fun hide() {
        if (!isShowing) {
            return
        }
        isShowing = false
        GlobalScope.launch(Dispatchers.JavaFx) {
            loading.hide()
        }
    }
}