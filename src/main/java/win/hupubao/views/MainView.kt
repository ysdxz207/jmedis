package win.hupubao.views

import com.alibaba.fastjson.JSON
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*
import win.hupubao.beans.RedisConfig
import win.hupubao.utils.ConfigUtils

class MainView : View() {
    override val root : BorderPane by fxml("/views/MainView.fxml")

    val btnCreateOrEdit: Button by fxid()
    val comboConfig: ComboBox<RedisConfig> by fxid()
    val comboChooseDatabase: ComboBox<String> by fxid()
    val comboOptKey: ComboBox<String> by fxid()
    val listViewKeys: ListView<String> by fxid()


    enum class MainViewInstance {
        INSTANCE
    }

    init {
        title = "Jmedis"
        currentStage?.isResizable = false


        loadComboRedisConfig(null)
        setbtnCreateOrEditText()
        comboConfig.onAction = EventHandler {
            setbtnCreateOrEditText()
        }



        val dbList = FXCollections.observableArrayList("DB0", "DB1")
        comboChooseDatabase.items = dbList


        val optList = FXCollections.observableArrayList("Update", "Delete")
        comboOptKey.items = optList


        val keysList = FXCollections.observableArrayList("111", "2222")
        listViewKeys.items = keysList

    }

    private fun setbtnCreateOrEditText() {

        if (comboConfig.selectedItem != null) {

            val redisConfig: RedisConfig = comboConfig.selectedItem as RedisConfig
            if (redisConfig.id == null) {
                btnCreateOrEdit.text = "Create"
            } else {
                btnCreateOrEdit.text = "Edit"
            }
        } else {
            btnCreateOrEdit.text = "Create"
        }
    }

    fun showEditConfigDialog() {
        find<EditConfigFragment>().openWindow(stageStyle = StageStyle.UTILITY, modality = Modality.WINDOW_MODAL, resizable = false)
    }

    fun loadComboRedisConfig(selectedConfig: RedisConfig?) {
        val itemsList = ArrayList<RedisConfig>()

        val list = ConfigUtils.get().redisConfigList
        val newRedisConfig = RedisConfig()
        newRedisConfig.alias = "Select to Create"

        itemsList.add(newRedisConfig)
        itemsList.addAll(list)
        comboConfig.items = FXCollections.observableArrayList(itemsList)

        comboConfig.value = if (selectedConfig == null) newRedisConfig else selectedConfig
    }
}

