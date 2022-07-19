package com.example.commandsqueue

import com.example.commandsqueue.models.AdminCommands
import com.example.commandsqueue.models.CommandStatus
import com.example.commandsqueue.models.DeviceStatus
import com.example.commandsqueue.utils.getUniqueConsecutiveCommands
import kotlinx.coroutines.delay
import java.lang.Exception

class CommandQueue {

    val adminClickEvents = mutableListOf(
        AdminCommands.ADMIN_LOCK,
        AdminCommands.ADMIN_LOCK,
        AdminCommands.ADMIN_LOCK,
        AdminCommands.ADMIN_UNLOCK,
        AdminCommands.ADMIN_UNLOCK,
        AdminCommands.KIOSK_LOCK,
        AdminCommands.KIOSK_UNLOCK,
        AdminCommands.KIOSK_UNLOCK,
        AdminCommands.KIOSK_UNLOCK,
        AdminCommands.CLEAR_KIOSK_PASSWORD,
        AdminCommands.KIOSK_LOCK,
        AdminCommands.ADMIN_UNLOCK,
        AdminCommands.CLEAR_KIOSK_PASSWORD,
        AdminCommands.REBOOT,
        AdminCommands.WIPE_DATA,
        AdminCommands.UNINSTALL
    )

    var kioskLockStatus = DeviceStatus.KIOSK_LOCKED
    var adminLockStatus = DeviceStatus.ADMIN_LOCKED
    var passwordStatus = DeviceStatus.PASSWORD_NOT_CLEARED
    var rebootStatus: Boolean = false
    var wipeData: Boolean = false
    var uninstall: Boolean = false
    private lateinit var commands: MutableList<AdminCommands>

    suspend fun scheduleCommandsSequentially() {
        commands = getUniqueConsecutiveCommands(adminClickEvents)
        executeCommands(commands)
    }

    private suspend fun executeCommands(commands: MutableList<AdminCommands>) {

        if (commands.isNotEmpty()) {
            when (obeyAdminCommand(commands.first())) {
                is CommandStatus.Failure -> {
                    commands.removeFirst()
                    executeCommands(commands)
                }

                CommandStatus.Success -> {
                    commands.removeFirst()
                    executeCommands(commands)
                }
            }
        } else {
            // All commands are executed
        }
    }

    private suspend fun obeyAdminCommand(command: AdminCommands): CommandStatus {
        return when (command) {
            AdminCommands.KIOSK_LOCK -> processKioskLock()
            AdminCommands.KIOSK_UNLOCK -> processKioskUnlock()
            AdminCommands.ADMIN_LOCK -> processAdminLock()
            AdminCommands.ADMIN_UNLOCK -> processAdminUnlock()
            AdminCommands.CLEAR_KIOSK_PASSWORD -> processClearPassword()
            AdminCommands.REBOOT -> processReboot()
            AdminCommands.WIPE_DATA -> processWipeData()
            AdminCommands.UNINSTALL -> processUninstall()
        }
    }

    private fun processUninstall(): CommandStatus {
        uninstall = true
        try {
            val listOnTheGo = mutableListOf(AdminCommands.ADMIN_LOCK,
                AdminCommands.ADMIN_LOCK,
                AdminCommands.ADMIN_UNLOCK,
                AdminCommands.ADMIN_UNLOCK)
            val uniqueConsecutiveCommands = getUniqueConsecutiveCommands(listOnTheGo)
            commands.addAll(uniqueConsecutiveCommands)
        } catch (e: Exception) {
            throw Exception(e)
        }
        return CommandStatus.Success
    }

    private fun processWipeData(): CommandStatus {
        wipeData = true
        return CommandStatus.Success
    }

    private fun processReboot(): CommandStatus {
        rebootStatus = true
        return CommandStatus.Success
    }

    private fun processClearPassword(): CommandStatus {
        passwordStatus = DeviceStatus.PASSWORD_CLEARED
        return CommandStatus.Success
    }

    private suspend fun processAdminLock(): CommandStatus {
        return if (adminLockStatus != DeviceStatus.ADMIN_LOCKED) {
            delay(2000L)
            adminLockStatus = DeviceStatus.ADMIN_LOCKED
            CommandStatus.Success
        } else {
            CommandStatus.Failure("Already locked")
        }
    }

    private suspend fun processAdminUnlock(): CommandStatus {
        return if (adminLockStatus != DeviceStatus.ADMIN_UNLOCKED) {
            delay(1000L)
            adminLockStatus = DeviceStatus.ADMIN_UNLOCKED
            CommandStatus.Success
        } else {
            CommandStatus.Failure("Already unlocked")
        }
    }

    private suspend fun processKioskLock(): CommandStatus {
        return if (kioskLockStatus != DeviceStatus.KIOSK_LOCKED) {
            delay(1000L)
            kioskLockStatus = DeviceStatus.KIOSK_LOCKED
            CommandStatus.Success
        } else {
            CommandStatus.Failure("Already locked")
        }
    }

    private suspend fun processKioskUnlock(): CommandStatus {
        return if (kioskLockStatus != DeviceStatus.KIOSK_UNLOCKED) {
            delay(1000L)
            kioskLockStatus = DeviceStatus.KIOSK_UNLOCKED
            CommandStatus.Success
        } else {
            CommandStatus.Failure("Already unlocked")
        }
    }
}