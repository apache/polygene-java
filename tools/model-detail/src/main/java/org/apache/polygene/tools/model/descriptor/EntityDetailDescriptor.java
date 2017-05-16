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
package org.apache.polygene.tools.model.descriptor;

import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.util.Visitable;
import org.apache.polygene.api.util.Visitor;

/**
 * Entity Detail Descriptor.
 */
public final class EntityDetailDescriptor
    extends CompositeDetailDescriptor<EntityDescriptor>
    implements Visitable<EntityDetailDescriptor>
{
    EntityDetailDescriptor( EntityDescriptor aDescriptor )
    {
        super( aDescriptor );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super EntityDetailDescriptor, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
    }
}
