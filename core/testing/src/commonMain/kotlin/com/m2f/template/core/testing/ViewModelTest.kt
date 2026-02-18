package com.m2f.template.core.testing

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Abstract base class for ViewModel tests that sets up [Dispatchers.Main]
 * with a [StandardTestDispatcher] before each test and resets it after.
 *
 * Extend this class to eliminate Dispatchers.setMain/resetMain boilerplate:
 * ```kotlin
 * class MyViewModelTest : ViewModelTest() {
 *     @Test
 *     fun `my test`() {
 *         viewModel.test {
 *             intent(MyIntent.Load)
 *             model(MyModel(loaded = true))
 *         }
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
