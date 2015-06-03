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

package org.qi4j.library.geometry.builders;

import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.TLinearRing;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.library.geometry.TGeometryBuilder;

public class TPolygonBuilder extends TGeometryBuilder<TPolygon>
{
    @Structure
    private Module module;

    public TPolygonBuilder()
    {
        super( TPolygon.class );
    }

    public TPolygonBuilder shell( TLinearRing shell )
    {
        geometry().of( shell );
        return this;
    }

    public TPolygonBuilder shell( double[][] shell )
    {
        geometry().of( module.newObject( TLinearRingBuilder.class ).ring( shell ).geometry() );
        return this;
    }

    public TPolygonBuilder withHoles( TLinearRing... holes )
    {
        geometry().withHoles( holes );
        return this;
    }

    public TPolygon geometry( String CRS )
    {
        geometry().setCRS( CRS );
        return geometry();
    }
}
