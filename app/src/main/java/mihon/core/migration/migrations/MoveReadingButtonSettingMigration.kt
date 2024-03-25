package mihon.core.migration.migrations

import androidx.preference.PreferenceManager
import eu.kanade.tachiyomi.App
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.domain.library.service.LibraryPreferences

class MoveReadingButtonSettingMigration : Migration {
    override val version: Float = 43f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean = withIOContext {
        val context = migrationContext.get<App>() ?: return@withIOContext false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val libraryPreferences = migrationContext.get<LibraryPreferences>() ?: return@withIOContext false
        if (prefs.getBoolean("start_reading_button", false)) {
            libraryPreferences.showContinueReadingButton().set(true)
        }

        return@withIOContext true
    }
}
