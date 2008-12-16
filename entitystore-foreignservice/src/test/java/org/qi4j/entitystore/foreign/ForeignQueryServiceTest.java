/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.foreign;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.Name;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceInstanceFactory;
import org.qi4j.api.service.ServiceInstanceFactoryException;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.library.beans.support.JavabeanSupport;
import org.qi4j.api.property.Property;
import org.qi4j.spi.query.named.QueryMethod;
import org.qi4j.spi.query.named.QueryResult;

public class ForeignQueryServiceTest extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
//        module.addServices( ForeignProxyEntityStoreService.class ).providedBy( HabbaServiceProvider.class );
    }

    @Test
    public void doNothingTest()
    {

    }

    public void whenCallingFindAllHabbaThenCorrectResultReturned()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            Query<Habba> query = unitOfWork.queryBuilderFactory().newNamedQuery( "findAll", Habba.class );
        } catch( Exception e )
        {
            unitOfWork.discard();
        }
    }

    public class HabbaServiceProvider
        implements ServiceInstanceFactory
    {

        @Structure CompositeBuilderFactory cbf;

        public Object newInstance( ServiceDescriptor serviceDescriptor ) throws ServiceInstanceFactoryException
        {
            CompositeBuilder<ForeignProxyEntityStoreService> builder = cbf.newCompositeBuilder( ForeignProxyEntityStoreService.class );
            builder.use( new HabbaServiceImpl() );
            builder.use( HabbaServiceQueryMap.class );
            return builder.newInstance();
        }

        public void releaseInstance( Object instance ) throws ServiceInstanceFactoryException
        {
        }
    }

    public interface QiHabbaComposite extends JavabeanSupport, Composite
    {
        Property<String> name();
    }

    public interface HabbaServiceQueryMap extends HabbaService
    {
        @QueryMethod
        @QueryResult( QiHabbaComposite.class )
        Habba findHabbaByName( @Name( "name" ) String name );

        @QueryMethod( "findAll")
        @QueryResult( QiHabbaComposite.class )
        List<Habba> findAllHabbas();

        @QueryMethod
        @QueryResult( QiHabbaComposite.class )
        List<Habba> findHabbasByFilter( @Name( "filter" ) String filter );

        @QueryMethod
        @QueryResult( QiHabbaComposite.class )
        Habba[] findHabbaByDates( @Name( "start" ) Date start, @Name("end" ) Date end );
    }

    public interface HabbaService
    {
        Habba findHabbaByName( String name );

        List<Habba> findAllHabbas();

        List<Habba> findHabbasByFilter( String filter );

        Habba[] findHabbaByDates( Date start, Date end );

        String someOtherMethod();
    }

    public class Habba
    {
        private String name;

        public Habba( String name )
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    public class HabbaServiceImpl
        implements HabbaService
    {

        public Habba findHabbaByName( String name )
        {
            return new Habba( "Habba:" + name );
        }

        public List<Habba> findAllHabbas()
        {
            ArrayList<Habba> result = new ArrayList<Habba>();
            for( int i = 0; i < 30; i++ )
            {
                result.add( new Habba( "Habba" + i ) );
            }
            return result;
        }

        public List<Habba> findHabbasByFilter( String filter )
        {
            ArrayList<Habba> result = new ArrayList<Habba>();
            for( int i = 0; i < 30; i++ )
            {
                result.add( new Habba( "Habba" + filter + "(" + i + ")" ) );
            }
            return result;
        }

        public Habba[] findHabbaByDates( Date start, Date end )
        {
            SimpleDateFormat sdf = new SimpleDateFormat( "YYYY-MM-dd" );
            String startDate = sdf.format( start );
            String endDate = sdf.format( end );
            ArrayList<Habba> result = new ArrayList<Habba>();
            for( int i = 0; i < 30; i++ )
            {
                result.add( new Habba( "Habba." + startDate + ":" + endDate + "->" + i ) );
            }
            Habba[] array = new Habba[result.size()];
            return result.toArray( array );
        }

        public String someOtherMethod()
        {
            return "Something";
        }
    }
}