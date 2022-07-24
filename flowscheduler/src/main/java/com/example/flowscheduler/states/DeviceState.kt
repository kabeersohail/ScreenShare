package com.example.flowscheduler.states

class DeviceState {
    var rebootState: RebootState = RebootState.CommandNotIssued
    var kioskLockState: KioskLockState = KioskLockState.Unlocked
    var adminLockState: AdminLockState = AdminLockState.Unlocked
    var clearKioskPasswordState: ClearKioskPasswordState = ClearKioskPasswordState.CommandNotIssued
    var wipeDataState: WipeDataState = WipeDataState.CommandNotIssued
    var uninstallState: UninstallState = UninstallState.CommandNotIssued
}

sealed class KioskLockState {
    object Locked : KioskLockState()
    object Unlocked : KioskLockState()
}

sealed class AdminLockState {
    object Locked : AdminLockState()
    object Unlocked : AdminLockState()
}

sealed class UninstallState {
    object CommandIssued : UninstallState()
    object CommandNotIssued : UninstallState()
}

sealed class WipeDataState {
    object CommandIssued : WipeDataState()
    object CommandNotIssued : WipeDataState()
}

sealed class ClearKioskPasswordState {
    object CommandIssued : ClearKioskPasswordState()
    object CommandNotIssued : ClearKioskPasswordState()
}

sealed class RebootState {
    object CommandIssued : RebootState()
    object CommandNotIssued : RebootState()
}