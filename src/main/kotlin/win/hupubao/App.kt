package win.hupubao

import com.alibaba.fastjson.parser.ParserConfig
import javafx.stage.Stage
import tornadofx.App
import win.hupubao.views.MainView

class App : App() {
    override val primaryView = MainView::class

    override fun start(stage: Stage) {
        // 开启autotype功能，防止解析json找不到对应的@type类型报错
        ParserConfig.getGlobalInstance().isAutoTypeSupport = true
        stage.icons += resources.image("/icon/icon.png")
        super.start(stage)
    }
}