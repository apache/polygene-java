package org.qi4j.library.rest.server.restlet.freemarker;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.qi4j.api.Qi4j;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

/**
 * TODO
 */
public class ValueCompositeTemplateModel
    implements TemplateHashModelEx, TemplateScalarModel
{
    private ValueComposite composite;
    private ObjectWrapper wrapper;
    private ValueDescriptor descriptor;

    public ValueCompositeTemplateModel( ValueComposite composite, ObjectWrapper wrapper )
    {
        this.composite = composite;
        this.wrapper = wrapper;
        descriptor = (ValueDescriptor) Qi4j.FUNCTION_DESCRIPTOR_FOR.map( composite );
    }

    @Override
    public int size()
        throws TemplateModelException
    {
        return (int) Iterables.count( descriptor.state().properties() );
    }

    @Override
    public TemplateCollectionModel keys()
        throws TemplateModelException
    {
        return (TemplateCollectionModel) wrapper.wrap( Iterables.map( new Function<PropertyDescriptor, String>()
        {
            @Override
            public String map( PropertyDescriptor propertyDescriptor )
            {
                return propertyDescriptor.qualifiedName().name();
            }
        }, descriptor.state().properties() ).iterator() );
    }

    @Override
    public TemplateCollectionModel values()
        throws TemplateModelException
    {
        return (TemplateCollectionModel) wrapper.wrap( Iterables.map( new Function<Property<?>, Object>()
        {
            @Override
            public Object map( Property<?> objectProperty )
            {
                try
                {
                    return wrapper.wrap( objectProperty.get() );
                }
                catch( TemplateModelException e )
                {
                    throw new IllegalStateException( e );
                }
            }
        }, Qi4j.FUNCTION_COMPOSITE_INSTANCE_OF.map( composite ).state().properties() ).iterator() );
    }

    @Override
    public TemplateModel get( String key )
        throws TemplateModelException
    {
        try
        {
            return wrapper.wrap( Qi4j.FUNCTION_COMPOSITE_INSTANCE_OF
                                     .map( composite )
                                     .state()
                                     .propertyFor( descriptor.state().findPropertyModelByName( key ).accessor() )
                                     .get() );
        }
        catch( IllegalArgumentException e )
        {
            return null;
        }
    }

    @Override
    public boolean isEmpty()
        throws TemplateModelException
    {
        return size() == 0;
    }

    @Override
    public String getAsString()
        throws TemplateModelException
    {
        return composite.toString();
    }
}
