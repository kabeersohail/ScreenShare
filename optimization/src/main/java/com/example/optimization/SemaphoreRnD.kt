package com.example.optimization

import com.example.optimization.models.AdminLockStatus
import com.example.optimization.models.DeviceLockStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore

class SemaphoreRnD {

    private val semaphore: Semaphore = Semaphore(1)
    private var deviceLockStatus: DeviceLockStatus = DeviceLockStatus.UNLOCKED
    private var adminLockStatus: AdminLockStatus = AdminLockStatus.UNLOCKED

    suspend fun doLock(){
        semaphore.acquire()
        try {
            if(deviceLockStatus != DeviceLockStatus.LOCKED){
                delay(3000L)
                deviceLockStatus = DeviceLockStatus.LOCKED
            }
        } finally {
            semaphore.release()
        }
    }

    suspend fun doUnlock() {
        semaphore.acquire()
        try {
            if(deviceLockStatus != DeviceLockStatus.UNLOCKED){
                delay(3000L)
                deviceLockStatus = DeviceLockStatus.UNLOCKED
            }
        } finally {
            semaphore.release()
        }
    }

    suspend fun doAdminLock() {
        semaphore.acquire()
        try {
            if(adminLockStatus != AdminLockStatus.LOCKED){
                delay(3000L)
                adminLockStatus = AdminLockStatus.LOCKED
            }
        } finally {
            semaphore.release()
        }
    }

    suspend fun doAdminUnlock(){
        semaphore.acquire()
        try {
            if(adminLockStatus != AdminLockStatus.UNLOCKED){
                delay(3000L)
                adminLockStatus = AdminLockStatus.UNLOCKED
            }
        } finally {
            semaphore.release()
        }
    }

    fun getLockStatus() = deviceLockStatus
    fun getAdminLockStatus() = adminLockStatus

}