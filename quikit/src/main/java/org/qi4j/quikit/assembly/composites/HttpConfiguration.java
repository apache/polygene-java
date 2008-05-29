/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.quikit.assembly.composites;

import org.qi4j.entity.EntityComposite;
import org.qi4j.library.framework.constraint.annotation.Range;
import org.qi4j.property.Property;

/**
 * Configuration entity for the Jetty Http Server.
 */
public interface HttpConfiguration extends EntityComposite
{
    @Range( min = 0, max = 65535 ) Property<Integer> hostPort();

    Property<String> hostName();

    Property<String> rootContextPath();

    Property<String> resourcePath();
}
