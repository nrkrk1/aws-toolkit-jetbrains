// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.core

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import software.amazon.awssdk.core.SdkClient
import software.aws.toolkits.core.credentials.ToolkitCredentialsChangeListener
import software.aws.toolkits.core.credentials.ToolkitCredentialsProvider
import software.aws.toolkits.core.region.AwsRegion
import software.aws.toolkits.core.utils.getLogger
import software.aws.toolkits.core.utils.warn
import software.aws.toolkits.jetbrains.core.credentials.CredentialManager
import software.aws.toolkits.jetbrains.core.credentials.ProjectAccountSettingsManager
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Intended to prevent repeated unnecessary calls to AWS to understand resource state.
 *
 * Will cache responses from AWS by [AwsRegion]/[ToolkitCredentialsProvider] - generically applicable to any AWS call.
 */
interface AwsResourceCache {

    /**
     * Get a [resource] either by making a call or returning it from the cache if present and unexpired. Uses the currently [AwsRegion]
     * & [ToolkitCredentialsProvider] active in [ProjectAccountSettingsManager].
     *
     * @param[useStale] if an exception occurs attempting to refresh the resource return a cached version if it exists (even if it's expired). Default: true
     * @param[forceFetch] force the resource to refresh (and update cache) even if a valid cache version exists. Default: false
     */
    fun <T> getResource(resource: Resource<T>, useStale: Boolean = true, forceFetch: Boolean = false): CompletionStage<T>

    /**
     * @see [getResource]
     *
     * @param[region] the specific [AwsRegion] to use for this resource
     * @param[credentialProvider] the specific [ToolkitCredentialsProvider] to use for this resource
     */
    fun <T> getResource(
        resource: Resource<T>,
        region: AwsRegion,
        credentialProvider: ToolkitCredentialsProvider,
        useStale: Boolean = true,
        forceFetch: Boolean = false
    ): CompletionStage<T>

    /**
     * Blocking version of [getResource]
     *
     * @param[useStale] if an exception occurs attempting to refresh the resource return a cached version if it exists (even if it's expired). Default: true
     * @param[forceFetch] force the resource to refresh (and update cache) even if a valid cache version exists. Default: false
     */
    fun <T> getResourceNow(resource: Resource<T>, timeout: Duration = DEFAULT_TIMEOUT, useStale: Boolean = true, forceFetch: Boolean = false): T =
        wait(timeout) { getResource(resource, useStale, forceFetch) }

    /**
     * Blocking version of [getResource]
     *
     * @param[region] the specific [AwsRegion] to use for this resource
     * @param[credentialProvider] the specific [ToolkitCredentialsProvider] to use for this resource
     */
    fun <T> getResourceNow(
        resource: Resource<T>,
        region: AwsRegion,
        credentialProvider: ToolkitCredentialsProvider,
        timeout: Duration = DEFAULT_TIMEOUT,
        useStale: Boolean = true,
        forceFetch: Boolean = false
    ): T = wait(timeout) { getResource(resource, region, credentialProvider, useStale, forceFetch) }

    /**
     * Gets the [resource] if it exists in the cache.
     *
     * @param[useStale] return a cached version if it exists (even if it's expired). Default: true
     */
    fun <T> getResourceIfPresent(resource: Resource<T>, useStale: Boolean = true): T?

    /**
     * Gets the [resource] if it exists in the cache.
     *
     * @param[region] the specific [AwsRegion] to use for this resource
     * @param[credentialProvider] the specific [ToolkitCredentialsProvider] to use for this resource
     */
    fun <T> getResourceIfPresent(resource: Resource<T>, region: AwsRegion, credentialProvider: ToolkitCredentialsProvider, useStale: Boolean = true): T?

    /**
     * Clears the contents of the cache across all regions, credentials and resource types.
     */
    fun clear()

    /**
     * Clears the contents of the cache for the specific [resource] type, in the currently active [AwsRegion] & [ToolkitCredentialsProvider]
     */
    fun clear(resource: Resource<*>)

    /**
     * Clears the contents of the cache for the specific [resource] type, [AwsRegion] & [ToolkitCredentialsProvider]
     */
    fun clear(resource: Resource<*>, region: AwsRegion, credentialProvider: ToolkitCredentialsProvider)

    companion object {
        @JvmStatic
        fun getInstance(project: Project): AwsResourceCache = ServiceManager.getService(project, AwsResourceCache::class.java)

        private val DEFAULT_TIMEOUT = Duration.ofSeconds(5)
        private fun <T> wait(timeout: Duration, call: () -> CompletionStage<T>) = try {
            call().toCompletableFuture().get(timeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
}

fun <T> Project.getResource(resource: Resource<T>, useStale: Boolean = true, forceFetch: Boolean = false) =
    AwsResourceCache.getInstance(this).getResource(resource, useStale, forceFetch)

sealed class Resource<T> {

    /**
     * A [Cached] resource is one whose fetch is potentially expensive, the result of which should be memoized for a period of time ([expiry]).
     */
    abstract class Cached<T> : Resource<T>() {
        abstract fun fetch(project: Project, region: AwsRegion, credentials: ToolkitCredentialsProvider): T
        open fun expiry(): Duration = DEFAULT_EXPIRY
        abstract val id: String

        companion object {
            private val DEFAULT_EXPIRY = Duration.ofMinutes(10)
        }
    }

    /**
     * A [View] resource depends on some other underlying [Resource] and then performs some [transform] of the [underlying]'s result
     * in order to return the desired type [Output]. The [transform] result is not cached, [transform]s are re-applied on each fetch - thus should
     * should be relatively cheap.
     */
    class View<Input, Output>(internal val underlying: Resource<Input>, private val transform: Input.() -> Output) : Resource<Output>() {
        @Suppress("UNCHECKED_CAST")
        internal fun doMap(input: Any) = transform(input as Input)
    }
}

class ClientBackedCachedResource<ReturnType, ClientType : SdkClient>(
    private val sdkClientClass: KClass<ClientType>,
    override val id: String,
    private val fetchCall: ClientType.() -> ReturnType
) : Resource.Cached<ReturnType>() {
    override fun fetch(project: Project, region: AwsRegion, credentials: ToolkitCredentialsProvider): ReturnType {
        val client = AwsClientManager.getInstance(project).getClient(sdkClientClass, credentials, region)
        return fetchCall(client)
    }
}

class DefaultAwsResourceCache(private val project: Project, private val clock: Clock, maximumCacheEntries: Long) :
    AwsResourceCache, ToolkitCredentialsChangeListener {

    @Suppress("unused")
    constructor(project: Project) : this(project, Clock.systemDefaultZone(), MAXIMUM_CACHE_ENTRIES)

    init {
        ApplicationManager.getApplication().messageBus.connect().subscribe(CredentialManager.CREDENTIALS_CHANGED, this)
    }

    private val cache = CacheBuilder.newBuilder().maximumSize(maximumCacheEntries).build<CacheKey, Entry<*>>().asMap()
    private val accountSettings = ProjectAccountSettingsManager.getInstance(project)

    override fun <T> getResource(resource: Resource<T>, useStale: Boolean, forceFetch: Boolean) =
        getResource(resource, accountSettings.activeRegion, accountSettings.activeCredentialProvider, useStale, forceFetch)

    override fun <T> getResource(
        resource: Resource<T>,
        region: AwsRegion,
        credentialProvider: ToolkitCredentialsProvider,
        useStale: Boolean,
        forceFetch: Boolean
    ): CompletionStage<T> = when (resource) {
        is Resource.View<*, T> ->
            getResource(resource.underlying, region, credentialProvider, useStale, forceFetch).thenApply { resource.doMap(it as Any) }
        is Resource.Cached<T> -> {
            val future = CompletableFuture<T>()
            val context = Context(resource, region, credentialProvider, useStale, forceFetch)
            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val result = cache.compute(context.cacheKey) { _, value ->
                        fetchIfNeeded(context, value as Entry<T>?)
                    } as Entry<T>
                    future.complete(result.value)
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }
            future
        }
    }

    override fun <T> getResourceIfPresent(resource: Resource<T>, useStale: Boolean): T? =
        getResourceIfPresent(resource, accountSettings.activeRegion, accountSettings.activeCredentialProvider, useStale)

    override fun <T> getResourceIfPresent(resource: Resource<T>, region: AwsRegion, credentialProvider: ToolkitCredentialsProvider, useStale: Boolean): T? =
        when (resource) {
            is Resource.Cached<T> -> {
                val entry = cache.getTyped<T>(CacheKey(resource.id, region.id, credentialProvider.id))
                when {
                    entry != null && (useStale || entry.notExpired) -> entry.value
                    else -> null
                }
            }
            is Resource.View<*, T> -> getResourceIfPresent(resource.underlying, region, credentialProvider, useStale)?.let { resource.doMap(it) }
        }

    override fun clear(resource: Resource<*>) {
        clear(resource, accountSettings.activeRegion, accountSettings.activeCredentialProvider)
    }

    override fun clear(resource: Resource<*>, region: AwsRegion, credentialProvider: ToolkitCredentialsProvider) {
        when (resource) {
            is Resource.Cached<*> -> cache.remove(CacheKey(resource.id, region.id, credentialProvider.id))
            is Resource.View<*, *> -> clear(resource.underlying, region, credentialProvider)
        }
    }

    override fun clear() {
        cache.clear()
    }

    override fun providerRemoved(providerId: String) = clearByCredential(providerId)

    override fun providerModified(provider: ToolkitCredentialsProvider) = clearByCredential(provider.id)

    private fun clearByCredential(providerId: String) {
        cache.keys.removeIf { it.credentialsId == providerId }
    }

    private fun <T> fetchIfNeeded(context: Context<T>, currentEntry: Entry<T>?) = when {
        currentEntry == null -> fetch(context)
        currentEntry.notExpired && !context.forceFetch -> currentEntry
        context.useStale -> fetchWithFallback(context, currentEntry)
        else -> fetch(context)
    }

    private fun <T> fetchWithFallback(context: Context<T>, currentEntry: Entry<T>) = try {
        fetch(context)
    } catch (e: Exception) {
        LOG.warn(e) { "Failed to fetch resource using ${context.cacheKey}, falling back to expired entry" }
        currentEntry
    }

    private fun <T> fetch(context: Context<T>): Entry<T> {
        val value = context.resource.fetch(project, context.region, context.credentials)
        return Entry(clock.instant().plus(context.resource.expiry()), value)
    }

    private val Entry<*>.notExpired get() = clock.instant().isBefore(expiry)

    companion object {
        private val LOG = getLogger<DefaultAwsResourceCache>()
        private const val MAXIMUM_CACHE_ENTRIES = 100L

        private data class CacheKey(val resourceId: String, val regionId: String, val credentialsId: String)

        private class Context<T>(
            val resource: Resource.Cached<T>,
            val region: AwsRegion,
            val credentials: ToolkitCredentialsProvider,
            val useStale: Boolean,
            val forceFetch: Boolean
        ) {
            val cacheKey = CacheKey(resource.id, region.id, credentials.id)
        }

        private class Entry<T>(val expiry: Instant, val value: T)

        private fun <T> ConcurrentMap<CacheKey, Entry<*>>.getTyped(key: CacheKey) = this[key]?.let {
            @Suppress("UNCHECKED_CAST")
            it as Entry<T>
        }
    }
}