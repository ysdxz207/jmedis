package win.hupubao.utils

import kotlinx.coroutines.*
import win.hupubao.views.AlertFragment

object Alert {

    fun show(text: String, time: Long) {
        val alertFragment = AlertFragment()
        alertFragment.show(text)
        GlobalScope.launch(Dispatchers.Main) {
            delay(time)
            alertFragment.hide()
        }
    }
}