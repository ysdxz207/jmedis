package win.hupubao.views

import com.alibaba.fastjson.JSON
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import tornadofx.*
import win.hupubao.beans.RedisConfig
import win.hupubao.utils.ConfigUtils
import win.hupubao.utils.StringUtils

class EditConfigFragment : Fragment() {
    override val root: BorderPane by fxml("/views/ConfigDialog.fxml")


    private val labelTitle: Label by fxid()

    private val alias: TextField by fxid()
    private val host: TextField by fxid()
    private val port: TextField by fxid()
    private val auth: TextField by fxid()

    private val btnDelete: Button by fxid()

    private var id: Long? = null

    init {
        currentStage?.isResizable = false
        val mainView = find(MainView::class)

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

        close()
        val mainView = find(MainView::class)
        mainView.loadComboRedisConfig(redisConfig)

    }

    private fun closeDialog() {
        close()
    }

    fun deleteConfig() {
        val mainView = find(MainView::class)
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