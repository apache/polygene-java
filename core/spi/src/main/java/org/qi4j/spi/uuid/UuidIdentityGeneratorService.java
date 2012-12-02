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

package org.qi4j.spi.uuid;

import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;

/**
 * UUID based Identity generator Service.
 */
@Mixins( UuidIdentityGeneratorMixin.class )
public interface UuidIdentityGeneratorService
    extends IdentityGenerator, ServiceComposite
{
}
