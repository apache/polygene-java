package org.qi4j.library.struts2.codebehind;

import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.opensymphony.xwork2.config.entities.ResultTypeConfig;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ClassLoaderUtil;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.servlet.ServletContext;
import org.apache.struts2.config.*;
import org.qi4j.library.struts2.ActionConfiguration;

/**
 * This is inspired by the ClasspathPackageProvider from the struts2-codebehind-plugin.  Most of the code
 * is directly copied from the 2.1.1 version of the plugin but modified to only look for the @Action annotation
 * and to accept interfaces as well as classes.
 */
public class Qi4jCodebehindPackageProvider
    implements PackageProvider
{

    private ActionConfiguration actionConfiguration;

    /**
     * The default page prefix (or "path").
     * Some applications may place pages under "/WEB-INF" as an extreme security precaution.
     */
    protected static final String DEFAULT_PAGE_PREFIX = "struts.configuration.classpath.defaultPagePrefix";

    /**
     * The default page prefix (none).
     */
    private String defaultPagePrefix = "";

    /**
     * The default page extension,  to use in place of ".jsp".
     */
    protected static final String DEFAULT_PAGE_EXTENSION = "struts.configuration.classpath.defaultPageExtension";

    /**
     * The defacto default page extension, usually associated with JavaServer Pages.
     */
    private String defaultPageExtension = ".jsp";

    /**
     * A setting to indicate a custom default parent package,
     * to use in place of "struts-default".
     */
    protected static final String DEFAULT_PARENT_PACKAGE = "struts.configuration.classpath.defaultParentPackage";

    /**
     * Name of the framework's default configuration package,
     * that application configuration packages automatically inherit.
     */
    private String defaultParentPackage = "struts-default";

    /**
     * The default page prefix (or "path").
     * Some applications may place pages under "/WEB-INF" as an extreme security precaution.
     */
    protected static final String FORCE_LOWER_CASE = "struts.configuration.classpath.forceLowerCase";

    /**
     * Whether to use a lowercase letter as the initial letter of an action.
     * If false, actions will retain the initial uppercase letter from the Action class.
     * (<code>view.action</code> (true) versus <code>View.action</code> (false)).
     */
    private boolean forceLowerCase = true;

    /**
     * Default suffix that can be used to indicate POJO "Action" classes.
     */
    private static final String ACTION = "Action";

    /**
     * Helper class to scan class path for server pages.
     */
    private PageLocator pageLocator = new ClasspathPageLocator();

    /**
     * Flag to indicate the packages have been loaded.
     *
     * @see #loadPackages
     * @see #needsReload
     */
    private boolean initialized = false;

    private PackageLoader packageLoader;

    /**
     * Logging instance for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger( Qi4jCodebehindPackageProvider.class );

    /**
     * The XWork Configuration for this application.
     *
     * @see #init
     */
    private Configuration configuration;

    private String actionPackages;

    private ServletContext servletContext;

    /**
     * PageLocator defines a locate method that can be used to discover server pages.
     */
    public static interface PageLocator
    {
        public URL locate( String path );
    }

    /**
     * ClasspathPathLocator searches the classpath for server pages.
     */
    public static class ClasspathPageLocator
        implements PageLocator
    {
        @Override
        public URL locate( String path )
        {
            return ClassLoaderUtil.getResource( path, getClass() );
        }
    }

    @Inject( "actionPackages" )
    public void setActionPackages( String packages )
    {
        this.actionPackages = packages;
    }

    @Inject
    public void setServletContext( ServletContext ctx )
    {
        this.servletContext = ctx;
    }

    @Inject
    public void setActionConfiguration( ActionConfiguration actionConfiguration )
    {
        this.actionConfiguration = actionConfiguration;
    }

    /**
     * Register a default parent package for the actions.
     *
     * @param defaultParentPackage the new defaultParentPackage
     */
    @Inject( value = DEFAULT_PARENT_PACKAGE, required = false )
    public void setDefaultParentPackage( String defaultParentPackage )
    {
        this.defaultParentPackage = defaultParentPackage;
    }

    /**
     * Register a default page extension to use when locating pages.
     *
     * @param defaultPageExtension the new defaultPageExtension
     */
    @Inject( value = DEFAULT_PAGE_EXTENSION, required = false )
    public void setDefaultPageExtension( String defaultPageExtension )
    {
        this.defaultPageExtension = defaultPageExtension;
    }

    /**
     * Reigster a default page prefix to use when locating pages.
     *
     * @param defaultPagePrefix the defaultPagePrefix to set
     */
    @Inject( value = DEFAULT_PAGE_PREFIX, required = false )
    public void setDefaultPagePrefix( String defaultPagePrefix )
    {
        this.defaultPagePrefix = defaultPagePrefix;
    }

    /**
     * Whether to use a lowercase letter as the initial letter of an action.
     *
     * @param force If false, actions will retain the initial uppercase letter from the Action class.
     *              (<code>view.action</code> (true) versus <code>View.action</code> (false)).
     */
    @Inject( value = FORCE_LOWER_CASE, required = false )
    public void setForceLowerCase( String force )
    {
        this.forceLowerCase = "true".equals( force );
    }

    /**
     * Register a PageLocation to use to scan for server pages.
     *
     * @param locator
     */
    public void setPageLocator( PageLocator locator )
    {
        this.pageLocator = locator;
    }

    @Override
    public void init( Configuration configuration )
        throws ConfigurationException
    {
        this.configuration = configuration;
    }

    @Override
    public boolean needsReload()
    {
        return !initialized;
    }

    /**
     * Clears and loads the list of packages registered at construction.
     *
     * @throws ConfigurationException
     */
    @Override
    public void loadPackages()
        throws ConfigurationException
    {
        packageLoader = new PackageLoader();
        String[] names = actionPackages.split( "\\s*[,]\\s*" );
        // Initialize the classloader scanner with the configured packages
        if( names.length > 0 )
        {
            setPageLocator( new ServletContextPageLocator( servletContext ) );
        }
        loadPackages( names );
        initialized = true;
    }

    protected void loadPackages( String[] pkgs )
        throws ConfigurationException
    {
        for( Class cls : actionConfiguration.getClasses() )
        {
            processActionClass( cls, pkgs );
        }

        for( PackageConfig config : packageLoader.createPackageConfigs() )
        {
            configuration.addPackageConfig( config.getName(), config );
        }
    }

    /**
     * Create a default action mapping for a class instance.
     *
     * The namespace annotation is honored, if found, otherwise
     * the Java package is converted into the namespace
     * by changing the dots (".") to slashes ("/").
     *
     * @param cls  Action or POJO instance to process
     * @param pkgs List of packages that were scanned for Actions
     */
    protected void processActionClass( Class<?> cls, String[] pkgs )
    {
        String name = cls.getName();
        String actionPackage = cls.getPackage().getName();
        String actionNamespace = null;
        String actionName = null;

        org.apache.struts2.config.Action actionAnn =
            (org.apache.struts2.config.Action) cls.getAnnotation( org.apache.struts2.config.Action.class );
        if( actionAnn != null )
        {
            actionName = actionAnn.name();
            if( actionAnn.namespace().equals( org.apache.struts2.config.Action.DEFAULT_NAMESPACE ) )
            {
                actionNamespace = "";
            }
            else
            {
                actionNamespace = actionAnn.namespace();
            }
        }
        else
        {
            for( String pkg : pkgs )
            {
                if( name.startsWith( pkg ) )
                {
                    if( LOG.isDebugEnabled() )
                    {
                        LOG.debug( "ClasspathPackageProvider: Processing class " + name );
                    }
                    name = name.substring( pkg.length() + 1 );

                    actionNamespace = "";
                    actionName = name;
                    int pos = name.lastIndexOf( '.' );
                    if( pos > -1 )
                    {
                        actionNamespace = "/" + name.substring( 0, pos ).replace( '.', '/' );
                        actionName = name.substring( pos + 1 );
                    }
                    break;
                }
            }
            // Truncate Action suffix if found
            if( actionName.endsWith( getClassSuffix() ) )
            {
                actionName = actionName.substring( 0, actionName.length() - getClassSuffix().length() );
            }

            // Force initial letter of action to lowercase, if desired
            if( ( forceLowerCase ) && ( actionName.length() > 1 ) )
            {
                int lowerPos = actionName.lastIndexOf( '/' ) + 1;
                StringBuilder sb = new StringBuilder();
                sb.append( actionName.substring( 0, lowerPos ) );
                sb.append( Character.toLowerCase( actionName.charAt( lowerPos ) ) );
                sb.append( actionName.substring( lowerPos + 1 ) );
                actionName = sb.toString();
            }
        }

        PackageConfig.Builder pkgConfig = loadPackageConfig( actionNamespace, actionPackage, cls );

        // In case the package changed due to namespace annotation processing
        if( !actionPackage.equals( pkgConfig.getName() ) )
        {
            actionPackage = pkgConfig.getName();
        }

        Annotation annotation = cls.getAnnotation( ParentPackage.class );
        if( annotation != null )
        {
            String parent = ( (ParentPackage) annotation ).value()[0];
            PackageConfig parentPkg = configuration.getPackageConfig( parent );
            if( parentPkg == null )
            {
                throw new ConfigurationException( "ClasspathPackageProvider: Unable to locate parent package " + parent, annotation );
            }
            pkgConfig.addParent( parentPkg );

            if( !isNotEmpty( pkgConfig.getNamespace() ) && isNotEmpty( parentPkg.getNamespace() ) )
            {
                pkgConfig.namespace( parentPkg.getNamespace() );
            }
        }

        ResultTypeConfig defaultResultType = packageLoader.getDefaultResultType( pkgConfig );
        ActionConfig actionConfig = new ActionConfig.Builder( actionPackage, actionName, cls.getName() )
            .addResultConfigs( new ResultMap<String, ResultConfig>( cls, actionName, defaultResultType ) )
            .build();
        pkgConfig.addActionConfig( actionName, actionConfig );
    }

    protected String getClassSuffix()
    {
        return ACTION;
    }

    /**
     * Finds or creates the package configuration for an Action class.
     *
     * The namespace annotation is honored, if found,
     * and the namespace is checked for a parent configuration.
     *
     * @param actionNamespace The configuration namespace
     * @param actionPackage   The Java package containing our Action classes
     * @param actionClass     The Action class instance
     *
     * @return PackageConfig object for the Action class
     */
    protected PackageConfig.Builder loadPackageConfig( String actionNamespace, String actionPackage, Class actionClass )
    {
        PackageConfig.Builder parent = null;

        // Check for the @Namespace annotation
        if( actionClass != null )
        {
            Namespace ns = (Namespace) actionClass.getAnnotation( Namespace.class );
            if( ns != null )
            {
                parent = loadPackageConfig( actionNamespace, actionPackage, null );
                actionNamespace = ns.value();
                actionPackage = actionClass.getName();

                // See if the namespace has been overridden by the @Action annotation
            }
            else
            {
                org.apache.struts2.config.Action actionAnn =
                    (org.apache.struts2.config.Action) actionClass.getAnnotation( org.apache.struts2.config.Action.class );
                if( actionAnn != null && !actionAnn.DEFAULT_NAMESPACE.equals( actionAnn.namespace() ) )
                {
                    // we pass null as the namespace in case the parent package hasn't been loaded yet
                    parent = loadPackageConfig( null, actionPackage, null );
                    actionPackage = actionClass.getName();
                }
            }
        }

        PackageConfig.Builder pkgConfig = packageLoader.getPackage( actionPackage );
        if( pkgConfig == null )
        {
            pkgConfig = new PackageConfig.Builder( actionPackage );

            pkgConfig.namespace( actionNamespace );
            if( parent == null )
            {
                PackageConfig cfg = configuration.getPackageConfig( defaultParentPackage );
                if( cfg != null )
                {
                    pkgConfig.addParent( cfg );
                }
                else
                {
                    throw new ConfigurationException( "ClasspathPackageProvider: Unable to locate default parent package: " +
                                                      defaultParentPackage );
                }
            }
            packageLoader.registerPackage( pkgConfig );

            // if the parent package was first created by a child, ensure the namespace is correct
        }
        else if( pkgConfig.getNamespace() == null )
        {
            pkgConfig.namespace( actionNamespace );
        }

        if( parent != null )
        {
            packageLoader.registerChildToParent( pkgConfig, parent );
        }

        if( LOG.isDebugEnabled() )
        {
            LOG.debug( "class:" + actionClass + " parent:" + parent + " current:" + ( pkgConfig != null ? pkgConfig.getName() : "" ) );
        }
        return pkgConfig;
    }

    /**
     * Creates ResultConfig objects from result annotations,
     * and if a result isn't found, creates it on the fly.
     */
    class ResultMap<K, V>
        extends HashMap<K, V>
    {
        private Class actionClass;
        private String actionName;
        private ResultTypeConfig defaultResultType;

        public ResultMap( Class actionClass, String actionName, ResultTypeConfig defaultResultType )
        {
            this.actionClass = actionClass;
            this.actionName = actionName;
            this.defaultResultType = defaultResultType;

            // check if any annotations are around
            buildFromAnnotations( actionClass );
        }

        /**
         * Recursively finds annotations from all parent classes and interfaces.
         *
         * @param actionClass
         */
        private void buildFromAnnotations( Class actionClass )
        {
            if( actionClass == null || actionClass.getName().equals( Object.class.getName() ) )
            {
                return;
            }

            //noinspection unchecked
            Results results = (Results) actionClass.getAnnotation( Results.class );
            if( results != null )
            {
                // first check here...
                for( int i = 0; i < results.value().length; i++ )
                {
                    Result result = results.value()[ i ];
                    ResultConfig config = createResultConfig( result );
                    if( !containsKey( (K) config.getName() ) )
                    {
                        put( (K) config.getName(), (V) config );
                    }
                }
            }

            // what about a single Result annotation?
            Result result = (Result) actionClass.getAnnotation( Result.class );
            if( result != null )
            {
                ResultConfig config = createResultConfig( result );
                if( !containsKey( (K) config.getName() ) )
                {
                    put( (K) config.getName(), (V) config );
                }
            }

            buildFromAnnotations( actionClass.getSuperclass() );
            for( Class implementedInterface : actionClass.getInterfaces() )
            {
                buildFromAnnotations( implementedInterface );
            }
        }

        /**
         * Extracts result name and value and calls {@link #createResultConfig}.
         *
         * @param result Result annotation reference representing result type to create
         *
         * @return New or cached ResultConfig object for result
         */
        protected ResultConfig createResultConfig( Result result )
        {
            Class<? extends Object> cls = result.type();
            if( cls == NullResult.class )
            {
                cls = null;
            }
            return createResultConfig( result.name(), cls, result.value(), createParameterMap( result.params() ) );
        }

        protected Map<String, String> createParameterMap( String[] parms )
        {
            Map<String, String> map = new HashMap<String, String>();
            int subtract = parms.length % 2;
            if( subtract != 0 )
            {
                LOG.warn( "Odd number of result parameters key/values specified.  The final one will be ignored." );
            }
            for( int i = 0; i < parms.length - subtract; i++ )
            {
                String key = parms[ i++ ];
                String value = parms[ i ];
                map.put( key, value );
                if( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Adding parmeter[" + key + ":" + value + "] to result." );
                }
            }
            return map;
        }

        /**
         * Creates a default ResultConfig,
         * using either the resultClass or the default ResultType for configuration package
         * associated this ResultMap class.
         *
         * @param key         The result type name
         * @param resultClass The class for the result type
         * @param location    Path to the resource represented by this type
         *
         * @return A ResultConfig for key mapped to location
         */
        private ResultConfig createResultConfig( Object key, Class<? extends Object> resultClass,
                                                 String location,
                                                 Map<? extends Object, ? extends Object> configParams
        )
        {
            if( resultClass == null )
            {
                configParams = defaultResultType.getParams();
                String className = defaultResultType.getClassName();
                try
                {
                    resultClass = ClassLoaderUtil.loadClass( className, getClass() );
                }
                catch( ClassNotFoundException ex )
                {
                    throw new ConfigurationException( "ClasspathPackageProvider: Unable to locate result class " + className, actionClass );
                }
            }

            String defaultParam;
            try
            {
                defaultParam = (String) resultClass.getField( "DEFAULT_PARAM" ).get( null );
            }
            catch( Exception e )
            {
                // not sure why this happened, but let's just use a sensible choice
                defaultParam = "location";
            }

            HashMap params = new HashMap();
            if( configParams != null )
            {
                params.putAll( configParams );
            }

            params.put( defaultParam, location );
            return new ResultConfig.Builder( (String) key, resultClass.getName() ).addParams( params ).build();
        }
    }

    /**
     * Search classpath for a page.
     */
    private final class ServletContextPageLocator
        implements PageLocator
    {
        private final ServletContext context;
        private ClasspathPageLocator classpathPageLocator = new ClasspathPageLocator();

        private ServletContextPageLocator( ServletContext context )
        {
            this.context = context;
        }

        @Override
        public URL locate( String path )
        {
            URL url = null;
            try
            {
                url = context.getResource( path );
                if( url == null )
                {
                    url = classpathPageLocator.locate( path );
                }
            }
            catch( MalformedURLException e )
            {
                if( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Unable to resolve path " + path + " against the servlet context" );
                }
            }
            return url;
        }
    }

    private static class PackageLoader
    {

        /**
         * The package configurations for scanned Actions.
         */
        private Map<String, PackageConfig.Builder> packageConfigBuilders = new HashMap<String, PackageConfig.Builder>();

        private Map<PackageConfig.Builder, PackageConfig.Builder> childToParent = new HashMap<PackageConfig.Builder, PackageConfig.Builder>();

        public PackageConfig.Builder getPackage( String name )
        {
            return packageConfigBuilders.get( name );
        }

        public void registerChildToParent( PackageConfig.Builder child, PackageConfig.Builder parent )
        {
            childToParent.put( child, parent );
        }

        public void registerPackage( PackageConfig.Builder builder )
        {
            packageConfigBuilders.put( builder.getName(), builder );
        }

        public Collection<PackageConfig> createPackageConfigs()
        {
            Map<String, PackageConfig> configs = new HashMap<String, PackageConfig>();

            Set<PackageConfig.Builder> builders;
            while( ( builders = findPackagesWithNoParents() ).size() > 0 )
            {
                for( PackageConfig.Builder parent : builders )
                {
                    PackageConfig config = parent.build();
                    configs.put( config.getName(), config );
                    packageConfigBuilders.remove( config.getName() );

                    for( Iterator<Map.Entry<PackageConfig.Builder, PackageConfig.Builder>> i = childToParent.entrySet()
                        .iterator(); i.hasNext(); )
                    {
                        Map.Entry<PackageConfig.Builder, PackageConfig.Builder> entry = i.next();
                        if( entry.getValue() == parent )
                        {
                            entry.getKey().addParent( config );
                            i.remove();
                        }
                    }
                }
            }
            return configs.values();
        }

        Set<PackageConfig.Builder> findPackagesWithNoParents()
        {
            Set<PackageConfig.Builder> builders = new HashSet<PackageConfig.Builder>();
            for( PackageConfig.Builder child : packageConfigBuilders.values() )
            {
                if( !childToParent.containsKey( child ) )
                {
                    builders.add( child );
                }
            }
            return builders;
        }

        public ResultTypeConfig getDefaultResultType( PackageConfig.Builder pkgConfig )
        {
            PackageConfig.Builder parent;
            PackageConfig.Builder current = pkgConfig;

            while( ( parent = childToParent.get( current ) ) != null )
            {
                current = parent;
            }
            return current.getResultType( current.getFullDefaultResultType() );
        }
    }

    public static boolean isNotEmpty( String text )
    {
        return ( text != null ) && !"".equals( text );
    }
}
