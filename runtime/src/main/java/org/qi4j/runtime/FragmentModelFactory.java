package org.qi4j.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import org.qi4j.model.FragmentModel;
import org.qi4j.model.InvalidCompositeException;
import org.qi4j.model.NullArgumentException;

/**
 * TODO
 */
public abstract class FragmentModelFactory<K extends FragmentModel>
    extends AbstractModelFactory
{
    public abstract <T> K newFragmentModel( Class<T> fragmentClass, Class compositeType, Class declaredBy )
        throws NullArgumentException, InvalidCompositeException;

    protected <T> Class<T> getFragmentClass( Class<T> mixinClass )
    {
        if( Modifier.isAbstract( mixinClass.getModifiers() ) )
        {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass( mixinClass );
            enhancer.setCallbackTypes( new Class[]{ NoOp.class } );
            enhancer.setCallbackFilter( new CallbackFilter()
            {

                public int accept( Method method )
                {
                    return 0;
                }
            } );
            mixinClass = enhancer.createClass();
            Enhancer.registerStaticCallbacks( mixinClass, new Callback[]{ NoOp.INSTANCE } );
        }
        return mixinClass;
    }
}