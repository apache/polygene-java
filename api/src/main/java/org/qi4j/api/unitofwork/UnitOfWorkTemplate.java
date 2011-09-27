package org.qi4j.api.unitofwork;

import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;

/**
 * TODO
 */
public abstract class UnitOfWorkTemplate<RESULT, ThrowableType extends Throwable>
{
    private Usecase usecase = Usecase.DEFAULT;
    private int retries = 10;
    private boolean complete = true;

    protected UnitOfWorkTemplate()
    {
    }

    protected UnitOfWorkTemplate( int retries, boolean complete )
    {
        this.retries = retries;
        this.complete = complete;
    }

    protected UnitOfWorkTemplate( Usecase usecase, int retries, boolean complete )
    {
        this.usecase = usecase;
        this.retries = retries;
        this.complete = complete;
    }

    protected abstract RESULT withUnitOfWork(UnitOfWork uow)
        throws ThrowableType;

    public RESULT withModule(Module module)
            throws ThrowableType
    {
        UnitOfWork uow = module.newUnitOfWork( usecase );

        int loop = 0;
        Throwable ex;
        do
        {
            try
            {
                RESULT result = withUnitOfWork( uow );
                try
                {
                    uow.complete();
                    return result;
                } catch( ConcurrentEntityModificationException e )
                {
                    // Retry?
                    ex = e;
                }
            } catch( Throwable e )
            {
                uow.discard();
                throw (ThrowableType) e;
            }
        } while (loop++ < retries);

        throw ex;
    }
}
