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
package org.apache.polygene.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.findFields;

public class TemporaryFolder
    implements Extension, BeforeEachCallback, AfterEachCallback
{
    private File root;

    public TemporaryFolder()
    {
    }

    @Override
    public void beforeEach( ExtensionContext context )
        throws Exception
    {
        try
        {
            root = File.createTempFile( "junit5-", ".tmp" );
        }
        catch( IOException ioe )
        {
            throw new RuntimeException( ioe );
        }
        root.delete(); // Remove if already exists
        root.mkdir();

        findFields( context.getRequiredTestClass(),
                    f -> f.getType().equals( TemporaryFolder.class ), BOTTOM_UP )
            .forEach( f -> {
                try
                {
                    f.setAccessible( true );
                    f.set( context.getRequiredTestInstance(), this );
                }
                catch( IllegalAccessException e )
                {
                    throw new UndeclaredThrowableException( e );
                }
            } );
    }

    @Override
    public void afterEach( ExtensionContext context )
        throws Exception
    {
        try
        {
            Files.walkFileTree( root.toPath(), new DeleteAllVisitor() );
        }
        catch( IOException ioe )
        {
            throw new RuntimeException( ioe );
        }
    }

    public File file( String name )
        throws IOException
    {
        return new File( root, name );
    }

    public File getRoot()
    {
        return root;
    }

    private static class DeleteAllVisitor extends SimpleFileVisitor<Path>
    {
        @Override
        public FileVisitResult visitFile( Path file, BasicFileAttributes attributes )
            throws IOException
        {
            Files.delete( file );
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory( Path directory, IOException exception )
            throws IOException
        {
            Files.delete( directory );
            return CONTINUE;
        }
    }
}