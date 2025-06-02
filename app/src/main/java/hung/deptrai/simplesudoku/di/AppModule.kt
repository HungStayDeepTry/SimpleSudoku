package hung.deptrai.simplesudoku.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hung.deptrai.simplesudoku.model.GameTimer
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
}
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope