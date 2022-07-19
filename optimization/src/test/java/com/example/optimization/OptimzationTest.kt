package com.example.optimization

import com.example.optimization.models.AdminCommand
import com.example.optimization.models.DeviceLockStatus
import org.junit.Assert
import org.junit.Before
import org.junit.Test


internal class OptimzationTest{

    private lateinit var optimzation: Optimzation

    @Before
    fun setup(){
        optimzation = Optimzation()
    }

    @Test
    fun `when admin command is lock and device is locked then should return false`() {
        val response = optimzation.shouldExecute(AdminCommand.LOCK, DeviceLockStatus.LOCKED)
        Assert.assertEquals(response, false)
    }

    @Test
    fun `when admin command is unlock and device is unlocked then should return false`() {
        val response = optimzation.shouldExecute(AdminCommand.UNLOCK, DeviceLockStatus.UNLOCKED)
        Assert.assertEquals(response, false)
    }

    @Test
    fun `when admin command is lock and device is unlocked then should return true`() {
        val response = optimzation.shouldExecute(AdminCommand.LOCK, DeviceLockStatus.UNLOCKED)
        Assert.assertEquals(response, true)
    }

    @Test
    fun `when admin command is unlock and device is locked then should return true`() {
        val response = optimzation.shouldExecute(AdminCommand.UNLOCK, DeviceLockStatus.LOCKED)
        Assert.assertEquals(response, true)
    }

}