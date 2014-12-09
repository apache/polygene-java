package org.qi4j.api.geometry.internal;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.geometry.TCRS;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

@Mixins( TGeometry.Mixin.class )
public interface TGeometry extends TGeomRoot {

    // @Optional
    Property<TGEOMETRY> geometryType();

    // Property<TGEOM_TYPE> type1();

    @Optional
    @UseDefaults
    Property<String> CRS();


    String getCRS();
    void setCRS(String crs);



    abstract Coordinate[] getCoordinates();
    abstract int getNumPoints();
    abstract boolean isEmpty();
    TGEOMETRY getType();


    public abstract class Mixin implements TGeometry
    {

        @Structure
        Module module;

        @This
        TGeometry self;

        public String getCRS()
        {
            return self.CRS().get();
        }

        public void setCRS(String crs)
        {
            self.CRS().set(crs);
        }


        public int getNumPoints() {
            return 0;
        }

        public Coordinate[] getCoordinates()
        {
            return null;
        }

        public boolean isEmpty() { throw new RuntimeException("Should never be called"); }

        public TGEOMETRY getType()  { return self.geometryType().get(); }

    }

}
