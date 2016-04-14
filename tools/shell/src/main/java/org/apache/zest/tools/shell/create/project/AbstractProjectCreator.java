package org.apache.zest.tools.shell.create.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.zest.tools.shell.FileUtils;

abstract class AbstractProjectCreator
    implements ProjectCreator
{

    @Override
    public void create( String projectName,
                        File projectDir,
                        Map<String, String> properties
    )
        throws IOException
    {
        File templateDir = new File( properties.get( "zest.home" ), properties.get( "template.dir" ) );
        copyFiles( templateDir, projectDir, properties.get( "root.package" ) );
    }

    private void copyFiles( File fromDir, File toDir, String rootpackage )
        throws IOException
    {
        File[] files = fromDir.listFiles();
        if( files == null )
        {
            return;
        }
        toDir.mkdirs();     // create all directories needed.
        for( File f : files )
        {
            String filename = f.getName();
            if( f.isDirectory() )
            {
                if( filename.equals( "__package__" ) )
                {
                    toDir = new File( toDir, rootpackage.replaceAll( "\\.", "/" ) );
                    copyFiles( f, toDir, rootpackage );
                }
                else
                {
                    copyFiles( f, new File( toDir, filename ), rootpackage );
                }
            }
            if( f.isFile() )
            {
                if( !filename.equals( "___placeholder___" ) )  // skip these files that are needed for GIT.
                {
                    if( filename.endsWith( "_" ) )
                    {
                        filename = filename.substring( 0, filename.length() - 1 );
                    }
                    File dest = new File( toDir, filename );
                    FileUtils.copyFile( f, dest );
                }
            }
        }
    }
}
