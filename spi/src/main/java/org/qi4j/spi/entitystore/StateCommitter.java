package org.qi4j.spi.entitystore;

/**
 * After all EntityStore's have been prepared by {@link org.qi4j.api.unitofwork.UnitOfWork#complete()}
 */
public interface StateCommitter
{
    void commit();

    void cancel();
}
