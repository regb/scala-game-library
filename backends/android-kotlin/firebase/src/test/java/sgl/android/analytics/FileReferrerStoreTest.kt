package sgl.android.analytics

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class FileReferrerStoreTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun processRestartAndUpgradeKeepSuccessReinstallAndDataResetDoNot() {
        val file = folder.newFile()
        val first = FileReferrerStore(file, 100)
        first.attempts = 1
        first.result = parseReferrer(NativeReferrer("utm_source=google&gclid=secret", 10, 20, 11, 21, "1.0"))
        val upgraded = FileReferrerStore(file, 100)
        assertEquals(first.result, upgraded.result)
        assertEquals(1, upgraded.attempts)
        assertFalse(file.readText().contains("secret"))
        // Restored cache from another installation is rejected even with the same app version.
        val reinstalled = FileReferrerStore(file, 200)
        assertNull(reinstalled.result)
        assertEquals(0, reinstalled.attempts)
        file.delete()
        assertNull(FileReferrerStore(file, 100).result)
    }
    @Test fun attemptBudgetPersistsAndCorruptCacheIsSafe() {
        val file = folder.newFile()
        FileReferrerStore(file, 100).attempts = 3
        assertEquals(3, FileReferrerStore(file, 100).attempts)
        file.writeText("broken")
        assertEquals(0, FileReferrerStore(file, 100).attempts)
        assertNull(FileReferrerStore(file, 100).result)
    }
}
