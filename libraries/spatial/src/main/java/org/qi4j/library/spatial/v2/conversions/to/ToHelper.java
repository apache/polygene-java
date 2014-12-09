package org.qi4j.library.spatial.v2.conversions.to;

import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.v2.conversions.to.geojson.GeoJsonToConverter;
import org.qi4j.library.spatial.v2.conversions.to.geometry.TGeometryToConverter;

import static org.qi4j.library.spatial.v2.transformations.TTransformations.Transform;

/**
 * Created by jj on 04.12.14.
 */
public class ToHelper<T extends TGeometry> {

    private Module module;
    private TGeometry intermediate;

    public ToHelper(Module module, TGeometry intermediate)
    {
        this.module = module;
        this.intermediate = intermediate;
    }

    private ToHelper() {}

    public TGeometry toTGeometry()
    {
        return new TGeometryToConverter(module).convert(intermediate);
    }

    public TGeometry toTGeometry(String CRS) throws Exception
    {
        if (!intermediate.getCRS().equalsIgnoreCase(CRS))
                Transform(module).from(intermediate).to(CRS) ;

        return new TGeometryToConverter(module).convert(intermediate, CRS);
    }



    public GeoJsonObject toGeoJson()
    {
        return new GeoJsonToConverter(module).convert(intermediate);
    }


}
