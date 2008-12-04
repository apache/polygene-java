/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.spaces.javaspaces;

import org.qi4j.library.spaces.SpaceTransaction;
import org.qi4j.library.spaces.SpaceException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.CannotAbortException;
import java.rmi.RemoteException;

final class TransactionProxy
    implements SpaceTransaction
{
    Transaction transaction;
    private JavaSpacesClientMixin javaspacesMixin;

    TransactionProxy( Transaction transaction, JavaSpacesClientMixin mixin )
    {
        this.transaction = transaction;
        javaspacesMixin = mixin;
    }

    public void commit()
    {
        try
        {
            System.out.println( "COMMIT: " + transaction );
            transaction.commit();
            javaspacesMixin.removeTransaction( this );
        }
        catch( UnknownTransactionException e )
        {
            throw new SpaceException( "Can not commit: " + e.getMessage(), e );
        }
        catch( CannotCommitException e )
        {
            throw new SpaceException( "Can not commit: " + e.getMessage(), e );
        }
        catch( RemoteException e )
        {
            throw new SpaceException( "Can not commit: " + e.getMessage(), e );
        }
    }

    public void abort()
    {
        try
        {
            System.out.println( "ABORT: " + transaction );
            transaction.abort();
            javaspacesMixin.removeTransaction( this );
        }
        catch( UnknownTransactionException e )
        {
            throw new SpaceException( "Can not abort: " + e.getMessage(), e );
        }
        catch( CannotAbortException e )
        {
            throw new SpaceException( "Can not abort: " + e.getMessage(), e );
        }
        catch( RemoteException e )
        {
            throw new SpaceException( "Can not abort: " + e.getMessage(), e );
        }
    }
}
