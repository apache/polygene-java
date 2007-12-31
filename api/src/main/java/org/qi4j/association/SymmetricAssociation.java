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

package org.qi4j.association;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * TODO
 */
public final class SymmetricAssociation<R, T>
    implements AssociationChangeObserver<R, T>
{
    private Class<T> referencedType;
    private Method symmetricAssociationAccessor;

    public SymmetricAssociation( Class<T> referencedType )
    {
        this.referencedType = referencedType;
    }

    public void onChange( AssociationChange<R, T> associationChange )
    {
        if( associationChange.getChangeType() == ChangeType.ADDED )
        {
            try
            {
                ManyAssociation<R> association = (ManyAssociation<R>) symmetricAssociationAccessor.invoke( associationChange.getReference() );
                association.add( associationChange.getReferer() );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        else if( associationChange.getChangeType() == ChangeType.REMOVED )
        {
            try
            {
                ManyAssociation<R> association = (ManyAssociation<R>) symmetricAssociationAccessor.invoke( associationChange.getReference() );
                association.remove( associationChange.getReferer() );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    public T symmetricWith()
    {
        return referencedType.cast( Proxy.newProxyInstance( referencedType.getClassLoader(), new Class[]{ referencedType }, new InvocationHandler()
        {
            public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
            {
                symmetricAssociationAccessor = method;

                return method.getDefaultValue();
            }
        } ) );
    }
}
