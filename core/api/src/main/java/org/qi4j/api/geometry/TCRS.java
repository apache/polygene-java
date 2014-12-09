package org.qi4j.api.geometry;

import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.HasNoArea;
import org.qi4j.api.geometry.internal.TGeomRoot;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.List;

@Mixins( TCRS.Mixin.class )
public interface TCRS extends TGeomRoot {


    Property<String> definition();

    TCRS of(String crs);
    String crs();


    public  abstract class Mixin implements TCRS
    {
        @Structure
        Module module;

        @This
        TCRS self;

        public TCRS of(String crs )
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
