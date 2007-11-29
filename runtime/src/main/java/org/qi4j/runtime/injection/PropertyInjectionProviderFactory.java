package org.qi4j.runtime.injection;

import java.util.Map;
import org.qi4j.PropertyValue;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.dependency.InjectionContext;
import org.qi4j.spi.dependency.InjectionProvider;
import org.qi4j.spi.dependency.InjectionProviderFactory;
import org.qi4j.spi.dependency.InjectionResolution;
import org.qi4j.spi.dependency.InvalidInjectionException;
import org.qi4j.spi.dependency.MixinInjectionContext;
import org.qi4j.spi.dependency.PropertyInjectionModel;

/**
 * TODO
 */
public class PropertyInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( InjectionResolution resolution ) throws InvalidInjectionException
    {
        return new PropertyInjectionProvider( resolution );
    }

    private class PropertyInjectionProvider implements InjectionProvider
    {
        private InjectionResolution resolution;

        public PropertyInjectionProvider( InjectionResolution resolution )
        {
            this.resolution = resolution;
        }

        public Object provideInjection( InjectionContext context )
        {
            if( context instanceof MixinInjectionContext )
            {
                MixinInjectionContext mixinContext = (MixinInjectionContext) context;

                // Check whether one or more properties should be injected
                if( resolution.getInjectionModel().getInjectedClass().equals( PropertyValue.class ) && resolution.getInjectionModel().getRawInjectionType().equals( Iterable.class ) )
                {
                    return mixinContext.getProperties().values();
                }
                else
                {
                    // TODO This needs to be muuuuch better. The injection point should be matched to a specific property model at this point
                    PropertyInjectionModel propertyModel = (PropertyInjectionModel) resolution.getInjectionModel();
                    String name = propertyModel.getName();
                    for( Map.Entry<PropertyResolution, PropertyValue> entry : mixinContext.getProperties().entrySet() )
                    {
                        if( entry.getKey().getPropertyModel().getName().equals( name ) )
                        {
                            return entry.getValue().getValue();
                        }

                    }
                }

                return null;
            }

            return null;
        }
    }
}