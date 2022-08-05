package com.example.flowscheduler

import com.example.flowscheduler.models.AdminCommand
import com.example.flowscheduler.models.Command
import com.example.flowscheduler.models.Reason
import com.example.flowscheduler.states.AdminLockState
import com.example.flowscheduler.states.DeviceState
import com.example.flowscheduler.states.KioskLockState
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SecondaryFlowSchedulerTest {

    private lateinit var flowScheduler: FlowScheduler

    @Before
    fun setup() {
        flowScheduler = spyk(FlowScheduler())
    }

    @After
    fun tearDown() {
        flowScheduler.commandHistory.clear()
    }

    @Test
    fun `when device is already in commanded state, then redundant method must be called only once`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Locked

            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            val command = Command(commandID = 1000, command = AdminCommand.ADMIN_LOCK)

            flowScheduler.singleListener.onMessageReceived(command)
            advanceUntilIdle()
            job.cancel()

            // Then
            verify(exactly = 1) { flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE) }

        }

    @Test
    fun `when device is already in commanded state, then schedule method must not be called`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Locked

            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            val command = Command(commandID = 1000, command = AdminCommand.ADMIN_LOCK)

            flowScheduler.singleListener.onMessageReceived(command)

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 0) { flowScheduler.scheduleCommand(any()) }

        }

    @Test
    fun `when device is already in commanded state and command is received multiple times, then redundant method must be called all the time`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Locked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(1111, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1234, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(9090, AdminCommand.ADMIN_LOCK))

            advanceUntilIdle()
            job.cancel()

            // Then
            verify(exactly = 4) { flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE) }

        }

    @Test
    fun `when device is already in commanded state and command is received multiple times, then schedule method must not be called`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Locked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1111, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(2222, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onMessageReceived(Command(3333, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(4444, AdminCommand.ADMIN_LOCK))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 0) { flowScheduler.scheduleCommand(any()) }

        }

    @Test
    fun `when device is not in commanded state, then schedule method must be called only once`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Unlocked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }

        }

    @Test
    fun `when device is not in commanded state, then redundant method must not be called`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Unlocked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 0) { flowScheduler.commandNotExecuted(any()) }

        }


    @Test
    fun `when device is not in commanded state and command is received multiple times, then schedule method must be called only once`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Unlocked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }
            coVerify(exactly = 2) { flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE) }

            coVerifyOrder {
                flowScheduler.scheduleCommand(any())
                flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE)
                flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE)
            }
        }

    @Test
    fun `when device is not in commanded state and command is received multiple times, then schedule method must be called for the first time and redundant command must be called for the rest`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Unlocked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }
            coVerify(exactly = 2) { flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE) }

            coVerifyOrder {
                flowScheduler.scheduleCommand(any())
                flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE)
                flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE)
            }
        }

    @Test
    fun `when distinct consecutive commands are received, schedule method must be called all the time`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Unlocked
            flowScheduler.device.kioskLockState = KioskLockState.Locked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.KIOSK_UNLOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_UNLOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.KIOSK_LOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.UNINSTALL))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.WIPE_DATA))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 6) { flowScheduler.scheduleCommand(any()) }
            coVerify(exactly = 0) { flowScheduler.commandNotExecuted(any()) }

            coVerifyOrder {
                flowScheduler.scheduleCommand(any())
                flowScheduler.scheduleCommand(any())
                flowScheduler.scheduleCommand(any())
                flowScheduler.scheduleCommand(any())
                flowScheduler.scheduleCommand(any())
                flowScheduler.scheduleCommand(any())
            }
        }

    @Test
    fun `when distinct consecutive commands are received, redundant method must not be called`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Unlocked
            flowScheduler.device.kioskLockState = KioskLockState.Locked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.KIOSK_UNLOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_UNLOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.KIOSK_LOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.UNINSTALL))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.WIPE_DATA))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 0) { flowScheduler.commandNotExecuted(any()) }
        }

    @Test
    fun `when distinct consecutive commands are received, then commands must be executed in the order of arrival`() =
        runTest {

            // Given
            flowScheduler.device = DeviceState()
            flowScheduler.device.adminLockState = AdminLockState.Unlocked
            flowScheduler.device.kioskLockState = KioskLockState.Unlocked
            val job: Job = launch {
                flowScheduler.scheduler()
            }

            advanceUntilIdle()

            // When
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
            flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.KIOSK_LOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_UNLOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.KIOSK_UNLOCK))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.UNINSTALL))
            flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.WIPE_DATA))

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerifyOrder {
                flowScheduler.processAdminLock()
                flowScheduler.processKioskLock()
                flowScheduler.processAdminUnlock()
                flowScheduler.processKioskUnlock()
                flowScheduler.uninstallWeGuard()
                flowScheduler.wipeData()
            }
        }

    @Test
    fun `new fail case`() = runTest {
        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked

        val job: Job = launch {
            flowScheduler.scheduler()
        }

        advanceUntilIdle()

        // When
        flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
        flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.ADMIN_LOCK))
        advanceUntilIdle()

        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.ADMIN_LOCK))

        advanceUntilIdle()
        job.cancel()

        coVerify(exactly = 2) { flowScheduler.scheduleCommand(any()) }
        coVerify(exactly = 1) { flowScheduler.commandNotExecuted(Reason.REDUNDANT_COMMAND_FROM_DIFFERENT_CHANNEL) }

        coVerifyOrder {
            flowScheduler.scheduleCommand(any())
            flowScheduler.commandNotExecuted(Reason.REDUNDANT_COMMAND_FROM_DIFFERENT_CHANNEL)
            flowScheduler.scheduleCommand(any())
        }
    }

    @Test
    fun `when command with same id is received from different channels, then schedule method must be called exactly once and redundant must be called exactly once`() = runTest {
        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked

        val job: Job = launch {
            flowScheduler.scheduler()
        }

        advanceUntilIdle()

        // When
        flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
        flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.ADMIN_LOCK))
        advanceUntilIdle()

        job.cancel()

        coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }
        coVerify(exactly = 1) { flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE) }

        coVerifyOrder {
            flowScheduler.scheduleCommand(any())
            flowScheduler.commandNotExecuted(Reason.DEVICE_ALREADY_IN_COMMANDED_STATE)
        }
    }

    @Test
    fun `change scenario 1`() = runTest {
        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.kioskLockState = KioskLockState.Locked

        val job: Job = launch {
            flowScheduler.scheduler()
        }

        advanceUntilIdle()

        // When

        // We receive admin command to unlock kiosk mode from RTDB channel
        flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.KIOSK_UNLOCK))
        advanceUntilIdle()

        coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }

        // Some scenario caused kiosk to lock
        flowScheduler.device.kioskLockState = KioskLockState.Locked

        Assert.assertEquals(KioskLockState.Locked, flowScheduler.device.kioskLockState)

        // After one hour we receive the previous admin command from FCM channel
        flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.KIOSK_UNLOCK))
        advanceUntilIdle()

        job.cancel()


        coVerify(exactly = 1) { flowScheduler.commandNotExecuted(Reason.REDUNDANT_COMMAND_FROM_DIFFERENT_CHANNEL) }

        coVerifyOrder {
            flowScheduler.scheduleCommand(any())
            flowScheduler.commandNotExecuted(Reason.REDUNDANT_COMMAND_FROM_DIFFERENT_CHANNEL)
        }
    }

    @Test
    fun `change scenario 2`() = runTest {
        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.kioskLockState = KioskLockState.Locked
        flowScheduler.device.adminLockState = AdminLockState.Unlocked

        val job: Job = launch {
            flowScheduler.scheduler()
        }

        advanceUntilIdle()

        // When

        // We receive admin command to unlock kiosk mode from RTDB channel
        flowScheduler.singleListener.onDataChange(Command(1000, AdminCommand.KIOSK_UNLOCK))
        advanceUntilIdle()

        // Some scenario caused kiosk to lock
        flowScheduler.device.kioskLockState = KioskLockState.Locked

        Assert.assertEquals(KioskLockState.Locked, flowScheduler.device.kioskLockState)

        // After one hour we receive the previous admin command from FCM channel
        flowScheduler.singleListener.onMessageReceived(Command(1000, AdminCommand.ADMIN_LOCK))
        advanceUntilIdle()

        job.cancel()


        coVerify(exactly = 2) { flowScheduler.scheduleCommand(any()) }

        coVerifyOrder {
            flowScheduler.scheduleCommand(any())
            flowScheduler.scheduleCommand(any())
        }
    }

}