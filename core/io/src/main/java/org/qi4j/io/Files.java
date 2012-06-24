package org.qi4j.io;

import java.io.File;
import java.util.Random;

public class Files
{
    private static Random random = new Random();

    public static File createTemporayFileOf( File file )
    {
        return new File(  file.getAbsolutePath() + "_" + Math.abs( random.nextLong() ) );
    }
}
