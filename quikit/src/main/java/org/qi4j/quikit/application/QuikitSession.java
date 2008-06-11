/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.quikit.application;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;
import org.qi4j.composite.CompositeBuilderFactory;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.structure.Module;

/**
 * @author edward.yakop@gmail.com
 * @since 0.2.0
 */
public class QuikitSession extends WebSession
{
    private static final long serialVersionUID = 1L;

    private UnitOfWork currentUnitOfWork;

    public QuikitSession(
        @Uses Request request,
        @Structure UnitOfWorkFactory aUOWF )
    {
        super( request );

        // TODO: Investigate use cases whether this is supposed to be done this way
        // Sets the initial unit of work
        currentUnitOfWork = aUOWF.newUnitOfWork();
    }

    /**
     * Get the session for the calling thread.
     *
     * @return Session for calling thread
     */
    public static QuikitSession get()
    {
        return (QuikitSession) WebSession.get();
    }

    /**
     * Sets the current unit of work to manage during attach and detach.
     *
     * @param aUnitOfWork The unit of work. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@code aUnitOfWork} is {@code null}.
     * @since 0.2.0
     */
    public final void setCurrentUnitOfWork( UnitOfWork aUnitOfWork )
        throws IllegalArgumentException
    {
        validateNotNull( "aUnitOfWork", aUnitOfWork );
        currentUnitOfWork = aUnitOfWork;
    }

    @Override
    protected final void attach()
    {
        currentUnitOfWork.resume();
        super.attach();
    }

    @Override
    protected final void detach()
    {
        currentUnitOfWork.pause();
        super.detach();
    }

    /* Serialization work around */

    // TODO: Remove this method once module is serializable

    @Deprecated
    public final Module getModule()
    {
        QuikItApplication quikitApplication = QuikItApplication.get();
        return quikitApplication.getModule();
    }

    // TODO: Remove this method once qi4jspi is serializable
    @Deprecated
    public final Qi4jSPI getQi4jSpi()
    {
        QuikItApplication quikitApplication = QuikItApplication.get();
        return quikitApplication.getQi4jSPI();
    }

    // TODO: Remove this method once unit of work factory is serializable
    @Deprecated
    public final UnitOfWorkFactory getUnitOfWorkFactory()
    {
        QuikItApplication quikitApplication = QuikItApplication.get();
        return quikitApplication.getUnitOfWorkFactory();
    }

    // TODO: Remove this method once object builder factory is serializable
    @Deprecated
    public final ObjectBuilderFactory getObjectBuilderFactory()
    {
        QuikItApplication quikItApplication = QuikItApplication.get();
        return quikItApplication.getObjectBuilderFactory();
    }

    // TODO: Remove this method once composite builder factory is serializable
    @Deprecated
    public final CompositeBuilderFactory getCompositeBuilderFactory()
    {
        QuikItApplication quikItApplication = QuikItApplication.get();
        return quikItApplication.getCompositeBuilderFactory();
    }

}
