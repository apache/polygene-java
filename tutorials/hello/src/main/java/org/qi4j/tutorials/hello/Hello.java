/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.tutorials.hello;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.NotEmpty;

// START SNIPPET: body

/**
 * This Composite interface declares a simple "Hello World" interface with a single say() method. What is being
 * said is defined in the HelloWorldState interface, which is a private mixin.
 * <p/>
 */
@Mixins( { Hello.HelloWorldMixin.class } )
public interface Hello
{
    String say();

    /**
     * This is the implementation of the say() method.
     */
    public abstract class HelloWorldMixin
        implements Hello
    {
        // @This reference the composite itself, and since HelloWorldState is not part of the public interface,
        // it is a private mixin.
        @This
        private State state;

        @Override
        public String say()
        {
            return state.phrase().get() + " " + state.name().get();
        }
    }

    /**
     * This interface contains only the state of the HelloWorld object.
     */
    public interface State
    {
        @NotEmpty
        @UseDefaults
        Property<String> phrase();

        @NotEmpty
        @UseDefaults
        Property<String> name();
    }
}
// END SNIPPET: body
