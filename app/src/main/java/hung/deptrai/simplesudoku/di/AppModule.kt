package hung.deptrai.simplesudoku.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hung.deptrai.simplesudoku.model.GameTimer
import hung.deptrai.simplesudoku.model.room.SudokuDatabase
import hung.deptrai.simplesudoku.model.room.dao.SudokuDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideGameTimer(@ApplicationScope scope: CoroutineScope): GameTimer = GameTimer(scope)

    @Provides
    @Singleton
    fun provideSudokuDatabase(
        @ApplicationContext context: Context
    ): SudokuDatabase {
        return Room.databaseBuilder(
            context,
            SudokuDatabase::class.java,
            "sudoku_db"
        ).build()
    }

    @Provides
    fun provideSudokuDao(db: SudokuDatabase): SudokuDao = db.sudokuDao()
}
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope