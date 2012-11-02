package org.qi4j.library.fileconfig;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( FileConfigurationService.Mixin.class )
@Activators( FileConfigurationService.Activator.class )
public interface FileConfigurationService
        extends FileConfiguration, ServiceComposite
{

    void resolveFileConfiguration();

    void eventuallyCleanupTestData();

    public static class Activator
            extends ActivatorAdapter<ServiceReference<FileConfigurationService>>
    {

        @Override
        public void afterActivation( ServiceReference<FileConfigurationService> activated )
                throws Exception
        {
            activated.get().resolveFileConfiguration();
        }

        @Override
        public void beforePassivation( ServiceReference<FileConfigurationService> passivating )
                throws Exception
        {
            passivating.get().eventuallyCleanupTestData();
        }

    }

    interface Data
    {

        Property<OS> os();

        Property<String> application();

        Property<File> user();

        Property<File> configuration();

        Property<File> data();

        Property<File> temporary();

        Property<File> cache();

        Property<File> log();

    }

    abstract class Mixin
            implements FileConfigurationService
    {

        final Logger logger = LoggerFactory.getLogger( getClass().getName() );

        @This
        Data data;

        @Uses
        ServiceDescriptor descriptor;

        @Structure
        Application app;

        @Override
        public void resolveFileConfiguration()
        {
            OS os = detectOS();

            data.os().set( os );
            logger.info( "Operating system : " + os.name() );

            // Get bundle with application name and configured directories
            ResourceBundle bundle = ResourceBundle.getBundle( FileConfiguration.class.getName(), new Locale( os.name() ) );

            Map arguments = getArguments( os );

            data.configuration().set( new File( format( bundle.getString( "configuration" ), arguments ) ) );
            data.data().set( new File( format( bundle.getString( "data" ), arguments ) ) );
            data.temporary().set( new File( format( bundle.getString( "temporary" ), arguments ) ) );
            data.cache().set( new File( format( bundle.getString( "cache" ), arguments ) ) );
            data.log().set( new File( format( bundle.getString( "log" ), arguments ) ) );

            applyOverride();

            eventuallyCleanupTestData();
            autoCreateDirectories();
        }

        private Map getArguments( OS os )
        {
            // Get user directory
            String user = System.getenv( "USERPROFILE" ); // On Windows we use this instead of user.home
            if ( user == null ) {
                user = System.getProperty( "user.home" );
            }

            data.user().set( new File( user ) );

            // Set application name. This is taken from the Qi4j application but can be overriden by a system property
            String application = System.getProperty( "application", app.name() );

            if ( !app.mode().equals( Application.Mode.production ) ) {
                application += "-" + app.mode().name();
            }

            data.application().set( application );

            // Temp dir
            String temp = System.getProperty( "java.io.tmpdir" );
            if ( temp.endsWith( "/" ) ) {
                temp = temp.substring( 0, temp.length() - 1 );
            }

            // Arguments available to use in directory specifications
            Map<String, String> arguments = new HashMap<String, String>();
            arguments.put( "application", application );
            arguments.put( "user", user );
            arguments.put( "os", os.name() );
            arguments.put( "temp", temp );

            // Add environment variables
            for ( Map.Entry<String, String> envVariable : System.getenv().entrySet() ) {
                arguments.put( "environment." + envVariable.getKey(), envVariable.getValue() );
            }
            // Add system properties
            for ( Map.Entry<Object, Object> envVariable : System.getProperties().entrySet() ) {
                arguments.put( "system." + envVariable.getKey(), envVariable.getValue().toString() );
            }

            return arguments;
        }

        private void applyOverride()
        {
            FileConfigurationOverride override = descriptor.metaInfo( FileConfigurationOverride.class );
            if ( override != null ) {
                if ( override.configuration() != null ) {
                    data.configuration().set( override.configuration() );
                }
                if ( override.data() != null ) {
                    data.data().set( override.data() );
                }
                if ( override.temporary() != null ) {
                    data.temporary().set( override.temporary() );
                }
                if ( override.cache() != null ) {
                    data.cache().set( override.cache() );
                }
                if ( override.log() != null ) {
                    data.log().set( override.log() );
                }
            }
        }

        @Override
        public void eventuallyCleanupTestData()
        {
            if ( app.mode().equals( Application.Mode.test ) ) {
                // Delete test data
                delete( configurationDirectory() );
                delete( dataDirectory() );
                delete( temporaryDirectory() );
                delete( cacheDirectory() );
                delete( logDirectory() );
            }
        }

        @Override
        public OS os()
        {
            return data.os().get();
        }

        @Override
        public File user()
        {
            return data.user().get();
        }

        @Override
        public File configurationDirectory()
        {
            return data.configuration().get();
        }

        @Override
        public File dataDirectory()
        {
            return data.data().get();
        }

        @Override
        public File temporaryDirectory()
        {
            return data.temporary().get();
        }

        @Override
        public File cacheDirectory()
        {
            return data.cache().get();
        }

        @Override
        public File logDirectory()
        {
            return data.log().get();
        }

        private boolean delete( File file )
        {
            if ( !file.exists() ) {
                return true;
            }

            if ( file.isFile() ) {
                return file.delete();
            } else {
                for ( File childFile : file.listFiles() ) {
                    if ( !delete( childFile ) ) {
                        return false;
                    }
                }

                return file.delete();
            }
        }

        private OS detectOS()
        {
            String osName = System.getProperty( "os.name" ).toLowerCase();
            OS os;
            if ( osName.indexOf( "win" ) != -1 ) {
                os = OS.windows;
            } else if ( osName.indexOf( "mac" ) != -1 ) {
                os = OS.mac;
            } else {
                os = OS.unix;
            }
            return os;
        }

        private void autoCreateDirectories()
        {
            // Create directories
            if ( !configurationDirectory().exists() && !configurationDirectory().mkdirs() ) {
                throw new IllegalStateException( "Could not create configuration directory(" + configurationDirectory() + ")" );
            }
            if ( !dataDirectory().exists() && !dataDirectory().mkdirs() ) {
                throw new IllegalStateException( "Could not create data directory(" + dataDirectory() + ")" );
            }
            if ( !temporaryDirectory().exists() && !temporaryDirectory().mkdirs() ) {
                throw new IllegalStateException( "Could not create temporary directory(" + temporaryDirectory() + ")" );
            }
            if ( !cacheDirectory().exists() && !cacheDirectory().mkdirs() ) {
                throw new IllegalStateException( "Could not create cache directory(" + cacheDirectory() + ")" );
            }
            if ( !logDirectory().exists() && !logDirectory().mkdirs() ) {
                throw new IllegalStateException( "Could not create log directory(" + logDirectory() + ")" );
            }
        }

        private String format( String configuration, Map arguments )
        {
            Pattern paramPattern = Pattern.compile( "\\{(.*?)\\}" );

            Matcher matcher = paramPattern.matcher( configuration );

            StringBuilder buffer = new StringBuilder();
            int lastEnd = 0;
            while ( matcher.find() ) {
                buffer.append( configuration.substring( lastEnd, matcher.start() ) );
                lastEnd = matcher.end();

                String var = matcher.group( 1 );
                Object value = arguments.get( var );

                if ( value != null ) {
                    buffer.append( format( value.toString(), arguments ) );
                } else {
                    throw new IllegalArgumentException( "Illegal file configuration parameter:" + var );
                }
            }
            buffer.append( configuration.substring( lastEnd ) );

            return buffer.toString();
        }

    }

}
