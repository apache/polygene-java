/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
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

package org.qi4j.api.geometry;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueComposite;

@Mixins(TCRS.Mixin.class)
public interface TCRS extends ValueComposite
{
    Property<String> definition();
    TCRS of(String crs);
    String crs();

    public abstract class Mixin implements TCRS
    {
        @Structure
        Module module;
        @This
        TCRS self;

        public TCRS of(String crs)
        {
            self.definition().set(crs);
            return self;
        }

        public String crs()
        {
            return self.definition().get();
        }
    }
}
