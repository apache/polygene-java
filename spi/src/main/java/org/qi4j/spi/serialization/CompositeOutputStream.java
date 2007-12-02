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

/**
 * TODO
 */
final class CompositeOutputStream extends ObjectOutputStream
{
    public CompositeOutputStream( OutputStream out )
        throws IOException
    {
        super( out );
        enableReplaceObject( true );
    }

    // TODO: EFY: Commented out, as this introduce cyclic dependencies between core-spi and core-runtime
    // TODO: EFY: and nobody use this method.
//    protected Object replaceObject( Object obj ) throws IOException
//    {
//        if( obj instanceof Composite )
//        {
//            Composite composite = (Composite) obj;
//            CompositeInstance compositeInstance = CompositeInstance.getCompositeInstance( obj );
//            CompositeModel compositeObject = compositeInstance.getContext().getCompositeModel();
//            Class compositeInterface = compositeObject.getCompositeClass();
//            if( obj instanceof EntityComposite )
//            {
//                String id = ( (EntityComposite) composite ).getIdentity();
//                return new SerializedEntity( id, compositeInterface );
//            }
//            else
//            {
//                Map<Class, Object> mixinsToSave = new HashMap<Class, Object>();
//                CompositeState mixinsHolder = (CompositeState) Proxy.getInvocationHandler( obj );
//                Object[] existingMixins = mixinsHolder.getMixins();
//                for( Object existingMixin : existingMixins )
//                {
//                    if( existingMixin instanceof Serializable )
//                    {
//                        mixinsToSave.put( existingMixin.getClass(), existingMixin );
//                    }
//                }
//                return new SerializedComposite( mixinsToSave, compositeInterface );
//            }
//        }
//        return obj;
//    }

}
