package org.qi4j.library.geometry;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.geometry.builders.TCRSBuilder;
import org.qi4j.library.geometry.builders.TFeatureBuilder;
import org.qi4j.library.geometry.builders.TFeatureCollectionBuilder;
import org.qi4j.library.geometry.builders.TLineStringBuilder;
import org.qi4j.library.geometry.builders.TLinearRingBuilder;
import org.qi4j.library.geometry.builders.TMultiLineStringBuilder;
import org.qi4j.library.geometry.builders.TMultiPointBuilder;
import org.qi4j.library.geometry.builders.TMultiPolygonsBuilder;
import org.qi4j.library.geometry.builders.TPointBuilder;
import org.qi4j.library.geometry.builders.TPolygonBuilder;

@Mixins( TGeometryFactory.Mixin.class )
public interface TGeometryFactory
{
    TCRSBuilder TCrs();

    TPointBuilder TPoint();

    TMultiPointBuilder TMultiPoint();

    TLinearRingBuilder TLinearRing();

    TLineStringBuilder TLineString();

    TMultiLineStringBuilder TMultiLineString();

    TPolygonBuilder TPolygon();

    TMultiPolygonsBuilder TMultiPolygon();

    TFeatureBuilder TFeature();

    TFeatureCollectionBuilder TFeatureCollection();

    class Mixin implements TGeometryFactory
    {
        @Structure
        private Module module;

        public TCRSBuilder TCrs()
        {
            return new TCRSBuilder( module );
        }

        public TPointBuilder TPoint()
        {
            return new TPointBuilder( module );
        }

        public TMultiPointBuilder TMultiPoint()
        {
            return new TMultiPointBuilder( module );
        }

        public TLinearRingBuilder TLinearRing()
        {
            return new TLinearRingBuilder( module );
        }

        public TLineStringBuilder TLineString()
        {
            return new TLineStringBuilder( module );
        }

        public TMultiLineStringBuilder TMultiLineString()
        {
            return new TMultiLineStringBuilder( module );
        }

        public TPolygonBuilder TPolygon()
        {
            return new TPolygonBuilder( module );
        }

        public TMultiPolygonsBuilder TMultiPolygon()
        {
            return new TMultiPolygonsBuilder( module );
        }

        public TFeatureBuilder TFeature()
        {
            return new TFeatureBuilder( module );
        }

        public TFeatureCollectionBuilder TFeatureCollection()
        {
            return new TFeatureCollectionBuilder( module );
        }
    }
}
