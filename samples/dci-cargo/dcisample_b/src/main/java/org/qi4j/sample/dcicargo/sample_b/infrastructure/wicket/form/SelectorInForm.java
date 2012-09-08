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
package org.qi4j.sample.dcicargo.sample_b.infrastructure.wicket.form;

import java.util.List;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;

/**
 * SelectorInForm
 *
 * Convenience drop down list that resolves properties against a form.
 */
public class SelectorInForm extends DropDownChoice<String>
{
    PropertyModel<String> disablingPropertyModel;
    String disablingValue = "";

    public SelectorInForm( String property, String label, List<String> choices, Form form )
    {
        super( property + "Selector", new PropertyModel<String>( form, property ), new ListModel<String>( choices ) );
        setNonEmptyLabel( label );
        setOutputMarkupId( true );
    }

    public SelectorInForm( String property, String label, List<String> choices, Form form, String disablingProperty )
    {
        super( property + "Selector", new PropertyModel<String>( form, property ), new ListModel<String>( choices ) );
        disablingPropertyModel = new PropertyModel<String>( form, disablingProperty );
        setNonEmptyLabel( label );
        setOutputMarkupId( true );
    }

    private void setNonEmptyLabel( String label )
    {
        if( label == null )
        {
            return;
        }

        if( label.isEmpty() )
        {
            throw new IllegalArgumentException( "Can't set an empty label on the drop down selector." );
        }

        setLabel( Model.of( label ) );
    }

    @Override
    protected boolean isDisabled( final String currentValue, int index, String selected )
    {
        if( disablingPropertyModel != null )
        {
            disablingValue = disablingPropertyModel.getObject();
        }

        return currentValue.equals( disablingValue );
    }
}