/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

package org.qi4j.index.sql.support.skeletons;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.index.sql.support.api.SQLAppStartup;
import org.sql.generation.api.vendor.SQLVendor;

/**
 * 
 * @author Stanislav Muhametsin
 */
public abstract class AbstractSQLStartup
    implements SQLAppStartup, Activatable
{
    @This
    private SQLDBState _state;

    public void activate()
        throws Exception
    {
        this._state.sqlVendor().set( this.instantiateVendor() );
    }

    public void passivate()
        throws Exception
    {
    }

    protected abstract SQLVendor instantiateVendor()
        throws Exception;
}
