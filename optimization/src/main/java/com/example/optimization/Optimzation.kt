package com.example.optimization

import com.example.optimization.models.AdminCommand
import com.example.optimization.models.DeviceLockStatus

class Optimzation {

    fun shouldExecute(adminCommand: AdminCommand, deviceLockStatus: DeviceLockStatus) = adminCommand.ordinal xor deviceLockStatus.ordinal == 1

}