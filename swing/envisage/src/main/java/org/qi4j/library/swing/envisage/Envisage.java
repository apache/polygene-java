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
package org.qi4j.library.swing.envisage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.LookAndFeel;
import java.awt.Dimension;
import org.qi4j.library.swing.envisage.EnvisageFrame;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.api.structure.Application;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

/**
 * Qi4J Application Viewer
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 *
 */
public class Envisage
{
    protected Energy4Java qi4j;
    protected Application application;

    public void run(Energy4Java qi4j, Application application)
    {
        initLookAndFeel();

        this.qi4j = qi4j;
        this.application = application;
        
        //final JPanel mainPane = createMainPanel();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showMainFrame();
            }
        });
    }

    private void showMainFrame() {
        EnvisageFrame mainFrame = new EnvisageFrame( qi4j, application );
        mainFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        mainFrame.setSize( new Dimension(800, 600) );
        mainFrame.setVisible( true );
        

        /*JFrame frame = new JFrame("Envisage");
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setSize( new Dimension(800, 600) );
        frame.setLayout( new BorderLayout( ) );
        frame.add( mainPane, BorderLayout.CENTER );
        frame.setVisible( true );
        */
    }

    /*private JPanel createMainPanel()
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout( new BorderLayout( ) );

        JSplitPane splitPane = new JSplitPane( );
        mainPanel.add( splitPane, BorderLayout.CENTER );

        ApplicationModelPane appModelPane = new ApplicationModelPane();
        appModelPane.initQi4J( qi4j, application );

        DetailModelPane detailModelPane = new DetailModelPane();

        splitPane.setLeftComponent( appModelPane );
        splitPane.setRightComponent( detailModelPane );
        splitPane.setDividerLocation( 300 );

        return mainPanel;
    } */

    private void initLookAndFeel() {
        String osName = System.getProperty("os.name").toUpperCase();

        // set default swing bold to false, only for JVM 1.5 or above
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        // set LaF
        LookAndFeel lnf = UIManager.getLookAndFeel();
        if (lnf != null && lnf.getID().equalsIgnoreCase("Metal")) {
            String lnfClassName = null;
            if (osName.startsWith("MAC")) {
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Envisage"); //TODO i18n
                System.setProperty("apple.laf.useScreenMenuBar","true");
                lnfClassName = UIManager.getSystemLookAndFeelClassName();
            } else if (osName.startsWith("WINDOWS")) {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                lnfClassName = Options.getSystemLookAndFeelClassName();
                Options.setUseNarrowButtons(false);
            } else {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                lnfClassName = Options.getCrossPlatformLookAndFeelClassName();
                PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_METAL_VALUE);
                PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                Options.setUseNarrowButtons(false);
                //PlasticLookAndFeel.setMyCurrentTheme(new ExperienceBlueDefaultFont());  // for CJK Font
            }

            if (lnfClassName != null) {
                try {
                    UIManager.setLookAndFeel(lnfClassName);
                } catch (Exception ex) {
                    System.err.println("Unable to set LookAndFeel, use default LookAndFeel.\n" + ex.getMessage());
                }
            }
        }
    }
}
