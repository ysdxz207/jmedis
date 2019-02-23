package win.hupubao.views

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.stage.StageStyle
import tornadofx.*


class AlertFragment : Fragment() {
    private val mainView: MainView by inject()
    private lateinit var labelContent: Label

    override val root = vbox {

        prefHeight = 54.0
        prefWidth = 168.0

        style {
            backgroundColor += Paint.valueOf("#5ad7e8")
            backgroundRadius += box(26.px)
        }

        vboxConstraints {
            alignment = Pos.CENTER
        }

        labelContent = label {
            textFill = c("#FFFFFF")
            font = Font(16.0)
        }
    }

    init {
        currentStage?.isResizable = false
    }

    fun show(text: String) {
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
        this.labelContent.text = text
    }

    fun hide() {
        currentStage?.hide()
    }
}