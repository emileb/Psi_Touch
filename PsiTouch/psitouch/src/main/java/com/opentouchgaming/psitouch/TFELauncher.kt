package com.opentouchgaming.psitouch

import android.app.Activity
import android.graphics.Bitmap
import androidx.core.util.Pair
import com.opentouchgaming.androidcore.*
import com.opentouchgaming.androidcore.common.GameLauncherInterface
import com.opentouchgaming.saffal.FileSAF
import java.io.*
import java.util.*

class TFELauncher : GameLauncherInterface
{

    private val log = DebugLog(DebugLog.Module.CONTROLS, "TFELauncher")


    val SUB_DIR = "TFE"

    val WEAPON_WHEEL_NBR = 10

    override fun updateSubGames(engine: GameEngine, availableSubGames: ArrayList<SubGame>)
    {
        log.log(DebugLog.Level.D, "updateSubGames")

        availableSubGames.clear()

        File(runDirectory).mkdirs()

        SubGame.addGame(availableSubGames, runDirectory, secondaryDirectory, SUB_DIR, "", 0, WEAPON_WHEEL_NBR,
                arrayOf("dark.gob", "sounds.gob", "sprites.gob", "textures.gob"), R.drawable.df_icon, "tfe",
                "Copy your Dark Forces files (inc LFD folder) to: ", "Put your Dark Forces files here.txt")

        for (game in availableSubGames)
        {
            if (game.getName() != null && game.getRootPath() != null)
            {
                game.load(AppInfo.getContext())
            }
        }
    }

    override fun getRunDirectory(): String
    {
        return AppInfo.getAppDirectory() + "/$SUB_DIR"
    }

    override fun getSecondaryDirectory(): String?
    {
        val secFolder = AppInfo.getAppSecDirectory()

        return if (secFolder != null) "$secFolder/$SUB_DIR" else null
    }

    override fun getQuickCommandsDirectory(subGame: SubGame): Pair<String, String>
    {
        val commonPath = AppInfo.getQuickCommandsPath() + "/" + SUB_DIR
        val modPath = commonPath + "/" + subGame.name
        return Pair(commonPath, modPath)
    }

    override fun getArgs(engine: GameEngine, subGame: SubGame): String
    {
        // When the game data lives on secondary (SD/scoped) storage, point TFE's
        // source-data path at it with the absolute path via --gamePath. The engine
        // keeps its working dir on the real primary storage (for saves/config) and
        // only reads game data from this path, which SAFFAL intercepts and routes
        // through scoped storage. (TFE has no fs_cdpath-style search list.)
        var args = ""
        if (secondaryDirectory != null && subGame.rootPath != null && subGame.rootPath.contentEquals(secondaryDirectory!!))
        {
            args += " --gamePath " + Utils.quoteString(secondaryDirectory)
        }
        return args
    }

    override fun checkForDownloads(activity: Activity, engine: GameEngine, subGame: SubGame): Boolean
    {
        ZipUtils.extractAsset(activity, "tfe.zip", runDirectory,2000000);
        return false
    }
}