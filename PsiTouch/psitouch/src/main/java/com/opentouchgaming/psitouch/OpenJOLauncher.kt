package com.opentouchgaming.psitouch

class OpenJOLauncher : OpenJKBaseLauncher(
        logTag = "OpenJOLauncher",
        subDir = "JO",
        iconResId = R.drawable.jo_icon,
        displayName = "Jedi Outcast",
        assetFiles = arrayOf("base/assets0.pk3", "base/assets1.pk3", "base/assets2.pk3", "base/assets5.pk3"),
        copyInstructions = "Copy assets0.pk3, assets1.pk3, assets2.pk3, assets5.pk3 to: ",
        placeholderFile = "Put your Jedi Outcast assets pk3 files here.txt"
)
