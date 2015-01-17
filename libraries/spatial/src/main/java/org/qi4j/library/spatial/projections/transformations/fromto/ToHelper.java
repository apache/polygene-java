/*
 * Copyright 2014 Jiri Jetmar.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.spatial.projections.transformations.fromto;

import org.cts.IllegalCoordinateException;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.projections.ProjectionsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class ToHelper
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ToHelper.class);


    private Module module;
    private TGeometry intermediate;
    private double maxPrecisionMeanConversionError = Double.MAX_VALUE;

    public ToHelper(Module module, TGeometry intermediate)
    {
        this.module = module;
        this.intermediate = intermediate;
    }

    private ToHelper()
    {
    }


    public void to(String CRS, double maxPrecisionMeanConversionError) throws RuntimeException
    {
        this.maxPrecisionMeanConversionError = maxPrecisionMeanConversionError;
        to(CRS);
    }

    public void to(String CRS) throws RuntimeException
    {
        try
        {
            GeodeticCRS sourceCRS = (GeodeticCRS) new ProjectionsRegistry().getCRS(intermediate.getCRS());
            GeodeticCRS targetCRS = (GeodeticCRS) new ProjectionsRegistry().getCRS(CRS);

            if (sourceCRS.equals(targetCRS))
            {
                return;
            }
            switch (intermediate.getType())
            {
                case POINT:
                    transform(sourceCRS, targetCRS, new Coordinate[]{((TPoint) intermediate).getCoordinate()});
                    break;
                case MULTIPOINT:
                    transform(sourceCRS, targetCRS, ((TMultiPoint) intermediate).getCoordinates());
                    break;
                case LINESTRING:
                    transform(sourceCRS, targetCRS, ((TLineString) intermediate).getCoordinates());
                    break;
                case MULTILINESTRING:
                    transform(sourceCRS, targetCRS, ((TMultiLineString) intermediate).getCoordinates());
                    break;
                case POLYGON:
                    transform(sourceCRS, targetCRS, ((TPolygon) intermediate).getCoordinates());
                    break;
                case MULTIPOLYGON:
                    transform(sourceCRS, targetCRS, ((TMultiPolygon) intermediate).getCoordinates());
                    break;
                case FEATURE:
                    transform(sourceCRS, targetCRS, ((TFeature) intermediate).getCoordinates());
                    break;
                case FEATURECOLLECTION:
                    transform(sourceCRS, targetCRS, ((TFeatureCollection) intermediate).getCoordinates());
                    break;
            }

            // JJ TODO - Set nested TGeometries CRSs as well
            intermediate.setCRS(targetCRS.getCode());
        } catch (Exception _ex)
        {
            throw new RuntimeException(_ex);
        }
    }


    private void transformTPoint(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, TPoint tPoint) throws Exception
    {
        transform(sourceCRS, targetCRS, new Coordinate[]{tPoint.getCoordinate()});
        tPoint.setCRS(targetCRS.getCode());
    }

    private void transformTMultiPoint(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, TMultiPoint tMultiPoint) throws Exception
    {
        transform(sourceCRS, targetCRS, tMultiPoint.getCoordinates());
        tMultiPoint.setCRS(targetCRS.getCode());
        // tMultiPoint.
    }

    private void transformTLineString(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, TLineString tLineString) throws Exception
    {

        transform(sourceCRS, targetCRS, tLineString.getCoordinates());
        tLineString.setCRS(targetCRS.getCode());
        // tMultiPoint.
    }


    private void transform(GeodeticCRS sourceCRS, GeodeticCRS targetCRS, Coordinate... coordinates) throws IllegalCoordinateException
    {
        List<CoordinateOperation> ops;
        ops = CoordinateOperationFactory.createCoordinateOperations(sourceCRS, targetCRS);

        if (!ops.isEmpty())
        {
            if (true)
            {
                LOGGER.trace("Projection conversion operations", ops.get(0));
                LOGGER.trace("Projection precision", ops.get(0).getPrecision(), " m.");
            }

            if (maxPrecisionMeanConversionError < Double.MAX_VALUE && maxPrecisionMeanConversionError < ops.get(0).getPrecision())
                throw new RuntimeException("Transformation from " + sourceCRS.getCode() + " to " + targetCRS.getCode() +
                        " can not be done with the requested precision of " + maxPrecisionMeanConversionError + " meters." +
                        " Current precision mean conversion error is " + ops.get(0).getPrecision() + " meters.");

            for (Coordinate coordinate : coordinates)
            {
                double[] c = ops.get(0).transform(new double[]{coordinate.y(), coordinate.x() /** z */});
                coordinate.y(c[1]).x(c[0]);
            }
        }
    }
}
