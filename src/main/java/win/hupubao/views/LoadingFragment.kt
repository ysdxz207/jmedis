package win.hupubao.views

import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import tornadofx.*


class LoadingFragment : Fragment() {

    override val root = vbox {
        style {
            backgroundColor += Color.TRANSPARENT
        }

        imageview(image = Image(resources.stream("/image/loading.gif"))) {
            style {
                backgroundColor += Color.TRANSPARENT
            }
        }
        style {
            backgroundColor += Color.TRANSPARENT
        }
    }


    init {
        currentStage?.isResizable = false
        currentStage?.x
        currentStage?.y = 100.0
    }

    fun show() {
        this.openModal(stageStyle = StageStyle.TRANSPARENT)
    }

    fun hide() {
        currentStage?.hide()
    }

}