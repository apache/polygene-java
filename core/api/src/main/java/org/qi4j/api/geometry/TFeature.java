package org.qi4j.api.geometry;

import org.qi4j.api.common.Optional;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.List;
import java.util.Map;


@Mixins( TFeature.Mixin.class )
public interface TFeature extends TGeometry  {

    @Optional
    Property<String> id();
    Property<TGeometry> geometry();
    @Optional
    Property<Map<String, List<String>>> properties();



    TFeature of(TGeometry geometry);
    TFeature withProperties(Map<String, List<String>> properties);

    TGeometry asGeometry();
    Map<String, List<String>> asProperties();



    public abstract class Mixin implements TFeature
    {

        @Structure
        Module module;

        @This
        TFeature self;


        public TFeature of(TGeometry geometry)
        {
            self.geometryType().set(TGEOMETRY.FEATURE);
            self.geometry().set(geometry);

            return self;
        }

        public TFeature withProperties(Map<String, List<String>> properties)
        {
            self.properties().set(properties);
            return self;
        }

        public boolean isEmpty() {
            return (self.geometry() == null) || (self.geometry().get() == null) || (self.geometry().get().isEmpty()) ? true : false;
        }

        public int getNumPoints() {
            return isEmpty() ? 0 : self.geometry().get().getNumPoints();
        }


        public TGeometry asGeometry()
        {
            return self.geometry().get();
        }

        public Map<String, List<String>> asProperties()
        {
            return self.properties().get();
        }

    }

}
