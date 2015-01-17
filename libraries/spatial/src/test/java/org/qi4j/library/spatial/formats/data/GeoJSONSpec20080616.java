package org.qi4j.library.spatial.formats.data;

/**
 * http://geojson.org/geojson-spec.html
 */
public class GeoJSONSpec20080616
{


    // Version 1.0

    public static final String LineString = "{ \"type\": \"LineString\",\r\n    \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ]\r\n }";

    public static final String Polygon = "{ \"type\": \"Polygon\",\r\n    \"coordinates\": [\r\n      [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]\r\n      ]\r\n }";

    public static final String Polygon_with_Holes = "{ \\\"type\\\": \\\"Polygon\\\",\\r\\n    \\\"coordinates\\\": [\\r\\n      [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ],\\r\\n      [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ]\\r\\n      ]\\r\\n  }";

    public static final String Multipoint = "{ \\\"type\\\": \\\"MultiPoint\\\",\\r\\n    \\\"coordinates\\\": [ [100.0, 0.0], [101.0, 1.0] ]\\r\\n  }";

    public static final String MultiLineString = "{ \\\"type\\\": \\\"MultiLineString\\\",\\r\\n    \\\"coordinates\\\": [\\r\\n        [ [100.0, 0.0], [101.0, 1.0] ],\\r\\n        [ [102.0, 2.0], [103.0, 3.0] ]\\r\\n      ]\\r\\n    }";

    public static final String MultiPolygon = "{ \\\"type\\\": \\\"MultiPolygon\\\",\\r\\n    \\\"coordinates\\\": [\\r\\n      [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],\\r\\n      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],\\r\\n       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]\\r\\n      ]\\r\\n  }";

    public static final String GeometryCollection = "{ \\\"type\\\": \\\"GeometryCollection\\\",\\r\\n    \\\"geometries\\\": [\\r\\n      { \\\"type\\\": \\\"Point\\\",\\r\\n        \\\"coordinates\\\": [100.0, 0.0]\\r\\n        },\\r\\n      { \\\"type\\\": \\\"LineString\\\",\\r\\n        \\\"coordinates\\\": [ [101.0, 0.0], [102.0, 1.0] ]\\r\\n        }\\r\\n    ]\\r\\n  }";

    public static final String FeatureCollection = "{ \"type\": \"FeatureCollection\",\r\n    \"features\": [\r\n      { \"type\": \"Feature\",\r\n        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\r\n        \"properties\": {\"prop0\": \"value0\"}\r\n        },\r\n      { \"type\": \"Feature\",\r\n        \"geometry\": {\r\n          \"type\": \"LineString\",\r\n          \"coordinates\": [\r\n            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\r\n            ]\r\n          },\r\n        \"properties\": {\r\n          \"prop0\": \"value0\",\r\n          \"prop1\": 0.0\r\n          }\r\n        },\r\n      { \"type\": \"Feature\",\r\n         \"geometry\": {\r\n           \"type\": \"Polygon\",\r\n           \"coordinates\": [\r\n             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\r\n               [100.0, 1.0], [100.0, 0.0] ]\r\n             ]\r\n         },\r\n         \"properties\": {\r\n           \"prop0\": \"value0\",\r\n           \"prop1\": {\"this\": \"that\"}\r\n           }\r\n         }\r\n       ]\r\n     }";

}
