/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
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

package org.qi4j.library.rest.server.restlet;

import java.lang.reflect.Method;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ObjectSelection;

/**
 * Service interface for checking whether a particular method or a whole class is not
 * valid at this point, for whatever reason (application state or authorization rules usually).
 */
public interface InteractionConstraints
{
    public boolean isValid( Method method, ObjectSelection objectSelection, Module module );

    public boolean isValid( Class resourceClass, ObjectSelection objectSelection, Module module );
}
