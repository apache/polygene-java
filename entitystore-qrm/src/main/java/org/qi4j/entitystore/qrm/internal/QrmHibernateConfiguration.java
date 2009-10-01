package org.qi4j.entitystore.qrm.internal;

import org.hibernate.cfg.Configuration;
import org.hibernate.HibernateException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * User: alex
 */
public class QrmHibernateConfiguration extends Configuration
{

    private String descriptor;

    public QrmHibernateConfiguration( String descriptor )
    {
        this.descriptor = descriptor;
    }

    protected InputStream getConfigurationInputStream( String resource ) throws HibernateException
    {
        return new ByteArrayInputStream( descriptor.getBytes() );
    }



}
