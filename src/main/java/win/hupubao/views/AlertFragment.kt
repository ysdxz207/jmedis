package win.hupubao.views

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.stage.StageStyle
import tornadofx.*

class AlertFragment : Fragment() {

    lateinit var labelContent: Label


    override val root = vbox {

        prefHeight = 60.0
        prefWidth = 200.0

        style {
            backgroundColor = multi(Paint.valueOf("#606266"))
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
        labelContent.text = text
    }

    fun hide() {
        currentStage?.hide()
    }

}