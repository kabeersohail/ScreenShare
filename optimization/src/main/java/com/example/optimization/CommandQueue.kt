package com.example.optimization

import kotlinx.coroutines.delay

enum class AdminCommands {
    KIOSK_LOCK,
    KIOSK_UNLOCK,
    ADMIN_LOCK,
    ADMIN_UNLOCK,
    CLEAR_KIOSK_PASSWORD,
    REBOOT,
    WIPE_DATA,
    UNINSTALL
}

enum class DeviceStatus{
    KIOSK_LOCKED,
    KIOSK_UNLOCKED,
    ADMIN_LOCKED,
    ADMIN_UNLOCKED,
    PASSWORD_NOT_CLEARED,
    PASSWORD_CLEARED
}

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

    private var kioskLockStatus = DeviceStatus.KIOSK_LOCKED
    var adminLockStatus = DeviceStatus.ADMIN_LOCKED
    var passwordStatus = DeviceStatus.PASSWORD_NOT_CLEARED
    var rebootStatus: Boolean = false
    private var wipeData: Boolean = false
    private var uninstall: Boolean = false

    fun getUniqueConsecutiveCommands(adminCommands: MutableList<AdminCommands>): MutableList<AdminCommands> {
        val length = adminCommands.size
        if (length < 2) return adminCommands
        var currentDistinctCommand = 0

        for (i in 1 until length) {
            if (adminCommands[currentDistinctCommand] != adminCommands[i]) {
                currentDistinctCommand++
                adminCommands[currentDistinctCommand] = adminCommands[i]
            }
        }

        return adminCommands.subList(0, currentDistinctCommand + 1)
    }

    suspend fun scheduleCommandsSequentially(){
        val commands: MutableList<AdminCommands> = getUniqueConsecutiveCommands(adminClickEvents)

        while (commands.size != 0){
            obeyAdminCommand(commands[0])
        }

        for (command in commands) {
            obeyAdminCommand(command)
        }
    }

    private suspend fun obeyAdminCommand(command: AdminCommands) {
        when(command){
            AdminCommands.KIOSK_LOCK -> processDeviceLock()
            AdminCommands.KIOSK_UNLOCK -> processDeviceUnlock()
            AdminCommands.ADMIN_LOCK -> adminLockStatus = DeviceStatus.ADMIN_LOCKED
            AdminCommands.ADMIN_UNLOCK -> adminLockStatus = DeviceStatus.ADMIN_UNLOCKED
            AdminCommands.CLEAR_KIOSK_PASSWORD -> passwordStatus = DeviceStatus.PASSWORD_CLEARED
            AdminCommands.REBOOT -> rebootStatus = true
            AdminCommands.WIPE_DATA -> wipeData = true
            AdminCommands.UNINSTALL -> uninstall = true
        }
    }

    private suspend fun processDeviceLock() {
        if(kioskLockStatus != DeviceStatus.KIOSK_LOCKED){
            delay(5000L)
            kioskLockStatus = DeviceStatus.KIOSK_LOCKED
        }
    }

    private suspend fun processDeviceUnlock() {
        if(kioskLockStatus != DeviceStatus.KIOSK_UNLOCKED){
            delay(5000L)
            kioskLockStatus = DeviceStatus.KIOSK_UNLOCKED
        }
    }

}