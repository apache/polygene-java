/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.api.unitofwork;

import org.apache.zest.api.structure.Module;
import org.apache.zest.api.usecase.Usecase;

/**
 * UnitOfWork Template.
 */
public abstract class UnitOfWorkTemplate<RESULT, ThrowableType extends Throwable>
{
    private Usecase usecase = Usecase.DEFAULT;
    private int retries = 10;
    private boolean complete = true;

    protected UnitOfWorkTemplate()
    {
    }

    protected UnitOfWorkTemplate( int retries, boolean complete )
    {
        this.retries = retries;
        this.complete = complete;
    }

    protected UnitOfWorkTemplate( Usecase usecase, int retries, boolean complete )
    {
        this.usecase = usecase;
        this.retries = retries;
        this.complete = complete;
    }

    protected abstract RESULT withUnitOfWork( UnitOfWork uow )
        throws ThrowableType;

    @SuppressWarnings( "unchecked" )
    public RESULT withModule( Module module )
        throws ThrowableType, UnitOfWorkCompletionException
    {
        int loop = 0;
        ThrowableType ex = null;
        do
        {
            UnitOfWork uow = module.newUnitOfWork( usecase );

            try
            {
                RESULT result = withUnitOfWork( uow );
                if( complete )
                {
                    try
                    {
                        uow.complete();
                        return result;
                    }
                    catch( ConcurrentEntityModificationException e )
                    {
                        // Retry?
                        ex = (ThrowableType) e;
                    }
                }
            }
            catch( Throwable e )
            {
                ex = (ThrowableType) e;
            }
            finally
            {
                uow.discard();
            }
        }
        while( loop++ < retries );

        throw ex;
    }
}
