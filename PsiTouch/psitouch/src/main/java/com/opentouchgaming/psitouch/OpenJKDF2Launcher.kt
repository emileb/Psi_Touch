package com.opentouchgaming.psitouch

import android.app.Activity
import androidx.core.util.Pair
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.androidcore.SubGame
import com.opentouchgaming.androidcore.Utils
import com.opentouchgaming.androidcore.common.GameLauncherInterface
import java.io.File

// Jedi Knight: Dark Forces II (OpenJKDF2) launcher. See docs/engines/openjkdf2.md.
class OpenJKDF2Launcher : GameLauncherInterface
{
    private val log = DebugLog(DebugLog.Module.CONTROLS, "OpenJKDF2Launcher")

    val SUB_DIR = "JKDF2"

    val WEAPON_WHEEL_NBR = 10

    override fun updateSubGames(engine: GameEngine, availableSubGames: ArrayList<SubGame>)
    {
        log.log(DebugLog.Level.D, "updateSubGames")

        availableSubGames.clear()

        File(runDirectory).mkdirs()

        // The engine's own required-asset list (src/Main/InstallHelper.c), in the
        // case a Steam/GOG install actually has. File names are matched
        // case-insensitively by the check; the directory part is not on SAF storage,
        // so the capitalised folder names matter. The engine itself opens everything
        // case-insensitively (fcaseopen).
        SubGame.addGame(availableSubGames, runDirectory, secondaryDirectory, SUB_DIR, "", 0, WEAPON_WHEEL_NBR,
                arrayOf("Episode/JK1.GOB", "Resource/Res1hi.gob", "Resource/Res2.gob", "Resource/JK_.CD"), R.drawable.jkdf2_icon, "Jedi Knight: Dark Forces II",
                "Copy your Jedi Knight folders (Episode, Resource, MUSIC) to: ", "Put your Jedi Knight files here.txt")

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
        // Consumed by mobile/game_interface.cpp (PortableInit), which turns it into
        // the engine's OPENJKDF2_ROOT override and strips it before main(). Always
        // absolute: SAFFAL matches SAF-backed storage by literal path prefix.
        var args = ""
        if (subGame.rootPath != null)
        {
            args += " --gamepath " + Utils.quoteString(subGame.rootPath)
        }

        return args
    }

    override fun checkForDownloads(activity: Activity, engine: GameEngine, subGame: SubGame): Boolean
    {
        return false
    }
}
