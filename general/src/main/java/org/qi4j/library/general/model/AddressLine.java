/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.model;

import java.io.Serializable;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.PropertyField;
import org.qi4j.property.Property;

/**
 * Generic interface for Address lines.
 */
@Mixins( AddressLine.AddressLineMixin.class )
public interface AddressLine
{
    Property<String> firstLine();

    Property<String> secondLine();

    /**
     * @since 0.1.0
     */
    final class AddressLineMixin
        implements AddressLine, Serializable
    {
        @PropertyField
        private Property<String> firstLine;

        @PropertyField
        private Property<String> secondLine;

        public final Property<String> firstLine()
        {
            return firstLine;
        }

        public final Property<String> secondLine()
        {
            return secondLine;
        }
    }
}
