package com.example.commandsqueue.utils

import com.example.commandsqueue.models.AdminCommands

fun getUniqueConsecutiveCommands(adminCommands: MutableList<AdminCommands>): MutableList<AdminCommands> {
    val length = adminCommands.size
    if (length < 2) return adminCommands
    var currentDistinctCommand = 0

    for (i in 1 until length) {
        if (adminCommands[currentDistinctCommand] != adminCommands[i]) {
            currentDistinctCommand++
            adminCommands[currentDistinctCommand] = adminCommands[i]
        }
    }

    return adminCommands.subList(0, currentDistinctCommand + 1)
}