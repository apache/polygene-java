/*
 * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.model.modifiers;

import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.api.persistence.PersistenceException;
import org.qi4j.library.general.model.Validatable;

/**
 * This modifier is invoked on Lifecycle mixin invocation.
 * Before the next target invocation, this modifier invokes
 * {@link org.qi4j.library.general.model.Validatable#validate()}.
 *
 * TODO: We need to distinguish between Validatable on create, delete and update.
 */
public class LifecycleValidationModifier
    implements Lifecycle
{
    @Uses Validatable validation;
    @Modifies Lifecycle next;

    public void create()
    {
        validation.validate();

        next.create();
    }

    public void initialize() throws PersistenceException
    {
        next.initialize();
    }

    public void delete()
    {
        next.delete();
    }

    public boolean isDirty()
    {
        return next.isDirty();
    }

    public boolean isNew()
    {
        return next.isNew();
    }

    public boolean isTransactional()
    {
        return next.isTransactional();
    }

    public boolean isDeleted()
    {
        return next.isDeleted();
    }

    public boolean isDetached()
    {
        return next.isDeleted();
    }
}

