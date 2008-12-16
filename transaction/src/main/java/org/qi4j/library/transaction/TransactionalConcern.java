/*
 * Copyright 2007 Rickard Öberg. All Rights Reserved.
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.api.injection.scope.Service;

/**
 * Transactional modifier. Implementation methods
 * can be annotated with transaction annotations, which
 * will be used appropriately by this modifier to begin
 * and commit transactions.
 *
 * @see Transactional
 * @see Transactional.Propagation
 */
@AppliesTo( Transactional.class )
public class TransactionalConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @Service TransactionManager tm;
    @Invocation Transactional transactional;

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {

        switch( transactional.value() )
        {

        case REQUIRED:
        {
            if( tm.getStatus() != Status.STATUS_ACTIVE )
            {
                tm.begin();
                try
                {
                    Object result = next.invoke( proxy, method, args );
                    tm.commit();
                    return result;
                }
                catch( Throwable throwable )
                {
                    tm.rollback();
                    throw throwable;
                }
            }
        }

        case SUPPORTS:
        {
            // nothing to do; should proceed regardles if there is an active transaction
        }

        case MANDATORY:
        {
            if( tm.getStatus() != Status.STATUS_ACTIVE )
            {
                throw new IllegalStateException( "Transaction was required but there is no available transaction." );
                // TODO other execption? what about the message?
            }
        }

        case REQUIRES_NEW:
        {
            if( tm.getStatus() == Status.STATUS_ACTIVE )
            {
                Transaction current = tm.suspend();
                tm.begin();
                try
                {
                    Object result = next.invoke( proxy, method, args );
                    tm.commit();
                    return result;
                }
                catch( Throwable throwable )
                {
                    tm.rollback();
                    throw throwable;
                }
                finally
                {
                    tm.resume( current );
                }
            }
            break;
        }

        case NOT_SUPPORTED:
        {
            if( tm.getStatus() == Status.STATUS_ACTIVE )
            {
                Transaction current = tm.suspend();
                try
                {
                    return next.invoke( proxy, method, args );
                }
                finally
                {
                    tm.resume( current );
                }
            }
            break;
        }

        case NEVER:
        {
            if( tm.getStatus() == Status.STATUS_ACTIVE )
            {
                throw new IllegalStateException( " Transaction is not supported but there is an active transaction." );
                // TODO other execption? what about the message?
            }
            break;
        }

        }

        return next.invoke( proxy, method, args );
    }

}
