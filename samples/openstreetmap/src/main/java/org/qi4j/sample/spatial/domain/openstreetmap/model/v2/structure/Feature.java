package org.qi4j.sample.spatial.domain.openstreetmap.model.v2.structure;

import org.qi4j.api.common.Optional;
import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TGeometryRoot;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import java.util.List;

@Mixins({
        Feature.Mixin.class,
        Feature.EventsMixin.class
})
public interface Feature
{

    void create(TFeature feature);

    interface Data {

        @Optional
        Property<TPoint> osmpoint();
        @Optional

        Property<TGeometry> osmway();
       //  Property<Map<String, List<String>>> properties();

        Property<List<String>> properties();
    }


    interface  Events
    {
        public void created(TFeature feature);
    }


    class Mixin implements Feature
    {

        @This
        Data state;

        @This
        Events events;

        public void create(TFeature feature)
        {
            events.created(feature);
        }

    }

    class EventsMixin implements Events {

        @This
        Data state;


        public void created(TFeature feature)
        {
            if (feature.asGeometry().getType() == TGeometryRoot.TGEOMETRY_TYPE.POINT)
            {
                state.osmpoint().set((TPoint)feature.asGeometry());
            } else
            {
                state.osmway().set(feature.asGeometry());
            }

           // state.properties().set(feature.properties().get());
            state.properties().set(feature.properties().get().get("osm"));
        }
    }

}
