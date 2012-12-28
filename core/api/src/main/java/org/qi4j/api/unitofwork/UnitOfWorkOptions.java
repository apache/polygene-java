package org.qi4j.api.unitofwork;

/**
 * Set instances of this in MetaInfo on UnitOfWork or the associated Usecase.
 *
 * Options: <br/>
 * "pruneOnPause": if true, then clear out all instances that have been loaded in the UoW but not modified
 */
public class UnitOfWorkOptions
{
    private boolean pruneOnPause = false;

    public UnitOfWorkOptions( boolean pruneOnPause )
    {
        this.pruneOnPause = pruneOnPause;
    }

    public boolean isPruneOnPause()
    {
        return pruneOnPause;
    }
}
