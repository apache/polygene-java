/*  Copyright 2009 Alex Shneyderman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.qrm.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.entitystore.qrm.QrmEntityStoreDescriptor;
import org.qi4j.entitystore.qrm.QrmMapper;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.helpers.DefaultEntityState;

import java.util.*;

@Mixins( { QrmMapperService.QrmMapperServiceMixin.class } )
public interface QrmMapperService
    extends QrmMapper, ServiceComposite
{

    class QrmMapperServiceMixin
        implements QrmMapper
    {

        private final static Log LOG = LogFactory.getLog( QrmMapperService.class );

        private SessionFactory sessoinFactory;

        private
        @Structure
        Application app;

        private Map<Class, QrmMapping> mappings = new HashMap<Class, QrmMapping>();

        private Map<Class, Long> classId = new HashMap<Class, Long>();

        private QrmEntityStoreDescriptor qrmCfg;

        public void bootstrap( final QrmEntityStoreDescriptor cfg )
        {
            gatherMetaInfo( cfg );

            qrmCfg = cfg;

            configureSessionFactory( cfg );
        }

        public Class findMappedMixin( EntityDescriptor eDesc )
        {
            for( Class mixinClass : eDesc.mixinTypes() )
            {
                if( mappings.get( mixinClass ) != null )
                {
                    return mixinClass;
                }
            }

            return null;
        }

        public String fetchNextId( Class clazz )
        {
            if( mappings.get( clazz ) != null )
            {
                classId.put( clazz, ( classId.get( clazz ) + 1L ) );

                return "" + classId.get( clazz );
            }

            return null;
        }

        public EntityDescriptor fetchDescriptor( Class mappedClazz )
        {
            QrmMapping qrmMapping = mappings.get( mappedClazz );

            if( qrmMapping == null )
            {
                return null;
            }

            return qrmMapping.getEntityDescriptor();
        }

        public EntityState get( DefaultEntityStoreUnitOfWork unitOfWork, Class mappedClazz, EntityReference identity )
        {
            EntityDescriptor desc = fetchDescriptor( mappedClazz );

            Session session = sessoinFactory.getCurrentSession();

            session.beginTransaction();

            Map dbState = (Map) session.get( mappedClazz.getName(), identity.identity() );

            session.close();

            if( dbState == null )
            {
                throw new EntityNotFoundException( identity );
            }

            Map<QualifiedName, Object> propState = new HashMap<QualifiedName, Object>();

            for( PropertyDescriptor pd : desc.state().properties() )
            {
                QualifiedName qualifiedName = pd.qualifiedName();

                propState.put( qualifiedName, dbState.get( qualifiedName.name() ) );
            }

            String version = "" + dbState.get( "version" );

            long lastModified = dbState.get( "updatedOn" ) == null ? 0L : ( (Date) dbState.get( "updatedOn" ) ).getTime();

            return new DefaultEntityState( unitOfWork,
                                           version,
                                           lastModified,
                                           identity,
                                           EntityStatus.LOADED,
                                           desc,
                                           propState,
                                           null,
                                           null );
        }

        public boolean newEntity( Class mappedClazz, DefaultEntityState state, String version, long lastModified )
        {
            EntityDescriptor desc = state.entityDescriptor();

            EntityReference identity = state.identity();

            QrmMapping mapping = mappings.get( mappedClazz );

            Map dbState = new HashMap();

            for( Map.Entry<QualifiedName, Object> entry : state.properties().entrySet() )
            {
                dbState.put( entry.getKey().name(), entry.getValue() );
            }

            dbState.put( mapping.identity().name(), state.identity().identity() );

            dbState.put( mapping.createdOn().name(), new Date(lastModified) );

            dbState.put( mapping.updatedOn().name(), new Date(lastModified) );

            Session session = sessoinFactory.getCurrentSession();

            session.beginTransaction();

            session.save( mappedClazz.getName(), dbState );

            session.getTransaction().commit();

            return true;
        }

        public boolean delEntity( Class mappedClazz, DefaultEntityState state, String version )
        {
            EntityDescriptor desc = state.entityDescriptor();

            EntityReference identity = state.identity();

            QrmMapping mapping = mappings.get( mappedClazz );

            String entityName = mappedClazz.getName();

            Session session = sessoinFactory.getCurrentSession();

            session.beginTransaction();

            Map dbState = (Map) session.get( entityName, identity.identity() );

            if( dbState != null )
            {
                session.delete( entityName, dbState );
            }

            session.getTransaction().commit();

            if( dbState == null )
            {
                throw new EntityNotFoundException( identity );
            }

            return true;
        }

        public boolean updEntity( Class mappedClazz, DefaultEntityState state, String version, long lastModified )
        {
            EntityReference identity = state.identity();

            QrmMapping mapping = mappings.get( mappedClazz );

            String entityName = mappedClazz.getName();

            Session session = sessoinFactory.getCurrentSession();

            session.beginTransaction();

            Map dbState = (Map) session.get( entityName, identity.identity() );

            if( dbState == null )
            {
                throw new EntityNotFoundException( identity );
            }

            for( Map.Entry<QualifiedName, Object> entry : state.properties().entrySet() )
            {
                dbState.put( entry.getKey().name(), entry.getValue() );
            }

            dbState.put( mapping.identity().name(), state.identity().identity() );

            dbState.put( mapping.updatedOn().name(), new Date(lastModified) );

            session.save( entityName, dbState );

            session.getTransaction().commit();

            return true;
        }

        // HELPERS.

        private void gatherMetaInfo( QrmEntityStoreDescriptor cfg )
        {
            final List<Class> types = cfg.types();

            app.descriptor().accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
            {
                @Override
                public boolean visitEnter( Object visited ) throws RuntimeException
                {
                    if (visited instanceof ObjectDescriptor )
                    {
                        if (visited instanceof EntityDescriptor)
                        {
                            EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
                            for( Class mixinClazz : entityDescriptor.mixinTypes() )
                            {
                                if( types.contains( mixinClazz ) )
                                {
                                    mappings.put( mixinClazz, createMapping( entityDescriptor, mixinClazz ) );
                                    classId.put( mixinClazz, new Long( 0 ) );
                                }
                            }
                        }

                        return false;
                    }

                    return true;
                }
            } );
        }

        private QrmMapping createMapping( EntityDescriptor entityDescriptor, Class mixinClazz )
        {
            QrmMapping result = new QrmMapping( entityDescriptor );

            for( PropertyDescriptor pPersistent : entityDescriptor.state().properties() )
            {
                String name = pPersistent.qualifiedName().name();

                String hibType = ((Class) pPersistent.type()).getName();

                if( "identity".equals( name ) )
                {
                    result.addIdentity( name, MapperUtils.idColumnName( mixinClazz, qrmCfg, mappings ), "string" );
                }
                else
                {
                    result.addProperty( name, hibType, true );
                }
            }

            return result;
        }

        private void configureSessionFactory( QrmEntityStoreDescriptor cfg )
        {
            Configuration hibCfg = new QrmHibernateConfiguration( createHibernateDescriptor( cfg.props() ) ).configure();

            addMappings( hibCfg );

            sessoinFactory = hibCfg.buildSessionFactory();
        }

        private void addMappings( Configuration hibCfg )
        {
            for( Map.Entry<Class, QrmMapping> entry : mappings.entrySet() )
            {
                hibCfg.addXML( createHibMappingXml( entry.getKey(), entry.getValue() ) );
            }
        }

        private String createHibMappingXml( Class clazz, QrmMapping mapping )
        {
            StringBuilder sb = new StringBuilder( "" );

            QrmProperty identity = mapping.identity();

            sb.append( "<?xml version=\"1.0\"?>\n" )
                .append( "<!DOCTYPE hibernate-mapping PUBLIC\n" )
                .append( "        \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" )
                .append( "        \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" )
                .append( "<hibernate-mapping>" )
                .append( "  <class \n" )
                .append( "      entity-name='" + MapperUtils.entityName( clazz, qrmCfg, mappings ) + "'\n" )
                .append( "      table='" + MapperUtils.tableName( clazz, qrmCfg, mappings ) + "'>\n" )
                .append( "    <id name='" + identity.name() + "' \n" )
                .append( "        column='" + identity.column() + "' \n" )
                .append( "        type='" + identity.hibernateType() + "'>\n" )
                .append( "      <generator class='assigned' />\n" )
                .append( "    </id>\n" )
                .append( "    <version name='version' type='long' column='version' />\n" )
                .append( "    <property name='updatedOn' column='updated_on' type='timestamp' not-null='false' />\n" )
                .append( "    <property name='createdOn' column='created_on' type='timestamp' not-null='false' />\n" );

            for( QrmProperty prop : mapping.properties() )
            {
                if( prop.isIdentity() ||
                    prop.name().equals( "updatedOn" ) ||
                    prop.name().equals( "createdOn" ) )
                {
                    continue;
                }

                sb.append(
                    "     <property name='" + prop.name() + "' \n" +
                    "               column='" + prop.column() + "' \n" +
                    "               type='" + prop.hibernateType() + "' \n" +
                    "               not-null='" + prop.isNullable() + "' />\n"
                );
            }

            sb.append( "  </class>\n" )
                .append( "</hibernate-mapping>\n" );

            String result = sb.toString();

            if( LOG.isDebugEnabled() )
            {
                LOG.debug( "Hibenrate mapping for " + clazz.getName() );
                LOG.debug( result );
            }

            System.err.println( "Hibenrate mapping for " + clazz.getName() );
            System.err.println( result );

            return result;
        }

        private String createHibernateDescriptor( Properties props )
        {
            StringBuilder sb = new StringBuilder( "" );

            sb.append( "<?xml version='1.0' encoding='utf-8'?>\n" +
                       "<!DOCTYPE hibernate-configuration PUBLIC\n" +
                       "        \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\"\n" +
                       "        \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">\n" +
                       "<hibernate-configuration>\n" +
                       "    <session-factory>\n" );

            for( Map.Entry<Object, Object> entry : props.entrySet() )
            {
                sb.append( "        <property name='" ).append( entry.getKey() ).append( "'>" )
                    .append( entry.getValue() )
                    .append( "</property>\n" );
            }

            sb.append( "    </session-factory>\n" )
                .append( "</hibernate-configuration>" );

            String result = sb.toString();

            if( LOG.isDebugEnabled() )
            {
                LOG.debug( "Hibenrate descriptor:" );
                LOG.debug( result );
            }

            System.err.println( "Hibenrate descriptor:" );
            System.err.println( result );

            return result;
        }
    }
}
