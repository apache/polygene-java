/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.unitofwork;

import org.qi4j.api.entity.EntityComposite;

/**
 * This exception is thrown by UnitOfWork.complete() if any entities that are being committed
 * had been changed while the UnitOfWork was being executed.
 */
public class ConcurrentEntityModificationException
    extends UnitOfWorkCompletionException
{
    private static final long serialVersionUID = 3872723845064767689L;

    private final Iterable<EntityComposite> concurrentlyModifiedEntities;

    public ConcurrentEntityModificationException( Iterable<EntityComposite> concurrentlyModifiedEntities )
    {
        super("Entities changed concurrently :" + concurrentlyModifiedEntities);
        this.concurrentlyModifiedEntities = concurrentlyModifiedEntities;
    }

    public Iterable<EntityComposite> concurrentlyModifiedEntities()
    {
        return concurrentlyModifiedEntities;
    }
}