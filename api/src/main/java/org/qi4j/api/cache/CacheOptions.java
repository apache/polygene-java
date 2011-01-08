package org.qi4j.api.cache;

/**
 * CacheOptions is a metaInfo class for the Cache system for Entity persistence.
 * CacheOptions should be assigned to the Usecase of the UnitOfWork, to give hint on caching to entity stores.
 * See {@link org.qi4j.api.usecase.UsecaseBuilder} on how to set the metaInfo on Usecases.
 */
public final class CacheOptions
{
    public static final CacheOptions ALWAYS = new CacheOptions( true, true, true );
    public static final CacheOptions NEVER = new CacheOptions( false, false, false );

    private boolean cacheOnRead;
    private boolean cacheOnWrite;
    private boolean cacheOnNew;

    /**
     * Constructor for CacheOptions.
     *
     * @param cacheOnRead  if true, give the hint to the Cache system that it may not be a good idea to cache the
     *                     read values. This is useful when it is known that the read will be over a large set and
     *                     shouldn't affect the existing cached entities. For instance, when traversing the EntityStore
     *                     this option is set to false.
     * @param cacheOnWrite if true, give the hint to the Cache system that it may not be a good idea to cache the
     *                     entity when the value is updated. If this is false, the cache should be emptied from any
     *                     cached entity instead of updated. There are few cases when this is useful, and if this is
     *                     false, it makes sense that the <i>cacheOnRead</i> is also false.
     * @param cacheOnNew   if true, give the hint to the Cache system that it may not be a good idea to cache a newly
     *                     created Entity, as it is not likely to be read in the near future. This is useful when
     *                     batch inserts are being made.
     */
    public CacheOptions( boolean cacheOnRead, boolean cacheOnWrite, boolean cacheOnNew )
    {
        this.cacheOnRead = cacheOnRead;
        this.cacheOnWrite = cacheOnWrite;
        this.cacheOnNew = cacheOnNew;
    }

    /**
     * @return if true, give the hint to the Cache system that it may not be a good idea to cache the
     *         read values. This is useful when it is known that the read will be over a large set and
     *         shouldn't affect the existing cached entities. For instance, when traversing the EntityStore
     */
    public boolean cacheOnRead()
    {
        return cacheOnRead;
    }

    /**
     * @return if true, give the hint to the Cache system that it may not be a good idea to cache the
     *         entity when the value is updated. If this is false, the cache should be emptied from any
     *         cached entity instead of updated. There are few cases when this is useful, and if this is
     *         false, it makes sense that the <i>cacheOnRead</i> is also false.
     */
    public boolean cacheOnWrite()
    {
        return cacheOnWrite;
    }

    /**
     * @return if true, give the hint to the Cache system that it may not be a good idea to cache a newly
     *         created Entity, as it is not likely to be read in the near future. This is useful when
     *         batch inserts are being made.
     */
    public boolean cacheOnNew()
    {
        return cacheOnNew;
    }
}
