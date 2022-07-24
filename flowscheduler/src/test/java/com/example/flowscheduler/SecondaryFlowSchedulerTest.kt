package com.example.flowscheduler

import com.example.flowscheduler.models.AdminCommand
import com.example.flowscheduler.states.AdminLockState
import com.example.flowscheduler.states.DeviceState
import com.example.flowscheduler.states.KioskLockState
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SecondaryFlowSchedulerTest {

    private lateinit var flowScheduler: FlowScheduler

    @Before
    fun setup(){
        flowScheduler = spyk(FlowScheduler())
    }

    @Test
    fun `when device is already in commanded state, then redundant method must be called only once`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Locked
        flowScheduler.scheduler()

        // When

        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

        // Then

        verify(exactly = 1) { flowScheduler.redundantCommand() }

    }

    @Test
    fun `when device is already in commanded state, then schedule method must not be called`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Locked
        flowScheduler.scheduler()

        // When

        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

        // Then

        coVerify(exactly = 0) { flowScheduler.scheduleCommand(any()) }

    }

    @Test
    fun `when device is already in commanded state and command is received multiple times, then redundant method must be called all the time`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Locked
        flowScheduler.scheduler()

        // When

        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)

        // Then

        verify(exactly = 4) { flowScheduler.redundantCommand() }

    }

    @Test
    fun `when device is already in commanded state and command is received multiple times, then schedule method must not be called`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Locked
        flowScheduler.scheduler()

        // When

        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)

        // Then

        coVerify(exactly = 0) { flowScheduler.scheduleCommand(any()) }

    }

    @Test
    fun `when device is not in commanded state, then schedule method must be called only once`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.scheduler()

        // When
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

        // Then
        coVerify(exactly = 1) { flowScheduler.scheduleCommand(any()) }

    }

    @Test
    fun `when device is not in commanded state, then redundant method must not be called`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.scheduler()

        // When
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

        // Then
        coVerify(exactly = 0) { flowScheduler.redundantCommand() }

    }


    @Test
    fun `when device is not in commanded state and command is received multiple times, then schedule method must be called only once`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.scheduler()

        // When
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

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
    fun `when device is not in commanded state and command is received multiple times, then schedule method must be called for the first time and redundant command must be called for the rest`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.scheduler()

        // When
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)

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
    fun `when distinct consecutive commands are received, schedule method must be called all the time`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.device.kioskLockState = KioskLockState.Locked
        flowScheduler.scheduler()

        // When
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.KIOSK_UNLOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_UNLOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.UNINSTALL)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.WIPE_DATA)

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
    fun `when distinct consecutive commands are received, redundant method must not be called`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.device.kioskLockState = KioskLockState.Locked
        flowScheduler.scheduler()

        // When
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.KIOSK_UNLOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_UNLOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.UNINSTALL)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.WIPE_DATA)

        // Then
        coVerify(exactly = 0) { flowScheduler.redundantCommand() }
    }

    @Test
    fun `when distinct consecutive commands are received, then commands must be executed in the order of arrival`() = runBlocking {

        // Given
        flowScheduler.device = DeviceState()
        flowScheduler.device.adminLockState = AdminLockState.Unlocked
        flowScheduler.device.kioskLockState = KioskLockState.Unlocked
        flowScheduler.scheduler()

        // When
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_LOCK)
        flowScheduler.singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.ADMIN_UNLOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.KIOSK_UNLOCK)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.UNINSTALL)
        flowScheduler.singleListener.onMessageReceived(AdminCommand.WIPE_DATA)

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