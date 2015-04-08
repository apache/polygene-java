package org.qi4j.tools.shell.model;

import java.io.File;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

@Mixins( Module.Mixin.class )
public interface Module extends Nameable
{
    // Commands
    void setPackageName( String packageName );

    // Queries
    File mainJavaRootPackageDirectory( File projectDir );

    File mainResourcesRootPackageDirectory( File projectDir );

    File testJavaRootPackageDirectory( File projectDir );

    File testResourcesRootPackageDirectory( File projectDir );

    public interface State
    {
        Property<String> name();

        Property<String> packageName();
    }

    abstract class Mixin
        implements Module
    {
        @This
        private State state;

        @Override
        public void setPackageName( String name )
        {
            state.packageName().set( name );
        }

        @Override
        public File mainJavaRootPackageDirectory( File projectDir )
        {
            return normalize( projectDir, "/src/main/java" );
        }

        @Override
        public File mainResourcesRootPackageDirectory( File projectDir )
        {
            return normalize( projectDir, "/src/main/resources" );
        }

        @Override
        public File testJavaRootPackageDirectory(File projectDir)
        {
            return normalize( projectDir, "/src/test/java" );
        }

        @Override
        public File testResourcesRootPackageDirectory(File projectDir)
        {
            return normalize( projectDir, "/src/test/resources" );
        }

        private File normalize( File projectDir, String location )
        {
            File dir = new File( projectDir, name() + location );
            dir = new File( dir, state.packageName().get().replace( '.', '/' ) );
            dir.mkdirs();
            return dir;
        }
    }
}
