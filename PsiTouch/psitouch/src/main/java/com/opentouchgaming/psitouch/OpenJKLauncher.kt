package com.opentouchgaming.psitouch

import android.app.Activity
import androidx.core.util.Pair
import com.opentouchgaming.androidcore.*
import com.opentouchgaming.androidcore.common.GameLauncherInterface
import java.io.*
import java.util.*

class OpenJKLauncher : GameLauncherInterface
{

    private val log = DebugLog(DebugLog.Module.CONTROLS, "OpenJKLauncher")

    val SUB_DIR = "JA"

    val WEAPON_WHEEL_NBR = 10

    override fun updateSubGames(engine: GameEngine, availableSubGames: ArrayList<SubGame>)
    {
        log.log(DebugLog.Level.D, "updateSubGames")

        availableSubGames.clear()

        File(runDirectory).mkdirs()

        SubGame.addGame(availableSubGames, runDirectory, secondaryDirectory, SUB_DIR, "base", 0, WEAPON_WHEEL_NBR,
                arrayOf(""), R.drawable.file_folder, "openjk",
                "Copy assets0.pk3, assets1.pk3, assets2.pk3, assets3.pk3 to: ", "Put your Jedi Academy assets pk3 files here.txt")

        val files = Utils.listFiles(arrayOf(runDirectory, secondaryDirectory))

        if (files != null)
        {
            for (f in files)
            {
                val dirName = f.name.lowercase(Locale.getDefault())
                val pathName = f.parent
                if (f.isDirectory && !dirName.contentEquals("base"))
                {
                    val pathInfo = f.absolutePath
                    val fileInfo = Utils.filesInfoString(pathInfo, null, 3)
                    val mod = SubGame(SUB_DIR + dirName, dirName, dirName, pathName, 0, R.drawable.jk_icon, pathInfo, fileInfo, WEAPON_WHEEL_NBR)
                    availableSubGames.add(mod)
                }
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
       return ""
    }

    override fun checkForDownloads(activity: Activity, engine: GameEngine, subGame: SubGame): Boolean
    {
        return false
    }
}
