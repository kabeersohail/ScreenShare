package com.example.commandsqueue

import com.example.commandsqueue.models.AdminCommands
import com.example.commandsqueue.models.DeviceStatus
import com.example.commandsqueue.utils.getUniqueConsecutiveCommands
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CommandQueueTest {
    private lateinit var commandQueue: CommandQueue

    @Before
    fun setup() {
        commandQueue = CommandQueue()
    }

    @Test
    fun `test to check if  getting unique consecutive commands as expected`() = runBlocking {
        val expectedCommandOrder = listOf(
            AdminCommands.ADMIN_LOCK,
            AdminCommands.ADMIN_UNLOCK,
            AdminCommands.KIOSK_LOCK,
            AdminCommands.KIOSK_UNLOCK,
            AdminCommands.CLEAR_KIOSK_PASSWORD,
            AdminCommands.KIOSK_LOCK,
            AdminCommands.ADMIN_UNLOCK,
            AdminCommands.CLEAR_KIOSK_PASSWORD,
            AdminCommands.REBOOT,
            AdminCommands.WIPE_DATA,
            AdminCommands.UNINSTALL
        )
        Assert.assertEquals(expectedCommandOrder, getUniqueConsecutiveCommands(commandQueue.adminClickEvents))
    }

    @Test
    fun `check schedule method`() = runBlocking {
        commandQueue.scheduleCommandsSequentially()

        Assert.assertEquals(true, commandQueue.uninstall)
        Assert.assertEquals(true, commandQueue.wipeData)
        Assert.assertEquals(true, commandQueue.rebootStatus)
        Assert.assertEquals(DeviceStatus.PASSWORD_CLEARED, commandQueue.passwordStatus)
        Assert.assertEquals(DeviceStatus.ADMIN_UNLOCKED, commandQueue.adminLockStatus)
        Assert.assertEquals(DeviceStatus.KIOSK_LOCKED, commandQueue.kioskLockStatus)
    }
}