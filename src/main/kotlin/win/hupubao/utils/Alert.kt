package win.hupubao.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import win.hupubao.views.AlertFragment

object Alert {

    fun show(text: String, time: Long) {
        val alertFragment = AlertFragment()
        alertFragment.show(text)
        GlobalScope.launch(Dispatchers.JavaFx) {
            delay(time)
            alertFragment.hide()
        }
    }
}