package org.qi4j.sample.spatial.domain.openstreetmap.model.interactions.api;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.library.eventsourcing.domain.api.DomainEvent;
import org.qi4j.sample.spatial.domain.openstreetmap.model.OSMEntity;
import org.qi4j.sample.spatial.domain.openstreetmap.model.state.FeatureState;

import java.util.Map;
import java.util.UUID;

@Mixins({
        OSM.Mixin.class,
        OSM.EventsMixin.class
})
public interface OSM {

    public static final String ID = UUID.randomUUID().toString();

    // API

    public FeatureState createFeature(TFeature feature, Map<String, String> properties) throws Exception;

    public static class Repository {

        public static OSM $(Module module) {
            try {
                return module.currentUnitOfWork().get(OSM.class, ID);
            } catch (NoSuchEntityException e) {
                return module.currentUnitOfWork().newEntity(OSM.class, ID);
            }
        }
    }


    /**
     * Events
     */
    interface Events {

        @DomainEvent
        OSM journalCreated(EntityReference owner, @Optional String name, @Optional String description) throws Exception;

        // @DomainEvent
        // void journalDestroyed(String journalId);
        FeatureState featureCreated(TFeature feature, Map<String, String> properties) throws Exception;
    }


    /**
     * Mixin
     */
    class Mixin implements OSM {

        @This
        Events events;

        @Structure
        Module module;

        // @Override
        public OSM create(EntityReference owner, @Optional String name, @Optional String description) throws Exception {
            return events.journalCreated(owner, name, description);
        }

        public FeatureState createFeature(TFeature feature, Map<String, String> properties) throws Exception
        {
            return events.featureCreated(feature, properties);
        }




    }

    /**
     * Events mixin
     */
    class EventsMixin implements Events {

        @Structure
        Module module;

        @This
        OSMEntity thisEntity;

        @Override
        public OSM journalCreated(EntityReference owner, @Optional String name, @Optional String description) throws Exception {
            return null;
        }

        public FeatureState featureCreated(TFeature feature, Map<String, String> properties) throws Exception
        {
           // System.out.println("Feature created : " + feature);
            EntityBuilder<FeatureCmds> eb = module.currentUnitOfWork().newEntityBuilder(FeatureCmds.class);
            // eb.instance().
            eb.instance().createWithProperties(feature, properties);
            eb.newInstance();

            return null;
        }
    }
}
