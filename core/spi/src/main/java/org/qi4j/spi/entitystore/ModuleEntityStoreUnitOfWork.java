package org.qi4j.spi.entitystore;

import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.module.ModuleSpi;

public class ModuleEntityStoreUnitOfWork
    implements EntityStoreUnitOfWork
{
    private final ModuleSpi module;
    private final EntityStoreUnitOfWork underlying;

    public ModuleEntityStoreUnitOfWork( ModuleSpi module, EntityStoreUnitOfWork underlying )
    {
        this.module = module;
        this.underlying = underlying;
    }

    public ModuleSpi module()
    {
        return module;
    }

    @Override
    public String identity()
    {
        return underlying.identity();
    }

    @Override
    public long currentTime()
    {
        return underlying.currentTime();
    }

    @Override
    public EntityState newEntityState( ModuleSpi module, EntityReference reference, EntityDescriptor descriptor )
        throws EntityStoreException
    {
        return underlying.newEntityState( module, reference, descriptor );
    }

    @Override
    public EntityState entityStateOf( ModuleSpi module, EntityReference reference )
        throws EntityStoreException, EntityNotFoundException
    {
        return underlying.entityStateOf( module, reference );
    }

    @Override
    public StateCommitter applyChanges()
        throws EntityStoreException
    {
        return underlying.applyChanges();
    }

    @Override
    public void discard()
    {
        underlying.discard();
    }

    public Usecase usecase()
    {
        return underlying.usecase();
    }
}
