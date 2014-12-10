package org.qi4j.sample.spatial.domain.openstreetmap.model;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.sample.spatial.domain.openstreetmap.model.interactions.api.FeatureCmds;
import org.qi4j.sample.spatial.domain.openstreetmap.model.state.FeatureState;
import org.qi4j.sample.spatial.domain.openstreetmap.model.state.IdentifiableState;

/**
 * Created by jj on 28.11.14.
 */
public interface FeatureEntity extends EntityComposite,

    IdentifiableState.latest,
    FeatureState.latest,
        FeatureCmds

{}
