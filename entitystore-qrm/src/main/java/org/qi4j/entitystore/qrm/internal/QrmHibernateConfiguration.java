package org.qi4j.entitystore.qrm.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;

/**
 * User: alex
 */
public class QrmHibernateConfiguration
    extends Configuration
{

    private String descriptor;

    public QrmHibernateConfiguration( String descriptor )
    {
        this.descriptor = descriptor;
    }

    protected InputStream getConfigurationInputStream( String resource )
        throws HibernateException
    {
        return new ByteArrayInputStream( descriptor.getBytes() );
    }
}
