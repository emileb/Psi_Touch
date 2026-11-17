package com.opentouchgaming.psitouch;

import static com.opentouchgaming.androidcore.DebugLog.Level.D;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.util.Pair;

import com.opentouchgaming.androidcore.AppInfo;
import com.opentouchgaming.androidcore.DebugLog;
import com.opentouchgaming.androidcore.EngineOptionsInterface;
import com.opentouchgaming.androidcore.GameEngine;
import com.opentouchgaming.androidcore.common.LaunchIntent;
import com.opentouchgaming.androidcore.common.MainFragment;
import com.opentouchgaming.androidcore.ui.OptionsDialogKt;
import com.opentouchgaming.androidcore.ui.widgets.SpinnerWidget;


public class PsiFragment extends MainFragment
{
    static DebugLog log;

    static
    {
        log = new DebugLog(DebugLog.Module.GAMEFRAGMENT, "PsiFragment");
    }

    TFELauncher tfeLauncher;

    OpenJKLauncher openjkLauncher;

    OpenJKMPLauncher openjkmpLauncher;

    OpenJOLauncher openjoLauncher;

    OpenJKDF2Launcher openjkdf2Launcher;

    public PsiFragment()
    {
        super();
        log.log(D, "New instant created!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        noLicCheck = true;

        tfeLauncher = new TFELauncher();

        openjkLauncher = new OpenJKLauncher();

        openjkmpLauncher = new OpenJKMPLauncher();

        openjoLauncher = new OpenJOLauncher();

        openjkdf2Launcher = new OpenJKDF2Launcher();
    }

    public void setLauncher()
    {
        switch (AppInfo.currentEngine.engine)
        {
            case TFE:
                launcher = tfeLauncher;
                break;
            case OPENJK:
                launcher = openjkLauncher;
                break;
            case OPENJK_MP:
                launcher = openjkmpLauncher;
                break;
            case OPENJO:
                launcher = openjoLauncher;
                break;
            case OPENJKDF2:
                launcher = openjkdf2Launcher;
                break;
        }
    }


    public void launchGame(final GameEngine engine, boolean download, final String multiplayerArgs)
    {
        final String rootPath = launcher.getRunDirectory(selectedSubGame);

        // Check if not installed yet
        if (selectedSubGame.getName() == null)
        {
            return;
        }

        // Check for engine specific downloads or issues
        if (download)
        {
            tfeLauncher.setOnExtracted(() -> launchGame(engine, false, multiplayerArgs));

            if (launcher.checkForDownloads(getActivity(), AppInfo.currentEngine, selectedSubGame))
                return;
        }

        // Save history
        engineData.addArgsHistory();

        EngineOptionsInterface.RunInfo runInfo = engine.engineOptions.getRunInfo(selectedVersion);

        // Build args
        String args = engine.args + " ";
        args += runInfo.args + " ";
        args += argsFinal;

        // Create Intent. OpenJKDF2 is an SDL3 engine (app3000); TFE / OpenJK are SDL2.
        Intent intent;

        if (runInfo.sdlVersion == 3)
        {
            intent = new Intent(getActivity(), org.libsdl.app3000.SDLActivity.class);
        }
        else
        {
            intent = new Intent(getActivity(), org.libsdl.app2012.SDLActivity.class);
        }

        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        intent.putExtra("load_libs", engine.loadLibs[selectedVersion]);
        intent.putExtra("log_filename", engine.getLogFilename());
        intent.putExtra("game_path", rootPath);
        intent.putExtra("args", args);

        Pair<String, String> quickCommandPaths = launcher.getQuickCommandsDirectory(selectedSubGame);
        int wheelNbr = selectedSubGame.getWheelNbr();
        LaunchIntent.populateIntent(getActivity(), intent, engine.engineOptions, runInfo, 0, wheelNbr, quickCommandPaths);

        startActivity(intent);
    }
}
