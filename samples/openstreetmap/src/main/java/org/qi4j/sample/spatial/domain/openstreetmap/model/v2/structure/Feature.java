package org.qi4j.sample.spatial.domain.openstreetmap.model.v2.structure;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TGeomRoot;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import java.util.List;
import java.util.Map;

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
            if (feature.asGeometry().getType() == TGeomRoot.TGEOMETRY.POINT)
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
