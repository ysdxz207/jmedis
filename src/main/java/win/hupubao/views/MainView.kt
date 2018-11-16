package win.hupubao.views

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableArray
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

class MainView : View() {
    override val root : BorderPane by fxml("/views/MainView.fxml")

    val comboConfig: ComboBox<String> by fxid()
    val listViewKeys: ListView<String> by fxid()

    init {
        title = "Jmedis"
        currentStage?.isResizable = false

        val configList = FXCollections.observableArrayList("aa", "bb")
        comboConfig.asyncItems {
            configList
        }

        val keysList = FXCollections.observableArrayList("111", "2222")
        listViewKeys.asyncItems {
            keysList
        }
    }

    fun showCreateConfigDialog() {
        find<EditConfigFragment>().openWindow(stageStyle = StageStyle.UTILITY, modality = Modality.WINDOW_MODAL, resizable = false)
    }

    fun showDeleteConfigConfirm() {
        confirmation("", "Are you sure to delete this config ?") {
            if (it == ButtonType.OK) {
                println("aaa")
            }
        }
    }
}

