package org.qi4j.library.general.model;

import org.qi4j.api.persistence.PersistentStorage;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.api.persistence.composite.PersistentComposite;
import java.io.Serializable;

public final class DummyPersistentStorage implements PersistentStorage
{
    public void create( PersistentComposite aProxy ) throws PersistenceException
    {
    }

    public void update( PersistentComposite aProxy, Serializable aMixin ) throws PersistenceException
    {
    }

    public void read( PersistentComposite aProxy ) throws PersistenceException
    {
    }

    public void delete( PersistentComposite aProxy ) throws PersistenceException
    {
    }
}
