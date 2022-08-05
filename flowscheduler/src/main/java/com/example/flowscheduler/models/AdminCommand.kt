package com.example.flowscheduler.models

enum class AdminCommand {
    KIOSK_LOCK,
    KIOSK_UNLOCK,
    ADMIN_LOCK,
    ADMIN_UNLOCK,
    CLEAR_KIOSK_PASSWORD,
    REBOOT,
    WIPE_DATA,
    UNINSTALL
}

data class Command(val commandID: Long, val command: AdminCommand)

enum class Reason {
    DEVICE_ALREADY_IN_COMMANDED_STATE,
    REDUNDANT_COMMAND_FROM_DIFFERENT_CHANNEL,

    /**
     * Will be added in later build
     */
    COMMAND_EXPIRED
}