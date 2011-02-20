package org.qi4j.tests.qi205;

import java.util.Iterator;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins( AuthorRepositoryService.DefaultMixin.class )
public interface AuthorRepositoryService
    extends AuthorRepository, ServiceComposite
{

    public class DefaultMixin
        implements AuthorRepository
    {
        @Structure
        UnitOfWorkFactory uowf;
        @Structure
        QueryBuilderFactory qbf;

        public Author findBySurname( String surname )
        {
            Query<Author> qry = findBySurnameQuery( surname );

            return qry.find();
        }

        public Iterator<Author> findAllBySurname( String surname )
        {
            Query<Author> qry = findBySurnameQuery( surname );

            return qry.iterator();
        }

        private Query<Author> findBySurnameQuery( String surname )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();

            QueryBuilder<Author> qb = qbf.newQueryBuilder( Author.class );

            Author template = templateFor( Author.class );

            qb.where( eq( template.surname(), surname ) );

            Query<Author> qry = qb.newQuery( uow );
            return qry;
        }
    }
}
