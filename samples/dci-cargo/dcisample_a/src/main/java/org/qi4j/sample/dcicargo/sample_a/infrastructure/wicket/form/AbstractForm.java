/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_a.infrastructure.wicket.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.StatelessForm;
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
                if( isOtherComponent( fc ) || fc.isValid() )
                {
                    return;
                }

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
