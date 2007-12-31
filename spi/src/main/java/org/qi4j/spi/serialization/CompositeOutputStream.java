/*
 * Copyright (c) 2007, Rickard �berg. All Rights Reserved.
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
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeState;

/**
 * TODO
 */
final class CompositeOutputStream extends ObjectOutputStream
{
    Qi4jSPI spi;

    public CompositeOutputStream( OutputStream out, Qi4jSPI spi )
        throws IOException
    {
        super( out );
        enableReplaceObject( true );
        this.spi = spi;
    }

    protected Object replaceObject( Object obj ) throws IOException
    {
        if( obj instanceof Composite && obj instanceof Proxy )
        {
            Composite composite = (Composite) obj;
            CompositeModel compositeObject = spi.getCompositeBinding( composite ).getCompositeResolution().getCompositeModel();
            Class compositeInterface = compositeObject.getCompositeClass();
            if( obj instanceof EntityComposite )
            {
                String id = ( (EntityComposite) composite ).identity().get();
                return new SerializedEntity( id, compositeInterface );
            }
            else
            {
                List mixinsToSave = new ArrayList();
                CompositeState mixinsHolder = (CompositeState) Proxy.getInvocationHandler( obj );
                Object[] existingMixins = mixinsHolder.getMixins();
                for( Object existingMixin : existingMixins )
                {
                    if( existingMixin instanceof Serializable )
                    {
                        mixinsToSave.add( existingMixin );
                    }
                }
                Object[] mixinArray = mixinsToSave.toArray( new Object[mixinsToSave.size()] );
                return new SerializedComposite( mixinArray, compositeInterface );
            }
        }
        return obj;
    }
}
