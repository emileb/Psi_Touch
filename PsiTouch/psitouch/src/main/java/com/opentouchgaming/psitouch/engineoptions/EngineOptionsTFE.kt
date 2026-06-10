package com.opentouchgaming.psitouch.engineoptions

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import androidx.arch.core.util.Function
import com.opentouchgaming.psitouch.R
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.EngineOptionsInterface
import com.opentouchgaming.androidcore.EngineOptionsInterface.MultiplayerCallback
import com.opentouchgaming.androidcore.EngineOptionsInterface.RunInfo
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.psitouch.databinding.DialogOptionsTfeBinding

class EngineOptionsTFE : EngineOptionsInterface
{
    var log = DebugLog(DebugLog.Module.GAMEFRAGMENT, "EngineOptionsTFE")

    lateinit var binding: DialogOptionsTfeBinding

    lateinit var dialog: Dialog


    override fun showDialog(activity: Activity, engine: GameEngine, version: Int, update: Function<Int, Void>)
    {
        binding = DialogOptionsTfeBinding.inflate(activity.layoutInflater)

        dialog = Dialog(activity, R.style.DialogEngineSettings)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setTitle("IOTRCW options")
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)


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