package win.hupubao.views

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.Priority
import tornadofx.*
import win.hupubao.beans.RedisValue
import win.hupubao.enums.FormatType
import win.hupubao.utils.RedisUtils
import win.hupubao.utils.StringUtils

class HvalueFragment : Fragment() {
    private val mainView: MainView by inject()

    lateinit var textfieldHkey: TextField
    lateinit var textareaHvalue: TextArea
    lateinit var comboboxFormat: ComboBox<FormatType>
    val hvalue = if (mainView.tableViewValueList.selectedItem != null) mainView.tableViewValueList.selectedItem as RedisValue else null

    override val root = vbox {
        paddingAll = 10.0
        prefWidth = 720.0
        prefHeight = 420.0

        vbox {

            hbox {
                spacing = 4.0
                hgrow = Priority.ALWAYS

                textfieldHkey = textfield {
                    prefWidth = 10000.0
                    if (hvalue != null) {
                        text = hvalue.key
                    }
                }

                comboboxFormat = combobox {
                    value = FormatType.Json
                    addClass("combo-format")

                    tooltip("Data format.")

                    items = FXCollections.observableList(FormatType.values().asList().filter {
                        it != FormatType.JsonPlusList
                    })
                }
            }
        }
        vbox {

            paddingTop = 4.0
            textareaHvalue = textarea {
                minHeight = 350.0
                prefHeight = 350.0
                maxHeight = 350.0

                if (hvalue != null) {
                    text = hvalue.value
                }
            }
        }

        vbox {
            hbox {
                paddingTop = 8.0
                buttonbar {

                    button ("Delete") {
                        action {
                            confirmation("", "Are you sure to delete this item ?") { confirmDel ->
                                if (confirmDel == ButtonType.OK) {

                                    try {
                                        if (hvalue != null) {
                                            RedisUtils.hdel(mainView.textFieldKey.text, hvalue.key!!)
                                        }
                                    } catch (e: Exception) {
                                        error("", "Error:" + e.message)
                                    }
                                    closeDialog()
                                    reloadTableViewList()
                                }
                            }
                        }
                    }
                    button ("Set") {
                        addClass("btn-info")

                        action {
                            confirmation("", "Set conformation.") { confirmDel ->
                                if (confirmDel == ButtonType.OK) {

                                    try {
                                        if (hvalue != null) {
                                            RedisUtils.hset(mainView.textFieldKey.text, textfieldHkey.text, textareaHvalue.text)
                                        }
                                    } catch (e: Exception) {
                                        error("", "Error:" + e.message)
                                    }

                                    closeDialog()
                                    reloadTableViewList()
                                }
                            }


                        }
                    }
                }
            }
        }
    }

    init {

        formatHvalue()

        comboboxFormat.onAction = EventHandler {
            formatHvalue()
        }

        textfieldHkey.onAction = EventHandler {
            textareaHvalue.text = RedisUtils.hget(mainView.textFieldKey.text, textfieldHkey.text)
            formatHvalue()
        }
    }

    fun formatHvalue() {
        if (hvalue != null && !StringUtils.isEmpty(hvalue.value)) {
            val valueFormat = if (comboboxFormat.selectedItem != null) comboboxFormat.selectedItem as FormatType else FormatType.Json
            textareaHvalue.text = StringUtils.formatJson(textareaHvalue.text, false, valueFormat)
        }
    }

    private fun closeDialog() {
        close()
    }

    fun reloadTableViewList() {

    }
}