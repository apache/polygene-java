/*
 * Copyright (c) 2014-2015 Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.conversion.values;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.mixin.Mixins;

/**
 * Service that creates or updates Entities from matching Values.
 * @deprecated Please use {@link org.qi4j.api.unitofwork.UnitOfWork#toEntity(Class, Identity)} instead.
 */
@Mixins( ValueToEntityMixin.class )
public interface ValueToEntityService
    extends ValueToEntity
{
}
