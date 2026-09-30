package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.SubnetViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SubnetCalc", appName)
  }

  @Test
  fun `viewModel initial calculation is populated`() {
    val viewModel = SubnetViewModel()
    val state = viewModel.uiState.value
    assertNotNull(state.calculationResult)
    assertEquals("192.168.1.0", state.calculationResult?.baseIp?.toDottedDecimal())
  }
}
