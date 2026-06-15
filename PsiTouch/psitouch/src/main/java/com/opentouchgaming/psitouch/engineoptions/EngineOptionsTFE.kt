package com.opentouchgaming.psitouch.engineoptions

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.util.Function
import com.opentouchgaming.psitouch.R
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.EngineOptionsInterface
import com.opentouchgaming.androidcore.EngineOptionsInterface.MultiplayerCallback
import com.opentouchgaming.androidcore.EngineOptionsInterface.RunInfo
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.androidcore.ui.widgets.SpinnerWidget
import com.opentouchgaming.psitouch.databinding.DialogOptionsTfeBinding

class EngineOptionsTFE : EngineOptionsInterface
{
    var log = DebugLog(DebugLog.Module.GAMEFRAGMENT, "EngineOptionsTFE")

    lateinit var binding: DialogOptionsTfeBinding

    lateinit var dialog: Dialog

    // GLES rendering mode override. Auto lets the engine pick the highest capable mode, the others
    // force it down via the engine's --force_gles* command line arguments.
    val GLES_MODE_AUTO = 0
    val GLES_MODE_30 = 1
    val GLES_MODE_20 = 2

    val GLES_MODE_PREFIX = "tfe_gles_mode"
    val GLES_MODE_DEFAULT = GLES_MODE_AUTO


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
            "Force the GLES feature level. Auto picks the highest the device supports. Lower modes are for testing/compatibility.",
            glesItems, GLES_MODE_PREFIX, GLES_MODE_DEFAULT, R.drawable.setting_gpu)

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