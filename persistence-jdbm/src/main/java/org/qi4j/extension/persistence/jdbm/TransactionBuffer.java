/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.extension.persistence.jdbm;

import java.io.Serializable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Proxy;
import jdbm.RecordManager;
import org.qi4j.api.persistence.binding.PersistenceBinding;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.runtime.ObjectInvocationHandler;
import org.qi4j.spi.object.ProxyReferenceInvocationHandler;
import javax.transaction.xa.Xid;


class TransactionBuffer
{
    private List<Operation> operations;
    private Xid xid;

    TransactionBuffer()
    {
        operations = new LinkedList<Operation>();
    }

    public TransactionBuffer( Xid xid )
    {
        this.xid = xid;
    }

    Xid getXid()
    {
        return xid;
    }

    void commit( RecordManager recordManager )
        throws IOException
    {
        for( Operation op : operations )
        {
            op.perform( recordManager );
        }
        recordManager.commit();
    }

    void create( PersistenceBinding binding )
    {
        operations.add( new CreateOperation( binding ) );
    }

    void update( PersistenceBinding binding, Serializable mixin )
    {
        operations.add( new UpdateOperation( binding, mixin ) );
    }

    void delete( PersistenceBinding binding )
    {
        operations.add( new DeleteOperation( binding ) );
    }

    private static interface Operation
    {
        void perform( RecordManager recordManager );
    }

    private static class CreateOperation
        implements Operation
    {
        private PersistenceBinding binding;

        public CreateOperation( PersistenceBinding binding )
        {
            this.binding = binding;
        }

        public void perform( RecordManager recordManager )
        {
            ObjectInvocationHandler handler = ObjectInvocationHandler.getInvocationHandler( binding );
            Map<Class, Object> mixins = handler.getMixins();

            Map<Class, Serializable> persistentMixins = new HashMap<Class, Serializable>();
            for( Map.Entry<Class, Object> entry : mixins.entrySet() )
            {
                Object value = entry.getValue();
                if( value instanceof Serializable )
                {
                    persistentMixins.put( entry.getKey(), (Serializable) value );
                }
            }
            String objectId = binding.getIdentity();
            try
            {
                long recordId = recordManager.insert( persistentMixins );
                recordManager.setNamedObject( objectId, recordId );
            }
            catch( IOException e )
            {
                throw new CreateOperationException( "Unable to insert object [" + objectId + "] into storage.", e );
            }
            
        }
    }

    private static class UpdateOperation
        implements Operation
    {
        private PersistenceBinding binding;
        private Serializable mixin;

        public UpdateOperation( PersistenceBinding binding, Serializable mixin )
        {
            this.binding = binding;
            this.mixin = mixin;
        }

        public void perform( RecordManager recordManager )
        {
            ProxyReferenceInvocationHandler handler = (ProxyReferenceInvocationHandler) Proxy.getInvocationHandler( binding );
            String identity = binding.getIdentity();
            try
            {
                long recordId = recordManager.getNamedObject( identity );
                Map<Class, Serializable> mixins = (Map<Class, Serializable>) recordManager.fetch( recordId );
                if( mixins != null )
                {
                    Class mixinType = handler.getMixinType();
                    Serializable oldValueObject = mixins.get( mixinType );

                    // Only update if there already were a value object. Otherwise ignore.
                    if( oldValueObject != null )
                    {
                        mixins.put( mixinType, mixin );
                        recordManager.update( recordId, mixins );
                    }
                }
            }
            catch( IOException e )
            {
                // TODO Better message
                throw new UpdateOperationException( "Unable to update object [" + identity + "]", e );
            }
        }
    }

    private static class DeleteOperation
        implements Operation
    {
        private PersistenceBinding binding;

        public DeleteOperation( PersistenceBinding binding )
        {
            this.binding = binding;
        }

        public void perform( RecordManager recordManager )
        {
            String identity = binding.getIdentity();
            try
            {
                long recordId = recordManager.getNamedObject( identity );
                recordManager.delete( recordId );
            }
            catch( IOException e )
            {
                throw new DeleteOperationException( "Unable to delete object [" + identity + "]", e );
            }
        }
    }
}
