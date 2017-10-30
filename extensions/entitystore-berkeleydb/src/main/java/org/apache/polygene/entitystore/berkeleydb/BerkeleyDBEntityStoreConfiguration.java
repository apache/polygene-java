/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.entitystore.berkeleydb;

import com.sleepycat.je.CacheMode;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.property.Property;

/**
 * Configuration for the BerkeleyDBEntityStoreService.
 * <p>
 * <strong>NOTE:</strong> Most of the documentation for all the properties are copied from the Berkeley DB javadocs.
 * </p>
 */
// START SNIPPET: config
public interface BerkeleyDBEntityStoreConfiguration
{
    /**
     * Name of the database containing the Polygene entities.
     */
    @UseDefaults( "polygene" )
    Property<String> databaseName();

    /**
     * The directory where the BDB JE data (files) will be stored
     * <p>
     * Default:
     * </p>
     * <pre><code>
     * if( fileConfiguration != null )
     *     dataDir = new File( fileConfiguration.dataDirectory(), application.name() + "/" + descriptor.identity() );
     * else
     *     dataDir = new File( System.getProperty( "user.dir" ) );
     * dataDir = new File( dataDir, "data" );
     * </code></pre>
     *
     * @return path to data directory, by passing the string to {@code new File(dataDirectory()).getAbsoluteFile() }
     */
    @Optional
    Property<String> dataDirectory();

    /**
     * If true, creates the database environment if it doesn't already exist.
     */
    @UseDefaults( "true" )
    Property<Boolean> allowCreate();

    /**
     * Configures the database environment for no locking.
     * <p>
     * If true, create the environment with record locking. This property should be set to false only in special
     * circumstances when it is safe to run without record locking.
     * </p>
     * <p>
     * This configuration option should be used when locking guarantees such as consistency and isolation are not
     * important. If locking mode is disabled (it is enabled by default), the cleaner is automatically disabled.
     * The user is responsible for invoking the cleaner and ensuring that there are no concurrent operations while
     * the cleaner is running.
     * </p>
     */
    @UseDefaults( "true" )
    Property<Boolean> locking();

    /**
     * Configures the default lock timeout.
     * <p>
     * A value of zero disables lock timeouts. This is not recommended, even when the application expects that
     * deadlocks will not occur or will be easily resolved. A lock timeout is a fall-back that guards against
     * unexpected "live lock", unresponsive threads, or application failure to close a cursor or to commit or
     * abort a transaction.
     * </p>
     * <p>
     * Expressed in milliseconds. Default: 500ms
     * </p>
     */
    @UseDefaults( "500" )
    Property<Long> lockTimeout();

    /**
     * Sets the user defined nodeName for the Environment.
     * <p>
     * If set, exception messages, logging messages, and thread names will have this nodeName included in them.
     * If a user has multiple Environments in a single JVM, setting this to a string unique to each Environment
     * may make it easier to diagnose certain exception conditions as well as thread dumps.
     * </p>
     */
    @Optional
    Property<String> nodeName();

    /**
     * Configures the database environment to be read-only, and any attempt to modify a database will fail.
     * <p>
     * A read-only environment has several limitations and is recommended only in special circumstances. Note that
     * there is no performance advantage to opening an environment read-only.
     * </p>
     * <p>
     * The primary reason for opening an environment read-only is to open a single environment in multiple JVM
     * processes. Only one JVM process at a time may open the environment read-write. See EnvironmentLockedException.
     * </p>
     * <p>
     * When the environment is open read-only, the following limitations apply.
     * </p>
     * <p>
     * In the read-only environment no writes may be performed, as expected, and databases must be opened read-only
     * using DatabaseConfig.setReadOnly.
     * </p>
     * <p>
     * The read-only environment receives a snapshot of the data that is effectively frozen at the time the environment
     * is opened. If the application has the environment open read-write in another JVM process and modifies the
     * environment's databases in any way, the read-only version of the data will not be updated until the read-only
     * JVM process closes and reopens the environment (and by extension all databases in that environment).
     * </p>
     * <p>
     * If the read-only environment is opened while the environment is in use by another JVM process in read-write mode,
     * opening the environment read-only (recovery) is likely to take longer than it does after a clean shutdown. This
     * is due to the fact that the read-write JVM process is writing and checkpoints are occurring that are not
     * coordinated with the read-only JVM process. The effect is similar to opening an environment after a crash.
     * </p>
     * <p>
     * In a read-only environment, the JE cache will contain information that cannot be evicted because it was
     * reconstructed by recovery and cannot be flushed to disk. This means that the read-only environment may not be
     * suitable for operations that use large amounts of memory, and poor performance may result if this is attempted.
     * </p>
     * <p>
     * In a read-write environment, the log cleaner will be prohibited from deleting log files for as long as the
     * environment is open read-only in another JVM process. This may cause disk usage to rise, and for this reason
     * it is not recommended that an environment is kept open read-only in this manner for long periods.
     * </p>
     * <p>
     * For these reasons, it is recommended that a read-only environment be used only for short periods and for
     * operations that are not performance critical or memory intensive. With few exceptions, all application functions
     * that require access to a JE environment should be built into a single application so that they can be performed
     * in the JVM process where the environment is open read-write.
     * </p>
     * <p>
     * In most applications, opening an environment read-only can and should be avoided.
     * </p>
     */
    @UseDefaults
    Property<Boolean> readOnly();

    /**
     * If true, the shared cache is used by this environment.
     * <p>
     * By default this parameter is false and this environment uses a private cache. If this parameter is set to true,
     * this environment will use a cache that is shared with all other open environments in this process that also set
     * this parameter to true. There is a single shared cache per process.
     * </p>
     * <p>
     * By using the shared cache, multiple open environments will make better use of memory because the cache LRU
     * algorithm is applied across all information in all environments sharing the cache. For example, if one
     * environment is open but not recently used, then it will only use a small portion of the cache, leaving the rest
     * of the cache for environments that have been recently used.
     * </p>
     */
    @UseDefaults
    Property<Boolean> sharedCache();

    /**
     * Configures the use of transactions.
     * <p>
     * This should be set to true when transactional guarantees such as atomicity of multiple operations and durability
     * are important.
     * </p>
     * <p>
     * If true, create an environment that is capable of performing transactions. If true is not passed, transactions
     * may not be used. For licensing purposes, the use of this method distinguishes the use of the Transactional
     * product. Note that if transactions are not used, specifying true does not create additional overhead in the
     * environment.
     * </p>
     */
    @UseDefaults( "true" )
    Property<Boolean> transactional();

    /**
     * The transaction timeout.
     * <p>
     * A value of 0 turns off transaction timeouts.
     * </p>
     * <p>
     * Expressed in milliseconds.
     * </p>
     */
    @UseDefaults
    Property<Long> txnTimeout();

    /**
     * Configures all transactions for this environment to have Serializable (Degree 3) isolation.
     * <p>
     * By setting Serializable isolation, phantoms will be prevented. By default transactions provide Repeatable Read
     * isolation. The default is false for the database environment.
     * </p>
     */
    @UseDefaults
    Property<Boolean> txnSerializableIsolation();

    /**
     * The default CacheMode used for operations performed in this environment.
     * <p>
     * The default cache mode may be overridden on a per-database basis using DatabaseConfig.setCacheMode, and on a
     * per-record or per-operation basis using Cursor.setCacheMode, ReadOptions.setCacheMode(CacheMode) or
     * WriteOptions.setCacheMode(CacheMode).
     * </p>
     */
    @UseDefaults( "DEFAULT" )
    Property<CacheMode> cacheMode();

    /**
     * Configures the memory available to the database system, as a percentage of the JVM maximum memory.
     * <p>
     * The system will evict database objects when it comes within a prescribed margin of the limit.
     * </p>
     * <p>
     * By default, JE sets the cache size to:
     * </p>
     * <p>
     * <code>(MAX_MEMORY_PERCENT * JVM maximum memory) / 100</code>
     * </p>
     * <p>
     * where JVM maximum memory is specified by the JVM -Xmx flag. However, setting MAX_MEMORY to a non-zero value
     * overrides the percentage based calculation and sets the cache size explicitly.
     * </p>
     * <p>
     * The following details apply to setting the cache size to a percentage of the JVM heap size byte size (this
     * parameter) as well as to a byte size (MAX_MEMORY
     * </p>
     * <p>
     * Note that the log buffer cache may be cleared if the cache size is changed after the environment has been opened.
     * </p>
     * <p>
     * If SHARED_CACHE is set to true, MAX_MEMORY and MAX_MEMORY_PERCENT specify the total size of the shared cache,
     * and changing these parameters will change the size of the shared cache.
     * </p>
     * <p>
     * When using the shared cache feature, new environments that join the cache may alter the cache percent setting
     * if their configuration is set to a different value.
     * </p>
     * <p>
     * To take full advantage of JE cache memory, it is strongly recommended that compressed oops
     * (-XX:+UseCompressedOops) is specified when a 64-bit JVM is used and the maximum heap size is less than 32 GB.
     * As described in the referenced documentation, compressed oops is sometimes the default JVM mode even when it is
     * not explicitly specified in the Java command. However, if compressed oops is desired then it must be explicitly
     * specified in the Java command when running DbCacheSize or a JE application. If it is not explicitly specified
     * then JE will not aware of it, even if it is the JVM default setting, and will not take it into account when
     * calculating cache memory sizes.
     * </p>
     */
    @UseDefaults( "60" )
    Property<Integer> cachePercent();

    /**
     * Configures the memory available to the database system, in bytes.
     * <p>
     * See MAX_MEMORY_PERCENT for more information.
     * </p>
     */
    @UseDefaults
    Property<Long> cacheSize();

    /**
     * Configures the number of bytes to be used as a secondary, off-heap cache.
     * <p>
     * The off-heap cache is used to hold record data and Btree nodes when these are evicted from the "main cache"
     * because it overflows. Eviction occurs according to an LRU algorithm and takes into account the user- specified
     * CacheMode. When the off-heap cache overflows, eviction occurs there also according to the same algorithm.
     * </p>
     * <p>
     * The main cache is in the Java heap and consists primarily of the Java objects making up the in-memory Btree
     * data structure. Btree objects are not serialized the main cache, so no object materialization is needed to
     * access the Btree there. Access to records in the main cache is therefore very fast, but the main cache has
     * drawbacks as well:
     * </p>
     * <ol>
     * <li>
     * The larger the main cache, the more likely it is to have Java GC performance problems.
     * </li>
     * <li>
     * When the Java heap exceeds 32GB, the "compressed OOPs" setting no longer applies and less data will fit in the
     * same amount of memory. For these reasons, JE applications often configure a heap of 32GB or less, and a main
     * cache that is significantly less than 32GB, leaving any additional machine memory for use by the file system
     * cache.
     * </li>
     * </ol>
     * <p>
     * The use of the file system cache has performance benefits, but also has its own drawbacks:
     * </p>
     * <ol>
     * <li>
     * There is a significant redundancy between the main cache and the file system cache because all data and Btree
     * information that is logged (written) by JE appears in the file system and may also appear in the main cache.
     * </li>
     * <li>
     * It is not possible for dirty Btree information to be placed in the file system cache without logging it, this
     * logging may be otherwise unnecessary, and the logging creates additional work for the JE cleaner; in other
     * words, the size of the main cache alone determines the maximum size of the in-memory "dirty set".
     * </li>
     * </ol>
     * <p>
     * The off-heap cache is stored outside the Java heap using a native platform memory allocator. The current
     * implementation relies on internals that are specific to the Oracle and IBM JDKs; however, a memory allocator
     * interface that can be implemented for other situations is being considered for a future release. Records and
     * Btree objects are serialized when they are placed in the off-heap cache, and they must be materialized when
     * they are moved back to the main cache in order to access them. This serialization and materialization adds
     * some CPU overhead and thread contention, as compared to accessing data directly in the main cache. The off-heap
     * cache can contain dirty Btree information, so it can be used to increase the maximum size of the in-memory
     * "dirty set".
     * </p>
     * <p>
     * NOTE: If an off-heap cache is configured but cannot be used because that native allocator is not available in
     * the JDK that is used, an IllegalStateException will be thrown by the Environment or
     * com.sleepycat.je.rep.ReplicatedEnvironment constructor. In the current release, this means that the
     * sun.misc.Unsafe class must contain the allocateMemory method and related methods, as defined in the Oracle JDK.
     * </p>
     * <p>
     * When configuring an off-heap cache you can think of the performance trade-offs in two ways. First, if the
     * off-heap cache is considered to be a replacement for the file system cache, the serialization and
     * materialization overhead is not increased. In this case, the use of the off-heap cache is clearly beneficial,
     * and using the off-heap cache "instead of" the file system cache is normally recommended. Second, the off-heap
     * cache can be used along with a main cache that is reduced in size in order to compensate for Java GC problems.
     * In this case, the trade-off is between the additional serialization, materialization and contention overheads
     * of the off-heap cache, as compared to the Java GC overhead.
     * </p>
     * <p>
     * When dividing up available memory for the JVM heap, the off-heap cache, and for other uses, please be aware
     * that the file system cache and the off-heap cache are different in one important respect. The file system cache
     * automatically shrinks when memory is needed by the OS or other processes, while the off-heap cache does not.
     * Therefore, it is best to be conservative about leaving memory free for other uses, and it is not a good idea to
     * size the off-heap cache such that all machine memory will be allocated. If off-heap allocations or other
     * allocations fail because there is no available memory, the process is likely to die without any exception
     * being thrown. In one test on Linux, for example, the process was killed abruptly by the OS and the only
     * indication of the problem was the following shown by dmesg.
     * </p>
     * <pre><code>
     * Out of memory: Kill process 28768 (java) score 974 or sacrifice child
     * Killed process 28768 (java)
     * total-vm:278255336kB, anon-rss:257274420kB, file-rss:0kB
     * </code></pre>
     * WARNING: Although this configuration property is mutable, it cannot be changed from zero to non-zero, or
     * non-zero to zero. In other words, the size of the off-heap cache can be changed after initially configuring
     * a non-zero size, but the off-heap cache cannot be turned on and off dynamically. An attempt to do so will
     * cause an IllegalArgumentException to be thrown by the Environment or com.sleepycat.je.rep.ReplicatedEnvironment
     * constructor.
     */
    @UseDefaults
    Property<Long> cacheHeapCacheSize();

    /**
     * Configures the default durability associated with transactions.
     * <p>
     * The string must have the following format:
     * </p>
     * <pre><code>
     * SyncPolicy[,SyncPolicy[,ReplicaAckPolicy]]
     * </code></pre>
     * <p>
     * The first SyncPolicy in the above format applies to the Master, and the optional second SyncPolicy to the
     * replica. Specific SyncPolicy or ReplicaAckPolicy values are denoted by the name of the enumeration value.
     * </p>
     * <p>
     * For example, the string:sync,sync,quorum describes a durability policy where the master and replica both use
     * Durability.SyncPolicy.SYNC to commit transactions and Durability.ReplicaAckPolicy.SIMPLE_MAJORITY to acknowledge
     * a transaction commit.
     * </p>
     * <p>
     * Durability.SyncPolicy.NO_SYNC, is the default value for a node's SyncPolicy.
     * </p>
     * <p>
     * Durability.ReplicaAckPolicy.SIMPLE_MAJORITY is the default for the ReplicaAckPolicy.
     * </p>
     */
    @Optional
    Property<String> durability();
}
// END SNIPPET: config
