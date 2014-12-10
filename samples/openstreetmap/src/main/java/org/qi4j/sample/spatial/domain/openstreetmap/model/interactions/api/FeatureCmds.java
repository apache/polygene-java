package org.qi4j.sample.spatial.domain.openstreetmap.model.interactions.api;

import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.sample.spatial.domain.openstreetmap.model.state.FeatureState;

import java.util.Map;

@Mixins(FeatureCmds.FeatureMixin.class)
public interface FeatureCmds {

    void createWithProperties(TFeature feature, Map<String, String> properties);


    @Mixins(FeatureEvents.PaymentEventsMixin.class)
    interface FeatureEvents
    {

        void created(TFeature feature, Map<String, String> properties);

        class PaymentEventsMixin implements FeatureEvents {
            @This
            private FeatureState.latest state;

            public void created(TFeature feature, Map<String, String> properties) {
                state.feature().set(feature);
                // state.properties().
            }
        }
    }

     class FeatureMixin implements FeatureCmds
    {

        // @This
        // private FeatureState.latest status;

        @This
        private FeatureEvents events;

        public void createWithProperties(TFeature feature, Map<String, String> properties)
        {
            events.created(feature, properties);
        }

    }
}
