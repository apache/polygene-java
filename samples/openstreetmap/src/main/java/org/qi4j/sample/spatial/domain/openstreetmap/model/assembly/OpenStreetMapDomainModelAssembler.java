package org.qi4j.sample.spatial.domain.openstreetmap.model.assembly;

import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.sample.spatial.domain.openstreetmap.model.OSMEntity;
import org.qi4j.sample.spatial.domain.openstreetmap.model.TagEntity;
import org.qi4j.sample.spatial.domain.openstreetmap.model.v2.FeatureEntityV2;
import org.qi4j.sample.spatial.domain.openstreetmap.model.v2.OSMEntityV2;
// import org.qi4j.sample.spatial.domain.openstreetmap.model.TagEntity;


/**
 * Created by jj on 28.11.14.
 */
public class OpenStreetMapDomainModelAssembler implements Assembler {

    private final String CRS_EPSG_4326 = "EPSG:4326";


    @Override
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
       // module.entities(FeatureEntity.class, TagEntity.class, OSMEntity.class);

        module.entities(OSMEntityV2.class, FeatureEntityV2.class);

        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);

        module.services(GeometryFactory.class);

        TGeometry tGeometry = module.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(CRS_EPSG_4326);


    }

}
