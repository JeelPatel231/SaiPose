package tel.jeelpa.saipose.utils

import android.content.Context
import dalvik.system.DexClassLoader
import tel.jeelpa.saipose.data.database.AppDatabase
import tel.jeelpa.saipose.data.database.Plugin
import java.io.File
import javax.inject.Inject
import kotlin.reflect.full.primaryConstructor


class PluginManager @Inject constructor(
    private val db:AppDatabase,
) {
    fun listAllPlugins() = db.pluginDao().getAll()

    suspend fun registerPlugin(plugin: Plugin) {
        // do other dexjar management
        db.pluginDao().insertAll(plugin)
    }
    suspend fun deletePlugin(plugin: Plugin) {
        // dex jar management
        db.pluginDao().delete(plugin)
    }
}

inline fun <reified T>loadAndCreateInstance(ctx: Context, jarName: String, className: String) : T {
//    val absPath = File(ctx.cacheDir, jarName).absolutePath
    val absPath = File(ctx.getExternalFilesDir(null)!!.path, jarName).absolutePath
    println(absPath)
    val classLoader = DexClassLoader(
        absPath,
        null,
        null,
        ctx.classLoader
    )
    val class_ = Class.forName(className, true, classLoader).kotlin

    val instance = class_.objectInstance ?: class_.primaryConstructor!!.call()

    if (instance is T) {
        return instance
    } else {
        throw IllegalArgumentException("Class $className does not implement given class")
    }
}

inline fun <reified T>loadPluginSafe(ctx: Context, jarName: String, className: String) : T? {
    return try {
        loadAndCreateInstance(ctx, jarName, className)
    } catch (e: Throwable){
        e.printStackTrace()
        null
    }
}