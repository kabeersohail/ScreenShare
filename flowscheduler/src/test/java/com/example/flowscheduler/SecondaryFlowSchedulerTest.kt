package com.example.flowscheduler

import com.example.flowscheduler.models.AdminCommand
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SecondaryFlowSchedulerTest {

    private lateinit var flowScheduler: FlowScheduler

    @Before
    fun setup() {
        flowScheduler = spyk(FlowScheduler())
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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            advanceUntilIdle()
            job.cancel()

            // Then
            verify(exactly = 1) { flowScheduler.redundantCommand() }

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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)

            advanceUntilIdle()
            job.cancel()

            // Then
            verify(exactly = 4) { flowScheduler.redundantCommand() }

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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)

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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 0) { flowScheduler.redundantCommand() }

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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }
            coVerify(exactly = 2) { flowScheduler.redundantCommand() }

            coVerifyOrder {
                flowScheduler.scheduleCommand(any())
                flowScheduler.redundantCommand()
                flowScheduler.redundantCommand()
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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }
            coVerify(exactly = 2) { flowScheduler.redundantCommand() }

            coVerifyOrder {
                flowScheduler.scheduleCommand(any())
                flowScheduler.redundantCommand()
                flowScheduler.redundantCommand()
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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.KIOSK_UNLOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_UNLOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.UNINSTALL)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.WIPE_DATA)

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 6) { flowScheduler.scheduleCommand(any()) }
            coVerify(exactly = 0) { flowScheduler.redundantCommand() }

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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.KIOSK_UNLOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_UNLOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.UNINSTALL)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.WIPE_DATA)

            advanceUntilIdle()
            job.cancel()

            // Then
            coVerify(exactly = 0) { flowScheduler.redundantCommand() }
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
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
            flowScheduler.singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_UNLOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.KIOSK_UNLOCK)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.UNINSTALL)
            flowScheduler.singleListener.onMessageReceived(AdminCommand.WIPE_DATA)

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

}