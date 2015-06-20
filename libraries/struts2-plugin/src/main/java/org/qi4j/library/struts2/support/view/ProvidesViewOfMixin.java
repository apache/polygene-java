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
package org.qi4j.library.struts2.support.view;

import org.qi4j.api.injection.scope.This;
import org.qi4j.library.struts2.support.ProvidesEntityOfMixin;

import static org.qi4j.library.struts2.util.ParameterizedTypes.findTypeVariables;

public abstract class ProvidesViewOfMixin<T>
    extends ProvidesEntityOfMixin<T>
    implements ProvidesViewOf<T>
{
    @This
    private ProvidesViewOf<T> action;

    @Override
    public String execute()
    {
        loadEntity();
        return SUCCESS;
    }

    @Override
    protected Class<T> typeToLoad()
    {
        return (Class<T>) findTypeVariables( action.getClass(), ProvidesViewOf.class )[ 0 ];
    }
}