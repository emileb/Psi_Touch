package com.opentouchgaming.psitouch

class OpenJKMPLauncher : OpenJKBaseLauncher(
        logTag = "OpenJKMPLauncher",
        subDir = "JAMP",
        iconResId = R.drawable.jk_mp_icon,
        displayName = "JA : Multiplayer",
        assetFiles = arrayOf("base/assets0.pk3", "base/assets1.pk3", "base/assets2.pk3", "base/assets3.pk3"),
        copyInstructions = "Copy assets0.pk3, assets1.pk3, assets2.pk3, assets3.pk3 to: ",
        placeholderFile = "Put your Jedi Academy assets pk3 files here.txt"
)
