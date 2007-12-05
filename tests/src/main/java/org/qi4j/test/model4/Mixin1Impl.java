/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.model4;

import java.util.List;
import org.qi4j.annotation.SideEffects;
import org.qi4j.annotation.scope.Structure;
import org.qi4j.annotation.scope.ThisCompositeAs;
import org.qi4j.composite.CompositeBuilderFactory;

@SideEffects( CountCallsSideEffect.class )
public class Mixin1Impl
    implements Mixin1
{
    private CompositeBuilderFactory builderFactory;
    private @ThisCompositeAs Mixin2 meAsMixin2;

    public Mixin1Impl( @Structure CompositeBuilderFactory builderFactory )
    {
        this.builderFactory = builderFactory;
    }

    @CountCalls public CompositeBuilderFactory getBuilderFactory()
    {
        return builderFactory;
    }

    public Mixin2 getMeAsMixin2()
    {
        return meAsMixin2;
    }
}

interface Service<T>
{
    int countAll();

    List<T> findAll( int first, int count );
}

interface FooService extends Service<Mixin1>, FooMiscService
{
}

interface FooMiscService
{
    public List<Mixin1> findAllByFooGroup( String fooGroupName );
}