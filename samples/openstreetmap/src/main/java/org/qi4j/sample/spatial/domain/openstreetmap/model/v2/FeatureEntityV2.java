package org.qi4j.sample.spatial.domain.openstreetmap.model.v2;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.sample.spatial.domain.openstreetmap.model.v2.structure.Feature;

/**
 * Created by jj on 10.12.14.
 */
public interface FeatureEntityV2 extends EntityComposite,

        Feature,
        Feature.Data
{}
