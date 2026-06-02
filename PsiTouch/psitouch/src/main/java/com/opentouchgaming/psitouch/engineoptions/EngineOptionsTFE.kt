package com.opentouchgaming.psitouch.engineoptions

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import androidx.arch.core.util.Function
import com.opentouchgaming.psitouch.R
import com.opentouchgaming.psitouch.databinding.DialogOptionsIortcwBinding
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.EngineOptionsInterface
import com.opentouchgaming.androidcore.EngineOptionsInterface.MultiplayerCallback
import com.opentouchgaming.androidcore.EngineOptionsInterface.RunInfo
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.androidcore.ui.widgets.DeleteDataWidget
import com.opentouchgaming.androidcore.ui.widgets.ResolutionOptionsWidget
import com.opentouchgaming.androidcore.ui.widgets.ResolutionOptionsWidget.ResolutionOptions
import com.opentouchgaming.androidcore.ui.widgets.ResolutionOptionsWidget.ResolutionType
import com.opentouchgaming.androidcore.ui.widgets.SwitchWidget

class EngineOptionsTFE : EngineOptionsInterface
{
    var log = DebugLog(DebugLog.Module.GAMEFRAGMENT, "EngineOptionsTFE")

    lateinit var binding: DialogOptionsIortcwBinding

    lateinit var dialog: Dialog

    val MAINTAIN_ASPECT_PREFIX = "iortcw_maintain_aspect"
    val MAINTAIN_ASPECT_DEFAULT = true

    val MENU_TOUCH_PREFIX = "iortcw_menu_touch"
    val MENU_TOUCH_DEFAULT = true

    val ALLOW_CHEATS_PREFIX = "iortcw_allow_cheats"
    val ALLOW_CHEATS_DEFAULT = false

    val RESOLUTION_PREFIX = "iortcw_resolution"
    val RESOLUTION_DEFAULT = 0

    companion object
    {
        var resolutionListDefault: ArrayList<ResolutionOptions> = ArrayList()

        init
        {
            resolutionListDefault.add(ResolutionOptions("Screen (100%)", ResolutionType.FULL, "\$W", "\$H"))
            resolutionListDefault.add(ResolutionOptions("Screen / 2 (50%)", ResolutionType.FULL_0_5, "\$W2", "\$H2"))
            resolutionListDefault.add(ResolutionOptions("Screen / 3 (33%)", ResolutionType.FULL_0_3, "\$W3", "\$H3"))
            resolutionListDefault.add(ResolutionOptions("640 x 480", ResolutionType.FULL_0_3, "640", "480"))
            resolutionListDefault.add(ResolutionOptions("Custom", ResolutionType.CUSTOM, "0", "0"));
        }
    }

    override fun showDialog(activity: Activity, engine: GameEngine, version: Int, update: Function<Int, Void>)
    {
        binding = DialogOptionsIortcwBinding.inflate(activity.layoutInflater)

        dialog = Dialog(activity, R.style.DialogEngineSettings)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setTitle("IOTRCW options")
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        ResolutionOptionsWidget(activity, binding.resolutionSelect.root, RESOLUTION_PREFIX, resolutionListDefault, RESOLUTION_DEFAULT)

        SwitchWidget(activity, binding.menuTouchSwitch.root, "Touch menu items", "Enables you to touch menu items", MENU_TOUCH_PREFIX, MENU_TOUCH_DEFAULT)

        SwitchWidget(activity, binding.maintainAspectSwitch.root, "Menu 4:3 aspect ratio", "Fix the aspect ratio of the menu to 4:3", MAINTAIN_ASPECT_PREFIX,
                MAINTAIN_ASPECT_DEFAULT)
/*
        SwitchWidget(activity, binding.allowCheatsSwitch.root, "Allow cheats", "Set sv_cheats = 1 to allow cheat codes", ALLOW_CHEATS_PREFIX,
                ALLOW_CHEATS_DEFAULT)
*/
        DeleteDataWidget(activity, dialog.findViewById(R.id.delete_data_button), "Delete RTCW config files?", arrayOf("/iortcw/main/"),
                arrayOf("wolfconfig.cfg"), "", arrayOf(""), arrayOf(""))

        dialog.setOnDismissListener {
            update.apply(version)
        }

        dialog.show()
    }

    override fun getRunInfo(version: Int): RunInfo
    {
        val info = RunInfo()
        info.glesVersion = 3

        return info
    }

    override fun hasMultiplayer(): Boolean
    {
        return false
    }

    override fun launchMultiplayer(ac: Activity, engine: GameEngine, version: Int, mainArgs: String, callback: MultiplayerCallback)
    {

    }

    override fun audioOverrideFreq(): Int
    {
        return 0
    }

    override fun audioOverrideSamples(): Int
    {
        return 0
    }
}