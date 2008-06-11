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

package org.qi4j.spi.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import org.qi4j.Qi4j;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.QualifiedIdentity;

/**
 * TODO
 */
public final class CompositeInputStream extends ObjectInputStream
{
    private UnitOfWork unitOfWork;
    private CompositeBuilderFactory cbf;
    private Qi4j is;

    public CompositeInputStream( InputStream in, UnitOfWork unitOfWork, Qi4j is )
        throws IOException
    {
        super( in );
        this.unitOfWork = unitOfWork;
        this.cbf = unitOfWork.compositeBuilderFactory();
        this.is = is;
        enableResolveObject( true );
    }

    public CompositeInputStream( InputStream in, CompositeBuilderFactory cbf, Qi4j is )
        throws IOException
    {
        super( in );
        this.cbf = cbf;
        this.is = is;
        enableResolveObject( true );
    }

    protected Object resolveObject( Object obj ) throws IOException
    {
        if( obj instanceof QualifiedIdentity && unitOfWork != null )
        {
            QualifiedIdentity holder = (QualifiedIdentity) obj;
            try
            {
                Class<? extends EntityComposite> clazz = (Class<? extends EntityComposite>) Class.forName( holder.type() );
                String id = holder.identity();
                Object instance = unitOfWork.find( id, clazz );
                return instance;
            }
            catch( ClassNotFoundException e )
            {
                throw (IOException) new IOException().initCause( e );
            }
        }

        if( obj instanceof SerializedComposite )
        {
            // TODO Fix this!!
            SerializedComposite holder = (SerializedComposite) obj;
            Class<? extends Composite> compositeInterface = holder.type();
            Object[] mixins = holder.mixins();

            CompositeBuilder<? extends Composite> builder = null;
            do
            {
                try
                {
                    builder = cbf.newCompositeBuilder( compositeInterface );
                }
                catch( InvalidApplicationException e )
                {
                    // Could not find this Composite - try superinterfaces
                    compositeInterface = is.getSuperComposite( compositeInterface );
                    if( compositeInterface == null )
                    {
                        throw e;
                    }
                }
            }
            while( builder == null );

            // CompositeBuilder found
            Composite composite = builder.newInstance();
            ( (Qi4jSPI) is ).setMixins( composite, mixins );
            return composite;
        }
        return obj;
    }

/*
    @Override protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException
    {
        String className = readUTF();
        Class clazz = Class.forName( className );
        return ObjectStreamClass.lookup( clazz );
    }
*/
}
