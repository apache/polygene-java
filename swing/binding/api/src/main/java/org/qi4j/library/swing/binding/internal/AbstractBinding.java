package org.qi4j.library.swing.binding.internal;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.library.swing.binding.StateModel;
import org.qi4j.library.swing.binding.SwingAdapter;
import org.qi4j.library.swing.binding.SwingBinding;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;

/**
 * @author Lan Boon Ping
 */
public abstract class AbstractBinding<T, Y, K> implements SwingBinding<T>, BoundField<Y, K>
{

    protected Method fieldMethod;
    protected Class<T> type;

    protected StateModel<T> stateModel;
    protected String name;

    protected Map<Class<? extends JComponent>, SwingAdapter> adapters;

    @SuppressWarnings( "unchecked" )
    public AbstractBinding( @Uses Method aFieldMethod, @Structure ObjectBuilderFactory anOBF,
                            @Service Iterable<SwingAdapter> allAdapters )
    {
        fieldMethod = aFieldMethod;
        ParameterizedType propertyType = (ParameterizedType) aFieldMethod.getGenericReturnType();
        Type[] actualTypeArguments = propertyType.getActualTypeArguments();

        type = (Class<T>) actualTypeArguments[ 0 ];
        name = aFieldMethod.getName();
        ObjectBuilder<StateModel> builder = anOBF.newObjectBuilder( StateModel.class );
        builder.use( type, this );
        stateModel = builder.newInstance();

        setupAdapters( allAdapters );
    }

    private void setupAdapters( Iterable<SwingAdapter> allAdapters )
    {
        adapters = new HashMap<Class<? extends JComponent>, SwingAdapter>();
        for( SwingAdapter adapterCandidate : allAdapters )
        {
            Set<SwingAdapter.Capabilities> canHandle = adapterCandidate.canHandle();
            for( SwingAdapter.Capabilities capabilities : canHandle )
            {
                if( requiredCapabilitySatisfied( capabilities ) )
                {
                    if( capabilities.fieldType.equals( type ) )
                    {
                        Class<? extends JComponent> component = capabilities.component;
                        adapters.put( component, adapterCandidate );
                    }
                }
            }
        }
    }

    protected abstract boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities );

    @Override
    public String toString()
    {
        return name + "[" + type.getSimpleName() + "] -> " + stateModel.toString();
    }

}
