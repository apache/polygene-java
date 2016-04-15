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
package org.apache.zest.api.unitofwork;

import org.apache.zest.api.entity.EntityReference;

/**
 * If you try to create an EntityComposite whose identity already exists,
 * then this exception will be thrown.
 */
public class EntityCompositeAlreadyExistsException
    extends UnitOfWorkException
{
    private static final long serialVersionUID = -7297710939536508481L;

    private final EntityReference identity;

    public EntityCompositeAlreadyExistsException( EntityReference identity )
    {
        super( "EntityComposite (" + identity + ") already exists." );
        this.identity = identity;
    }

    public EntityReference identity()
    {
        return identity;
    }
}
