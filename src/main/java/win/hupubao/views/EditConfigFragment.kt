package win.hupubao.views

import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import tornadofx.Fragment

class EditConfigFragment: Fragment() {
    override val root : BorderPane by fxml("/views/ConfigDialog.fxml")

    val alias: TextField by fxid()
    val host: TextField by fxid()
    val port: TextField by fxid()
    val auth: PasswordField by fxid()

    init {
        currentStage?.isResizable = false
    }

    fun createNewConfig() {

    }
}