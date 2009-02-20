/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.swing.binding.example.dom;

import java.awt.Container;
import java.io.InputStream;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.swing.binding.tree.TreeNodeComposite;
import org.w3c.dom.Document;

/**
 * JAVADOC
 */
public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        SingletonAssembler singletonAssembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites( DocumentComposite.class );
                module.addComposites( TreeNodeComposite.class );
                module.addComposites( ElementComposite.class );
                module.addComposites( TextComposite.class );
                module.addComposites( AttributeComposite.class );
            }
        };
        CompositeBuilderFactory cbf = singletonAssembly.compositeBuilderFactory();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputStream testStream = Main.class.getResourceAsStream( "test.xml" );
        Document doc = documentBuilder.parse( testStream );
        CompositeBuilder<DocumentComposite> docBuilder = cbf.newCompositeBuilder( DocumentComposite.class );
        docBuilder.use( doc );
        DocumentComposite documentComposite = docBuilder.newInstance();

        CompositeBuilder<TreeNodeComposite> tnacBuilder = cbf.newCompositeBuilder( TreeNodeComposite.class );
        tnacBuilder.use( documentComposite );
        TreeNodeComposite root = tnacBuilder.newInstance();

        JTree tree = new JTree( root );
        tree.setCellRenderer( root );

        JFrame frame = new JFrame( "Document viewer" );
        Container contentPane = frame.getContentPane();
        contentPane.add( tree );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setSize( 300, 400 );
        frame.setVisible( true );
    }

    protected void setUp()
        throws Exception
    {
    }
}
