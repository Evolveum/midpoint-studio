package com.evolveum.midpoint.studio

import com.evolveum.midpoint.util.ClassPathUtil
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class ClassPathUtilTest : BasePlatformTestCase() {

    override fun getTestDataPath() = "src/test/testData/common"

    fun testClasspathLoading() {
        try {
            val classes = ClassPathUtil.listClasses(ObjectReferenceType::class.java.`package`)
            assertNotEmpty(classes)
        } catch (e: Exception) {
            e.printStackTrace()
            fail("Failed to load classes from classpath: ${e.message}")
        }
    }
}
