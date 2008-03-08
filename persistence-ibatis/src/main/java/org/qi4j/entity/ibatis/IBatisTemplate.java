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
package org.qi4j.entity.ibatis;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import java.sql.SQLException;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.spi.entity.StoreException;

/**
 * @author edward.yakop@gmail.com
 */
abstract class IBatisTemplate<T>
{
    private final SqlMapClient client;

    /**
     * Construct a new {@code IBatisTemplate}.
     *
     * @param aClient The client.
     * @throws IllegalArgumentException Thrown if the specified {@code aClient} argument is {@code null}.
     * @since 0.1.0
     */
    IBatisTemplate( SqlMapClient aClient )
        throws IllegalArgumentException
    {
        validateNotNull( "aClient", aClient );
        client = aClient;
    }

    /**
     * Execute IBatis template.
     *
     * @return Result returned by {@link #onExecute(SqlMapClient)} .
     * @throws StoreException Thrown if persistence failed.
     * @since 0.1.0
     */
    final T execute()
        throws StoreException
    {
        SqlMapSession session = client.openSession();
        try
        {
            // TODO: This is just a simple template that is not verified
            client.startTransaction();
            T result = onExecute( client );
            session.commitTransaction();

            return result;
        }
        catch( SQLException e )
        {
            throw new StoreException( e );
        }
        finally
        {
            try
            {
                session.endTransaction();
            }
            catch( SQLException e )
            {
                throw new StoreException( "End transaction failed", e );
            }
        }
    }

    /**
     * Implement this method to execute sqls.
     * <p/>
     * By default the client will start a new transaction, commit and end transaction.
     *
     * @param aClient The sql map client. This argumetn must not be {@code null}.
     * @return an object that should be returned by {@link #execute()}
     * @throws java.sql.SQLException Thrown if there is any problem during execution.
     * @since 0.1.0
     */
    protected abstract T onExecute( SqlMapClient aClient )
        throws SQLException;
}
