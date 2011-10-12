package org.qi4j.library.rest.server.restlet.freemarker;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.qi4j.api.Qi4j;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;

/**
 * TODO
 */
public class ValueCompositeTemplateModel
    implements TemplateHashModelEx
{
    private ValueComposite composite;
    private ObjectWrapper wrapper;
    private ValueDescriptor descriptor;

    public ValueCompositeTemplateModel( ValueComposite composite, ObjectWrapper wrapper )
    {
        this.composite = composite;
        this.wrapper = wrapper;
        descriptor = (ValueDescriptor) Qi4j.DESCRIPTOR_FUNCTION.map( composite );
    }

    @Override
    public int size()
        throws TemplateModelException
    {
        return (int) Iterables.count(descriptor.state().properties());
    }

    @Override
    public TemplateCollectionModel keys()
        throws TemplateModelException
    {
        return (TemplateCollectionModel) wrapper.wrap( descriptor.state().properties() );
    }

    @Override
    public TemplateCollectionModel values()
        throws TemplateModelException
    {
        return (TemplateCollectionModel) wrapper.wrap(Iterables.map( new Function<Property<?>, Object>()
        {
            @Override
            public Object map( Property<?> objectProperty )
            {
                return objectProperty.get();
            }
        }, Qi4j.INSTANCE_FUNCTION.map( composite ).state().properties()));
    }

    @Override
    public TemplateModel get( String key )
        throws TemplateModelException
    {
        try
        {
            return wrapper.wrap( Qi4j.INSTANCE_FUNCTION
                                     .map( composite )
                                     .state()
                                     .propertyFor( descriptor.state().getPropertyByName( key ).accessor() )
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
}
