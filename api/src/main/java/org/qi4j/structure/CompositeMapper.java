/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.structure;

import org.qi4j.Composite;

/**
 * CompositeMappers are responsible for mapping interfaces,
 * either sets of one or more, into Composite interfaces.
 * <p/>
 * This is used so that code does not have to refer to specific
 * Composite interfaces, but can instead refer to domain interfaces
 * which by using a CompositeMapper is mapped into a specific Composite
 * interface that can be used for casting or instantiation.
 * <p/>
 * Example:
 * CompositeMapper mapper = ...
 * mapper.map(MyUserComposite.class, User.class);
 */
public interface CompositeMapper
{
    void register( Class<? extends Composite> compositeType, Class domainInterface );
}
