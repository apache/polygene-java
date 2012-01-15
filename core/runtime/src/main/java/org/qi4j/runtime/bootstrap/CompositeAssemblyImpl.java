package org.qi4j.runtime.bootstrap;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Fields;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.functional.*;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.property.PropertyModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static java.util.Arrays.asList;
import static org.qi4j.api.util.Annotations.*;
import static org.qi4j.api.util.Classes.isAssignableFrom;
import static org.qi4j.functional.Iterables.*;
import static org.qi4j.functional.Specifications.*;

/**
 * TODO
 */
public abstract class CompositeAssemblyImpl
{
    protected boolean immutable;
    protected Class<?> compositeType;
    protected PropertiesModel propertiesModel;
    protected StateModel stateModel;
    protected MixinsModel mixinsModel;
    protected CompositeMethodsModel compositeMethodsModel;
    protected AssemblyHelper helper;
    protected StateDeclarations stateDeclarations;

    protected Set<String> registeredStateNames = new HashSet<String>();

    protected void addAnnotationsMetaInfo( Class<?> type, MetaInfo compositeMetaInfo )
    {
        Class[] declaredInterfaces = type.getInterfaces();
        for( int i = declaredInterfaces.length - 1; i >= 0; i-- )
        {
            addAnnotationsMetaInfo( declaredInterfaces[ i ], compositeMetaInfo );
        }
        compositeMetaInfo.withAnnotations( type );
    }

    protected void implementMixinType( Class<?> mixinType,
                                    Iterable<Class<? extends Constraint<?, ?>>> constraintClasses, Iterable<Class<?>> concernClasses, Iterable<Class<?>> sideEffectClasses, Iterable<Class<?>> mixinClasses)
    {
        Set<Class<?>> thisDependencies = new HashSet<Class<?>>();
        for( Method method : mixinType.getMethods() )
        {
            if( !compositeMethodsModel.isImplemented( method ) &&
                    !Proxy.class.equals( method.getDeclaringClass().getSuperclass() ) &&
                    !Proxy.class.equals( method.getDeclaringClass() ) &&
                    !Modifier.isStatic( method.getModifiers() ))
            {
                MixinModel mixinModel = implementMethod( method, mixinClasses);
                ConcernsModel concernsModel = concernsFor( method, mixinModel.mixinClass(), Iterables.<Class<?>, Iterable<Class<?>>>flatten( concernDeclarations( mixinModel.mixinClass() ), concernClasses ));
                SideEffectsModel sideEffectsModel = sideEffectsFor( method, mixinModel.mixinClass(), Iterables.<Class<?>, Iterable<Class<?>>>flatten( sideEffectDeclarations( mixinModel.mixinClass() ), sideEffectClasses ) );
                method.setAccessible( true );

                ConstraintsModel constraints = constraintsFor( method, Iterables.<Class<? extends Constraint<?,?>>, Iterable<Class<? extends Constraint<?, ?>>>>flatten( constraintDeclarations( mixinModel.mixinClass() ), constraintClasses ) );

                CompositeMethodModel methodComposite = new CompositeMethodModel( method,
                        constraints,
                        concernsModel,
                        sideEffectsModel,
                        mixinsModel );

                // Implement @This references
                Iterable<Class<?>> map = map( new DependencyModel.InjectionTypeFunction(), filter( new DependencyModel.ScopeSpecification( This.class ), methodComposite.dependencies() ) );
                Iterable<Class<?>> map1 = map( new DependencyModel.InjectionTypeFunction(), filter( new DependencyModel.ScopeSpecification( This.class ), mixinModel.dependencies() ) );
                Iterable<Class<?>> filter = filter( not( in( Activatable.class, Initializable.class, Lifecycle.class, InvocationHandler.class ) ), map( Classes.RAW_CLASS, Classes.INTERFACES_OF.map( mixinModel.mixinClass() ) ) );
                Iterables.addAll( thisDependencies, (Iterable<? extends Class<?>>) flatten( map,
                        map1,
                        filter ) );

                compositeMethodsModel.addMethod( methodComposite );
            }
        }

        // Add type to set of mixin types
        mixinsModel.addMixinType( mixinType );

        // Implement all @This dependencies that were found
        for( Class<?> thisDependency : thisDependencies )
        {
            // Add additional declarations from the @This type
            Iterable<Class<? extends Constraint<?, ?>>> typeConstraintClasses = Iterables.<Class<? extends Constraint<?, ?>>, Iterable<Class<? extends Constraint<?, ?>>>>flatten( constraintClasses, constraintDeclarations( thisDependency ) );
            Iterable<Class<?>> typeConcernClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( concernClasses, concernDeclarations( thisDependency ) );
            Iterable<Class<?>> typeSideEffectClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( sideEffectClasses, sideEffectDeclarations( thisDependency ) );
            Iterable<Class<?>> typeMixinClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( mixinClasses, mixinDeclarations( thisDependency ) );

            implementMixinType( thisDependency,
                    typeConstraintClasses, typeConcernClasses, typeSideEffectClasses, typeMixinClasses);
        }
    }

    protected MixinModel implementMethod( Method method, Iterable<Class<?>> mixinDeclarations)
    {
        MixinModel implementationModel = mixinsModel.mixinFor( method );
        if( implementationModel != null )
        {
            return implementationModel;
        }
        Class mixinClass = findTypedImplementation( method, mixinDeclarations );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass);
        }

        // Check generic implementations
        mixinClass = findGenericImplementation( method, mixinDeclarations );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        throw new InvalidCompositeException( "No implementation found for method " + method.toGenericString() + " in " + compositeType.getName() );
    }

    private Class findTypedImplementation( final Method method, Iterable<Class<?>> mixins)
    {
        // Check if mixinClass implements the method. If so, check if the mixinClass is generic or if the filter passes
        // If a mixinClass is both generic AND non-generic at the same time, then the filter applies to the non-generic side only
        return first( filter( and( isAssignableFrom( method.getDeclaringClass() ),
                or( GenericSpecification.INSTANCE, new Specification<Class<?>>()
                {
                    @Override
                    public boolean satisfiedBy( Class<?> item )
                    {
                        return helper.appliesTo( item, method, compositeType, item );
                    }
                } ) ), mixins ) );
    }

    private Class<?> findGenericImplementation( final Method method, Iterable<Class<?>> mixins)
    {
        // Check if mixinClass is generic and the applies-to filter passes
        return first( filter( and( GenericSpecification.INSTANCE, new Specification<Class<?>>()
                {
                    @Override
                    public boolean satisfiedBy( Class<?> item )
                    {
                        return helper.appliesTo( item, method, compositeType, item );
                    }
                } ), mixins ));
    }

    private MixinModel implementMethodWithClass( Method method, Class mixinClass)
    {
        MixinModel mixinModel = mixinsModel.getMixinModel( mixinClass );
        if (mixinModel == null)
        {
            mixinModel = helper.getMixinModel( mixinClass );
            mixinsModel.addMixinModel( mixinModel );
        }

        mixinsModel.addMethodMixin( method, mixinModel );

        return mixinModel;
    }

    protected void addState( final Iterable<Class<? extends Constraint<?, ?>>> constraintClasses )
    {
        // Add method state
        compositeMethodsModel.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited ) throws RuntimeException
            {
                if (visited instanceof CompositeMethodModel)
                {
                    CompositeMethodModel methodModel = (CompositeMethodModel) visited;
                    if (methodModel.method().getParameterTypes().length == 0)
                        addStateFor(methodModel.method(), constraintClasses);

                    return false;
                }

                return super.visitEnter( visited );
            }
        });

        // Add field state
        mixinsModel.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited ) throws RuntimeException
            {
                if (visited instanceof MixinModel)
                {
                    MixinModel model = (MixinModel) visited;
                    ForEach.forEach( Fields.FIELDS_OF.map( model.mixinClass() ) ).
                            filter( Annotations.hasAnnotation( State.class ) ).
                            visit( new Visitor<Field, RuntimeException>()
                            {
                                @Override
                                public boolean visit( Field visited ) throws RuntimeException
                                {
                                    addStateFor( visited, constraintClasses );
                                    return true;
                                }
                            } );
                    return false;
                }
                return super.visitEnter( visited );
            }
        });
    }

    protected void addStateFor( AccessibleObject accessor, Iterable<Class<? extends Constraint<?, ?>>> constraintClasses )
    {
        String stateName = QualifiedName.fromAccessor( accessor ).name();

        if (registeredStateNames.contains( stateName ))
            return; // Skip already registered names

        if( Property.class.isAssignableFrom( Classes.RAW_CLASS.map( Classes.TYPE_OF.map( accessor ) )))
        {
            propertiesModel.addProperty( newPropertyModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
    }

    protected PropertyModel newPropertyModel( AccessibleObject accessor, Iterable<Class<? extends Constraint<?, ?>>> constraintClasses )
    {
        Iterable<Annotation> annotations = Annotations.getAccessorAndTypeAnnotations( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericPropertyInfo.getPropertyType( accessor ), ((Member) accessor)
                .getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.getMetaInfo( accessor );
        Object initialValue = stateDeclarations.getInitialValue( accessor );
        boolean useDefaults = metaInfo.get( UseDefaults.class ) != null || stateDeclarations.isUseDefaults( accessor );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        PropertyModel propertyModel = new PropertyModel( accessor, immutable, useDefaults, valueConstraintsInstance, metaInfo, initialValue );
        return propertyModel;
    }


    // Model
    private ConstraintsModel constraintsFor( Method method, Iterable<Class<? extends Constraint<?, ?>>> constraintClasses)
    {
        List<ValueConstraintsModel> parameterConstraintModels = Collections.emptyList();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Type[] parameterTypes = method.getGenericParameterTypes();
        boolean constrained = false;
        for( int i = 0; i < parameterAnnotations.length; i++ )
        {
            Annotation[] parameterAnnotation = parameterAnnotations[i];

            Name nameAnnotation = (Name) Iterables.first( Iterables.filter( isType( Name.class ), iterable( parameterAnnotation ) ) );
            String name = nameAnnotation == null ? "param" + (i + 1) : nameAnnotation.value();

            boolean optional = Iterables.first( Iterables.filter( isType( Optional.class ), iterable( parameterAnnotation ) ) ) != null;
            ValueConstraintsModel parameterConstraintsModel = constraintsFor( Arrays.asList( parameterAnnotation ), parameterTypes[i], name, optional, constraintClasses, method);
            if( parameterConstraintsModel.isConstrained() )
            {
                constrained = true;
            }

            if( parameterConstraintModels.isEmpty() )
            {
                parameterConstraintModels = new ArrayList<ValueConstraintsModel>();
            }
            parameterConstraintModels.add( parameterConstraintsModel );
        }

        if (!constrained)
            return new ConstraintsModel( Collections.<ValueConstraintsModel>emptyList() );
        else
            return new ConstraintsModel( parameterConstraintModels );
    }

    protected ValueConstraintsModel constraintsFor(
            Iterable<Annotation> constraintAnnotations,
            Type valueType,
            String name,
            boolean optional,
            Iterable<Class<? extends Constraint<?, ?>>> constraintClasses,
            AccessibleObject accessor )
    {
        valueType = Classes.WRAPPER_CLASS.map( valueType );

        List<AbstractConstraintModel> constraintModels = new ArrayList<AbstractConstraintModel>();
        nextConstraint:
        for( Annotation constraintAnnotation : filter( translate( type(), hasAnnotation( org.qi4j.api.constraint.ConstraintDeclaration.class ) ), constraintAnnotations ) )
        {
            // Check composite declarations first
            Class<? extends Annotation> annotationType = constraintAnnotation.annotationType();
            for( Class<? extends Constraint<?, ?>> constraint : constraintClasses )
            {
                if( helper.appliesTo( constraint, annotationType, valueType ) )
                {
                    constraintModels.add( new ConstraintModel( constraintAnnotation, constraint ) );
                    continue nextConstraint;
                }
            }

            // Check the annotation itself
            Constraints constraints = annotationType.getAnnotation( Constraints.class );
            if( constraints != null )
            {
                for( Class<? extends Constraint<?, ?>> constraintClass : constraints.value() )
                {
                    if( helper.appliesTo( constraintClass, annotationType, valueType ) )
                    {
                        constraintModels.add( new ConstraintModel( constraintAnnotation, constraintClass ) );
                        continue nextConstraint;
                    }
                }
            }

            // No implementation found!

            // Check if if it's a composite constraints
            if( Iterables.matchesAny( translate( type(), hasAnnotation( org.qi4j.api.constraint.ConstraintDeclaration.class ) ), asList( constraintAnnotation
                    .annotationType()
                    .getAnnotations() ) ) )
            {
                ValueConstraintsModel valueConstraintsModel = constraintsFor( iterable( constraintAnnotation.annotationType()
                        .getAnnotations() ), valueType, name, optional, constraintClasses, accessor );
                CompositeConstraintModel compositeConstraintModel = new CompositeConstraintModel( constraintAnnotation, valueConstraintsModel );
                constraintModels.add( compositeConstraintModel );
                continue nextConstraint;
            }

            throw new InvalidCompositeException( "Cannot find implementation of constraint @" + constraintAnnotation.annotationType().getSimpleName() + " for " + valueType + " in method "+((Member)accessor).getName()+" of composite " + compositeType );
        }

        return new ValueConstraintsModel( constraintModels, name, optional );
    }

    private ConcernsModel concernsFor( Method method,
                                             Class<?> mixinClass,
                                             Iterable<Class<?>> concernClasses)
    {
        List<ConcernModel> concernsFor = new ArrayList<ConcernModel>();
        for( Class<?> concern : concernClasses )
        {
            if( helper.appliesTo( concern, method, compositeType, mixinClass ) )
            {
                concernsFor.add( helper.getConcernModel( concern ) );
            } else
            {
                // Lookup method in mixin
                if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                {
                    try
                    {
                        Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                        if( helper.appliesTo( concern, mixinMethod, compositeType, mixinClass ) )
                            concernsFor.add( helper.getConcernModel( concern ) );
                    } catch( NoSuchMethodException e )
                    {
                        // Ignore
                    }
                }
            }
        }

        // Check annotations on method that have @Concerns annotations themselves
        for( Annotation annotation : method.getAnnotations() )
        {
            Concerns concerns = annotation.annotationType().getAnnotation( Concerns.class );
            if ( concerns != null )
            {
                for( Class<?> concern : concerns.value() )
                {
                    if( helper.appliesTo( concern, method, compositeType, mixinClass ) )
                    {
                        concernsFor.add( helper.getConcernModel( concern ) );
                    }
                }
            }
        }

        if( concernsFor.isEmpty() )
            return ConcernsModel.EMPTY_CONCERNS;
        else
            return new ConcernsModel( concernsFor );
    }

    private SideEffectsModel sideEffectsFor( Method method,
                                                   Class<?> mixinClass,
                                                   Iterable<Class<?>> sideEffectClasses
    )
    {
        List<SideEffectModel> sideEffectsFor = new ArrayList<SideEffectModel>();
        for( Class<?> sideEffect : sideEffectClasses )
        {
            if( helper.appliesTo( sideEffect, method, compositeType, mixinClass ) )
            {
                sideEffectsFor.add( helper.getSideEffectModel( sideEffect ) );
            } else
            {
                // Lookup method in mixin
                if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                {
                    try
                    {
                        Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                        if( helper.appliesTo( sideEffect, mixinMethod, compositeType, mixinClass ) )
                            sideEffectsFor.add( helper.getSideEffectModel( sideEffect ) );
                    } catch( NoSuchMethodException e )
                    {
                        // Ignore
                    }
                }

            }
        }

        if( sideEffectsFor.isEmpty() )
            return SideEffectsModel.EMPTY_SIDEEFFECTS;
        else
            return new SideEffectsModel( sideEffectsFor );
    }

    protected Iterable<Class<? extends Constraint<?, ?>>> constraintDeclarations( Class<?> type )
    {
        // Find constraint declarations
        Iterable<Type> types = Classes.TYPES_OF.map( type );

        // Find all constraints and flattern them into an iterable
        return Iterables.toList( Iterables.flattenIterables( Iterables.map( new Function<Type, Iterable<Class<? extends Constraint<?, ?>>>>()
        {
            @Override
            public Iterable<Class<? extends Constraint<?, ?>>> map( Type type )
            {
                Constraints constraints = Annotations.getAnnotation( type, Constraints.class );
                if( constraints == null )
                    return Iterables.empty();
                else
                    return iterable( constraints.value() );
            }
        }, types ) ) );
    }

    protected Iterable<Class<?>> concernDeclarations( Class<?> type )
    {
        // Find concern declarations
        Iterable<Type> types = type.isInterface() ? Classes.TYPES_OF.map( type ) : Iterables.<Type, Class<?>>cast( Classes.CLASS_HIERARCHY.map( type ) );

        // Find all concerns and flattern them into an iterable
        return Iterables.toList( Iterables.flattenIterables( Iterables.map( new Function<Type, Iterable<Class<?>>>()
        {
            @Override
            public Iterable<Class<?>> map( Type type )
            {
                Concerns concerns = Annotations.getAnnotation( type, Concerns.class );
                if( concerns == null )
                    return Iterables.empty();
                else
                    return iterable( concerns.value() );
            }
        }, types ) ) );
    }

    protected Iterable<Class<?>> sideEffectDeclarations( Class<?> type )
    {
        // Find side-effect declarations
        Iterable<Type> types = Classes.TYPES_OF.map( type );

        // Find all side-effects and flattern them into an iterable
        return Iterables.toList( Iterables.flattenIterables( Iterables.map( new Function<Type, Iterable<Class<?>>>()
        {
            @Override
            public Iterable<Class<?>> map( Type type )
            {
                SideEffects sideEffects = Annotations.getAnnotation( type, SideEffects.class );
                if( sideEffects == null )
                    return Iterables.empty();
                else
                    return iterable( sideEffects.value() );
            }
        }, types ) ) );
    }

    protected Iterable<Class<?>> mixinDeclarations( Class<?> type )
    {
        // Find mixin declarations
        Iterable<Type> types = Classes.TYPES_OF.map( type );

        // Find all mixins and flattern them into an iterable
        return Iterables.toList( Iterables.flattenIterables( Iterables.map( new Function<Type, Iterable<Class<?>>>()
        {
            @Override
            public Iterable<Class<?>> map( Type type )
            {
                Mixins mixins = Annotations.getAnnotation( type, Mixins.class );
                if( mixins == null )
                    return Iterables.empty();
                else
                    return iterable( mixins.value() );
            }
        }, types ) ) );
    }
}
