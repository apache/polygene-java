/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.composite;

import java.lang.reflect.InvocationHandler;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.MetaInfoHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.HasTypes;

/**
 * Composite Instance.
 */
public interface CompositeInstance
    extends InvocationHandler, CompositeInvoker, HasTypes, MetaInfoHolder
{
    <T> T proxy();

    <T> T newProxy( Class<T> mixinType )
        throws IllegalArgumentException;

    Module module();

    CompositeDescriptor descriptor();

    StateHolder state();
}
