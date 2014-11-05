package org.qi4j.api.geometry;

import org.qi4j.api.common.Optional;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;


@Mixins(GeometryFactory.Mixin.class)
public interface GeometryFactory
        extends ServiceComposite, ServiceActivation {

    // TGeomPoint Point(double lat, double lng);

    TGeometry of(TGeomRoot.TGEOM_2D type, double lat, double lng);

    TPolygon asPolygon(TLinearRing shell, @Optional TLinearRing... holes);

    TPoint asPoint(Coordinate... coordinates);
    TPoint as2DPoint(double x, double y);

    Coordinate asCoordinate(double... coordinates);

    TFeature asFeature(TGeometry geometry);

    TLinearRing asLinearRing(TPoint... points);

    TLineString asLinearString(TPoint... points);


    public abstract class Mixin implements GeometryFactory {

        @Structure
        Module module;

        public TGeometry of(TGeomRoot.TGEOM_2D type, double lat, double lng) {

            switch (type) {
                case POINT: {
                    return newPoint(lat, lng);
                }
                // case POLYGON : {return newPolygon(); }
            }

            return null;
        }

        public Coordinate asCoordinate(double... xyzn) {

            return module.newValueBuilder(Coordinate.class).prototype().of(xyzn);
        }

        public TPoint asPoint(Coordinate... coordinates) {

            return module.newValueBuilder(TPoint.class).prototype().of(coordinates);
        }


        public TPoint as2DPoint(double x, double y) {
            return module.newValueBuilder(TPoint.class).prototype().of(
                    module.newValueBuilder(Coordinate.class).prototype().of(x),
                    module.newValueBuilder(Coordinate.class).prototype().of(y)
            );
        }


        public TPolygon asPolygon(TLinearRing shell, @Optional TLinearRing... holes) {
            return module.newValueBuilder(TPolygon.class).prototype().of(shell, holes);
        }

        public TLinearRing asLinearRing(TPoint... points) {
            return module.newValueBuilder(TLinearRing.class).prototype().of(points);
        }

        public TLineString asLinearString(TPoint... points) {
            return module.newValueBuilder(TLineString.class).prototype().of(points);
        }

        public TFeature asFeature(TGeometry geometry) {

            return module.newValueBuilder(TFeature.class).prototype().of(geometry);
        }


        private TPoint newPoint(double x, double y) {
            return module.newValueBuilder(TPoint.class).prototype().of(
                    module.newValueBuilder(Coordinate.class).prototype().of(x),
                    module.newValueBuilder(Coordinate.class).prototype().of(y)
            );

        }


        public void activateService()
                throws Exception {


        }

        public void passivateService()
                throws Exception {
        }

    }

}
