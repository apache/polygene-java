package org.qi4j.runtime;

import org.qi4j.api.model.FragmentModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.NullArgumentException;

/**
 * TODO
 */
public abstract class FragmentModelFactory<K extends FragmentModel>
    extends AbstractModelFactory
{
    public abstract <T> K newFragmentModel( Class<T> fragmentClass, Class compositeType )
        throws NullArgumentException, InvalidCompositeException;
}