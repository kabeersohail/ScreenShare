package com.example.flowscheduler

import androidx.annotation.VisibleForTesting
import com.example.flowscheduler.models.AdminCommand
import com.example.flowscheduler.states.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Semaphore

class FlowScheduler {

    @VisibleForTesting
    internal lateinit var device: DeviceState

    lateinit var singleListener: SingleListener
    private val semaphore: Semaphore = Semaphore(1)

    suspend fun scheduler() {

        val singleFlow: Flow<AdminCommand> = callbackFlow {

            singleListener = object : SingleListener {
                override suspend fun onMessageReceived(adminCommand: AdminCommand) {
                    send(adminCommand)
                }

                override suspend fun onDataChange(adminCommand: AdminCommand) {
                    send(adminCommand)
                }
            }

            awaitClose()
        }.distinctUntilChanged { old, new ->
            if (old == new) {
                redundantCommand()
                true
            } else {
                false
            }
        }

        singleFlow.collect { incomingCommand ->

            when (isDeviceAlreadyInCommandedState(incomingCommand)) {
                true -> {
                    redundantCommand()
                }
                false -> {
                    scheduleCommand(incomingCommand)
                }
            }
        }
    }

    @VisibleForTesting
    internal suspend fun scheduleCommand(incomingCommand: AdminCommand) {
        print("Command scheduled\n")
        executeCommand(incomingCommand)
    }

    private suspend fun executeCommand(incomingCommand: AdminCommand) {
        try {
            semaphore.acquire()
            when (incomingCommand) {
                AdminCommand.KIOSK_LOCK -> processKioskLock()
                AdminCommand.KIOSK_UNLOCK -> processKioskUnlock()
                AdminCommand.ADMIN_LOCK -> processAdminLock()
                AdminCommand.ADMIN_UNLOCK -> processAdminUnlock()
                AdminCommand.CLEAR_KIOSK_PASSWORD -> clearKioskPassword()
                AdminCommand.REBOOT -> device.rebootState = reboot()
                AdminCommand.WIPE_DATA -> device.wipeDataState = wipeData()
                AdminCommand.UNINSTALL -> device.uninstallState = uninstallWeGuard()
            }
        } finally {
            semaphore.release()
        }
    }

    @VisibleForTesting
    internal fun redundantCommand() {
        print("Redundant command\n")
    }

    private suspend fun isDeviceAlreadyInCommandedState(incomingCommand: AdminCommand): Boolean {
        try {
            semaphore.acquire()
            return deviceState(incomingCommand)
        } finally {
            semaphore.release()
        }
    }

    private fun deviceState(incomingCommand: AdminCommand): Boolean =
        when (incomingCommand) {
            AdminCommand.KIOSK_LOCK -> device.kioskLockState == KioskLockState.Locked
            AdminCommand.KIOSK_UNLOCK -> device.kioskLockState == KioskLockState.Unlocked
            AdminCommand.ADMIN_LOCK -> device.adminLockState == AdminLockState.Locked
            AdminCommand.ADMIN_UNLOCK -> device.adminLockState == AdminLockState.Unlocked
            AdminCommand.CLEAR_KIOSK_PASSWORD -> device.clearKioskPasswordState == ClearKioskPasswordState.CommandIssued
            AdminCommand.REBOOT -> device.rebootState == RebootState.CommandIssued
            AdminCommand.WIPE_DATA -> device.wipeDataState == WipeDataState.CommandIssued
            AdminCommand.UNINSTALL -> device.uninstallState == UninstallState.CommandIssued
        }

    @VisibleForTesting
    internal fun processKioskLock() {
        device.kioskLockState = KioskLockState.Locked
    }

    @VisibleForTesting
    internal fun processKioskUnlock() {
        device.kioskLockState = KioskLockState.Unlocked
    }

    @VisibleForTesting
    internal fun processAdminLock() {
        device.adminLockState = AdminLockState.Locked
    }

    @VisibleForTesting
    internal fun processAdminUnlock() {
        device.adminLockState = AdminLockState.Unlocked
    }

    @VisibleForTesting
    internal fun clearKioskPassword() {
        device.clearKioskPasswordState = ClearKioskPasswordState.CommandIssued
    }

    @VisibleForTesting
    internal fun uninstallWeGuard() = UninstallState.CommandIssued

    @VisibleForTesting
    internal fun wipeData() = WipeDataState.CommandIssued

    @VisibleForTesting
    internal fun reboot() = RebootState.CommandIssued

}

interface SingleListener {
    suspend fun onMessageReceived(adminCommand: AdminCommand)
    suspend fun onDataChange(adminCommand: AdminCommand)
}