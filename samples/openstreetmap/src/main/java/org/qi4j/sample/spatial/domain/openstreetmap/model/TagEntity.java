package org.qi4j.sample.spatial.domain.openstreetmap.model;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.sample.spatial.domain.openstreetmap.model.state.IdentifiableState;
import org.qi4j.sample.spatial.domain.openstreetmap.model.state.PropertiesState;

/**
 * Created by jj on 28.11.14.
 */
public interface TagEntity extends EntityComposite

        ,PropertiesState.latest
        ,IdentifiableState.latest

{}
