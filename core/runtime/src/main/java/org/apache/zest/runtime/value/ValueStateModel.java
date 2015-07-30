/*
 * Copyright (c) 2008-2011, Rickard Öberg.
 * Copyright (c) 2012, Kent Sølvsten.
 * Copyright (c) 2014-2015, Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.apache.zest.runtime.value;

import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.association.AssociationStateDescriptor;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;
import org.apache.zest.runtime.association.AssociationModel;
import org.apache.zest.runtime.association.AssociationsModel;
import org.apache.zest.runtime.association.ManyAssociationModel;
import org.apache.zest.runtime.association.ManyAssociationsModel;
import org.apache.zest.runtime.association.NamedAssociationModel;
import org.apache.zest.runtime.association.NamedAssociationsModel;
import org.apache.zest.runtime.composite.StateModel;
import org.apache.zest.runtime.property.PropertiesModel;

/**
 * Model for ValueComposite state.
 */
public final class ValueStateModel
    extends StateModel
    implements AssociationStateDescriptor
{
    private final AssociationsModel associationsModel;
    private final ManyAssociationsModel manyAssociationsModel;
    private final NamedAssociationsModel namedAssociationsModel;

    public ValueStateModel( PropertiesModel propertiesModel,
                            AssociationsModel associationsModel,
                            ManyAssociationsModel manyAssociationsModel,
                            NamedAssociationsModel namedAssociationsModel
    )
    {
        super( propertiesModel );
        this.associationsModel = associationsModel;
        this.manyAssociationsModel = manyAssociationsModel;
        this.namedAssociationsModel = namedAssociationsModel;
    }

    @Override
    public AssociationDescriptor getAssociationByName( String name )
    {
        return associationsModel.getAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
    {
        return associationsModel.getAssociationByQualifiedName( name );
    }

    @Override
    public AssociationDescriptor getManyAssociationByName( String name )
    {
        return manyAssociationsModel.getManyAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getManyAssociationByQualifiedName( QualifiedName name )
    {
        return manyAssociationsModel.getManyAssociationByQualifiedName( name );
    }

    @Override
    public AssociationDescriptor getNamedAssociationByName( String name )
    {
        return namedAssociationsModel.getNamedAssociationByName( name );
    }

    @Override
    public AssociationDescriptor getNamedAssociationByQualifiedName( QualifiedName name )
    {
        return namedAssociationsModel.getNamedAssociationByQualifiedName( name );
    }

    @Override
    public Iterable<AssociationModel> associations()
    {
        return associationsModel.associations();
    }

    @Override
    public Iterable<ManyAssociationModel> manyAssociations()
    {
        return manyAssociationsModel.manyAssociations();
    }

    @Override
    public Iterable<NamedAssociationModel> namedAssociations()
    {
        return namedAssociationsModel.namedAssociations();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( ( (VisitableHierarchy<Object, Object>) propertiesModel ).accept( visitor ) )
            {
                if( ( (VisitableHierarchy<AssociationsModel, AssociationModel>) associationsModel ).accept( visitor ) )
                {
                    if( ( (VisitableHierarchy<ManyAssociationsModel, ManyAssociationModel>) manyAssociationsModel ).accept( visitor ) )
                    {
                        ( (VisitableHierarchy<NamedAssociationsModel, NamedAssociationModel>) namedAssociationsModel ).accept( visitor );
                    }
                }
            }
        }
        return visitor.visitLeave( this );
    }
}
