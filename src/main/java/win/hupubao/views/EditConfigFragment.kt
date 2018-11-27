package win.hupubao.views

import javafx.scene.control.*
import tornadofx.*
import win.hupubao.beans.RedisConfig
import win.hupubao.utils.ConfigUtils
import win.hupubao.utils.StringUtils

class EditConfigFragment : Fragment() {
    private val mainView: MainView by inject()

    lateinit var labelTitle: Label

    lateinit var alias: TextField
    lateinit var host: TextField
    lateinit var port: TextField
    lateinit var auth: TextField

    lateinit var btnDelete: Button

    private var id: Long? = null


    override val root = vbox {
        
        prefHeight = 240.0
        prefWidth = 400.0
        
        
        vbox {
            minWidth = 380.0
            addClass("config-dialog")
            
            paddingBottom = 10.0
            paddingRight = 10.0
            
            hbox {
                paddingBottom = 10.0
                paddingLeft = 10.0
                paddingTop = 8.0
                
                labelTitle = label {
                    addClass("title")
                }
            }

            hbox {
                paddingBottom = 10.0
                paddingLeft = 10.0

                label("alias")
                alias = textfield {

                }
            }

            hbox {
                paddingBottom = 10.0
                paddingLeft = 10.0

                label("host")
                host = textfield {

                }
            }

            hbox {
                paddingBottom = 10.0
                paddingLeft = 10.0

                label("port")
                port = textfield {

                }
            }

            hbox {
                paddingBottom = 10.0
                paddingLeft = 10.0

                label("auth")
                auth = textfield {

                    promptText = "Keep empty without auth."
                }
            }

            hbox {
                buttonbar {
                    minWidth = 380.0
                    paddingBottom = 10.0
                    paddingLeft = 10.0

                    btnDelete = button("Delete") {
                        ButtonBar.setButtonData(this, ButtonBar.ButtonData.LEFT)

                        action {
                            deleteConfig()
                        }
                    }

                    button("Save") {
                        ButtonBar.setButtonData(this, ButtonBar.ButtonData.RIGHT)
                        addClass("btn-success")

                        action {
                            editConfig()
                        }
                    }
                }
            }
        }
    }
    
    

    init {
        currentStage?.isResizable = false

        // load redis configuration info
        if (mainView.comboConfig.selectedItem != null) {
            val redisConfig: RedisConfig = mainView.comboConfig.selectedItem as RedisConfig
            if (redisConfig.id != null) {
                id = redisConfig.id
                alias.text = redisConfig.alias
                host.text = redisConfig.host
                port.text= redisConfig.port.toString()
                auth.text = redisConfig.auth

                labelTitle.text = "Edit Redis Config"
                btnDelete.isVisible = true
            } else {
                labelTitle.text = "Create Redis Config"
                btnDelete.isVisible = false
            }
        }
    }

    fun editConfig() {
        if (StringUtils.isEmpty(host.text)) {
            alert(Alert.AlertType.ERROR, "", "Please enter host.")
            return
        }
        if (StringUtils.isEmpty(port.text) || !StringUtils.isNumeric(port.text)) {
            alert(Alert.AlertType.ERROR, "", "Port should be a number.")
            return
        }

        close()

        val config = ConfigUtils.get()
        val redisConfig = RedisConfig()
        redisConfig.alias = if (alias.text == null || alias.text.isEmpty()) host.text else alias.text
        redisConfig.host = host.text
        redisConfig.port = port.text.toInt()
        redisConfig.auth = auth.text

        if (id == null) {
            // new configuration
            redisConfig.id = System.nanoTime()
        } else {
            redisConfig.id = id
            val it = config.redisConfigList.iterator()
            while (it.hasNext()) {
                val redisConfigOld = it.next()
                if (redisConfigOld.id == id) {
                    it.remove()
                }
            }
        }

        config.redisConfigList.add(redisConfig)

        ConfigUtils.save(config)
        ConfigUtils.fireChanged()

        mainView.loadComboRedisConfig(redisConfig)

    }

    private fun closeDialog() {
        close()
    }

    fun deleteConfig() {
        confirmation("", "Are you sure to delete this config ?") {
            if (it == ButtonType.OK && mainView.comboConfig.selectedItem != null) {
                closeDialog()
                val redisConfig: RedisConfig = mainView.comboConfig.selectedItem as RedisConfig
                ConfigUtils.deleteRedisConfigById(redisConfig.id!!)
                ConfigUtils.fireChanged()
                mainView.loadComboRedisConfig(null)
            }
        }
    }
}