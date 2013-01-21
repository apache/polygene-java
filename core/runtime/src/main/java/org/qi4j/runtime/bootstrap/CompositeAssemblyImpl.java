package org.qi4j.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.InvalidCompositeException;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
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
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.type.HasTypes;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Fields;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.functional.ForEach;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Visitor;
import org.qi4j.runtime.composite.AbstractConstraintModel;
import org.qi4j.runtime.composite.CompositeConstraintModel;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.ConcernModel;
import org.qi4j.runtime.composite.ConcernsModel;
import org.qi4j.runtime.composite.ConstraintModel;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.GenericSpecification;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.SideEffectModel;
import org.qi4j.runtime.composite.SideEffectsModel;
import org.qi4j.runtime.composite.StateModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.property.PropertyModel;

import static org.qi4j.api.util.Annotations.*;
import static org.qi4j.api.util.Classes.*;
import static org.qi4j.functional.Iterables.*;
import static org.qi4j.functional.Specifications.*;

/**
 * TODO
 */
public abstract class CompositeAssemblyImpl
    implements HasTypes
{
    protected List<Class<?>> concerns = new ArrayList<Class<?>>();
    protected List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    protected List<Class<?>> mixins = new ArrayList<Class<?>>();
    protected List<Class<?>> types = new ArrayList<Class<?>>();
    protected MetaInfo metaInfo = new MetaInfo();
    protected Visibility visibility = Visibility.module;

    protected boolean immutable;
    protected PropertiesModel propertiesModel;
    protected StateModel stateModel;
    protected MixinsModel mixinsModel;
    protected CompositeMethodsModel compositeMethodsModel;
    private AssemblyHelper helper;
    protected StateDeclarations stateDeclarations;

    protected Set<String> registeredStateNames = new HashSet<String>();

    public CompositeAssemblyImpl( Class<?> mainType )
    {
        types.add( mainType );
    }

    @Override
    public Iterable<Class<?>> types()
    {
        return types;
    }

    protected StateModel createStateModel()
    {
        return new StateModel( propertiesModel );
    }

    protected MixinsModel createMixinsModel()
    {
        return new MixinsModel();
    }

    protected void buildComposite( AssemblyHelper helper,
                                   StateDeclarations stateDeclarations
    )
    {
        this.stateDeclarations = stateDeclarations;
        this.helper = helper;
        for( Class<?> compositeType : types )
        {
            metaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
            addAnnotationsMetaInfo( compositeType, metaInfo );
        }

        immutable = metaInfo.get( Immutable.class ) != null;
        propertiesModel = new PropertiesModel();
        stateModel = createStateModel();
        mixinsModel = createMixinsModel();
        compositeMethodsModel = new CompositeMethodsModel( mixinsModel );

        // Implement composite methods
        Iterable<Class<? extends Constraint<?, ?>>> constraintClasses = constraintDeclarations( this.types );
        Iterable<Class<?>> concernClasses = Iterables.flatten( concerns, concernDeclarations( this.types ) );
        Iterable<Class<?>> sideEffectClasses = Iterables.flatten( sideEffects, sideEffectDeclarations( this.types ) );
        Iterable<Class<?>> mixinClasses = Iterables.flatten( mixins, mixinDeclarations( this.types ) );
        implementMixinType( types, constraintClasses, concernClasses, sideEffectClasses, mixinClasses );

        // Add state from methods and fields
        addState( constraintClasses );
    }

    protected void addAnnotationsMetaInfo( Class<?> type, MetaInfo compositeMetaInfo )
    {
        Class[] declaredInterfaces = type.getInterfaces();
        for( int i = declaredInterfaces.length - 1; i >= 0; i-- )
        {
            addAnnotationsMetaInfo( declaredInterfaces[ i ], compositeMetaInfo );
        }
        compositeMetaInfo.withAnnotations( type );
    }

    protected void implementMixinType( Iterable<? extends Class<?>> types,
                                       Iterable<Class<? extends Constraint<?, ?>>> constraintClasses,
                                       Iterable<Class<?>> concernClasses,
                                       Iterable<Class<?>> sideEffectClasses,
                                       Iterable<Class<?>> mixinClasses
    )
    {
        Set<Class<?>> thisDependencies = new HashSet<Class<?>>();
        for( Class<?> mixinType : types )
        {
            for( Method method : mixinType.getMethods() )
            {
                if( !compositeMethodsModel.isImplemented( method ) &&
                    !Proxy.class.equals( method.getDeclaringClass().getSuperclass() ) &&
                    !Proxy.class.equals( method.getDeclaringClass() ) &&
                    !Modifier.isStatic( method.getModifiers() ) )
                {
                    MixinModel mixinModel = implementMethod( method, mixinClasses );
                    ConcernsModel concernsModel = concernsFor(
                        method,
                        mixinModel.mixinClass(),
                        Iterables.<Class<?>>flatten( concernDeclarations( mixinModel.mixinClass() ), concernClasses )
                    );
                    SideEffectsModel sideEffectsModel = sideEffectsFor(
                        method,
                        mixinModel.mixinClass(),
                        Iterables.<Class<?>>flatten( sideEffectDeclarations( mixinModel.mixinClass() ), sideEffectClasses )
                    );
                    method.setAccessible( true );
                    ConstraintsModel constraints = constraintsFor(
                        method,
                        Iterables.<Class<? extends Constraint<?, ?>>>flatten( constraintDeclarations( mixinModel.mixinClass() ), constraintClasses )
                    );
                    CompositeMethodModel methodComposite = new CompositeMethodModel(
                        method,
                        constraints,
                        concernsModel,
                        sideEffectsModel,
                        mixinsModel
                    );

                    // Implement @This references
                    Iterable<Class<?>> map = map( new DependencyModel.InjectionTypeFunction(), filter( new DependencyModel.ScopeSpecification( This.class ), methodComposite
                        .dependencies() ) );
                    Iterable<Class<?>> map1 = map( new DependencyModel.InjectionTypeFunction(), filter( new DependencyModel.ScopeSpecification( This.class ), mixinModel
                        .dependencies() ) );
                    Iterable<Class<?>> filter = filter(
                        not(
                            in( Initializable.class, Lifecycle.class, InvocationHandler.class )
                        ),
                        map( Classes.RAW_CLASS, interfacesOf( mixinModel.mixinClass() ) )
                    );
                    Iterable<? extends Class<?>> flatten = flatten( map, map1, filter );
                    Iterables.addAll( thisDependencies, flatten );

                    compositeMethodsModel.addMethod( methodComposite );
                }
            }
            // Add type to set of mixin types
            mixinsModel.addMixinType( mixinType );
        }

        // Implement all @This dependencies that were found
        for( Class<?> thisDependency : thisDependencies )
        {
            // Add additional declarations from the @This type
            Iterable<Class<? extends Constraint<?, ?>>> typeConstraintClasses = Iterables.flatten( constraintClasses, constraintDeclarations( thisDependency ) );
            Iterable<Class<?>> typeConcernClasses = Iterables.flatten( concernClasses, concernDeclarations( thisDependency ) );
            Iterable<Class<?>> typeSideEffectClasses = Iterables.flatten( sideEffectClasses, sideEffectDeclarations( thisDependency ) );
            Iterable<Class<?>> typeMixinClasses = Iterables.flatten( mixinClasses, mixinDeclarations( thisDependency ) );

            Iterable<? extends Class<?>> singleton = Iterables.iterable( thisDependency );
            implementMixinType( singleton, typeConstraintClasses, typeConcernClasses, typeSideEffectClasses, typeMixinClasses );
        }
    }

    protected MixinModel implementMethod( Method method, Iterable<Class<?>> mixinDeclarations )
    {
        MixinModel implementationModel = mixinsModel.mixinFor( method );
        if( implementationModel != null )
        {
            return implementationModel;
        }
        Class mixinClass = findTypedImplementation( method, mixinDeclarations );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        // Check generic implementations
        mixinClass = findGenericImplementation( method, mixinDeclarations );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        throw new InvalidCompositeException( "No implementation found for method \n    " + method.toGenericString() + "\nin\n    " + types );
    }

    private Class findTypedImplementation( final Method method, Iterable<Class<?>> mixins )
    {
        // Check if mixinClass implements the method. If so, check if the mixinClass is generic or if the filter passes
        // If a mixinClass is both generic AND non-generic at the same time, then the filter applies to the non-generic side only
        return first( filter( and( isAssignableFrom( method.getDeclaringClass() ),
                                   or( GenericSpecification.INSTANCE, new Specification<Class<?>>()
                                   {
                                       @Override
                                       public boolean satisfiedBy( Class<?> item )
                                       {
                                           return helper.appliesTo( item, method, types, item );
                                       }
                                   } ) ), mixins ) );
    }

    private Class<?> findGenericImplementation( final Method method, Iterable<Class<?>> mixins )
    {
        // Check if mixinClass is generic and the applies-to filter passes
        return first( filter( and( GenericSpecification.INSTANCE, new Specification<Class<?>>()
        {
            @Override
            public boolean satisfiedBy( Class<?> item )
            {
                return helper.appliesTo( item, method, types, item );
            }
        } ), mixins ) );
    }

    private MixinModel implementMethodWithClass( Method method, Class mixinClass )
    {
        MixinModel mixinModel = mixinsModel.getMixinModel( mixinClass );
        if( mixinModel == null )
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
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof CompositeMethodModel )
                {
                    CompositeMethodModel methodModel = (CompositeMethodModel) visited;
                    if( methodModel.method().getParameterTypes().length == 0 )
                    {
                        addStateFor( methodModel.method(), constraintClasses );
                    }

                    return false;
                }

                return super.visitEnter( visited );
            }
        } );

        // Add field state
        mixinsModel.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof MixinModel )
                {
                    MixinModel model = (MixinModel) visited;
                    ForEach.forEach( Fields.FIELDS_OF.map( model.mixinClass() ) ).
                        filter( Annotations.hasAnnotation( State.class ) ).
                        visit( new Visitor<Field, RuntimeException>()
                        {
                            @Override
                            public boolean visit( Field visited )
                                throws RuntimeException
                            {
                                addStateFor( visited, constraintClasses );
                                return true;
                            }
                        } );
                    return false;
                }
                return super.visitEnter( visited );
            }
        } );
    }

    protected void addStateFor( AccessibleObject accessor,
                                Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        String stateName = QualifiedName.fromAccessor( accessor ).name();

        if( registeredStateNames.contains( stateName ) )
        {
            return; // Skip already registered names
        }

        if( Property.class.isAssignableFrom( Classes.RAW_CLASS.map( typeOf( accessor ) ) ) )
        {
            propertiesModel.addProperty( newPropertyModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
    }

    protected PropertyModel newPropertyModel( AccessibleObject accessor,
                                              Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        Iterable<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericPropertyInfo.propertyTypeOf( accessor ), ( (Member) accessor )
            .getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        Object initialValue = stateDeclarations.initialValueOf( accessor );
        boolean useDefaults = metaInfo.get( UseDefaults.class ) != null || stateDeclarations.useDefaults( accessor );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        PropertyModel propertyModel = new PropertyModel( accessor, immutable, useDefaults, valueConstraintsInstance, metaInfo, initialValue );
        return propertyModel;
    }

    // Model
    private ConstraintsModel constraintsFor( Method method,
                                             Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        List<ValueConstraintsModel> parameterConstraintModels = Collections.emptyList();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Type[] parameterTypes = method.getGenericParameterTypes();
        boolean constrained = false;
        for( int i = 0; i < parameterAnnotations.length; i++ )
        {
            Annotation[] parameterAnnotation = parameterAnnotations[ i ];

            Name nameAnnotation = (Name) Iterables.first( Iterables.filter( isType( Name.class ), iterable( parameterAnnotation ) ) );
            String name = nameAnnotation == null ? "param" + ( i + 1 ) : nameAnnotation.value();

            boolean optional = Iterables.first( Iterables.filter( isType( Optional.class ), iterable( parameterAnnotation ) ) ) != null;
            ValueConstraintsModel parameterConstraintsModel = constraintsFor( Arrays.asList( parameterAnnotation ), parameterTypes[ i ], name, optional, constraintClasses, method );
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

        if( !constrained )
        {
            return new ConstraintsModel( Collections.<ValueConstraintsModel>emptyList() );
        }
        else
        {
            return new ConstraintsModel( parameterConstraintModels );
        }
    }

    protected ValueConstraintsModel constraintsFor(
        Iterable<Annotation> constraintAnnotations,
        Type valueType,
        String name,
        boolean optional,
        Iterable<Class<? extends Constraint<?, ?>>> constraintClasses,
        AccessibleObject accessor
    )
    {
        valueType = wrapperClass( valueType );

        List<AbstractConstraintModel> constraintModels = new ArrayList<AbstractConstraintModel>();
        nextConstraint:
        for( Annotation constraintAnnotation : filter( translate( type(), hasAnnotation( ConstraintDeclaration.class ) ), constraintAnnotations ) )
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
            Iterable<Annotation> annotations = iterable( annotationType.getAnnotations() );
            if( Iterables.matchesAny( translate( type(), hasAnnotation( ConstraintDeclaration.class ) ), annotations ) )
            {
                ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, valueType, name, optional, constraintClasses, accessor );
                CompositeConstraintModel compositeConstraintModel = new CompositeConstraintModel( constraintAnnotation, valueConstraintsModel );
                constraintModels.add( compositeConstraintModel );
                continue nextConstraint;
            }

            throw new InvalidCompositeException(
                "Cannot find implementation of constraint @"
                + annotationType.getSimpleName()
                + " for "
                + valueType
                + " in method "
                + ( (Member) accessor ).getName()
                + " of composite " + types );
        }

        return new ValueConstraintsModel( constraintModels, name, optional );
    }

    private ConcernsModel concernsFor( Method method,
                                       Class<?> mixinClass,
                                       Iterable<Class<?>> concernClasses
    )
    {
        List<ConcernModel> concernsFor = new ArrayList<ConcernModel>();
        for( Class<?> concern : concernClasses )
        {
            if( helper.appliesTo( concern, method, types, mixinClass ) )
            {
                concernsFor.add( helper.getConcernModel( concern ) );
            }
            else
            {
                // Lookup method in mixin
                if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                {
                    try
                    {
                        Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                        if( helper.appliesTo( concern, mixinMethod, types, mixinClass ) )
                        {
                            concernsFor.add( helper.getConcernModel( concern ) );
                        }
                    }
                    catch( NoSuchMethodException e )
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
            if( concerns != null )
            {
                for( Class<?> concern : concerns.value() )
                {
                    if( helper.appliesTo( concern, method, types, mixinClass ) )
                    {
                        concernsFor.add( helper.getConcernModel( concern ) );
                    }
                }
            }
        }

        if( concernsFor.isEmpty() )
        {
            return ConcernsModel.EMPTY_CONCERNS;
        }
        else
        {
            return new ConcernsModel( concernsFor );
        }
    }

    private SideEffectsModel sideEffectsFor( Method method,
                                             Class<?> mixinClass,
                                             Iterable<Class<?>> sideEffectClasses
    )
    {
        List<SideEffectModel> sideEffectsFor = new ArrayList<SideEffectModel>();
        for( Class<?> sideEffect : sideEffectClasses )
        {
            if( helper.appliesTo( sideEffect, method, types, mixinClass ) )
            {
                sideEffectsFor.add( helper.getSideEffectModel( sideEffect ) );
            }
            else
            {
                // Lookup method in mixin
                if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                {
                    try
                    {
                        Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                        if( helper.appliesTo( sideEffect, mixinMethod, types, mixinClass ) )
                        {
                            sideEffectsFor.add( helper.getSideEffectModel( sideEffect ) );
                        }
                    }
                    catch( NoSuchMethodException e )
                    {
                        // Ignore
                    }
                }
            }
        }

        if( sideEffectsFor.isEmpty() )
        {
            return SideEffectsModel.EMPTY_SIDEEFFECTS;
        }
        else
        {
            return new SideEffectsModel( sideEffectsFor );
        }
    }

    private Iterable<Class<? extends Constraint<?, ?>>> constraintDeclarations( Class<?> type )
    {
        Iterable<? extends Class<?>> iterable = Iterables.iterable( type );
        return constraintDeclarations( iterable );
    }

    private Iterable<Class<? extends Constraint<?, ?>>> constraintDeclarations( Iterable<? extends Class<?>> typess )
    {
        // Find constraint declarations
        List<Type> allTypes = new ArrayList<Type>();
        for( Class<?> type : typess )
        {
            Iterable<Type> types = typesOf( type );
            Iterables.addAll( allTypes, types );
        }

        // Find all constraints and flatten them into an iterable
        Function<Type, Iterable<Class<? extends Constraint<?, ?>>>> function = new Function<Type, Iterable<Class<? extends Constraint<?, ?>>>>()
        {
            @Override
            public Iterable<Class<? extends Constraint<?, ?>>> map( Type type )
            {
                Constraints constraints = Annotations.annotationOn( type, Constraints.class );
                if( constraints == null )
                {
                    return Iterables.empty();
                }
                else
                {
                    return iterable( constraints.value() );
                }
            }
        };
        Iterable<Class<? extends Constraint<?,?>>> flatten = Iterables.flattenIterables( Iterables.map( function, allTypes ) );
        return Iterables.toList( flatten );
    }

    private Iterable<Class<?>> concernDeclarations( Class<?> type )
    {
        Iterable<? extends Class<?>> iterable = Iterables.iterable( type );
        return concernDeclarations( iterable );
    }

    private Iterable<Class<?>> concernDeclarations( Iterable<? extends Class<?>> typess )
    {
        // Find concern declarations
        ArrayList<Type> allTypes = new ArrayList<Type>();
        for( Class<?> type : typess )
        {
            Iterable<Type> types;
            if( type.isInterface() )
            {
                types = typesOf( type );
            }
            else
            {
                types = Iterables.<Type>cast( classHierarchy( type ) );
            }
            Iterables.addAll( allTypes, types );
        }

        // Find all concerns and flattern them into an iterable
        Function<Type, Iterable<Class<?>>> function = new Function<Type, Iterable<Class<?>>>()
        {
            @Override
            public Iterable<Class<?>> map( Type type )
            {
                Concerns concerns = Annotations.annotationOn( type, Concerns.class );
                if( concerns == null )
                {
                    return Iterables.empty();
                }
                else
                {
                    return iterable( concerns.value() );
                }
            }
        };
        Iterable<Class<?>> flatten = Iterables.flattenIterables( Iterables.map( function, allTypes ) );
        return Iterables.toList( flatten );
    }

    protected Iterable<Class<?>> sideEffectDeclarations( Class<?> type )
    {
        Iterable<? extends Class<?>> iterable = Iterables.iterable( type );
        return sideEffectDeclarations( iterable );
    }

    protected Iterable<Class<?>> sideEffectDeclarations( Iterable<? extends Class<?>> typess )
    {
        // Find side-effect declarations
        ArrayList<Type> allTypes = new ArrayList<Type>();
        for( Class<?> type : typess )
        {
            Iterable<Type> types = typesOf( type );
            Iterables.addAll( allTypes, types );
        }

        // Find all side-effects and flattern them into an iterable
        Function<Type, Iterable<Class<?>>> function = new Function<Type, Iterable<Class<?>>>()
        {
            @Override
            public Iterable<Class<?>> map( Type type )
            {
                SideEffects sideEffects = Annotations.annotationOn( type, SideEffects.class );
                if( sideEffects == null )
                {
                    return Iterables.empty();
                }
                else
                {
                    return iterable( sideEffects.value() );
                }
            }
        };
        Iterable<Class<?>> flatten = Iterables.flattenIterables( Iterables.map( function, allTypes ) );
        return Iterables.toList( flatten );
    }

    protected Iterable<Class<?>> mixinDeclarations( Class<?> type )
    {
        Iterable<? extends Class<?>> iterable = Iterables.iterable( type );
        return mixinDeclarations( iterable );
    }

    protected Iterable<Class<?>> mixinDeclarations( Iterable<? extends Class<?>> typess )
    {
        // Find mixin declarations
        ArrayList<Type> allTypes = new ArrayList<Type>();
        for( Class<?> type : typess )
        {
            Iterable<Type> types = typesOf( type );
            Iterables.addAll( allTypes, types );
        }

        // Find all mixins and flattern them into an iterable
        Function<Type, Iterable<Class<?>>> function = new Function<Type, Iterable<Class<?>>>()
        {
            @Override
            public Iterable<Class<?>> map( Type type )
            {
                Mixins mixins = Annotations.annotationOn( type, Mixins.class );
                if( mixins == null )
                {
                    return Iterables.empty();
                }
                else
                {
                    return iterable( mixins.value() );
                }
            }
        };
        Iterable<Class<?>> flatten = Iterables.flattenIterables( Iterables.map( function, allTypes ) );
        return Iterables.toList( flatten );
    }
}
