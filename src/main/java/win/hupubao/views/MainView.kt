package win.hupubao.views

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.StageStyle
import redis.clients.jedis.exceptions.JedisDataException
import tornadofx.*
import win.hupubao.beans.RedisConfig
import win.hupubao.beans.RedisDB
import win.hupubao.enums.ValueFormat
import win.hupubao.utils.ConfigUtils
import win.hupubao.utils.RedisUtils
import win.hupubao.utils.StringUtils

class MainView : View() {
    override val root : BorderPane by fxml("/views/MainView.fxml")

    val btnCreateOrEdit: Button by fxid()
    val comboConfig: ComboBox<RedisConfig> by fxid()
    val comboChooseDatabase: ComboBox<RedisDB> by fxid()
    val comboOptKey: ComboBox<String> by fxid()
    val listViewKeys: ListView<String> by fxid()
    val textFieldPattern: TextField by fxid()
    val textFieldKey: TextField by fxid()
    val textAreaValue: TextArea by fxid()
    val comboDataFormat: ComboBox<String> by fxid()
    val textFieldHKey: TextField by fxid()


    init {
        title = "Jmedis"
        currentStage?.isResizable = false


        loadComboRedisConfig(null)
        setbtnCreateOrEditText()
        // on select redis configuration
        comboConfig.onAction = EventHandler {
            // change "create or edit button" text
            setbtnCreateOrEditText()
            // load db list
            loadDbList()
            // reset key/pattern textfield text
            textFieldPattern.text = ""
            // reset key list
            listViewKeys.items = null
        }

        // on choose database
        comboChooseDatabase.onAction = EventHandler {
            // get selected database
            val redisDB = getSelectedDatabase()
            if (redisDB != null) {
                RedisUtils.selectDB(redisDB.index!!)

                // make key/pattern textfield change
                if (StringUtils.isEmpty(textFieldPattern.text)) {
                    textFieldPattern.text = "*"
                }

                // reload keys
                loadKeyListToViewList()
                // reload value
                loadValuetoTextField(textFieldKey.text)

            }
        }

        // on key/pattern textfield text change
        textFieldPattern.textProperty().onChange {
            loadKeyListToViewList()
        }

        // on key/pattern textfield typed in Enter
        textFieldPattern.onKeyReleased = EventHandler {
            if (it.code == KeyCode.ENTER) {
                loadKeyListToViewList()
            }
        }

        // on select key listView
        listViewKeys.onUserSelect(1) {
            textFieldKey.text = it
            loadValuetoTextField(it)
        }

        // on change value format combo
        comboDataFormat.onAction = EventHandler {
            textAreaValue.text = formatValue(textAreaValue.text)
        }

        // on key textfield typed in Enter
        textFieldKey.onKeyReleased = EventHandler {
            if (it.code == KeyCode.ENTER) {
                loadValuetoTextField(textFieldKey.text)
            }
        }

        val optList = FXCollections.observableArrayList("Update", "Delete")
        comboOptKey.items = optList
    }

    private fun loadValuetoTextField(key: String?) {
        if (StringUtils.isEmpty(key)) {
            return
        }
        val text: String? = try {
            RedisUtils[key!!]
        } catch (e: JedisDataException) {
            // hash
            JSON.toJSONString(RedisUtils.hvals(key!!), SerializerFeature.BrowserSecure)
        }

        textAreaValue.text = formatValue(text)
    }

    private fun formatValue(value: String?): String {

        if (value == null) {
            return ""
        }
        return when(getValueFormat()) {
            ValueFormat.Json -> if (StringUtils.isJson(value)) JSON.toJSONString(JSON.parse(value), SerializerFeature.PrettyFormat) else value
            ValueFormat.Text -> if (StringUtils.isJson(value)) JSON.toJSONString(JSON.parse(value)) else value
        }
    }

    private fun getValueFormat(): ValueFormat {
        val valueFormatStr = if (comboDataFormat.selectedItem != null) comboDataFormat.selectedItem as String else ValueFormat.Json.name
        return ValueFormat.valueOf(valueFormatStr)
    }

    private fun loadKeyListToViewList() {
        listViewKeys.items = null

        if (!StringUtils.isEmpty(textFieldPattern.text)
                && getSelectedRedisConfig() != null
                && getSelectedDatabase() != null) {
            // get keys by kes or pattern
            val keysList = FXCollections.observableArrayList(RedisUtils.keys(textFieldPattern.text))
            listViewKeys.items = keysList
        }
    }

    private fun getSelectedDatabase(): RedisDB? {
        if (getSelectedRedisConfig() == null) {
            return null
        }
        return if (comboChooseDatabase.selectedItem != null) comboChooseDatabase.selectedItem as RedisDB else null
    }

    /**
     * load db list to comboChooseDatabase
     */
    private fun loadDbList() {
        comboChooseDatabase.items = null
        val redisConfig: RedisConfig? = getSelectedRedisConfig()

        if (redisConfig != null) {
            RedisUtils.config(redisConfig.host!!, redisConfig.port!!, redisConfig.auth!!)
            val dbList = FXCollections.observableArrayList(RedisUtils.dbList())
            comboChooseDatabase.items = dbList
        }
    }

    private fun getSelectedRedisConfig(): RedisConfig? {
        val config = if (comboConfig.selectedItem != null) comboConfig.selectedItem as RedisConfig else null
        return if (config?.id == null) null else config
    }

    private fun setbtnCreateOrEditText() {

        val redisConfig: RedisConfig? = getSelectedRedisConfig()
        if (redisConfig != null) {
            btnCreateOrEdit.text = "Edit"
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

        comboConfig.value = selectedConfig ?: newRedisConfig
    }

}

