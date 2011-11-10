/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.vaadin;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import org.qi4j.api.injection.scope.Service;

/**
 * @author Paul Merlin
 */
public class MyVaadinApplication
        extends Application
{

    private static final long serialVersionUID = 1L;

    private Window window;

    @Service
    private GreetService greeter;

    @Override
    public void init()
    {
        final TextField field = new TextField();
        field.setInputPrompt( "Type your name here" );
        Button button = new Button( "Greetings mortals.." );
        button.addListener( new Button.ClickListener()
        {

            public void buttonClick( ClickEvent event )
            {
                String name = "" + field.getValue();
                if ( name.length() <= 0 ) {
                    window.showNotification( "You must type your name in the field", Window.Notification.TYPE_ERROR_MESSAGE );
                } else {
                    window.showNotification( greeter.greet( "" + field.getValue() ) );
                }
            }

        } );

        HorizontalLayout horizLayout = new HorizontalLayout();
        horizLayout.setMargin( true );
        horizLayout.addComponent( field );
        horizLayout.addComponent( button );

        Panel panel = new Panel( "Vaadin seems nice : )" );
        panel.addComponent( horizLayout );

        VerticalLayout vertLayout = new VerticalLayout();
        vertLayout.setMargin( true );
        vertLayout.addComponent( panel );

        window = new Window( "test app" );
        window.setSizeFull();
        window.setContent( vertLayout );
        setMainWindow( window );
    }

}
