package com.example.commandsqueue.models

import java.lang.Exception

sealed class CommandStatus{
    object Success: CommandStatus()
    class Failure(val exception: String): CommandStatus()
}
