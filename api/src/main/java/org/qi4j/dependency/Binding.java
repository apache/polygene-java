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

package org.qi4j.dependency;

import java.util.Iterator;

/**
 * TODO
 */
public class Binding
{
    public static Binding bind( InjectionKey key, DependencyResolution resolution )
    {
        return new Binding( key, resolution );
    }

    public static Binding bind( InjectionKey key, Object instance )
    {
        return new Binding( key, new StaticDependencyResolution( instance ) );
    }

    public static Binding bind( InjectionKey key, Iterable iterable )
    {
        return new Binding( key, new IteratorDependencyResolution( iterable ) );
    }

    public static Binding bind( InjectionKey key, Iterator iterator )
    {
        return new Binding( key, new IteratorDependencyResolution( iterator ) );
    }

    InjectionKey key;
    DependencyResolution resolution;

    public Binding( InjectionKey key, DependencyResolution resolution )
    {
        this.key = key;
        this.resolution = resolution;
    }

    public InjectionKey getInjectionKey()
    {
        return key;
    }

    public DependencyResolution getDependencyResolution()
    {
        return resolution;
    }
}
