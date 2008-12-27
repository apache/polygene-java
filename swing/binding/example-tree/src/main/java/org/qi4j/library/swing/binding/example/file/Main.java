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
package org.qi4j.library.swing.binding.example.file;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JTree;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.library.swing.binding.tree.TreeNodeComposite;

/**
 * TODO
 */
public final class Main
{
    public static void main( String[] args )
        throws Exception
    {
        SingletonAssembler singletonAssembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addComposites( DirectoryComposite.class );
                module.addComposites( FileComposite.class );
                module.addComposites( TreeNodeComposite.class );
            }
        };
        CompositeBuilderFactory cbf = singletonAssembly.compositeBuilderFactory();

        File rootdir = new File( "/" );
        CompositeBuilder<DirectoryComposite> builder1 = cbf.newCompositeBuilder( DirectoryComposite.class );
        DirectoryComposite prototype = builder1.stateOfComposite();
        prototype.file().set( rootdir );
        DirectoryComposite dir = builder1.newInstance();

        CompositeBuilder<TreeNodeComposite> builder = cbf.newCompositeBuilder( TreeNodeComposite.class );
        builder.use( dir );
        TreeNodeComposite root = builder.newInstance();
        JTree tree = new JTree( root );
        tree.setCellRenderer( root );

        JFrame frame = new JFrame( "Directory viewer" );
        frame.getContentPane().add( tree );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setSize( 300, 400 );
        frame.setVisible( true );
    }
}
