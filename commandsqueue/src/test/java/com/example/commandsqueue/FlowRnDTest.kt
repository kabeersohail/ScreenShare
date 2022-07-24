package com.example.commandsqueue

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test


class FlowRnDTest {

    lateinit var flowRnD: FlowRnD

    var flowCollector: FlowCollector<AdminCommand> = spyk(FlowCollector { println("Here") })

    private lateinit var myFlow: Flow<AdminCommand>

    @RelaxedMockK
    lateinit var a: A

    @RelaxedMockK
    lateinit var b: B

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        flowRnD = spyk(FlowRnD())
    }

    @Test
    fun `receive from two different channels`() = runBlocking {


        myFlow = flow {
            flowCollector = this

        }

        flowRnD.executeCommand(myFlow)

        a.emit(flowCollector)
        b.emit(flowCollector)

        delay(5000L)

        coVerify { flowRnD.executeLock() }


    }

}