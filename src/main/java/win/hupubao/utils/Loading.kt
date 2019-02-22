package win.hupubao.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import win.hupubao.views.LoadingFragment

object Loading {

    val loading = LoadingFragment()

    fun show() {
        GlobalScope.launch(Dispatchers.Main) {
            loading.show()
        }
    }

    fun hide() {
        GlobalScope.launch(Dispatchers.Main) {
            loading.hide()
        }
    }
}