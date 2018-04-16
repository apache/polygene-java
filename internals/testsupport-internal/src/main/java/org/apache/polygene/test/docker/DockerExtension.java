package org.apache.polygene.test.docker;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DockerExtension
    implements BeforeAllCallback, AfterAllCallback
{
    @Override
    public void afterAll( ExtensionContext context )
        throws Exception
    {

    }

    @Override
    public void beforeAll( ExtensionContext context )
        throws Exception
    {

    }
}
