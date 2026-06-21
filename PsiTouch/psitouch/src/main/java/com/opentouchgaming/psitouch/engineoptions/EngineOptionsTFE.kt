package com.opentouchgaming.psitouch.engineoptions

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.EngineOptionsInterface
import com.opentouchgaming.androidcore.EngineOptionsInterface.MultiplayerCallback
import com.opentouchgaming.androidcore.EngineOptionsInterface.RunInfo
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.androidcore.ui.widgets.AudioOverrideWidget
import com.opentouchgaming.androidcore.ui.widgets.DeleteDataWidget
import com.opentouchgaming.androidcore.ui.widgets.GamepadConfigWidget
import com.opentouchgaming.androidcore.ui.widgets.SpinnerWidget
import com.opentouchgaming.androidcore.ui.widgets.SwitchWidget
import com.opentouchgaming.psitouch.R
import com.opentouchgaming.psitouch.databinding.DialogOptionsTfeBinding

class EngineOptionsTFE : EngineOptionsInterface
{
    var log = DebugLog(DebugLog.Module.GAMEFRAGMENT, "EngineOptionsTFE")

    lateinit var binding: DialogOptionsTfeBinding

    lateinit var dialog: Dialog

    val audioOverride = AudioOverrideWidget("tfe_")

    // GLES rendering mode override. Auto lets the engine pick the highest capable mode, the others
    // force it down via the engine's --force_gles* command line arguments.
    val GLES_MODE_AUTO = 0
    val GLES_MODE_30 = 1
    val GLES_MODE_20 = 2

    val GLES_MODE_PREFIX = "tfe_gles_mode"
    val GLES_MODE_DEFAULT = GLES_MODE_AUTO

    val TAP_MODE_PREFIX = "tfe_mouse_tap_mode"
    val TAP_MODE_DEFAULT = true

    val GAMEPAD_CONFIG_KEY = "tfe_gamepad_config"


    override fun showDialog(activity: Activity, engine: GameEngine, version: Int, update: Function<Int, Void>)
    {
        binding = DialogOptionsTfeBinding.inflate(activity.layoutInflater)

        dialog = Dialog(activity, R.style.DialogEngineSettings)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setTitle("IOTRCW options")
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        val glesItems = arrayOf<Pair<String, View?>>(Pair("Auto (highest)", null), Pair("Force GLES 3.0", null), Pair("Force GLES 2.0", null))
        SpinnerWidget(activity, binding.glesModeSpinner.root, "Rendering mode",
            "Force the GLES feature level.",
            glesItems, GLES_MODE_PREFIX, GLES_MODE_DEFAULT, R.drawable.setting_gpu)

        SwitchWidget(activity, binding.mouseTapModeSwitch.root, "Tap-to-position menu mouse",
            "Tap in-game menu items, turn off to drag cursor.",
            TAP_MODE_PREFIX, TAP_MODE_DEFAULT, R.drawable.setting_gear)

        GamepadConfigWidget(activity, binding.gamepadConfigSpinner.root, GAMEPAD_CONFIG_KEY)

        // Audio buffer override. A larger buffer avoids stuttery audio (e.g.
        // while playing the startup video). TFE mixes at a fixed rate, so only
        // the buffer size is honoured (see audioOverrideFreq below).
        audioOverride.linkUI(activity, dialog)

        DeleteDataWidget(
            activity,
            dialog.findViewById(R.id.delete_data_button),
            "Delete all Dark Forces settings files?",
            arrayOf<String>("/tfe/"),
            arrayOf<String>("settings.ini", "settings_default.ini", "tfe_input_remapping.bin"),
            "",
            arrayOf<String>(""),
            arrayOf<String>("")
        )

        dialog.show()
    }

    override fun getRunInfo(version: Int): RunInfo
    {
        val info = RunInfo()
        info.glesVersion = 3
        info.args = ""

        when (SpinnerWidget.fetchValue(AppInfo.getContext(), GLES_MODE_PREFIX, GLES_MODE_DEFAULT))
        {
            GLES_MODE_30 -> info.args = " --force_gles30 "
            GLES_MODE_20 -> info.args = " --force_gles20 "
        }

        info.mouseTapMode = SwitchWidget.fetchValue(AppInfo.getContext(), TAP_MODE_PREFIX, TAP_MODE_DEFAULT)
        info.gamepadConfig = GamepadConfigWidget.fetchValue(AppInfo.getContext(), GAMEPAD_CONFIG_KEY)

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
        // TFE's mixer runs at a fixed sample rate, so the device frequency can't
        // be changed without breaking playback pitch. Only the buffer size is
        // overridable (see audioDevice.cpp).
        return 0
    }

    override fun audioOverrideSamples(): Int
    {
        return audioOverride.getSamples()
    }

    override fun audioOverrideBackend(): Int
    {
        return audioOverride.getBackend()
    }
}