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
package org.qi4j.api.structure;

import org.qi4j.api.Qi4j;
import org.qi4j.functional.VisitableHierarchy;

/**
 * Application Descriptor.
 */
public interface ApplicationDescriptor
    extends VisitableHierarchy<Object, Object>
{
    /**
     * Create a new instance of the Application.
     * @param runtime Qi4j Runtime
     * @param importedServiceInstances Imported Services instances
     * @return a new instance of the Application.
     */
    Application newInstance( Qi4j runtime, Object... importedServiceInstances );

    /**
     * @return the Application's name
     */
    String name();
}
