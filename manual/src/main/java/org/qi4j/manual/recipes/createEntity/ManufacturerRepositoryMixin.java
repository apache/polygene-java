package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import java.util.Iterator;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

// START SNIPPET: repo
public class ManufacturerRepositoryMixin
        implements ManufacturerRepository
{
    @Structure
    private UnitOfWorkFactory uowf;

    @Structure
    private Module module;

    public Manufacturer findByIdentity( String identity )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        return uow.get(Manufacturer.class, identity);
    }

    public Manufacturer findByName( String name )
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        QueryBuilder<Manufacturer> builder =
                module.newQueryBuilder( Manufacturer.class );

        Manufacturer template = templateFor( Manufacturer.class );
        builder.where( eq( template.name(), name ) );

        Query<Manufacturer> query = uow.newQuery( builder);
        return query.find();
    }
}

// END SNIPPET: repo