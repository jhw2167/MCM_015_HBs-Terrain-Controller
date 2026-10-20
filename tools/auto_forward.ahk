#Requires AutoHotkey v2.0
#SingleInstance Force
SetTitleMatchMode 2

global holding := false
global pressed := false

F8:: {
    global holding
    holding := !holding
    if holding {
        SetTimer(SyncKey, 100)
        SyncKey()
    } else {
        SetTimer(SyncKey, 0)
        ReleaseKey()
    }
}

F10:: {
    ReleaseKey()
    ExitApp
}

SyncKey() {
    global pressed
    if WinActive("Minecraft") {
        if !pressed {
            Send "{w down}"
            pressed := true
        }
    } else {
        ReleaseKey()
    }
}

ReleaseKey() {
    global pressed
    if pressed {
        Send "{w up}"
        pressed := false
    }
}

OnExit((*) => ReleaseKey())
