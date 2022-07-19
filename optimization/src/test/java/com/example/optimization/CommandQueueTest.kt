package com.example.optimization

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
        Assert.assertEquals(expectedCommandOrder, commandQueue.getUniqueConsecutiveCommands(commandQueue.adminClickEvents))
    }

    @Test
    fun `check schedule method`() = runBlocking {
        commandQueue.scheduleCommandsSequentially()
    }

}