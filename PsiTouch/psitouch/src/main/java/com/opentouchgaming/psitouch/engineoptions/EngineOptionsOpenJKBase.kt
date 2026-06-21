package com.opentouchgaming.psitouch.engineoptions

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import androidx.arch.core.util.Function
import com.opentouchgaming.psitouch.R
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.EngineOptionsInterface
import com.opentouchgaming.androidcore.EngineOptionsInterface.MultiplayerCallback
import com.opentouchgaming.androidcore.EngineOptionsInterface.RunInfo
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.androidcore.ui.widgets.AudioOverrideWidget
import com.opentouchgaming.androidcore.ui.widgets.DeleteDataWidget
import com.opentouchgaming.androidcore.ui.widgets.GamepadConfigWidget
import com.opentouchgaming.androidcore.ui.widgets.SwitchWidget
import com.opentouchgaming.psitouch.databinding.DialogOptionsOpenjkBinding

// Common options for the OpenJK-derived engines (JKA SP, JO SP, JKA MP). Each subclass passes a
// distinct settings prefix so every engine keeps its own independent settings, plus the per-game
// dialog title and config-file delete paths.
abstract class EngineOptionsOpenJKBase(
    logTag: String,
    private val prefix: String,
    private val dialogTitle: String,
    private val deleteDirs: Array<String>,
    private val deleteFiles: Array<String>
) : EngineOptionsInterface
{
    var log = DebugLog(DebugLog.Module.GAMEFRAGMENT, logTag)

    lateinit var binding: DialogOptionsOpenjkBinding

    lateinit var dialog: Dialog

    val audioOverride = AudioOverrideWidget(prefix + "_")

    val OPENAL_PREFIX = prefix + "_use_openal"
    val OPENAL_DEFAULT = false

    val TAP_MODE_PREFIX = prefix + "_mouse_tap_mode"
    val TAP_MODE_DEFAULT = true

    val GAMEPAD_CONFIG_KEY = prefix + "_gamepad_config"


    override fun showDialog(activity: Activity, engine: GameEngine, version: Int, update: Function<Int, Void>)
    {
        binding = DialogOptionsOpenjkBinding.inflate(activity.layoutInflater)

        dialog = Dialog(activity, R.style.DialogEngineSettings)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setTitle(dialogTitle)
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        SwitchWidget(activity, binding.openalSwitch.root, "Use OpenAL audio",
            "Use the OpenAL sound backend (positional/EAX audio) instead of the default software mixer.",
            OPENAL_PREFIX, OPENAL_DEFAULT, R.drawable.setting_audio)

        SwitchWidget(activity, binding.mouseTapModeSwitch.root, "Tap-to-position menu mouse",
            "Tap in-game menu items, turn off to drag cursor.",
            TAP_MODE_PREFIX, TAP_MODE_DEFAULT, R.drawable.setting_gear)

        GamepadConfigWidget(activity, binding.gamepadConfigSpinner.root, GAMEPAD_CONFIG_KEY)

        // Audio buffer / rate override. A larger buffer avoids the stuttery
        // software-mixer audio during the startup cinematic.
        audioOverride.linkUI(activity, dialog)

        DeleteDataWidget(
            activity,
            dialog.findViewById(R.id.delete_data_button),
            "Delete $dialogTitle settings files?",
            deleteDirs,
            deleteFiles,
            "",
            arrayOf<String>(""),
            arrayOf<String>("")
        )

        dialog.show()
    }

    override fun getRunInfo(version: Int): RunInfo
    {
        val info = RunInfo()
        info.glesVersion = 1

        val useOpenAL = if (SwitchWidget.fetchValue(AppInfo.getContext(), OPENAL_PREFIX, OPENAL_DEFAULT)) 1 else 0
        info.args = " +set r_mode -1 +set r_customwidth \$W +set r_customheight \$H +set s_UseOpenAL $useOpenAL "

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
        return audioOverride.getFreq()
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
