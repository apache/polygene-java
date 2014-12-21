package org.qi4j.test.indexing;

import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.geometry.*;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.test.EntityTestAssembler;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGeometryFactory.*;
import static org.qi4j.api.geometry.TGeometryFactory.TLinearRing;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import static org.qi4j.api.query.grammar.extensions.spatial.SpatialQueryExpressions.ST_Within;

/**
 * Created by jj on 21.12.14.
 */
public abstract class AbstractSpatialRegressionTest
        extends AbstractAnyQueryTest
{

    public interface SpatialRegressionEntity extends EntityComposite, SpatialRegressionsValues {}

    public interface SpatialRegressionsValues
    {
        @Optional Property<TPoint>              point();
        @Optional Property<TMultiPoint>         multipoint();
        @Optional Property<TLineString>         line();
        @Optional Property<TPolygon>            polygon();
        @Optional Property<TMultiPolygon>       multipolygon();
        @Optional Property<TFeature>            feature();
        @Optional Property<TFeatureCollection>  featurecollection();
    }

    private TPoint      _TPoint;
    private TMultiPoint _TMultiPoint;
    private TLineString _TLineString;
    private TPolygon    _TPolygon;


    public void setUp() throws Exception {
        super.setUp();

        _TPoint = TPoint(module).y(48.13905780942574).x(11.57958984375)
                .geometry();

        _TMultiPoint = TMultiPoint(module).points(new double[][]
                {
                                                { 48.13905780942574 , 11.579589843750000 },
                                                { 48.14913756559802 , 11.599502563476562 }
                })
                .geometry();
        _TLineString = TlineString(module).points(new double[][]
                {
                                                { 48.109035906197036 , 11.550750732421875 },
                                                { 48.16608541901253  , 11.552810668945312 }
                })
                .geometry();
        _TPolygon = TPolygon(module)
                .shell
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                { 48.14478518644042 , 11.475906372070312 },
                                                { 48.18760570101003 , 11.572723388671875 },
                                                { 48.14043243818813 , 11.692886352539062 },
                                                { 48.08243697630599 , 11.679153442382812 },
                                                { 48.07211472138644 , 11.581306457519531 },
                                                { 48.10124109364004 , 11.522941589355469 },
                                                { 48.10949438777014 , 11.470069885253906 },
                                                { 48.14478518644042 , 11.475906372070312 }

                                        })
                                        .geometry()
                        )
                .withHoles
                        (
                                TLinearRing(module).ring(new double[][]
                                        {
                                                { 48.13837048124154 , 11.538391113281250 },
                                                { 48.15028286718964 , 11.614952087402344 },
                                                { 48.10513864768105 , 11.640357971191406 },
                                                { 48.10330454141599 , 11.558303833007812 },
                                                { 48.13837048124154 , 11.538391113281250 }

                                        })
                                        .geometry()
                        )
                .geometry();



        try (UnitOfWork unitOfWork = module.newUnitOfWork()) {

            // TPoint
            {
                EntityBuilder<SpatialRegressionEntity> pointBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "Point");
                pointBuilder.instance().point().set(_TPoint);
                pointBuilder.newInstance();
            }

            // TMultiPoint
            {
                 EntityBuilder<SpatialRegressionEntity> mPointBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "MultiPoint");
                 mPointBuilder.instance().multipoint().set(_TMultiPoint);
                 mPointBuilder.newInstance();
            }

            // TLineString
            {
                 EntityBuilder<SpatialRegressionEntity> tlineStringBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "LineString");
                 tlineStringBuilder.instance().line().set(_TLineString);
                 tlineStringBuilder.newInstance();
            }

            // TPolygon
            {
                EntityBuilder<SpatialRegressionEntity> tPolygonBuilder = unitOfWork.newEntityBuilder(SpatialRegressionEntity.class, "Polygon");
                tPolygonBuilder.instance().polygon().set(_TPolygon);
                tPolygonBuilder.newInstance();
            }

            unitOfWork.complete();
        }
    }




    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        super.assemble( module );

        module.entities(SpatialRegressionEntity.class);
    }

    @Test
    public void script01()
            throws EntityFinderException
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(SpatialRegressionsValues.class).point(),
                                         TPoint(module).y(48.13905780942574).x(11.57958984375).geometry(),
                                         10,TUnit.METER
                                        )
                        ));
        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);
    }

    @Test
    public void script02()
    {
        QueryBuilder<SpatialRegressionsValues> qb = this.module.newQueryBuilder(SpatialRegressionsValues.class);

        Query<SpatialRegressionsValues> query = unitOfWork.newQuery(
                qb
                        .where(
                                ST_Within
                                        (
                                         templateFor(SpatialRegressionsValues.class).point(),
                                         TPoint(module).y(2389280.7514562616).x(1286436.5975464052).geometry("EPSG:27572"),
                                         10,TUnit.METER
                                        )
                        ));

        query.find();
        assertEquals(1, query.count());
        TPoint tPoint = query.iterator().next().point().get();
        assertTrue(tPoint.compareTo(_TPoint) == 0);

        // Transform(module).from(tPoint).to("EPSG:4326");
    }
}
