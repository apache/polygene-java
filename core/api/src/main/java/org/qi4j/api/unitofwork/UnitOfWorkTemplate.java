package org.qi4j.api.unitofwork;

import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;

/**
 * UnitOfWork Template.
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

    protected abstract RESULT withUnitOfWork( UnitOfWork uow )
        throws ThrowableType;

    @SuppressWarnings( "unchecked" )
    public RESULT withModule( Module module )
        throws ThrowableType, UnitOfWorkCompletionException
    {
        int loop = 0;
        ThrowableType ex = null;
        do
        {
            UnitOfWork uow = module.newUnitOfWork( usecase );

            try
            {
                RESULT result = withUnitOfWork( uow );
                if( complete )
                {
                    try
                    {
                        uow.complete();
                        return result;
                    }
                    catch( ConcurrentEntityModificationException e )
                    {
                        // Retry?
                        ex = (ThrowableType) e;
                    }
                }
            }
            catch( Throwable e )
            {
                ex = (ThrowableType) e;
            }
            finally
            {
                uow.discard();
            }
        }
        while( loop++ < retries );

        throw ex;
    }
}
