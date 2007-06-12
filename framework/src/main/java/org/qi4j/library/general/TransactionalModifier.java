/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.general;

import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Dependency;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.Status;

/**
 * Transactional modifier. Implementation methods
 * can be annotated with transaction annotations, which
 * will be used appropriately by this modifier to begin
 * and commit transactions.
 *
 * @see Transactional
 */
@AppliesTo(Transactional.class)
public class TransactionalModifier
    implements InvocationHandler
{
    // Attributes ----------------------------------------------------
    TransactionManager tm;

    @Modifies InvocationHandler next;
    @Dependency Method method;

    // Constructors --------------------------------------------------
    public TransactionalModifier( TransactionManager tm)
    {
        this.tm = tm;
    }

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        int tx = method.getAnnotation( Transactional.class ).value();

        switch (tx)
        {
            case Transactional.NEVER:
            {
                if (tm.getStatus() == Status.STATUS_ACTIVE)
                {
                    Transaction current = tm.suspend();
                    try
                    {
                        return next.invoke( proxy, method, args);
                    }
                    finally
                    {
                        tm.resume( current );
                    }
                }
                break;
            }

            case Transactional.NOT_SUPPORTED:
            {
                // ?
                break;
            }

            case Transactional.REQUIRED:
            {
                if (tm.getStatus() != Status.STATUS_ACTIVE)
                {
                    tm.begin();
                    try
                    {
                        Object result = next.invoke( proxy, method, args);
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

            case Transactional.REQUIRES_NEW:
            {
                if (tm.getStatus() == Status.STATUS_ACTIVE)
                {
                    Transaction current = tm.suspend();
                    tm.begin();
                    try
                    {
                        Object result = next.invoke( proxy, method, args);
                        tm.commit();
                        return result;
                    }
                    catch( Throwable throwable )
                    {
                        tm.rollback();
                        throw throwable;
                    } finally
                    {
                        tm.resume( current );
                    }
                }
                break;
            }

            case Transactional.SUPPORTS:
            {
                // ?
            }
        }

        return next.invoke( proxy, method, args);
    }
}
