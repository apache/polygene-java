package org.qi4j.api.geometry;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

@Mixins( TGeometry.Mixin.class )
public interface TGeometry extends TGeomRoot {

    // @Optional
    Property<String> type();

    @Optional
    Property<Integer> SRID();


    int getSRID();
    void setSRID(int SRID);


    public abstract class Mixin implements TGeometry
    {

        @Structure
        Module module;

        @This
        TGeometry self;

        public int getSRID()
        {
            return self.SRID().get();
        }

        public void setSRID(int SRID)
        {
           self.SRID().set(SRID);
        }



    }

}
