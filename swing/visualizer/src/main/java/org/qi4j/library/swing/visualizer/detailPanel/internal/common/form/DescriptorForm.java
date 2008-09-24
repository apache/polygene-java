/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.visualizer.detailPanel.internal.common.form;

import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class DescriptorForm extends JPanel
{
    private JLabel tobeRemoved;

    public DescriptorForm()
    {
        super( new BorderLayout() );

        tobeRemoved = new JLabel( "TODO" );
        add( tobeRemoved, CENTER );
    }

    @SuppressWarnings( "unchecked" )
    public final void display( Object aModel )
    {
        removeAll();

        if( aModel != null )
        {
            Class<?> modelClass = aModel.getClass();
            if( ServiceDetailDescriptor.class.isAssignableFrom( modelClass ) )
            {
                ServiceDetailDescriptor descriptor = (ServiceDetailDescriptor) aModel;
                ServiceForm form = new ServiceForm();
                form.updateModel( descriptor );
                add( form, CENTER );
            }
            else
            {
                tobeRemoved.setText( "TODO: " + modelClass.getName() + " " + aModel );
                add( tobeRemoved, CENTER );
            }
        }

        revalidate();
        repaint();
    }
}
