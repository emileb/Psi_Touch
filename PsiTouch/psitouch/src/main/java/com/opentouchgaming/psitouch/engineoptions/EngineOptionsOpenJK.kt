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
import com.opentouchgaming.androidcore.ui.widgets.SwitchWidget
import com.opentouchgaming.psitouch.databinding.DialogOptionsOpenjkBinding

class EngineOptionsOpenJK : EngineOptionsInterface
{
    var log = DebugLog(DebugLog.Module.GAMEFRAGMENT, "EngineOptionsOpenJK")

    lateinit var binding: DialogOptionsOpenjkBinding

    lateinit var dialog: Dialog

    val OPENAL_PREFIX = "openjk_use_openal"
    val OPENAL_DEFAULT = false


    override fun showDialog(activity: Activity, engine: GameEngine, version: Int, update: Function<Int, Void>)
    {
        binding = DialogOptionsOpenjkBinding.inflate(activity.layoutInflater)

        dialog = Dialog(activity, R.style.DialogEngineSettings)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setTitle("OpenJK options")
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        SwitchWidget(activity, binding.openalSwitch.root, "Use OpenAL audio",
            "Use the OpenAL sound backend (positional/EAX audio) instead of the default software mixer.",
            OPENAL_PREFIX, OPENAL_DEFAULT, R.drawable.setting_audio)

        dialog.show()
    }

    override fun getRunInfo(version: Int): RunInfo
    {
        val info = RunInfo()
        info.glesVersion = 1

        val useOpenAL = if (SwitchWidget.fetchValue(AppInfo.getContext(), OPENAL_PREFIX, OPENAL_DEFAULT)) 1 else 0
        info.args = " +set r_mode -1 +set r_customwidth \$W +set r_customheight \$H +set s_UseOpenAL $useOpenAL "
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
