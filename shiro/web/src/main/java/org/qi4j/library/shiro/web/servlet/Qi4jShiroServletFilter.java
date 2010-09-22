/*
 * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.qi4j.library.shiro.web.servlet;

import java.util.Iterator;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.config.IniFilterChainResolverFactory;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.library.servlet.Qi4jServletSupport;
import org.qi4j.library.shiro.web.filter.authc.X509AuthenticationFilter;

public class Qi4jShiroServletFilter
        extends AbstractShiroFilter
{

    public static final String REALM_LAYER_PARAM = "realm-layer";
    public static final String REALM_MODULE_PARAM = "realm-module";
    public static final String FILTER_CHAINS_PARAM = "filterChains";

    @Override
    public void init()
            throws Exception
    {
        Application application = Qi4jServletSupport.application( filterConfig.getServletContext() );
        applySecurityManager( application );
        applyFilterChain( application );
    }

    private void applySecurityManager( Application application )
    {
        String layerName = getFilterConfig().getInitParameter( REALM_LAYER_PARAM );
        NullArgumentException.validateNotEmpty( REALM_LAYER_PARAM, layerName );

        String moduleName = getFilterConfig().getInitParameter( REALM_MODULE_PARAM );
        NullArgumentException.validateNotEmpty( REALM_MODULE_PARAM, moduleName );

        Module module = application.findModule( layerName, moduleName );
        Realm realm = module.objectBuilderFactory().newObject( Realm.class );
        setSecurityManager( new DefaultWebSecurityManager( realm ) );
    }

    private void applyFilterChain( Application application )
            throws JSONException
    {

        String filterChainsConfig = getFilterConfig().getInitParameter( FILTER_CHAINS_PARAM );
        NullArgumentException.validateNotEmpty( FILTER_CHAINS_PARAM, filterChainsConfig );

        JSONObject filterChainsJson = new JSONObject( filterChainsConfig );

        Ini ini = new Ini();
        Section urls = ini.addSection( "urls" );
        Iterator it = filterChainsJson.keys();
        while ( it.hasNext() ) {
            String eachUrl = ( String ) it.next();
            urls.put( eachUrl, ( String ) filterChainsJson.get( eachUrl ) );
        }

        Section filters = ini.addSection( "filters" );
        filters.put( "authcBasic.applicationName", application.name() );
        filters.put( "authcX509", X509AuthenticationFilter.class.getName() );


        IniFilterChainResolverFactory filterChainResolverFactory = new IniFilterChainResolverFactory( ini );
        filterChainResolverFactory.setFilterConfig( getFilterConfig() );
        setFilterChainResolver( filterChainResolverFactory.getInstance() );
    }

}
