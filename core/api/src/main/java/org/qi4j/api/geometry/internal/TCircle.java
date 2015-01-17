package org.qi4j.api.geometry.internal;


import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

@Mixins(TCircle.Mixin.class)
public interface TCircle extends TGeometry
{
    Property<TPoint> point();

    Property<Double> radius();

    TCircle of(TPoint point, double radius);
    TCircle of(double x, double y, double radius);
    TCircle of(TPoint point);
    TCircle of();

    TCircle yx(double y, double x);
    TCircle yx(double y, double x, double radius);

    TCircle radius(double r);
    TPoint getCentre();
    TPolygon polygonize(int numOfPoints);

    public abstract class Mixin implements TCircle
    {
        @This
        TCircle self;

        @Structure
        Module module;

        public TCircle of()
        {
            return self;
        }

        public TCircle of(TPoint point)
        {
            self.point().set(point);
            return self;
        }

        public TCircle of(double x, double y, double radius)
        {
            yx(y, x).radius(radius);
            return self;
        }

        public TCircle yx(double y, double x)
        {
            of(module.newValueBuilder(TPoint.class).prototype().x(x).y(y));
            return self;
        }

        public TCircle yx(double y, double x, double radius)
        {
            of(module.newValueBuilder(TPoint.class).prototype().x(x).y(y)).radius(radius);
            return self;
        }

        public TCircle of(TPoint point, double r)
        {
            of(point).radius(r);
            return self;
        }

        public TPoint getCentre()
        {
            return self.point().get();
        }

        public TCircle radius(double r)
        {
            self.radius().set(r);
            return self;
        }


        public TPolygon polygonize(int numOfPoints)
        {
            double xRadius = self.radius().get();
            double yRadius = self.radius().get();

            double centreX = self.getCentre().x();
            double centreY = self.getCentre().y();

            TPoint[] pts = new TPoint[numOfPoints + 1];
            int pt = 0;
            for (int i = 0; i < numOfPoints; i++)
            {
                double ang = i * (2 * Math.PI / numOfPoints);
                double x = xRadius * Math.cos(ang) + centreX;
                double y = yRadius * Math.sin(ang) + centreY;
                pts[pt++] = module.newValueBuilder(TPoint.class).prototype().of().x(x).y(y);
            }

            pts[pt++] = module.newValueBuilder(TPoint.class).prototype().of(pts[0].getCoordinates());
            TLinearRing tLinearRing = (TLinearRing) module.newValueBuilder(TLinearRing.class).prototype().of(pts);

            if (tLinearRing.isValid())
            {
                TPolygon tPolygon = module.newValueBuilder(TPolygon.class).prototype().of(tLinearRing);
                tPolygon.setCRS(self.getCRS());
                return module.newValueBuilder(TPolygon.class).prototype().of(tLinearRing);
            } else
                return null;
        }
    }
}
