package win.hupubao.listener.impl

import win.hupubao.listener.ConfigEventListener
import win.hupubao.views.MainView

class ConfigEventListenerImpl : ConfigEventListener {

    override fun changed() {
        // Reload redis configuration list.
        MainView.MainViewInstance.INSTANCE
    }
}
