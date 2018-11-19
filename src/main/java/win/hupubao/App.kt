package win.hupubao

import javafx.stage.Stage
import tornadofx.App
import win.hupubao.views.MainView

object App : App() {
    override val primaryView = MainView::class
}