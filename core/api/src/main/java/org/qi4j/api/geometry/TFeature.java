package org.qi4j.api.geometry;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.Map;


@Mixins( TFeature.Mixin.class )
public interface TFeature extends TGeomRoot {

    @Optional
    Property<String> id();

    Property<TGeometry> geometry();

    @Optional
    Property<Map<String, Object>> properties();

    TFeature of(TGeometry geometry);
    TGeometry asGeometry();
    Map<String, Object> asProperties();



    public abstract class Mixin implements TFeature
    {

        @Structure
        Module module;

        @This
        TFeature self;

        public TFeature of(TGeometry geometry)
        {


            self.geometry().set(geometry);

            return self;
        }

        public TGeometry asGeometry()
        {
            return self.geometry().get();
        }

        public Map<String, Object> asProperties()
        {
            return self.properties().get();
        }

    }

}
