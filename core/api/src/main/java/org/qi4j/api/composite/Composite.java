/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.PropertyMixin;

/**
 * Base Composite interface.
 * 
 * All Composite objects must implement this interface. Let the
 * Composite interface extend this one. An implementation will be provided
 * by the framework.
 * <p/>
 * Properties and associations are handled by default.
 */
@Mixins( { PropertyMixin.class } )
public interface Composite
{
}
