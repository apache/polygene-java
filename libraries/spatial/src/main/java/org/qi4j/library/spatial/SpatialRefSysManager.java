package org.qi4j.library.spatial;

// https://joinup.ec.europa.eu/svn/gearscape/versions/0.1/platform/gdms/src/test/java/org/gdms/source/crs/TransformTest.java

import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.library.spatial.projections.ProjectionsRegistry;
import org.qi4j.library.spatial.projections.ProjectionsTransformation;

/**
 * Created by jakes on 2/20/14.
 */
public class SpatialRefSysManager {

    // getWKT(srid)


    static public CoordinateReferenceSystem getCRS(String wkt) throws CRSException
    {
       return new ProjectionsRegistry().getCRS(wkt);
    }

    public void getTransformation(int sourceSRID, int targetSRID) throws Exception
    {

    }

    public void getTransformation(String sourceSRCodeAndAuthority, String targetSRAuthorityAndCode) throws Exception
    {

    }

    static public TGeometry transform(TGeometry geometry, String targetSRAuthorityAndCode) throws Exception
    {
       return new ProjectionsTransformation().transform(geometry, getCRS(targetSRAuthorityAndCode));
    }
}
