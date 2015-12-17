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
package org.apache.zest.api.value;

import org.apache.zest.api.structure.Module;
import org.apache.zest.api.structure.ModuleDescriptor;

/**
 * Builder template for Values.
 */
public abstract class ValueBuilderTemplate<T>
{
    Class<T> type;

    protected ValueBuilderTemplate( Class<T> type )
    {
        this.type = type;
    }

    protected abstract void build( T prototype );

    public T newInstance( ModuleDescriptor module )
    {
        ValueBuilder<T> builder = module.instance().newValueBuilder( type );
        build( builder.prototype() );
        return builder.newInstance();
    }
}
