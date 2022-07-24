package com.example.commandsqueue

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

sealed class AdminCommand {
    object Lock : AdminCommand()
    object Unlock : AdminCommand()
}

class FlowRnD {
    suspend fun executeCommand(flow: Flow<AdminCommand>) {
        flow.collect {
            executeLock()
        }
    }

    fun executeUnlock() {
        val x =1
    }

    fun executeLock() {
        val x = 1

    }
}

class A {
    suspend fun emit(flow: FlowCollector<AdminCommand>) {
        flow.emit(AdminCommand.Lock)
    }
}

class B {
    suspend fun emit(flow: FlowCollector<AdminCommand>) {
        flow.emit(AdminCommand.Lock)
    }
}

