package org.qi4j.entity.ibatis;

import com.ibatis.sqlmap.client.SqlMapClient;
import static com.ibatis.sqlmap.client.SqlMapClientBuilder.buildSqlMapClient;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;

/**
 * @autor Michael Hunger
 * @since 19.05.2008
 */
public class IbatisClient implements Serializable, StateCommitter
{
    private transient SqlMapClient client;
    private final String sqlMapConfigUrl;
    private Properties configProperties;

    public IbatisClient( final String sqlMapConfigURL, final Properties configProperties )
    {
        this.sqlMapConfigUrl = sqlMapConfigURL;
        this.configProperties = configProperties;
    }

    public void checkActive()
    {
        if( client == null )
        {
            throw new EntityStoreException( "IBatis Client for " + sqlMapConfigUrl + " was not activated." );
        }
    }

    public Map<String, Object> executeLoad( final QualifiedIdentity qualifiedIdentity )
    {
        checkActive();
        final String statementId = getStatementId( qualifiedIdentity, "load" );
        try
        {
            return (Map<String, Object>) client.queryForObject( statementId, qualifiedIdentity.getIdentity() );
        }
        catch( SQLException e )
        {
            throw new EntityStoreException( "Error executing Operation " + statementId + " for identity " + qualifiedIdentity, e );
        }
    }

    private String getStatementId( final QualifiedIdentity qualifiedIdentity, final String suffix )
    {
        return qualifiedIdentity.getCompositeType() + "." + suffix;
    }

    public void startTransaction()
    {
        checkActive();

        try
        {
            client.startTransaction();
        }
        catch( SQLException e )
        {
            throw new EntityStoreException( "Error starting transaction", e );
        }
    }

    public int executeUpdate( final String operation, final QualifiedIdentity identity, final Object params )
    {
        checkActive();

        final String statementId = getStatementId( identity, operation );
        try
        {
            return client.update( statementId, params );
        }
        catch( SQLException e )
        {
            throw new EntityStoreException( "Error executing Operation " + statementId + " for identity " + identity + " params " + params, e );
        }
    }

    public void commit()
    {
        checkActive();
        try
        {
            client.commitTransaction();
        }
        catch( SQLException e )
        {
            throw new EntityStoreException( "Error commiting transaction", e );
        }
    }

    public void cancel()
    {
        checkActive();
        try
        {
            client.endTransaction();
        }
        catch( SQLException e )
        {
            throw new EntityStoreException( "Error canceling transaction", e );
        }
    }

    public void passivate()
    {
        client = null;
    }

    public void activate() throws Exception
    {
        final InputStream configInputStream = new URL( sqlMapConfigUrl ).openStream();
        client = buildSqlMapClient( configInputStream, configProperties );
    }

}

