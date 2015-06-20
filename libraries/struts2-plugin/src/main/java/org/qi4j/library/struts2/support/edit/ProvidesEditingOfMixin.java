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
package org.qi4j.library.struts2.support.edit;

import org.qi4j.api.injection.scope.This;
import org.qi4j.library.struts2.support.ProvidesEntityOfMixin;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesEditingOfMixin<T>
    extends ProvidesEntityOfMixin<T>
    implements ProvidesEditingOf<T>
{

    @This
    private ProvidesEditingOf<T> action;

    @Override
    public void prepare()
        throws Exception
    {
        loadEntity();
    }

    @Override
    public void prepareInput()
        throws Exception
    {

    }

    @Override
    public String input()
    {
        if( getEntity() == null )
        {
            return "entity-not-found";
        }
        return INPUT;
    }

    @Override
    protected Class<T> typeToLoad()
    {
        return (Class<T>) findTypeVariables( action.getClass(), ProvidesEditingOf.class )[ 0 ];
    }
}