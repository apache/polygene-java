/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.unitofwork;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;

import static org.apache.polygene.api.util.Classes.RAW_CLASS;

public class EntityFunction
    implements BiFunction<EntityReference, Type, Object>
{

    private final UnitOfWorkFactory uowf;

    public EntityFunction( UnitOfWorkFactory uowf )
    {
        this.uowf = uowf;
    }

    @Override
    public Object apply( EntityReference entityReference, Type type )
    {
        return uowf.currentUnitOfWork().get( RAW_CLASS.apply( type ), entityReference.identity() );
    }
}
