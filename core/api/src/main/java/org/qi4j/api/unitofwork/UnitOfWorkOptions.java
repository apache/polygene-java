package org.qi4j.api.unitofwork;

/**
 * Set instances of this in MetaInfo on UnitOfWork or the associated Usecase.
 *  <p>
 * Options:
 *  </p>
 * <p>
 * "pruneOnPause": if true, then clear out all instances that have been loaded in the UoW but not modified
 * </p>
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
