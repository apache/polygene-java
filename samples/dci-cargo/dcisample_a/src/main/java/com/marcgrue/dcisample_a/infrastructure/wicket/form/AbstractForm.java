package com.marcgrue.dcisample_a.infrastructure.wicket.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 *
 */
//public abstract class AbstractForm<T> extends Form<T>
public abstract class AbstractForm<T> extends StatelessForm<T>
{
    public AbstractForm( String id )
    {
        super( id );
    }
    public AbstractForm()
    {
        super( "form" );
    }

    public void focusFirstError( final AjaxRequestTarget target )
    {
        visitFormComponents( new IVisitor<FormComponent<?>, Void>()
        {
            public void component( FormComponent<?> fc, IVisit<Void> visit )
            {
                if (isOtherComponent( fc ) || fc.isValid())
                    return;

                target.focusComponent( fc );
                visit.stop();
            }
        } );
    }

    private boolean isOtherComponent( FormComponent<?> fc )
    {
        return fc instanceof Button || fc instanceof CheckBox || fc instanceof RadioChoice;
    }
}
