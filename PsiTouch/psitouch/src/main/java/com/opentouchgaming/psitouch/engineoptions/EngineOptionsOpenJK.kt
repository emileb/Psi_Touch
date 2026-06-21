package com.opentouchgaming.psitouch.engineoptions

class EngineOptionsOpenJK : EngineOptionsOpenJKBase(
    logTag = "EngineOptionsOpenJK",
    prefix = "openjk",
    dialogTitle = "Jedi Academy options",
    deleteDirs = arrayOf("/ja/base/"),
    deleteFiles = arrayOf("openjk_sp.cfg")
)
