package com.opentouchgaming.psitouch.engineoptions

class EngineOptionsOpenJKMP : EngineOptionsOpenJKBase(
    logTag = "EngineOptionsOpenJKMP",
    prefix = "openjkmp",
    dialogTitle = "JA Multiplayer options",
    deleteDirs = arrayOf("/jamp/base/"),
    deleteFiles = arrayOf("openjk.cfg")
)
