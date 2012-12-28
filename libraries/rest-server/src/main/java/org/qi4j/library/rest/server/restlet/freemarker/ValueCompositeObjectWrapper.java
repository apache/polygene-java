package org.qi4j.library.rest.server.restlet.freemarker;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.qi4j.api.value.ValueComposite;

/**
 * ObjectWrapper implementation that adds support for ValueComposites, exposing Property state.
 */
public class ValueCompositeObjectWrapper
    extends DefaultObjectWrapper
{
    @Override
    public TemplateModel wrap( Object obj )
        throws TemplateModelException
    {
        if( obj instanceof ValueComposite )
        {
            return new ValueCompositeTemplateModel( (ValueComposite) obj, this );
        }
        else
        {
            return super.wrap( obj );
        }
    }
}
