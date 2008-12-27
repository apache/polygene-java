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
import java.util.List;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.swing.binding.tree.Container;
import org.qi4j.library.swing.binding.tree.Child;

/**
 * TODO
 */
public final class DirectoryListingContainerConcern extends ConcernOf<Container>
    implements Container
{
    @This private FilePath meAsPath;
    @Structure private CompositeBuilderFactory factory;

    public List<Child> children()
    {
        List<Child> children = next.children();
        if( children.size() == 0 )
        {
            // Populate list
            File[] childFiles = meAsPath.file().get().listFiles();
            if( childFiles == null )
            {
                return children;
            }
            for( File child : childFiles )
            {
                if( child.isDirectory() )
                {
                    CompositeBuilder<DirectoryComposite> builder = factory.newCompositeBuilder( DirectoryComposite.class );
                    builder.stateOfComposite().file().set( child );
                    DirectoryComposite directoryComposite = builder.newInstance();
                    children.add( directoryComposite );
                }
                else
                {
                    CompositeBuilder<FileComposite> builder = factory.newCompositeBuilder( FileComposite.class );
                    builder.stateOfComposite().file().set( child );
                    FileComposite file = builder.newInstance();
                    children.add( file );
                }
            }
        }
        return children;
    }
}
