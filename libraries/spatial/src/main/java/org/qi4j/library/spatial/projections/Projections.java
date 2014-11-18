package org.qi4j.library.spatial.projections;

/**
 * Created by jakes on 2/20/14.
 */
public interface Projections {

    String EPSG_1102110 = "PROJCS[\"RGF93_Lambert_93\",GEOGCS[\"GCS_RGF_1993\","
            + "DATUM[\"D_RGF_1993\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],"
            + "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],"
            + "PROJECTION[\"Lambert_Conformal_Conic\"],PARAMETER[\"False_Easting\",700000.0],"
            + "PARAMETER[\"False_Northing\",6600000.0],PARAMETER[\"Central_Meridian\",3.0],"
            + "PARAMETER[\"Standard_Parallel_1\",44.0],PARAMETER[\"Standard_Parallel_2\",49.0],"
            + "PARAMETER[\"Latitude_Of_Origin\",46.5],UNIT[\"Meter\",1.0]]";


    String EPSG_27561= "PROJCS[\" NTF (Paris) / Lambert zone II \",GEOGCS[\" NTF (Paris) \","
            + "DATUM[\" Nouvelle_Triangulation_Francaise_Paris \","
            + "SPHEROID[\" Clarke 1880 (IGN) \", 6378249.2 , 293.4660212936269 ,"
            + "AUTHORITY[\" EPSG \" , \" 7011 \"]],TOWGS84[ -168 , -60 , 320 , 0 , 0 , 0 , 0 ],"
            + "AUTHORITY[\" EPSG \" , \" 6807 \"]],PRIMEM[\" Paris \", 2.33722917 ,"
            + "AUTHORITY[\" EPSG \" , \" 8903 \"]],UNIT[\" grad \", 0.01570796326794897 ,"
            + "AUTHORITY[\" EPSG \" , \" 9105 \"]],AUTHORITY[\" EPSG \",\" 4807 \"]],UNIT[\" metre \" , 1 , "
            + "AUTHORITY[\" EPSG \" , \" 9001 \"]],PROJECTION[\" Lambert_Conformal_Conic_1SP \"],"
            + "PARAMETER[\" latitude_of_origin \" , 52 ],PARAMETER[\" central_meridian \" , 0 ],"
            + "PARAMETER[\" scale_factor \" , 0.99987742 ],PARAMETER[\" false_easting \" , 600000 ],"
            + "PARAMETER[\" false_northing \" , 2200000 ],"
            + "AUTHORITY[\" EPSG \" , \" 27572 \"],AXIS[\" X \" , EAST ],AXIS[\" Y \" , NORTH ]] ";
}
