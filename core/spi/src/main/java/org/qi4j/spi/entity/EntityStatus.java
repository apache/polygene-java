package org.qi4j.spi.entity;

/**
 * Status of entity. This is used in the default implementation
 * of EntityState
 */
public enum EntityStatus
{
    NEW, // When the Entity has just been created in the UnitOfWork 
    LOADED,  // When it has been previously created, and is loaded through the UnitOfWork
    UPDATED, // When it has been previously loaded, and has been changed in the UnitOfWork
    REMOVED // When the Entity has been removed in the UnitOfWork
}
