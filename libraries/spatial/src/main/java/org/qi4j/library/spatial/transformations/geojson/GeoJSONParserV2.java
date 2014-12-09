package org.qi4j.library.spatial.transformations.geojson;

import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.transformations.geojson.internal.ParserBuilder;
import org.qi4j.library.spatial.transformations.geojson.internal.TransformationBuilder;

import java.io.InputStream;

/**
 * Created by jj on 28.11.14.
 */
public class GeoJSONParserV2 {

    public static final ParserBuilder source(InputStream source)
    {
        return new ParserBuilder(source);
    }

    public static final TransformationBuilder transform(Module module)
    {
        return new TransformationBuilder(module);
    }
}
