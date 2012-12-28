package org.qi4j.io;

import java.io.File;
import java.util.Random;

/**
 * Utility class for files.
 */
public class Files
{
    private static Random random = new Random();

    public static File createTemporayFileOf( File file )
    {
        return new File(  file.getAbsolutePath() + "_" + Math.abs( random.nextLong() ) );
    }
}
