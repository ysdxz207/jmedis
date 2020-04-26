package win.hupubao

import com.alibaba.fastjson.parser.ParserConfig
import javafx.stage.Stage
import tornadofx.App
import win.hupubao.enums.WindowSize
import win.hupubao.views.MainView
import java.awt.GraphicsEnvironment

class App : App() {
    override val primaryView = MainView::class

    override fun start(stage: Stage) {
        // 开启autotype功能，防止解析json找不到对应的@type类型报错
        ParserConfig.getGlobalInstance().isAutoTypeSupport = true
        stage.icons += resources.image("/icon/icon.png")
        super.start(stage)
    }

    companion object {
        val windowSize = resolveWindowSize()

        /**
         * Decide which window size to choose.
         * WindowSize.Normal or WindowSize.Large
         */
        private fun resolveWindowSize(): WindowSize {
            val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
            val screenWidth = gd.displayMode.width
            val screenHeight = gd.displayMode.height

            if (screenWidth > 1440
                    || screenHeight > 900) {
                return WindowSize.Large
            }
            return WindowSize.Normal
        }
    }
}