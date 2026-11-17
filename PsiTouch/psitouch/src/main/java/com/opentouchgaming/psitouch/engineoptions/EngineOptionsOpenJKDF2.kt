package com.opentouchgaming.psitouch.engineoptions

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import androidx.arch.core.util.Function
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.EngineOptionsInterface
import com.opentouchgaming.androidcore.EngineOptionsInterface.MultiplayerCallback
import com.opentouchgaming.androidcore.EngineOptionsInterface.RunInfo
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.androidcore.ui.widgets.DeleteDataWidget
import com.opentouchgaming.androidcore.ui.widgets.GamepadConfigWidget
import com.opentouchgaming.androidcore.ui.widgets.ResolutionOptionsWidget
import com.opentouchgaming.psitouch.R
import com.opentouchgaming.psitouch.databinding.DialogOptionsOpenjkdf2Binding

class EngineOptionsOpenJKDF2 : EngineOptionsInterface
{
    var log = DebugLog(DebugLog.Module.GAMEFRAGMENT, "EngineOptionsOpenJKDF2")

    lateinit var binding: DialogOptionsOpenjkdf2Binding

    lateinit var dialog: Dialog

    lateinit var resolutionOptionsWidget: ResolutionOptionsWidget

    val PREFIX = "openjkdf2"

    // Default to half the screen: the engine renders its scene FBO at the full
    // window size, which is too much for mid-range GPUs to hold 60 fps.
    val RESOLUTION_DEFAULT = 1

    val GAMEPAD_CONFIG_KEY = "openjkdf2_gamepad_config"

    override fun showDialog(activity: Activity, engine: GameEngine, version: Int, update: Function<Int, Void>)
    {
        binding = DialogOptionsOpenjkdf2Binding.inflate(activity.layoutInflater)

        dialog = Dialog(activity, R.style.DialogEngineSettings)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setTitle("Jedi Knight options")
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        // Framebuffer scaler resolution (handled by the app's SDL layer): the
        // engine sees a smaller window and sizes its scene FBO from it.
        resolutionOptionsWidget = ResolutionOptionsWidget(activity, binding.glResolution.root, PREFIX, RESOLUTION_DEFAULT)

        GamepadConfigWidget(activity, binding.gamepadConfigSpinner.root, GAMEPAD_CONFIG_KEY)

        // Phase 1: the engine still writes its config/saves next to the game data
        // (player/, openjkdf2.json), so nothing under user_files to delete yet.
        DeleteDataWidget(
            activity, binding.deleteDataButton.root,
            "Delete all Jedi Knight settings files?", arrayOf("/openjkdf2/"), arrayOf(""),
            "", arrayOf(""), arrayOf("")
        )

        dialog.setOnDismissListener { resolutionOptionsWidget.save() }

        dialog.show()
    }

    override fun getRunInfo(version: Int): RunInfo
    {
        val info = RunInfo()

        // The engine's own std3D renderer targets GLES 3.0 (#version 300 es).
        info.glesVersion = 3

        // OpenJKDF2 is an SDL3 engine, so it needs the app3000 SDL activity.
        info.sdlVersion = 3

        val res = ResolutionOptionsWidget.getResOption(PREFIX, RESOLUTION_DEFAULT)
        info.frameBufferWidth = res.w
        info.frameBufferHeight = res.h
        info.maintainAspect = res.maintainAspect
        // The engine never renders below 640x480 (jkMain/stdDisplay clamp); Screen / 2 on a 720p phone is 360 high.
        info.frameBufferMinWidth = 640
        info.frameBufferMinHeight = 480

        // Must be non-null: PsiFragment concatenates runInfo.args into the command
        // line, and a null here becomes the literal string "null".
        info.args = " "

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
}
