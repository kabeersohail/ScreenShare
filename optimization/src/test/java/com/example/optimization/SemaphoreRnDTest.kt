package com.example.optimization

import com.example.optimization.models.AdminLockStatus
import com.example.optimization.models.DeviceLockStatus
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class SemaphoreRnDTest{

    lateinit var semaphoreRnD: SemaphoreRnD

    @Before
    fun setup(){
        semaphoreRnD = SemaphoreRnD()
    }

    @Test
    fun `when two coroutines try to lock simultaneously`()  = runBlocking{
        listOf(
            launch { semaphoreRnD.doLock() },
            launch { semaphoreRnD.doUnlock() },
            launch { semaphoreRnD.doUnlock() },
            launch { semaphoreRnD.doAdminLock() },
            launch { semaphoreRnD.doLock() },
            launch { semaphoreRnD.doAdminUnlock() }
        ).joinAll()

        Assert.assertEquals(DeviceLockStatus.LOCKED, semaphoreRnD.getLockStatus())
        Assert.assertEquals(AdminLockStatus.UNLOCKED, semaphoreRnD.getAdminLockStatus())
    }

}