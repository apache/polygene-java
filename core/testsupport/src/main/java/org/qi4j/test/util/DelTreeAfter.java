/*
 * Copyright 2013 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.util;

import java.io.File;
import java.io.IOException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Test Rule that delete directories after the test.
 */
public class DelTreeAfter
    implements TestRule
{

    private final boolean createDirsBefore;
    private final File[] directories;

    public DelTreeAfter( File... directories )
    {
        this( false, directories );
    }

    public DelTreeAfter( boolean createDirsBefore, File[] directories )
    {
        this.createDirsBefore = createDirsBefore;
        this.directories = directories;
    }

    @Override
    public Statement apply( final Statement base, Description description )
    {
        return new Statement()
        {
            @Override
            public void evaluate()
                throws Throwable
            {
                if( createDirsBefore )
                {
                    for( File dir : directories )
                    {
                        if( !dir.mkdirs() )
                        {
                            throw new IOException( "Unable to create directory: " + dir );
                        }
                    }
                }
                try
                {
                    base.evaluate();
                }
                finally
                {
                    if( directories != null )
                    {
                        for( File dir : directories )
                        {
                            if( dir.exists() )
                            {
                                delTree( dir );
                            }
                        }
                    }
                }
            }
        };
    }

    private static void delTree( File dir )
    {
        if( dir.isDirectory() )
        {
            for( File file : dir.listFiles() )
            {
                delTree( file );
            }
            dir.delete();
        }
        else
        {
            dir.delete();
        }
    }

}
