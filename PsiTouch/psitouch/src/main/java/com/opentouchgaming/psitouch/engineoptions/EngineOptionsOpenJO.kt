package com.opentouchgaming.psitouch.engineoptions

class EngineOptionsOpenJO : EngineOptionsOpenJKBase(
    logTag = "EngineOptionsOpenJO",
    prefix = "openjo",
    dialogTitle = "Jedi Outcast options",
    deleteDirs = arrayOf("/jo/base/"),
    deleteFiles = arrayOf("openjo_sp.cfg")
)
