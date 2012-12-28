package org.qi4j.runtime.structure;

import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.functional.Function;

/**
 * TODO
 */
public class ModelModule<T extends ModelDescriptor>
{
    public static <T extends ModelDescriptor> Function<T, ModelModule<T>> modelModuleFunction( final ModuleInstance module )
    {
        return new Function<T, ModelModule<T>>()
        {
            @Override
            public ModelModule<T> map( T model )
            {
                return new ModelModule<T>( module, model );
            }
        };
    }

    public static <T extends ModelDescriptor> Function<ModelModule<T>, T> modelFunction()
    {
        return new Function<ModelModule<T>, T>()
        {
            @Override
            public T map( ModelModule<T> modelModule )
            {
                return modelModule.model();
            }
        };
    }

    private ModuleInstance module;
    private T model;

    public ModelModule( ModuleInstance module, T model )
    {
        this.module = module;
        this.model = model;
    }

    public ModuleInstance module()
    {
        return module;
    }

    public T model()
    {
        return model;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        ModelModule that = (ModelModule) o;

        if( model != null ? !model.equals( that.model ) : that.model != null )
        {
            return false;
        }
        if( module != null ? !module.equals( that.module ) : that.module != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = module != null ? module.hashCode() : 0;
        result = 31 * result + ( model != null ? model.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString()
    {
        return module.name() + ":" + model;
    }
}
