/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.envisage;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import org.qi4j.api.structure.ApplicationDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Qi4J Application Viewer
 */
public class Envisage
{
    protected ApplicationDescriptor application;

    public void run( ApplicationDescriptor application )
    {
        initLookAndFeel();

        this.application = application;

        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                showMainFrame();
            }
        } );
    }

    private void showMainFrame()
    {
        final EnvisageFrame mainFrame = new EnvisageFrame( application );
        mainFrame.setLocationByPlatform( true );
        mainFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        mainFrame.setSize( new Dimension( 1024, 768 ) );
        mainFrame.setVisible( true );

        mainFrame.addWindowListener( new WindowAdapter()
        {
            public void windowOpened( WindowEvent evt )
            {
                SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        mainFrame.initQi4J();
                    }
                } );
            }
        } );
    }

    private void initLookAndFeel()
    {
        String osName = System.getProperty( "os.name" ).toUpperCase();

        // set to use swing anti alias text only for JVM <= 1.5 
        System.setProperty( "swing.aatext", "true" );

        // set default swing bold to false, only for JVM 1.5 or above
        UIManager.put( "swing.boldMetal", Boolean.FALSE );

        // set LaF
        LookAndFeel lnf = UIManager.getLookAndFeel();
        if( lnf != null && lnf.getID().equalsIgnoreCase( "Metal" ) )
        {
            String lnfClassName = null;
            if( osName.startsWith( "MAC" ) )
            {
                System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "Envisage" ); //TODO i18n
                System.setProperty( "apple.laf.useScreenMenuBar", "true" );
                lnfClassName = UIManager.getSystemLookAndFeelClassName();
            }
            else if( osName.startsWith( "WINDOWS" ) )
            {
                UIManager.put( "ClassLoader", LookUtils.class.getClassLoader() );
                lnfClassName = Options.getSystemLookAndFeelClassName();
                Options.setUseNarrowButtons( false );
            }
            else
            {
                UIManager.put( "ClassLoader", LookUtils.class.getClassLoader() );
                lnfClassName = Options.getCrossPlatformLookAndFeelClassName();
                PlasticLookAndFeel.setTabStyle( PlasticLookAndFeel.TAB_STYLE_METAL_VALUE );
                PlasticLookAndFeel.setPlasticTheme( new ExperienceBlue() );
                Options.setUseNarrowButtons( false );
                //PlasticLookAndFeel.setMyCurrentTheme(new ExperienceBlueDefaultFont());  // for CJK Font
            }

            if( lnfClassName != null )
            {
                try
                {
                    UIManager.setLookAndFeel( lnfClassName );
                }
                catch( Exception ex )
                {
                    System.err.println( "Unable to set LookAndFeel, use default LookAndFeel.\n" + ex.getMessage() );
                }
            }
        }
    }
}
