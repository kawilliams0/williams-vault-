package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CategoryEntity
import com.example.data.model.FinanceCategories
import com.example.data.model.TransactionType
import com.example.data.model.toCategoryItem
import com.example.ui.components.FinanceFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Williams Vault", appName)
  }

  @Test
  fun `currency formatters produce correct output`() {
    val formatted = FinanceFormatters.formatCurrency(12450.50)
    assertTrue(formatted.contains("12,450.50"))
  }

  @Test
  fun `category entity conversion to CategoryItem`() {
    val entity = CategoryEntity(
      id = 101,
      name = "Entertainment",
      type = "EXPENSE",
      iconKey = "movie",
      colorHex = "#8B5CF6",
      isDefault = true
    )
    val item = entity.toCategoryItem()
    assertEquals("Entertainment", item.name)
    assertEquals(TransactionType.EXPENSE, item.type)
    assertNotNull(item.icon)
  }

  @Test
  fun `finance categories lookup returns default or fallback`() {
    val foodCategory = FinanceCategories.getCategory("Food & Dining")
    assertEquals("Food & Dining", foodCategory.name)

    val customCategory = FinanceCategories.getCategory("Custom Cafe", listOf(
      CategoryEntity(id = 99, name = "Custom Cafe", type = "EXPENSE", iconKey = "local_cafe", colorHex = "#D0BCFF")
    ))
    assertEquals("Custom Cafe", customCategory.name)
  }

  @Test
  fun `finance categories smart keyword lookup resolves cafe and uber`() {
    val chaiCategory = FinanceCategories.getCategory("Chai & Snacks")
    assertNotNull(chaiCategory)
    assertEquals("local_cafe", chaiCategory.iconKey)
    assertEquals(TransactionType.EXPENSE, chaiCategory.type)

    val uberCategory = FinanceCategories.getCategory("Uber Ride to Office")
    assertNotNull(uberCategory)
    assertEquals("directions_car", uberCategory.iconKey)
    assertEquals(TransactionType.EXPENSE, uberCategory.type)
  }

  @Test
  fun `apk export helper returns valid apk metadata`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val apkInfo = com.example.util.ApkExportHelper.getApkInfo(context)
    assertEquals("WilliamsVault.apk", apkInfo.fileName)
    assertEquals(context.packageName, apkInfo.packageName)
  }
}
