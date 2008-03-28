/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.property;

import java.io.File;
import org.junit.Test;
import org.qi4j.association.ManyAssociation;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class FileSystemAssociationTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( MemoryEntityStoreComposite.class, UuidIdentityGeneratorComposite.class );

        module.addComposites( Directory.class,
                              FileEntry.class );
    }

    @Test
    public void testAssociation()
        throws Exception
    {
        // Read file system and create entities with associations
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        Directory root = work.newEntityBuilder( Directory.class ).newInstance();

        File rootFile = new File( "." );

        root.name().set( rootFile.getName() );
        populate( work, root, rootFile );
        work.complete();

        // Try loading it
        work = unitOfWorkFactory.newUnitOfWork();
        root = work.getReference( root );

        String indent = "";
        print( root, indent );
    }

    private void print( Directory root, String indent )
    {
        for( Entry entry : root.entries() )
        {
            System.out.println( indent + entry.name() );
            if( entry instanceof Directory )
            {
                print( (Directory) entry, indent + "  " );
            }
        }
    }

    private void populate( UnitOfWork work, Directory root, File rootFile )
    {
        // System.out.println("Populate "+rootFile+" "+root);
        File[] files = rootFile.listFiles();
        for( File file : files )
        {
            if( file.isDirectory() )
            {
                CompositeBuilder<Directory> builder = work.newEntityBuilder( Directory.class );
                Directory dir = builder.propertiesOfComposite();
                dir.name().set( file.getName() );
                dir = builder.newInstance();
                populate( work, dir, file );
                root.entries().add( dir );
            }
            else
            {
                CompositeBuilder<FileEntry> builder = work.newEntityBuilder( FileEntry.class );
                FileEntry fileEntry = builder.propertiesOfComposite();
                fileEntry.name().set( file.getName() );
                fileEntry = builder.newInstance();
                root.entries().add( fileEntry );
            }
        }
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public interface Entry
    {
        Property<String> name();
    }

    public interface Directory
        extends Entry, EntityComposite
    {
        ManyAssociation<Entry> entries();
    }

    public interface FileEntry
        extends Entry, EntityComposite
    {
        Property<Long> size();
    }
}