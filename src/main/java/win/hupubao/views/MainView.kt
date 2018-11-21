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
import win.hupubao.enums.Operation
import win.hupubao.enums.ValueFormat
import win.hupubao.utils.ConfigUtils
import win.hupubao.utils.RedisUtils
import win.hupubao.utils.StringUtils
import java.lang.Exception

class MainView : View() {
    override val root: BorderPane by fxml("/views/MainView.fxml")

    val btnCreateOrEditRedisConfig: Button by fxid()
    val comboConfig: ComboBox<RedisConfig> by fxid()
    val comboChooseDatabase: ComboBox<RedisDB> by fxid()
    val listViewKeys: ListView<String> by fxid()
    val textFieldPattern: TextField by fxid()
    val textFieldKey: TextField by fxid()
    val textAreaValue: TextArea by fxid()
    val comboDataFormat: ComboBox<String> by fxid()
    val textFieldHKey: TextField by fxid()

    val btnSet: Button by fxid()
    val btnDelete: Button by fxid()

    init {
        title = "Jmedis"
        currentStage?.isResizable = false


        loadComboRedisConfig(null)
        setBtnCreateOrEditText()
        // on select redis configuration
        comboConfig.onAction = EventHandler {
            // reset key/pattern textfield text
            textFieldPattern.text = ""
            // reset key list
            listViewKeys.items = null
            // reset database list
            comboChooseDatabase.items = null
            // check redis configuration
            if (getSelectedRedisConfig() == null) {
                return@EventHandler
            }
            try {
                initRedis()
            } catch (e: Exception) {
                error("", "Redis connection failed,please check your redis configuration.")
                return@EventHandler
            }
            // change "create or edit button" text
            setBtnCreateOrEditText()
            // load db list
            loadDbList()

        }

        // on choose database
        comboChooseDatabase.onAction = EventHandler {
            // get selected database
            val redisDB = getSelectedDatabase()
            if (redisDB != null) {
                RedisUtils.selectDB(redisDB.index!!)

                // get all keys
                if (StringUtils.isEmpty(textFieldPattern.text)) {
                    textFieldPattern.text = "*"
                }

                // reload keys
                loadKeyListToViewList()
                // reload value
                loadValueToTextField(textFieldKey.text)
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
            loadValueToTextField(it)
        }

        // on change value format combo
        comboDataFormat.onAction = EventHandler {
            textAreaValue.text = formatValue(textAreaValue.text)
        }

        // on key textfield typed in Enter
        textFieldKey.onKeyReleased = EventHandler {
            if (it.code == KeyCode.ENTER) {
                loadValueToTextField(textFieldKey.text)
            }
        }

        // on field textfield typed in Enter
        textFieldHKey.onKeyReleased = EventHandler {
            if (it.code == KeyCode.ENTER) {
                loadValueToTextField(textFieldKey.text, textFieldHKey.text)
            }
        }

        // action on set button
        btnSet.onAction = EventHandler {

            if (getSelectedDatabase() == null) {
                return@EventHandler
            }
            try {
                if (StringUtils.isEmpty(textFieldHKey.text)) {
                    RedisUtils[textFieldKey.text] = textAreaValue.text
                } else {
                    RedisUtils.hset(textFieldKey.text, textFieldHKey.text, textAreaValue.text)
                }
                information("", "Success!")
            } catch (e: Exception) {
                error("", "Error:" + e.message)
            }

        }

        // action on delete button
        btnDelete.onAction = EventHandler {
            if (getSelectedDatabase() == null) {
                return@EventHandler
            }

            confirmation("", "Are you sure to delete this item ?") {
                if (it == ButtonType.OK) {

                    try {
                        if (!StringUtils.isEmpty(textFieldHKey.text)) {
                            RedisUtils.hdel(textFieldKey.text, textFieldHKey.text)
                        } else {
                            RedisUtils.delete(textFieldKey.text)
                        }
                        information("", "Success!")
                    } catch (e: Exception) {
                        error("", "Error:" + e.message)
                    }
                }
            }

        }
    }

    private fun initRedis() {

        val redisConfig: RedisConfig? = getSelectedRedisConfig()

        if (redisConfig != null) {
            RedisUtils.config(redisConfig.host!!, redisConfig.port!!, redisConfig.auth!!)
            RedisUtils.testConnection()
        }
    }

    /**
     * load value to textfield by get
     */
    private fun loadValueToTextField(key: String) {
        if (StringUtils.isEmpty(key)) {
            return
        }
        val text: String? = try {
            RedisUtils[key]
        } catch (e: JedisDataException) {
            // hash
            JSON.toJSONString(RedisUtils.hvals(key), SerializerFeature.BrowserSecure)
        }

        textAreaValue.text = formatValue(text)
    }

    /**
     * load value to textfield by hget
     */
    private fun loadValueToTextField(key: String, field: String) {
        if (StringUtils.isEmpty(key)) {
            return
        }

        if (StringUtils.isEmpty(field)) {
            return loadValueToTextField(key)
        }
        val text: String? = RedisUtils.hget(key, field)
        textAreaValue.text = formatValue(text)
    }

    private fun formatValue(value: String?): String {

        if (value == null) {
            return ""
        }
        return when (getValueFormat()) {
            ValueFormat.Json -> if (StringUtils.isJson(value)) JSON.toJSONString(JSON.parse(value), SerializerFeature.PrettyFormat) else value
            ValueFormat.Text -> if (StringUtils.isJson(value)) JSON.toJSONString(JSON.parse(value)) else value
        }
    }

    /**
     * get selected value format enum
     */
    private fun getValueFormat(): ValueFormat {
        val valueFormatStr = if (comboDataFormat.selectedItem != null) comboDataFormat.selectedItem as String else ValueFormat.Json.name
        return ValueFormat.valueOf(valueFormatStr)
    }

    /**
     * get keys by pattern/key and render to ViewList
     */
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
        val dbList = FXCollections.observableArrayList(RedisUtils.dbList())
        comboChooseDatabase.items = dbList
    }

    private fun getSelectedRedisConfig(): RedisConfig? {
        val config = if (comboConfig.selectedItem != null) comboConfig.selectedItem as RedisConfig else null
        return if (config?.id == null) null else config
    }

    private fun setBtnCreateOrEditText() {

        val redisConfig: RedisConfig? = getSelectedRedisConfig()
        if (redisConfig != null) {
            btnCreateOrEditRedisConfig.text = "Edit"
        } else {
            btnCreateOrEditRedisConfig.text = "Create"
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

