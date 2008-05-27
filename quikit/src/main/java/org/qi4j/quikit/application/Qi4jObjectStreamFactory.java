/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.quikit.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.apache.wicket.util.io.IObjectStreamFactory;
import org.apache.wicket.util.io.SerializableChecker;
import org.qi4j.Qi4j;
import org.qi4j.composite.scope.Structure;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.spi.serialization.CompositeInputStream;
import org.qi4j.spi.serialization.CompositeOutputStream;

/**
 * @author edward.yakop@gmail.com
 * @since 0.2.0
 */
public final class Qi4jObjectStreamFactory
    implements IObjectStreamFactory
{
    private static final Logger LOGGER = Logger.getLogger( Qi4jObjectStreamFactory.class.getName() );

    @Structure
    private UnitOfWorkFactory uowf;

    @Structure
    private Qi4j qi4j;

    public final ObjectInputStream newObjectInputStream( InputStream in )
        throws IOException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        return new CompositeInputStream( in, uow, qi4j );
    }

    public final ObjectOutputStream newObjectOutputStream( OutputStream out )
        throws IOException
    {
        final CompositeOutputStream oos = new CompositeOutputStream( out );
        return new ObjectOutputStream()
        {
            @Override
            protected final void writeObjectOverride( final Object obj ) throws IOException
            {
                try
                {
                    oos.writeObject( obj );
                }
                catch( IOException e )
                {
                    if( SerializableChecker.isAvailable() )
                    {
                        // trigger serialization again, but this time gather some more info
                        new SerializableChecker( (NotSerializableException) e ).writeObject( obj );

                        // if we get here, we didn't fail, while we should;
                        throw e;
                    }
                    throw e;
                }
                catch( RuntimeException e )
                {
                    LOGGER.throwing( CompositeOutputStream.class.getName(), "replaceObject", e );
                    throw e;
                }
            }

            @Override
            public final void flush()
                throws IOException
            {
                oos.flush();
            }

            @Override
            public final void close()
                throws IOException
            {
                oos.close();
            }
        };
    }
}
