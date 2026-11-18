package com.example.projetorevisao.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [IncidentEntity::class], version = 3, exportSchema = false)
abstract class CitizenDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao

    companion object {
        /** v2: coluna de anexos (fotos/vídeos). Mantém os relatos já salvos no aparelho. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE incidents ADD COLUMN createdAtEpochMs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE incidents ADD COLUMN fromServer INTEGER NOT NULL DEFAULT 0")
                // Ids do servidor têm 8 caracteres depois de "inc-"; os criados no aparelho usam a hora (13 dígitos).
                db.execSQL("UPDATE incidents SET fromServer = 1 WHERE length(id) = 12")
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE incidents ADD COLUMN media TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile private var instance: CitizenDatabase? = null

        fun getInstance(context: Context): CitizenDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CitizenDatabase::class.java,
                "rede_cidada.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }
    }
}
