package com.opentouchgaming.psitouch

import android.app.Activity
import androidx.core.util.Pair
import com.opentouchgaming.androidcore.AppInfo
import com.opentouchgaming.androidcore.DebugLog
import com.opentouchgaming.androidcore.GameEngine
import com.opentouchgaming.androidcore.PakExt
import com.opentouchgaming.androidcore.SubGame
import com.opentouchgaming.androidcore.Utils
import com.opentouchgaming.androidcore.common.GameLauncherInterface
import com.opentouchgaming.saffal.FileSAF
import java.io.File
import java.util.Locale

// Common launcher for the OpenJK-derived engines (JKA SP, JKA MP, JO); subclasses supply the per-game differences.
abstract class OpenJKBaseLauncher(
        logTag: String,
        private val subDir: String,
        private val iconResId: Int,
        private val displayName: String,
        private val assetFiles: Array<String>,
        private val copyInstructions: String,
        private val placeholderFile: String
) : GameLauncherInterface
{
    private val log = DebugLog(DebugLog.Module.CONTROLS, logTag)

    private val weaponWheelNbr = 10

    override fun updateSubGames(engine: GameEngine, availableSubGames: ArrayList<SubGame>)
    {
        log.log(DebugLog.Level.D, "updateSubGames")

        availableSubGames.clear()

        File(runDirectory).mkdirs()

        SubGame.addGame(availableSubGames, runDirectory, secondaryDirectory, subDir, "base", 0, weaponWheelNbr,
                assetFiles, iconResId, displayName, copyInstructions, placeholderFile)

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
                    val mod = SubGame(subDir + dirName, dirName, dirName, pathName, 0, iconResId, pathInfo, fileInfo, weaponWheelNbr)
                    availableSubGames.add(mod)
                }
            }
        }

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
        return AppInfo.getAppDirectory() + "/$subDir"
    }

    override fun getSecondaryDirectory(): String?
    {
        val secFolder = AppInfo.getAppSecDirectory()

        return if (secFolder != null) "$secFolder/$subDir" else null
    }

    override fun getQuickCommandsDirectory(subGame: SubGame): Pair<String, String>
    {
        val commonPath = AppInfo.getQuickCommandsPath() + "/" + subDir
        val modPath = commonPath + "/" + subGame.name
        return Pair(commonPath, modPath)
    }

    override fun getArgs(engine: GameEngine, subGame: SubGame): String
    {
        var args = ""

        if (subGame.getName() != null)
        {
            args = " +set fs_game " + Utils.quoteString(subGame.getName())
        }

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
