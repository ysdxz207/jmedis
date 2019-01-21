package win.hupubao.views

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.stage.StageStyle
import tornadofx.*
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.BorderStrokeStyle



class AlertFragment : Fragment() {

    lateinit var labelContent: Label


    override val root = vbox {

        prefHeight = 54.0
        prefWidth = 168.0

        style {
            backgroundColor = multi(Paint.valueOf("#606266"))
            backgroundRadius = multi(box(26.px))
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