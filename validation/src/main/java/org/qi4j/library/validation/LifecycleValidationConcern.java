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
package org.qi4j.library.validation;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.injection.scope.This;

/**
 * This modifier is invoked on Lifecycle mixin invocation.
 * Before the next target invocation, this modifier invokes
 * {@link Validatable#validate()}.
 * <p/>
 * JAVADOC: We need to distinguish between Validatable on create, delete and update.
 */
public class LifecycleValidationConcern extends ConcernOf<Lifecycle>
    implements Lifecycle
{
    @This Validatable validation;

    public void create()
    {
        validation.validate();

        next.create();
    }

    public void remove()
    {
        next.remove();
    }
}

