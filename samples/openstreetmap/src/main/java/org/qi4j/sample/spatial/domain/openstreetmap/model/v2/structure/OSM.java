package org.qi4j.sample.spatial.domain.openstreetmap.model.v2.structure;

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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixins({
        OSM.Mixin.class,
        OSM.EventsMixin.class
})
public interface OSM {

    public static final String ID = UUID.randomUUID().toString();

    // API

    public Feature createFeature(TFeature feature) throws Exception;

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
        Feature featureCreated(TFeature feature) throws Exception;
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

        public Feature createFeature(TFeature feature) throws Exception
        {
            return events.featureCreated(feature);
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

        public Feature featureCreated(TFeature feature) throws Exception
        {
           // System.out.println("Feature created : " + feature);
            EntityBuilder<Feature> eb = module.currentUnitOfWork().newEntityBuilder(Feature.class);
            eb.instance().create(feature);
            return eb.newInstance();
        }
    }


}
