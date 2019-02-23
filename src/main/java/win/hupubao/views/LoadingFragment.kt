package win.hupubao.views

import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import tornadofx.*


class LoadingFragment : Fragment() {
    private val mainView: MainView by inject()

    override val root = vbox {
        imageview(image = Image(resources.stream("/image/loading.gif")))

        // 背景透明
        style {
            backgroundColor += Color.TRANSPARENT
        }
    }

    /**
     * 背景透明
     */
    override fun onDock() {
        currentStage?.scene?.fill = null
    }

    init {
        currentStage?.isResizable = false
    }

    fun show() {
        this.openModal(stageStyle = StageStyle.TRANSPARENT)

        // 计算位置，使其居中
        val mainX = mainView.currentStage?.x ?: 0.0
        val mainY = mainView.currentStage?.y ?: 0.0
        val mainWidth = mainView.currentStage?.width ?: 0.0
        val mainHeight = mainView.currentStage?.height ?: 0.0
        val width = this.currentStage?.width ?: 0.0
        val height = this.currentStage?.height ?: 0.0

        val x = mainX + (mainWidth.div(2)) - (width.div(2))
        val y = mainY + (mainHeight.div(2)) - (height.div(2))

        this.currentStage?.x = x
        this.currentStage?.y = y
    }

    fun hide() {
        currentStage?.hide()
    }
}