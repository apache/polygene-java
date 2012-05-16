package com.marcgrue.dcisample_b.infrastructure.model;

import com.marcgrue.dcisample_b.infrastructure.conversion.EntityToDTOService;
import org.apache.wicket.model.IModel;
import org.qi4j.api.Qi4j;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * ReadOnlyModel
 *
 * Abstract base model for Wicket model objects taking Qi4j objects.
 */
public abstract class ReadOnlyModel<T>
      implements IModel<T>
{
    private static final long serialVersionUID = 1L;

    static protected EntityToDTOService valueConverter;
    static protected Qi4j qi4j;
    static protected Module module;

    /**
     * This default implementation of setObject unconditionally throws an
     * UnsupportedOperationException. Since the method is final, any subclass is effectively a
     * read-only model.
     *
     * @param object The object to set into the model
     * @throws UnsupportedOperationException
     */
    public final void setObject( final T object )
    {
        throw new UnsupportedOperationException( "Model " + getClass() +
                                                       " does not support setObject(Object)" );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "Model:classname=[" );
        sb.append( getClass().getName() ).append( "]" );
        return sb.toString();
    }

    public static void prepareModelBaseClass( Module m,
                                              Qi4j api,
                                              EntityToDTOService entityToDTO )
    {
        module = m;
        qi4j = api;
        valueConverter = entityToDTO;
    }
}