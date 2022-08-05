package com.example.flowscheduler

import androidx.annotation.VisibleForTesting
import com.example.flowscheduler.models.AdminCommand
import com.example.flowscheduler.models.Command
import com.example.flowscheduler.models.Reason
import com.example.flowscheduler.states.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Semaphore

class FlowScheduler {

    @VisibleForTesting
    internal lateinit var device: DeviceState

    lateinit var singleListener: SingleListener
    private val semaphore: Semaphore = Semaphore(1)

    @VisibleForTesting
    internal val commandHistory: MutableMap<Long, AdminCommand> = mutableMapOf()
    
    suspend fun scheduler() {

        val singleFlow: Flow<Command> = callbackFlow {

            singleListener = object : SingleListener {
                override suspend fun onMessageReceived(command: Command) {
                    send(command)
                }

                override suspend fun onDataChange(command: Command) {
                    send(command)
                }
            }

            awaitClose()
        }.distinctUntilChanged { _, new ->
            return@distinctUntilChanged when(deviceState(new.command)) {
                true -> {
                    commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE)
                    true
                }
                false -> false
            }
        }

        singleFlow.collect { incomingCommand ->

            when (isDeviceAlreadyInCommandedState(incomingCommand)) {
                true -> {
                    commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE)
                }
                false -> {

                    when(commandHistory.isEmpty()) {
                        true -> {
                            scheduleCommand(incomingCommand.command)
                            commandHistory[incomingCommand.commandID] = incomingCommand.command
                        }
                        false -> checkIdAndSchedule(incomingCommand)
                    }
                }
            }
        }
    }

    private suspend fun checkIdAndSchedule(incomingCommand: Command) = when(val command: AdminCommand? = commandHistory[incomingCommand.commandID]) {
        null -> {
            scheduleCommand(incomingCommand.command)
            commandHistory[incomingCommand.commandID] = incomingCommand.command
        }
        else -> if(command == incomingCommand.command) commandNotExecuted(Reason.REDUNDANT_COMMAND_FROM_DIFFERENT_CHANNEL) else {
            scheduleCommand(incomingCommand.command)
            commandHistory[incomingCommand.commandID] = incomingCommand.command
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
    internal fun commandNotExecuted(reason: Reason) {
        print("Redundant command $reason \n")
    }

    private suspend fun isDeviceAlreadyInCommandedState(incomingCommand: Command): Boolean {
        try {
            semaphore.acquire()
            return deviceState(incomingCommand.command)
        } finally {
            semaphore.release()
        }
    }

    /**
     * Returns true if device is already in commanded state
     *
     * @param incomingCommand [AdminCommand]
     * @return [Boolean]
     */
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
    suspend fun onMessageReceived(command: Command)
    suspend fun onDataChange(command: Command)
}