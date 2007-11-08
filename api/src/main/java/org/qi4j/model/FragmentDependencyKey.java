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

package org.qi4j.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Dependency key used for dependency resolutions. The key is comprised
 * of the thisAs type, the fragment type, the annotation type, the generic dependency type, and an optional name.
 */
public class FragmentDependencyKey
    extends DependencyKey
{
    private Class compositeType;

    public FragmentDependencyKey( Class<? extends Annotation> annotationType, Type genericType, String name, Class dependentType, Class compositeType )
    {
        super( annotationType, genericType, name, dependentType );
        this.compositeType = compositeType;
    }

    public Class getCompositeType()
    {
        return compositeType;
    }

    @Override public String toString()
    {
        return compositeType.getSimpleName() + ":" + super.toString();
    }
}