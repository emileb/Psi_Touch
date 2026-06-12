package com.opentouchgaming.psitouch

import android.app.Activity
import androidx.core.util.Pair
import com.opentouchgaming.androidcore.*
import com.opentouchgaming.androidcore.common.GameLauncherInterface
import java.io.*
import java.util.*

class OpenJOLauncher : GameLauncherInterface
{

    private val log = DebugLog(DebugLog.Module.CONTROLS, "OpenJOLauncher")

    val SUB_DIR = "JO"

    val WEAPON_WHEEL_NBR = 10

    override fun updateSubGames(engine: GameEngine, availableSubGames: ArrayList<SubGame>)
    {
        log.log(DebugLog.Level.D, "updateSubGames")

        availableSubGames.clear()

        File(runDirectory).mkdirs()

        SubGame.addGame(availableSubGames, runDirectory, secondaryDirectory, SUB_DIR, "base", 0, WEAPON_WHEEL_NBR,
                arrayOf(""), R.drawable.file_folder, "openjo",
                "Copy assets0.pk3, assets1.pk3, assets2.pk3, assets5.pk3 to: ", "Put your Jedi Outcast assets pk3 files here.txt")

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
                    val mod = SubGame(SUB_DIR + dirName, dirName, dirName, pathName, 0, R.drawable.jo_icon, pathInfo, fileInfo, WEAPON_WHEEL_NBR)
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
        // When the game files live on secondary (SD/scoped) storage, point the
        // engine at it via fs_cdpath. The engine then searches that path, and
        // SAFFAL intercepts the file access and routes it through scoped storage.
        var args = ""
        if (secondaryDirectory != null && subGame.rootPath != null && subGame.rootPath.contentEquals(secondaryDirectory!!))
        {
            args += " +set fs_cdpath " + Utils.quoteString(secondaryDirectory)
        }
        return args
    }

    override fun checkForDownloads(activity: Activity, engine: GameEngine, subGame: SubGame): Boolean
    {
        return false
    }
}
