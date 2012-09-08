/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.sample.dcicargo.sample_b.data.structure.location;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

/**
 * Location
 *
 * A location is a stop on a journey, such as cargo origin or destination, or
 * carrier movement endpoints.
 *
 * It is uniquely identified by a {@link UnLocode}.
 *
 * All properties are mandatory and immutable.
 */
@Immutable
@Mixins( Location.Mixin.class )
public interface Location
{
    Property<UnLocode> unLocode();

    Property<String> name();

    // Side-effects free and UI agnostic convenience methods
    String getCode();

    String getString();

    abstract class Mixin
        implements Location
    {
        public String getCode()
        {
            return unLocode().get().code().get();
        }

        public String getString()
        {
            return name().get() + " (" + getCode() + ")";
        }
    }
}
