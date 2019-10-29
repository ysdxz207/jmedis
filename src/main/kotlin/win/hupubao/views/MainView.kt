package win.hupubao.views

import com.alibaba.fastjson.JSON
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.StageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*
import win.hupubao.beans.RedisConfig
import win.hupubao.beans.RedisDB
import win.hupubao.beans.RedisValue
import win.hupubao.enums.FormatType
import win.hupubao.utils.*
import win.hupubao.utils.Alert
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException


class MainView : View("Jmedis") {


    lateinit var comboConfig: ComboBox<RedisConfig>
    lateinit var btnCreateOrEditRedisConfig: Button
    lateinit var comboChooseDatabase: ComboBox<RedisDB>
    lateinit var btnSet: Button
    lateinit var btnDelete: Button
    lateinit var textFieldPattern: TextField
    lateinit var listViewKeys: ListView<String>
    lateinit var textFieldKey: TextField
    lateinit var comboType: ComboBox<String>
    lateinit var textFieldExpire: TextField
    lateinit var textFieldHKey: TextField
    lateinit var comboDataFormat: ComboBox<FormatType>
    lateinit var textAreaValue: TextArea
    lateinit var tableViewValueList: TableView<RedisValue>

    val hvalueList: MutableList<RedisValue> = emptyList<RedisValue>().toMutableList()
    var redisValueText: String? = ""

    override val root = vbox {
        prefWidth = 960.0
        prefHeight = 580.0

        paddingLeft = 20
        paddingRight = 20
        paddingTop = 10



        hbox {
            spacing = 6.0

            comboConfig = combobox {

            }


            btnCreateOrEditRedisConfig = button("Create or Edit") {
                addClass("btn-success")
                action {
                    showEditConfigDialog()
                }
            }


            comboChooseDatabase = combobox {
                promptText = "Choose Database"
            }


            region {
                hgrow = Priority.ALWAYS
                opaqueInsets
            }

            btnSet = button("Set") {

                addClass("btn-info")
            }

            btnDelete = button("Delete") {
            }
        }

        hbox {
            vbox {
                paddingTop = 10.0
                vbox {
                    textFieldPattern = textfield {
                        promptText = "key/pattern"

                        addClass("textfiled-pattern")
                    }
                }

                vbox {
                    paddingTop = 4.0
                    listViewKeys = listview {
                        minHeight = 444.0 + 32.0 + 4.0
                        // on select key listView
                        onUserSelect(1) {
                            textFieldKey.text = it
                            getRedisValue()
                        }
                    }
                }
            }

            vbox {
                paddingLeft = 8.0
                paddingTop = 10.0

                vbox {
                    hbox {
                        spacing = 4.0
                        hgrow = Priority.ALWAYS
                        textFieldKey = textfield {
                            promptText = "key"
                            prefWidth = 10000.0
                        }

                        label("Type:")
                        comboType = combobox {
                            items = observableList("none", "string", "list", "set", "zset", "hash")

                            onAction = EventHandler {
                                textFieldHKey.isDisable = comboType.value != "hash"
                            }
                        }

                        textFieldExpire = textfield {
                            promptText = "TTL"
                            prefWidth = 180.0

                            tooltip {
                                text = "TTL"
                            }

                            text = "-1"
                        }
                    }
                }

                vbox {
                    paddingTop = 4.0

                    hbox {
                        spacing = 4.0
                        hgrow = Priority.ALWAYS

                        textFieldHKey = textfield {
                            promptText = "field"
                            prefWidth = 10000.0

                            isDisable = true
                        }

                        comboDataFormat = combobox {
                            value = FormatType.Json
                            addClass("combo-format")

                            tooltip("Data format.")

                            items = FXCollections.observableList(FormatType.values().asList())
                        }
                    }
                }

                vbox {
                    paddingTop = 4.0

                    drawer(side = Side.RIGHT, multiselect = false) {
                        item(title = "Text", expanded = true) {
                            textAreaValue = textarea {
                                minHeight = 444.0
                                minWidth = 610.0
                                prefWidth = 610.0

                                addClass("textarea-value")
                            }

                        }

                        item("List") {
                            tableViewValueList = tableview {
                                minHeight = 444.0
                                minWidth = 610.0
                                prefWidth = 610.0

                                column("field", RedisValue::key).pctWidth(40.0)
                                column("value", RedisValue::value).pctWidth(60.0)
                                columnResizePolicy = SmartResize.POLICY

                                onUserSelect {
                                    find<HvalueFragment>().openWindow(stageStyle = StageStyle.UTILITY, modality = Modality.WINDOW_MODAL, resizable = false)
                                }
                            }

                        }

                        item(title = "About", expanded = false) {

                            hyperlink(text = "Github", graphic = FontAwesomeIconView(FontAwesomeIcon.GITHUB).apply { glyphSize = 20 }).action {
                                if (Desktop.isDesktopSupported()) {
                                    try {
                                        Desktop.getDesktop().browse(URI("https://github.com/ysdxz207/jmedis"))
                                    } catch (e1: IOException) {
                                        e1.printStackTrace()
                                    } catch (e1: URISyntaxException) {
                                        e1.printStackTrace()
                                    }

                                }
                            }

                        }

                    }
                }
            }


        }
    }


    init {
        currentStage?.isResizable = false

        importStylesheet("/css/style.css")


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
            // change "create or edit button" text
            setBtnCreateOrEditText()
            // check redis configuration
            if (getSelectedRedisConfig() == null) {
                return@EventHandler
            }


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
//                    textFieldPattern.text = "*"
                }

                // reload keys
                loadKeyListToViewList()
                // reload value
                getRedisValue()
            }
        }
        // action on set button
        btnSet.onAction = EventHandler {

            if (getSelectedDatabase() == null) {
                return@EventHandler
            }


            val confirmationText = "Set confirmation."

            var type = RedisUtils.type(textFieldKey.text)

            if (type == "none") {
                type = comboType.value
            }


            confirmation("", confirmationText) { confirmSet ->
                if (confirmSet == ButtonType.OK) {
                    try {

                        val expire: Int
                        try {
                            expire = Integer.valueOf(textFieldExpire.text)
                        } catch (e: NumberFormatException) {
                            throw Exception("TTL should be a number.")
                        }

                        var success = false


                        when (type) {
                            "string" -> {
                                RedisUtils[textFieldKey.text] = textAreaValue.text
                                success = true
                            }

                            "hash" -> {
                                RedisUtils.hset(textFieldKey.text, textFieldHKey.text, textAreaValue.text)
                                success = true
                            }

                            else -> {
                                error("", "Do not support \"$type\" type now.")
                            }
                        }

                        if (success) {
                            if (expire != -1) {
                                RedisUtils.expire(textFieldKey.text, expire)
                            }

                            Alert.show("Success!", 1200L)
                        }
                    } catch (e: Exception) {
                        error("", "Error:" + e.message)
                    }
                }
            }

        }

        // on key/pattern textfield text change
        textFieldPattern.textProperty().onChange {
            //            loadKeyListToViewList()
        }

        // on key/pattern textfield typed in Enter
        textFieldPattern.onKeyReleased = EventHandler {
            if (it.code == KeyCode.ENTER) {
                loadKeyListToViewList()
            }
        }


        // on change value format combo
        comboDataFormat.onAction = EventHandler {
            getRedisValue()
        }

        // on key textfield typed in Enter
        textFieldKey.onKeyReleased = EventHandler {
            if (it.code == KeyCode.ENTER) {
                getRedisValue()
            }
        }

        // on field textfield typed in Enter
        textFieldHKey.onKeyReleased = EventHandler {
            if (it.code == KeyCode.ENTER) {
                getRedisValue()
            }
        }

        // action on delete button
        btnDelete.onAction = EventHandler {
            if (getSelectedDatabase() == null) {
                return@EventHandler
            }

            confirmation("", "Are you sure to delete this item ?") { confirmDel ->
                if (confirmDel == ButtonType.OK) {

                    try {
                        if (!StringUtils.isEmpty(textFieldHKey.text)) {
                            RedisUtils.hdel(textFieldKey.text, textFieldHKey.text)
                        } else {
                            RedisUtils.delete(textFieldKey.text)
                        }
                        Alert.show("Success!", 1200L)
                    } catch (e: Exception) {
                        error("", "Error:" + e.message)
                    }
                }
            }

        }
    }

    private fun checkAndConfigRedis(): Boolean {

        val redisConfig: RedisConfig? = getSelectedRedisConfig()

        if (redisConfig != null) {
            RedisUtils.config(redisConfig.host!!, redisConfig.port!!, redisConfig.auth!!)
            return try {
                RedisUtils.testConnection()
                true
            } catch (e: Exception) {
                false
            }
        }

        return false
    }

    /**
     * load value to textfield by get
     */
    fun getRedisValue() {
        Loading.show()
        val key = textFieldKey.text
        if (StringUtils.isEmpty(key)) {
            Loading.hide()
            return
        }


        // get expire
        Platform.runLater {
            textFieldExpire.text = RedisUtils.ttl(key).toString()
            val type = RedisUtils.type(key)
            comboType.selectionModel.select(type)

            // set hkey enabled
            textFieldHKey.isDisable = false


            if (!checkAndConfigRedis()) {
                GlobalScope.launch(Dispatchers.Main) {
                    root.requestFocus()
                    error("", "Redis connection failed,please check your redis configuration.")
                }
                Loading.hide()
                return@runLater
            }

            hvalueList.clear()

            var isHash = false

            val value: Any? = when (type) {
                "string" -> {
                    RedisUtils[key]
                }
                "hash" -> {
                    isHash = true
                    if (StringUtils.isEmpty(textFieldHKey.text)) {

                        val map = RedisUtils.hgetAll(key)
                        if (!map.isEmpty()) {
                            // set tablevalue value list
                            map.forEach {
                                val hvalue = RedisValue()
                                hvalue.key = it.key
                                hvalue.value = it.value
                                hvalueList.add(hvalue)
                            }
                            JSON.toJSONString(map)
                        } else {
                            ""
                        }
                    } else {
                        RedisUtils.hget(key, textFieldHKey.text)
                    }
                }
                "list" -> {
                    RedisUtils.getList(key)
                }
                "set" -> {
                    RedisUtils.smembers(key)
                }
                "zset" -> {
                    RedisUtils.getZSet(key)
                }

                "none" -> {
                    ""
                }
                else -> {
                    ""
                }
            }


            tableViewValueList.asyncItems {
                FXCollections.observableList(hvalueList)
            }
            redisValueText = StringUtils.formatJson(value, isHash, getFormatType())
            textAreaValue.text = redisValueText

            if (!isHash) {
                // set hkey disabled
                textFieldHKey.isDisable = true
            }


            Loading.hide()
        }
    }

    /**
     * load value to textfield by hget
     */
    /**
     * get selected value format enum
     */
    private fun getFormatType(): FormatType {
        return if (comboDataFormat.selectedItem != null) comboDataFormat.selectedItem as FormatType else FormatType.Json
    }

    /**
     * get keys by pattern/key and render to ViewList
     */
    private fun loadKeyListToViewList() {
        if (!StringUtils.isEmpty(textFieldPattern.text)
                && getSelectedRedisConfig() != null
                && getSelectedDatabase() != null) {
            Loading.show()
            listViewKeys.asyncItems {

                val keys = RedisUtils.keys(textFieldPattern.text)
                // get keys by key or pattern
                val list = FXCollections.observableArrayList(keys)
                Loading.hide()
                list
            }
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

        runAsync {
            comboChooseDatabase.isDisable = true
            if (!checkAndConfigRedis()) {
                kotlin.error("")
            }
            val dbList = FXCollections.observableArrayList(RedisUtils.dbList())
            comboChooseDatabase.items = dbList
        } fail {
            error("", "Redis connection failed,please check your redis configuration.")
        } finally {
            comboChooseDatabase.isDisable = false
        }
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

    private fun showEditConfigDialog() {
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

