package org.qi4j.api.geometry;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

@Mixins( TGeometry.Mixin.class )
public interface TGeometry extends TGeomRoot {

    // @Optional
    Property<TGEOMETRY> type();

    // Property<TGEOM_TYPE> type1();


    @Optional
    @UseDefaults
    Property<Integer> SRIDCode();

    @Optional
    @UseDefaults
    Property<String> SRIDAuthority();

    String getSRID();
    void setSRIDCode(int SRID);
    void setSRIDAuthority(String authority);
    void setSRID(String authority, Integer code);
    String getSRIDWkt();


    abstract Coordinate[] getCoordinates();
    abstract int getNumPoints();
    // abstract boolean isEmpty();


    public abstract class Mixin implements TGeometry
    {

        @Structure
        Module module;

        @This
        TGeometry self;

        public String getSRID()
        {
            return self.SRIDAuthority().get() + ":" + self.SRIDCode().get();
        }

        public void setSRIDCode(int SRID)
        {
           self.SRIDCode().set(SRID);
        }

        public void setSRIDAuthority(String authority) { self.SRIDAuthority().set(authority);}

        public void setSRID(String authority, Integer code) {
            self.setSRIDAuthority(authority);
            self.setSRIDCode(code);
        }

        public String getSRIDWkt() {
            return self.SRIDAuthority() + ":" + self.SRIDCode();
        }

        public int getNumPoints() {
            return 0;
        }

        public Coordinate[] getCoordinates()
        {
            return null;
        }

    }

}
