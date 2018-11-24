package win.hupubao

import javafx.stage.Stage
import tornadofx.App
import win.hupubao.views.MainView

class App : App() {
    override val primaryView = MainView::class

    override fun start(stage: Stage) {
        stage.icons += resources.image("/icon/icon.png")
        super.start(stage)
    }
}