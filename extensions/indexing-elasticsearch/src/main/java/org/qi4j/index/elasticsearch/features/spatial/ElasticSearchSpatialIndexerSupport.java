package org.qi4j.index.elasticsearch.features.spatial;

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

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilder;
import org.json.JSONObject;
import org.qi4j.api.geometry.TGeometry;
import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.query.grammar.ComparisonSpecification;
import org.qi4j.api.query.grammar.ContainsAllSpecification;
import org.qi4j.api.query.grammar.ContainsSpecification;
import org.qi4j.api.query.grammar.Variable;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;

import java.io.IOException;
import java.util.*;

public final class ElasticSearchSpatialIndexerSupport
{

    private static Set<String> KNOWN_SPATIAL_INDEX_MAPPINGS_SUPPORT = new HashSet<String>();

    private static final String DEFAULT_PRECISION           = "10m";
    private static final boolean DEFAULT_GEOHASH_SUPPORT    = true;


    public static void supportedSpatialMappings(ElasticSearchSupport support, TGeometry spatialValueType, String valueType, String property, boolean updateESIndex )
    {

        try {
        // seems that ES does not like "dots" to be part of the mapping definition
        // valueType = valueType.replace(".", ":");

            valueType = valueType.replace(":", ".");

        System.out.println("ValueType " + valueType);

        System.out.println("--> Property " + property);

        if (!KNOWN_SPATIAL_INDEX_MAPPINGS_SUPPORT.contains(property))
        {
            if (updateESIndex)
            {
                String ESSpatialMapping = null;


                if (spatialValueType instanceof TPoint) // || spatialValueType.type().get().equalsIgnoreCase("point"))
                {
                    System.out.println("TPoint  supported " + spatialValueType);
                    ESSpatialMapping = createESGeoPointMapping(valueType, property);
                }
                else if (spatialValueType instanceof TLineString)
                {
                    // JJ TODO
                    System.out.println("TLineString not supported " + spatialValueType);

                }
                else if (spatialValueType instanceof TPolygon)
                {
                    System.out.println("TPolygon not supported " + spatialValueType);

                }
                else if (spatialValueType instanceof TGeometry)
                {
                    System.out.println("TGeometry..");
                    throw new UnsupportedOperationException( TGeometry.class.getName() + " not supported. Please use a concrete geometry type e.g. " + TPoint.class.getName());
                }
                else
                {
                    System.out.println("TOTher not supported " + spatialValueType);
                }



                System.out.println("# ESSpatialMapping " + ESSpatialMapping);


             PutMappingResponse ESSpatialMappingPUTResponse = support.client().admin().indices()
                .preparePutMapping(support.index()).setType("qi4j_entities" /** valueType */ )
                //.setSource("{\"type\":{\"properties\":{\"body\":{\"type\":\"integer\"}}}}")
                .setSource(ESSpatialMapping)
                .execute().actionGet();

                System.out.println("ESSpatialMappingPUTResponse " + ESSpatialMappingPUTResponse);

            }
            KNOWN_SPATIAL_INDEX_MAPPINGS_SUPPORT.add(property);
            System.out.println("Added to Index as spatial type" + property);
        }
        } catch(Exception _ex) {
            _ex.printStackTrace();
        }
    }

    public static JSONObject indexSpatialType(ElasticSearchSupport support, Object spatialValueType, String valueType, String property, JSONObject json, boolean updateESIndex ) throws Exception
    {
        System.out.println(spatialValueType);

        if (spatialValueType instanceof TPoint)
        {
            return createESGeoPointIndexValue( property, json, (TPoint)spatialValueType);
        }
        return null;
    }


    private static JSONObject createESGeoPointIndexValue(String property, JSONObject json, TPoint point) throws Exception
    {

        JSONObject jsonPoint = new JSONObject();
        // Format for location is [lon, lat]
        // jsonPoint.put("location", point.coordinates().get().get(1)+ "," + point.coordinates().get().get(0));
                 // .put("type", "geo_point");

        System.out.println("$lat " +  point.coordinates().get().get(0).coordinate().get().get(0));
        System.out.println("$lan " +  point.coordinates().get().get(1).coordinate().get().get(0));


        Map pointDef = new HashMap <String, Double>(2);
        // pointDef.put("lat", point.coordinates().get().get(0));
        // pointDef.put("lon", point.coordinates().get().get(1));
        pointDef.put("lat", point.coordinates().get().get(0).coordinate().get().get(0));
        pointDef.put("lon", point.coordinates().get().get(1).coordinate().get().get(0));
        // pointDef.put("type", "geo_point");

        // jsonPoint.put("Location", pointDef);
        // jsonPoint.put(property, pointDef);

        json.put(property, pointDef);


        System.out.println("#Indexing TPoint : " + json);

        return jsonPoint;
    }


    // https://www.found.no/foundation/elasticsearch-mapping-introduction/
    private static String createESGeoPointMapping(String valueType, String property) throws IOException
    {

        valueType = "qi4j_entities"; // TODO JJ hack here

        System.out.println("############## Property Tree" + property);

        XContentBuilder qi4jRootType = XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities"); // .startObject("properties");

        StringTokenizer tokenizer1 = new StringTokenizer(property, ".");
        String propertyLevel1;
        while(tokenizer1.hasMoreTokens()) {
            propertyLevel1 = tokenizer1.nextToken();
            System.out.println("--> start level " + propertyLevel1);
            qi4jRootType.startObject("properties").startObject(propertyLevel1);
        }


        qi4jRootType.field("type", "geo_point")
            .field("lat_lon", true)
                    // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
            .field("precision", DEFAULT_PRECISION)
            .field("validate_lat", "true")
            .field("validate_lon", "true");

        StringTokenizer tokenizer2 = new StringTokenizer(property, ".");
        String propertyLevel2;
        while(tokenizer2.hasMoreTokens()) {
            propertyLevel2 = tokenizer2.nextToken();
            System.out.println("--> end level " + propertyLevel2);
            // qi4jRootType.startObject(propertyLevel1);
            qi4jRootType.endObject();
        }

/**
        return XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities")// valueType)
                .startObject("properties").startObject(property)
                .field("type", "geo_point")
                .field("lat_lon", true)
                        // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
                .field("precision", DEFAULT_PRECISION)
                .field("validate_lat", "true")
                .field("validate_lon", "true")
                .endObject().endObject()
                .endObject().endObject().string();
   */

        qi4jRootType.endObject().endObject().endObject();

        System.out.println("qi4jRootType.toString() " + qi4jRootType.string());

        return qi4jRootType.string();
    }

    /**
    private static String createESGeoPointMapping(String valueType, String property) throws IOException
    {

        valueType = "qi4j_entities"; // TODO JJ hack here

        return XContentFactory.jsonBuilder().startObject().startObject("qi4j_entities")// valueType)
                .startObject("properties").startObject(property)
                .field("type", "geo_point")
                 .field("lat_lon", true)
                // .field("geohash", DEFAULT_GEOHASH_SUPPORT)
                .field("precision", DEFAULT_PRECISION)
                .field("validate_lat", "true")
                .field("validate_lon", "true")
                .endObject().endObject()
                .endObject().endObject().string();
    }
     */

    /* package */ static Object resolveVariable( Object value, Map<String, Object> variables )
    {
        if( value == null )
        {
            return null;
        }
        if( value instanceof Variable)
        {
            Variable var = (Variable) value;
            Object realValue = variables.get( var.variableName() );
            if( realValue == null )
            {
                throw new IllegalArgumentException( "Variable " + var.variableName() + " not bound" );
            }
            return realValue;
        }
        return value;
    }

    /* package */ static interface ComplexTypeSupport
    {

        FilterBuilder comparison(ComparisonSpecification<?> spec, Map<String, Object> variables);

        FilterBuilder contains(ContainsSpecification<?> spec, Map<String, Object> variables);

        FilterBuilder containsAll(ContainsAllSpecification<?> spec, Map<String, Object> variables);

    }


    /* package */ static class SpatialSupport
                implements  ComplexTypeSupport {


        public FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables )
        {
            return null;
        }

        public FilterBuilder contains( ContainsSpecification<?> spec, Map<String, Object> variables )
        {
            return null;
        }

        public FilterBuilder containsAll( ContainsAllSpecification<?> spec, Map<String, Object> variables )
        {
            return null;
        }

    }

//    /* package */ static class MoneySupport
//            implements ComplexTypeSupport
//    {
//        private static final String CURRENCY = ".currency";
//        private static final String AMOUNT = ".amount";
//
//        @Override
//        public FilterBuilder comparison( ComparisonSpecification<?> spec, Map<String, Object> variables )
//        {
////            String name = spec.property().toString();
////            String currencyTerm = name + CURRENCY;
////            String amountTerm = name + AMOUNT;
////            BigMoney money = ( (BigMoneyProvider) spec.value() ).toBigMoney();
////            String currency = money.getCurrencyUnit().getCurrencyCode();
////            BigDecimal amount = money.getAmount();
////            if( spec instanceof EqSpecification)
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        termFilter( amountTerm, amount )
////                );
////            }
////            else if( spec instanceof NeSpecification )
////            {
////                return andFilter(
////                        existsFilter( name ),
////                        orFilter( notFilter( termFilter( currencyTerm, currency ) ),
////                                notFilter( termFilter( amountTerm, amount ) ) )
////                );
////            }
////            else if( spec instanceof GeSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).gte( amount )
////                );
////            }
////            else if( spec instanceof GtSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).gt( amount )
////                );
////            }
////            else if( spec instanceof LeSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).lte( amount )
////                );
////            }
////            else if( spec instanceof LtSpecification )
////            {
////                return andFilter(
////                        termFilter( currencyTerm, currency ),
////                        rangeFilter( amountTerm ).lt( amount )
////                );
////            }
////            else
////            {
////                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
////                        + "(New Query API support missing?): "
////                        + spec.getClass() + ": " + spec );
////            }
//        }
//
//        @Override
//        public FilterBuilder contains( ContainsSpecification<?> spec,
//                                       Map<String, Object> variables )
////        {
////            String name = spec.collectionProperty().toString();
////            BigMoney money = ( (BigMoneyProvider) spec.value() ).toBigMoney();
////            String currency = money.getCurrencyUnit().getCurrencyCode();
////            BigDecimal amount = money.getAmount();
////            return andFilter(
////                    termFilter( name + CURRENCY, currency ),
////                    termFilter( name + AMOUNT, amount )
////            );
//        }
//
//        // @Override
//        public FilterBuilder containsAll( ContainsAllSpecification<?> spec,
//                                          Map<String, Object> variables )
//        {
////            String name = spec.collectionProperty().toString();
////            AndFilterBuilder contAllFilter = new AndFilterBuilder();
////            for( Object value : spec.containedValues() )
////            {
////                BigMoney money = ( (BigMoneyProvider) value ).toBigMoney();
////                String currency = money.getCurrencyUnit().getCurrencyCode();
////                BigDecimal amount = money.getAmount();
////                contAllFilter.add( termFilter( name + CURRENCY, currency ) );
////                contAllFilter.add( termFilter( name + AMOUNT, amount ) );
////            }
////            return contAllFilter;
//            return null;
//        }



    private ElasticSearchSpatialIndexerSupport()
    {
    }

}
