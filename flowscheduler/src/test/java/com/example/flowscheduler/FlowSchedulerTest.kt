package com.example.flowscheduler

import com.example.flowscheduler.models.AdminCommand
import com.example.flowscheduler.states.DeviceState
import com.example.flowscheduler.states.KioskLockState
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test


class FlowSchedulerTest {

    private lateinit var flowScheduler: FlowScheduler

    @Before
    fun setup() {
        flowScheduler = spyk(FlowScheduler())
    }

    @Test
    fun `when device is already in commanded state, then redundant method must be called only once`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Locked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                verify(exactly = 1) { redundantCommand() }
            }
        }

    @Test
    fun `when device is already in commanded state, then schedule method must not be called`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Locked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                coVerify(exactly = 0) { scheduleCommand(any()) }
            }
        }

    @Test
    fun `when device is already in commanded state and command is received multiple times, then redundant method must be called all the time`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Locked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)

                verify(exactly = 4) { redundantCommand() }
            }
        }

    @Test
    fun `when device is already in commanded state and command is received multiple times, then schedule method must not be called`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Locked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)

                coVerify(exactly = 0) { scheduleCommand(any()) }
            }
        }

    @Test
    fun `when device is not in commanded state, then schedule method must be called only once`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Unlocked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                coVerify(exactly = 1) { scheduleCommand(any()) }
            }
        }

    @Test
    fun `when device is not in commanded state, then redundant method must not be called`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Unlocked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                coVerify(exactly = 0) { redundantCommand() }
            }
        }

    @Test
    fun `when device is not in commanded state, and command is received multiple times then schedule method must be called only once`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Unlocked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                coVerify(exactly = 1) { scheduleCommand(any()) }
            }
        }

    @Test
    fun `when device is not in commanded state and command is received multiple times, then schedule method must be called for the first time and redundant command must be called for the rest`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Unlocked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_LOCK)
                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                coVerify(exactly = 1) { scheduleCommand(any()) }
                verify(exactly = 2) { redundantCommand() }

                coVerifyOrder {
                    scheduleCommand(AdminCommand.KIOSK_LOCK)
                    redundantCommand()
                    redundantCommand()
                }

            }
        }

    @Test
    fun `when different commands come, then schedule command must be called all the time`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Unlocked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_UNLOCK)
                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                coVerify(exactly = 3) { scheduleCommand(any()) }
            }
        }

    @Test
    fun `when different commands come, then redundant command must not be called`(): Unit =
        runBlocking {
            flowScheduler.apply {
                device = DeviceState()
                device.kioskLockState = KioskLockState.Unlocked
                flowScheduler.scheduler()

                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)
                singleListener.onMessageReceived(AdminCommand.KIOSK_UNLOCK)
                singleListener.onDataChange(AdminCommand.KIOSK_LOCK)

                verify(exactly = 0) { redundantCommand() }
            }
        }

}