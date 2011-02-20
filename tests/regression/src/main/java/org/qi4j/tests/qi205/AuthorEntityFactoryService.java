package org.qi4j.tests.qi205;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

@Mixins( AuthorEntityFactoryService.DefaultMixin.class )
public interface AuthorEntityFactoryService
    extends AuthorFactory, ServiceComposite
{
    class DefaultMixin
        implements AuthorFactory
    {
        @Structure
        UnitOfWorkFactory uowf;

        public Author createAuthor( String forename, String surename )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            EntityBuilder<Author> builder = uow.newEntityBuilder( Author.class );

            Author prototype = builder.instance();

            prototype.forename().set( forename );
            prototype.surname().set( surename );

            return builder.newInstance();
        }
    }
}
