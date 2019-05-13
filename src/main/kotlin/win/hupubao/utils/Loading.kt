package win.hupubao.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import win.hupubao.views.LoadingFragment

object Loading {

    val loading = LoadingFragment()

    fun show() {
        GlobalScope.launch(Dispatchers.JavaFx) {
            loading.show()
        }
    }

    fun hide() {
        GlobalScope.launch(Dispatchers.JavaFx) {
            loading.hide()
        }
    }
}