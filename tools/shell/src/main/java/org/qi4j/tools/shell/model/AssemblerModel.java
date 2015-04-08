package org.qi4j.tools.shell.model;

import java.io.File;
import java.io.IOException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.tools.shell.model.generation.AssemblerGenerator;

@Mixins( { AssemblerModel.Mixin.class, AssemblerGenerator.class } )
public interface AssemblerModel
{
    // Commands
    void generate( Project project, File projectDir )
        throws IOException;

    // Queries
    String packageName();

    // Queries
    File mainJavaRootPackageDirectory( File projectDir );

    File mainResourcesRootPackageDirectory( File projectDir );

    File testJavaRootPackageDirectory( File projectDir );

    File testResourcesRootPackageDirectory( File projectDir );

    interface State
    {
        Property<String> packageName();
    }

    abstract class Mixin
        implements AssemblerModel
    {
        @This
        private State state;

        @Override
        public File mainJavaRootPackageDirectory( File projectDir )
        {
            return normalize( projectDir, "/bootstrap/src/main/java" );
        }

        @Override
        public File mainResourcesRootPackageDirectory( File projectDir )
        {
            return normalize( projectDir, "/bootstrap/src/main/resources" );
        }

        @Override
        public File testJavaRootPackageDirectory( File projectDir )
        {
            return normalize( projectDir, "/bootstrap/src/test/java" );
        }

        @Override
        public File testResourcesRootPackageDirectory( File projectDir )
        {
            return normalize( projectDir, "/bootstrap/src/test/resources" );
        }

        private File normalize( File projectDir, String location )
        {
            File dir = new File( projectDir, location );
            dir = new File( dir, state.packageName().get().replace( '.', '/' ) );
            dir.mkdirs();
            return dir;
        }

        @Override
        public String packageName()
        {
            return state.packageName().get();
        }
    }
}
