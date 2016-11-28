/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.library.uowfile.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UoWFile
{
    /* package */ static final Logger LOGGER = LoggerFactory.getLogger( "org.apache.zest.library.uowfile" );
    private static final AtomicLong COUNT = new AtomicLong( 0L );
    private final long originalIdentity;
    private final File original;
    private final File current;
    private final File backup;

    UoWFile( File original, File workDir )
    {
        this.originalIdentity = original.length() + original.lastModified();
        this.original = original;
        long count = COUNT.incrementAndGet();
        this.current = new File( workDir, original.getName() + ".current." + count );
        this.backup = new File( workDir, original.getName() + ".backup." + count );
    }

    public File asFile()
    {
        return current;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder().append( UoWFile.class.getSimpleName() );
        // UoWFile{parent/( original(oid->id) | current(id) | backup(id) )}
        sb.append( "{" ).append( original.getParentFile().getName() ).append( "/( " )
          .append( original.getName() ).append( "(" ).append( originalIdentity ).append( "->" )
          .append( fileTag( original ) ).append( ") | " ).append( current.getName() )
          .append( "(" ).append( fileTag( current ) ).append( ") | " ).append( backup.getName() )
          .append( "(" ).append( fileTag( backup ) ).append( ") )}" );
        return sb.toString();
    }

    void copyOriginalToCurrent()
    {
        if( original.exists() )
        {
            copy( original, current );
        }
    }

    void apply()
        throws ConcurrentUoWFileStateModificationException
    {
        LOGGER.trace( "Will apply changes to {}", this );
        if( fileTag( current ) != originalIdentity )
        {
            if( fileTag( original ) != originalIdentity )
            {
                LOGGER.info(
                    "Concurrent modification, original creation reference is {} and original apply reference is {}",
                    originalIdentity, fileTag( original ) );
                throw new ConcurrentUoWFileStateModificationException( this );
            }
            if( original.exists() )
            {
                move( original, backup );
            }
            if( current.exists() )
            {
                move( current, original );
            }
            LOGGER.debug( "Applied changes to {}", original );
        }
    }

    void rollback()
    {
        if( backup.exists() )
        {
            if( fileTag( original ) != originalIdentity )
            {
                delete( original );
                move( backup, original );
            }
            LOGGER.debug( "Restored backup to {}", original );
        }
    }

    void cleanup()
    {
        if( current.exists() )
        {
            delete( current );
        }
        if( backup.exists() )
        {
            delete( backup );
        }
    }

    /**
     * @return OL if the file does not exist
     */
    private long fileTag( File file )
    {
        return file.length() + file.lastModified();
    }

    private void copy( File source, File dest )
    {
        try
        {
            Files.copy( source.toPath(), dest.toPath() );
        }
        catch( IOException ex )
        {
            throw new UoWFileException( ex );
        }
    }

    private void delete( File file )
    {
        if( !file.delete() )
        {
            throw new UoWFileException( new IOException( "Unable to delete file " + file ) );
        }
    }

    private void move( File source, File dest )
    {
        // Atomic move attempt
        if( !source.renameTo( dest ) )
        {
            // source and dest are probably on different filesystem, fallback to a non atomic copy/move operation
            copy( source, dest );
            if( !source.delete() )
            {
                throw new UoWFileException( new IOException( "Unable to delete source file " + source
                                                             + " after copy(move) to " + dest
                                                             + " (rename failed before that)." ) );
            }
            LOGGER.warn( "Moved {} to {} using a copy/delete operation instead of an atomic move. "
                         + "Are they on different filesystems?", source, dest );
        }
    }
}
