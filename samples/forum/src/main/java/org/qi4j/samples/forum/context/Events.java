package org.qi4j.samples.forum.context;

import org.qi4j.samples.forum.context.signup.Registration;

/**
 * Domain Events that can be triggered by the contexts
 */
public interface Events
{
    void signedup( Registration registration );
}
