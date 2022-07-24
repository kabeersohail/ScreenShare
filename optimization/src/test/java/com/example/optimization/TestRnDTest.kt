package com.example.optimization

import io.mockk.spyk
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.junit.Before
import org.junit.Test


class TestRnDTest{

    lateinit var testRnD: TestRnD

    @Before
    fun setup(){
        testRnD = spyk(TestRnD())
    }

    @Test
    fun sequenceTest() {

        testRnD.start()

        verifyOrder {
            testRnD.firstCall()
            testRnD.secondCall()
            testRnD.start()
            testRnD.thirdCall()
        }
    }

}