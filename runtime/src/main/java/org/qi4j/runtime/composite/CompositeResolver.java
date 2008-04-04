package org.qi4j.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.ConstraintDeclaration;
import org.qi4j.spi.composite.CompositeMethodModel;
import org.qi4j.spi.composite.CompositeMethodResolution;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.ConcernModel;
import org.qi4j.spi.composite.ConcernResolution;
import org.qi4j.spi.composite.ConstraintModel;
import org.qi4j.spi.composite.ConstraintResolution;
import org.qi4j.spi.composite.ConstructorModel;
import org.qi4j.spi.composite.ConstructorResolution;
import org.qi4j.spi.composite.FieldModel;
import org.qi4j.spi.composite.FieldResolution;
import org.qi4j.spi.composite.FragmentModel;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.composite.MethodModel;
import org.qi4j.spi.composite.MethodResolution;
import org.qi4j.spi.composite.MixinModel;
import org.qi4j.spi.composite.MixinResolution;
import org.qi4j.spi.composite.ModifierModel;
import org.qi4j.spi.composite.ParameterConstraintsModel;
import org.qi4j.spi.composite.ParameterConstraintsResolution;
import org.qi4j.spi.composite.ParameterModel;
import org.qi4j.spi.composite.ParameterResolution;
import org.qi4j.spi.composite.PropertyResolution;
import org.qi4j.spi.composite.ResolutionException;
import org.qi4j.spi.composite.SideEffectModel;
import org.qi4j.spi.composite.SideEffectResolution;
import org.qi4j.spi.entity.association.AssociationModel;
import org.qi4j.spi.entity.association.AssociationResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.ResolutionContext;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.ApplicationModel;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.spi.structure.LayerModel;
import org.qi4j.spi.structure.ModuleModel;

/**
 * TODO
 */
public final class CompositeResolver
    extends AbstractResolver
{
    public CompositeResolution resolveCompositeModel( CompositeDescriptor compositeDescriptor, ResolutionContext resolutionContext )
        throws ResolutionException
    {
        // First figure out which interfaces map to which methods
        Map<Method, MixinModel> mixinsForMethods = getMixinsForMethods( compositeDescriptor.getCompositeModel() );

        // Determine list of mixin resolutions. Remove all duplicates
        Map<MixinModel, MixinResolution> resolvedMixins = getResolvedMixins( mixinsForMethods, compositeDescriptor.getCompositeModel(), resolutionContext );

        // Get mixin resolutions for all methods
        Map<Method, MixinResolution> mixinResolutionsForMethods = getResolvedMethodMixins( mixinsForMethods, resolvedMixins );

        // Map concerns and side-effects to methods
        List<CompositeMethodResolution> methodResolutions = getMethodResolutions( compositeDescriptor.getCompositeModel(), resolutionContext, mixinResolutionsForMethods, resolvedMixins.values() );

        List<PropertyResolution> propertyResolutions = new ArrayList<PropertyResolution>();
        List<AssociationResolution> associationResolutions = new ArrayList<AssociationResolution>();
        for( CompositeMethodResolution methodResolution : methodResolutions )
        {
            propertyResolutions.add( methodResolution.getPropertyResolution() );
            associationResolutions.add( methodResolution.getAssociationResolution() );
        }

        CompositeResolution resolution = new CompositeResolution( compositeDescriptor, methodResolutions, resolvedMixins.values(), propertyResolutions, associationResolutions );
        return resolution;
    }

    private ConcernResolution resolveConcern( ConcernModel concernModel, ResolutionContext resolutionContext ) throws InvalidInjectionException
    {
        List<ConstructorResolution> constructors = new ArrayList<ConstructorResolution>();
        resolveConstructorModel( concernModel.getConstructorModels(), constructors, resolutionContext );
        List<FieldResolution> fields = new ArrayList<FieldResolution>();
        resolveFieldModels( concernModel.getFieldModels(), fields, resolutionContext );
        List<MethodResolution> methods = new ArrayList<MethodResolution>();
        resolveMethodModels( concernModel.getMethodModels(), methods, resolutionContext );

        ConcernResolution resolution = new ConcernResolution( concernModel, constructors, fields, methods );
        return resolution;
    }

    private SideEffectResolution resolveSideEffect( SideEffectModel sideEffectModel, ResolutionContext resolutionContext )
        throws InvalidInjectionException
    {
        List<ConstructorResolution> constructors = new ArrayList<ConstructorResolution>();
        resolveConstructorModel( sideEffectModel.getConstructorModels(), constructors, resolutionContext );
        List<FieldResolution> fields = new ArrayList<FieldResolution>();
        resolveFieldModels( sideEffectModel.getFieldModels(), fields, resolutionContext );
        List<MethodResolution> methods = new ArrayList<MethodResolution>();
        resolveMethodModels( sideEffectModel.getMethodModels(), methods, resolutionContext );

        SideEffectResolution resolution = new SideEffectResolution( sideEffectModel, constructors, fields, methods );
        return resolution;
    }

    private MixinResolution resolveMixin(
        MixinModel mixinModelToResolve, CompositeModel compositeModel, ResolutionContext resolutionContext, Map<Method, MixinModel> mixinsForMethods )
        throws InvalidInjectionException
    {
        ModuleModel moduleModel = resolutionContext.getModule();
        LayerModel layerModel = resolutionContext.getLayer();
        ApplicationModel application = resolutionContext.getApplication();

        List<ConstructorResolution> constructors = new ArrayList<ConstructorResolution>();

        Iterable<ConstructorModel> constructorModels = mixinModelToResolve.getConstructorModels();
        resolveConstructorModel( constructorModels, constructors, resolutionContext );

        List<FieldResolution> fields = new ArrayList<FieldResolution>();
        Iterable<FieldModel> fieldModels = mixinModelToResolve.getFieldModels();
        resolveFieldModels( fieldModels, fields, resolutionContext );

        List<MethodResolution> methods = new ArrayList<MethodResolution>();
        Iterable<MethodModel> methodModels = mixinModelToResolve.getMethodModels();
        resolveMethodModels( methodModels, methods, resolutionContext );

        // Compute set of implemented properties and associations in this mixin
        Map<String, PropertyResolution> propertyResolutions = new HashMap<String, PropertyResolution>();
        Map<String, AssociationResolution> associationResolutions = new HashMap<String, AssociationResolution>();
        for( Map.Entry<Method, MixinModel> mixinMethodEntry : mixinsForMethods.entrySet() )
        {
            MixinModel mixinModel = mixinMethodEntry.getValue();
            if( mixinModel.equals( mixinModelToResolve ) )
            {
                Method mixinMethod = mixinMethodEntry.getKey();
                CompositeMethodModel cmm = compositeModel.getCompositeMethodModel( mixinMethod );

                PropertyModel pm = cmm.getPropertyModel();
                if( pm != null )
                {
                    String propertyModelName = pm.getName();
                    propertyResolutions.put( propertyModelName, new PropertyResolution( pm ) );
                }

                AssociationModel am = cmm.getAssociationModel();
                if( am != null )
                {
                    String associationModelName = am.getName();
                    associationResolutions.put( associationModelName, new AssociationResolution( am ) );
                }
            }
        }

        return new MixinResolution( mixinModelToResolve, constructors, fields, methods, propertyResolutions, associationResolutions );
    }

    private Map<Method, MixinResolution> getResolvedMethodMixins( Map<Method, MixinModel> mixinsForMethods, Map<MixinModel, MixinResolution> resolvedMixins )
    {
        Map<Method, MixinResolution> resolvedMethodMixins = new HashMap<Method, MixinResolution>();
        for( Map.Entry<Method, MixinModel> entry : mixinsForMethods.entrySet() )
        {
            resolvedMethodMixins.put( entry.getKey(), resolvedMixins.get( entry.getValue() ) );
        }
        return resolvedMethodMixins;
    }

    private Map<MixinModel, MixinResolution> getResolvedMixins( Map<Method, MixinModel> mixinsForMethods, CompositeModel compositeModel, ResolutionContext resolutionContext )
    {
        // Build set of used mixins
        Set<MixinModel> usedMixins = new LinkedHashSet<MixinModel>();
        usedMixins.addAll( mixinsForMethods.values() );

        // If a mixin A uses mixin B as a @ThisCompositeAs parameterModel dependency, then ensure that the order is correct.
        Set<MixinModel> orderedUsedMixins = new LinkedHashSet<MixinModel>();
        dependencyCheck:
        while( !usedMixins.isEmpty() )
        {
            Iterator<MixinModel> iterator = usedMixins.iterator();
            MixinModel model = iterator.next();

            // Check if any of the other resolutions depends on this one
            while( iterator.hasNext() )
            {
                MixinModel otherMixinModel = iterator.next();
                if( dependsOn( model, otherMixinModel, mixinsForMethods ) )
                {
                    // Check cyclic dependency
                    if( dependsOn( otherMixinModel, model, mixinsForMethods ) )
                    {
                        throw new InvalidCompositeException( "Cyclic dependency between mixins " + model.getModelClass().getName() + " and " + otherMixinModel.getModelClass().getName(), compositeModel.getCompositeType() );
                    }
                    else
                    {
                        // Move to last
                        usedMixins.remove( model );
                        usedMixins.add( model );
                        continue dependencyCheck;
                    }
                }
            }

            // No dependencies from this model to the remaining resolutions
            usedMixins.remove( model );
            orderedUsedMixins.add( model );
        }

        // Now that we have an ordered set of mixins, resolve them
        Map<MixinModel, MixinResolution> mixinResolutions = new LinkedHashMap<MixinModel, MixinResolution>();
        for( MixinModel orderedUsedMixin : orderedUsedMixins )
        {
            try
            {
                MixinResolution mixinResolution = resolveMixin( orderedUsedMixin, compositeModel, resolutionContext, mixinsForMethods );
                mixinResolutions.put( orderedUsedMixin, mixinResolution );
            }
            catch( InvalidInjectionException e )
            {
                throw new ResolutionException( "Could not resolve mixin " + orderedUsedMixin.getModelClass().getName(), e );
            }
        }
        return mixinResolutions;
    }

    private boolean dependsOn( MixinModel model, MixinModel otherModel, Map<Method, MixinModel> mixinsForMethods )
    {
        Set<Method> methods = model.getThisCompositeAsMethods();
        for( Method methodModel : methods )
        {
            if( mixinsForMethods.get( methodModel ).equals( otherModel ) )
            {
                return true;
            }
        }

        return false;
    }

    private List<CompositeMethodResolution> getMethodResolutions( CompositeModel compositeModel, ResolutionContext resolutionContext, Map<Method, MixinResolution> methodMixins, Collection<MixinResolution> mixinResolutions )
        throws ResolutionException
    {
        List<CompositeMethodResolution> methodResolutions = new ArrayList<CompositeMethodResolution>();
        Map<ConcernModel, ConcernResolution> concernResolutions = new HashMap<ConcernModel, ConcernResolution>();
        Map<SideEffectModel, SideEffectResolution> sideEffectResolutions = new HashMap<SideEffectModel, SideEffectResolution>();

        // Set up annotation -> constraintModel model mappings
        Iterable<ConstraintModel> constraintModels = compositeModel.getConstraintModels();
        Map<Class<? extends Annotation>, ConstraintModel> constraintModelMappings = new HashMap<Class<? extends Annotation>, ConstraintModel>();
        for( ConstraintModel constraintModel : constraintModels )
        {
            constraintModelMappings.put( constraintModel.getAnnotationType(), constraintModel );
        }

        Collection<CompositeMethodModel> methodModels = compositeModel.getCompositeMethodModels();
        resolveMethods( compositeModel, methodModels, methodMixins, resolutionContext, mixinResolutions, concernResolutions, sideEffectResolutions, constraintModelMappings, methodResolutions );

        Iterable<CompositeMethodModel> thisAsMethodModels = compositeModel.getThisCompositeAsModels();
        resolveMethods( compositeModel, thisAsMethodModels, methodMixins, resolutionContext, mixinResolutions, concernResolutions, sideEffectResolutions, constraintModelMappings, methodResolutions );

        return methodResolutions;
    }

    private void resolveMethods( CompositeModel compositeModel, Iterable<CompositeMethodModel> methodModels, Map<Method, MixinResolution> methodMixins, ResolutionContext resolutionContext, Collection<MixinResolution> mixinResolutions, Map<ConcernModel, ConcernResolution> concernResolutions, Map<SideEffectModel, SideEffectResolution> sideEffectResolutions, Map<Class<? extends Annotation>, ConstraintModel> constraintModelMappings, List<CompositeMethodResolution> methodResolutions )
    {
        for( CompositeMethodModel methodModel : methodModels )
        {
            MixinResolution mixinResolution = methodMixins.get( methodModel.getMethod() );

            try
            {
                Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();

                // Add interface annotations
                Annotation[] interfaceAnnotations = methodModel.getMethod().getDeclaringClass().getAnnotations();
                for( Annotation annotation : interfaceAnnotations )
                {
                    annotationMap.put( annotation.annotationType(), annotation );
                }

                // Add interface method annotations
                Annotation[] interfaceMethodAnnotations = methodModel.getMethod().getAnnotations();
                for( Annotation annotation : interfaceMethodAnnotations )
                {
                    annotationMap.put( annotation.annotationType(), annotation );
                }

                // Add mixin annotations
                Annotation[] mixinAnnotations = mixinResolution.getMixinModel().getModelClass().getAnnotations();
                for( Annotation annotation : mixinAnnotations )
                {
                    annotationMap.put( annotation.annotationType(), annotation );
                }

                // Add mixin method annotations
                if( methodModel.getMethod().getDeclaringClass().isAssignableFrom( mixinResolution.getMixinModel().getModelClass() ) )
                {
                    Method mixinMethod = mixinResolution.getMixinModel().getModelClass().getMethod( methodModel.getMethod().getName(), methodModel.getMethod().getParameterTypes() );
                    Annotation[] mixinMethodAnnotations = mixinMethod.getAnnotations();
                    for( Annotation annotation : mixinMethodAnnotations )
                    {
                        annotationMap.put( annotation.annotationType(), annotation );
                    }
                }

                AnnotatedElement annotatedElement = annotationMap.size() == 0 ? new EmptyAnnotatedElement() : new MapAnnotatedElement( annotationMap );

                // Find concerns for methodModel
                Iterable<ConcernModel> concerns = getConcernsForMethod( compositeModel, methodModel.getMethod(), annotatedElement, mixinResolution.getMixinModel(), mixinResolutions );

                // Resolve concerns
                List<ConcernResolution> methodConcernResolutions = new ArrayList<ConcernResolution>();
                for( ConcernModel concern : concerns )
                {
                    ConcernResolution resolution = concernResolutions.get( concern );
                    if( resolution == null )
                    {
                        try
                        {
                            resolution = resolveConcern( concern, resolutionContext );
                            concernResolutions.put( concern, resolution );
                        }
                        catch( InvalidInjectionException e )
                        {
                            throw new ResolutionException( "Could not resolve concern " + concern.getModelClass() + " in composite " + compositeModel.getCompositeType().getName(), e );
                        }
                    }
                    methodConcernResolutions.add( resolution );
                }

                // Find side-effects for methodModel
                Iterable<SideEffectModel> sideEffects = getSideEffectsForMethod( compositeModel, methodModel.getMethod(), annotatedElement, mixinResolution.getMixinModel(), mixinResolutions );

                // Resolve side-effects
                List<SideEffectResolution> methodSideEffectResolutions = new ArrayList<SideEffectResolution>();
                for( SideEffectModel sideEffect : sideEffects )
                {
                    SideEffectResolution resolution = sideEffectResolutions.get( sideEffect );
                    if( resolution == null )
                    {
                        try
                        {
                            resolution = resolveSideEffect( sideEffect, resolutionContext );
                            sideEffectResolutions.put( sideEffect, resolution );
                        }
                        catch( InvalidInjectionException e )
                        {
                            throw new ResolutionException( "Could not resolve side-effect " + sideEffect.getModelClass() + " in composite " + compositeModel.getCompositeType().getName(), e );
                        }
                    }
                    methodSideEffectResolutions.add( resolution );
                }

                // Resolve parameters
                Iterable<ParameterModel> parameterModels = methodModel.getParameterModels();
                List<ParameterResolution> parameterResolutions = new ArrayList<ParameterResolution>();
                for( ParameterModel parameterModel : parameterModels )
                {
                    ParameterConstraintsResolution parameterConstraintsResolution = null;
                    ParameterConstraintsModel parameterConstraintsModel = parameterModel.getParameterConstraintModel();
                    if( parameterConstraintsModel != null )
                    {
                        Iterable<Annotation> constraintAnnotations = parameterConstraintsModel.getConstraints();
                        List<ConstraintResolution> constraintResolutions = new ArrayList<ConstraintResolution>();
                        for( Annotation constraintAnnotation : constraintAnnotations )
                        {
                            addConstraintResolution( compositeModel, constraintAnnotation, parameterModel, constraintResolutions );
                        }
                        parameterConstraintsResolution = new ParameterConstraintsResolution( parameterConstraintsModel, constraintResolutions );
                    }
                    ParameterResolution parameterResolution = new ParameterResolution( parameterModel, parameterConstraintsResolution, null );
                    parameterResolutions.add( parameterResolution );
                }

                PropertyResolution propertyResolution = null;
                if( methodModel.getPropertyModel() != null )
                {
                    propertyResolution = new PropertyResolution( methodModel.getPropertyModel() );
                }

                AssociationResolution associationResolution = null;
                if( methodModel.getAssociationModel() != null )
                {
                    associationResolution = new AssociationResolution( methodModel.getAssociationModel() );
                }

                // Create aggregation of annotations (interface+mixin, mixin takes precedence)
                CompositeMethodResolution methodResolution = new CompositeMethodResolution( methodModel, parameterResolutions, methodConcernResolutions, methodSideEffectResolutions, mixinResolution, propertyResolution, annotatedElement, associationResolution );
                methodResolutions.add( methodResolution );
            }
            catch( NoSuchMethodException e )
            {
                throw new InvalidCompositeException( "Could not find mapped method in mixin", compositeModel.getCompositeType() );
            }

        }
    }

    private void addConstraintResolution( CompositeModel compositeModel, Annotation constraintAnnotation, ParameterModel parameterModel, List<ConstraintResolution> constraintResolutions )
    {
        ConstraintModel constraintModel = compositeModel.getConstraintModel( constraintAnnotation.annotationType(), parameterModel.getType() );
        if( constraintModel != null )
        {
            ConstraintResolution constraintResolution = new ConstraintResolution( constraintModel, constraintAnnotation );
            constraintResolutions.add( constraintResolution );
        }
        else
        {
            // Handle compound annotations, i.e. the constraint may have constraints declared on it which should be checked
            Annotation[] annotations = constraintAnnotation.annotationType().getAnnotations();
            int sizeBefore = constraintResolutions.size();
            for( Annotation annotation : annotations )
            {
                if( annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null )
                {
                    addConstraintResolution( compositeModel, annotation, parameterModel, constraintResolutions );
                }
            }

            if( sizeBefore == constraintResolutions.size() )
            {
                throw new ResolutionException( "No constraint implementation found for annotation @" + constraintAnnotation.annotationType().getSimpleName() );
            }
        }
    }

    private Map<Method, MixinModel> getMixinsForMethods( CompositeModel compositeModel )
        throws ResolutionException
    {
        Iterable<CompositeMethodModel> methodModels = compositeModel.getCompositeMethodModels();
        Map<Method, MixinModel> methodMixinMappings = new HashMap<Method, MixinModel>();

        // Map methods in composite type to mixins
        for( CompositeMethodModel methodModel : methodModels )
        {
            methodMixinMappings.put( methodModel.getMethod(), getMixinForMethod( methodModel.getMethod(), compositeModel, methodModel.getMethod() ) );
        }

        // Map methods in internal @ThisCompositeAs dependencies to mixins
        Iterable<CompositeMethodModel> thisAsMethodModels = compositeModel.getThisCompositeAsModels();
        for( CompositeMethodModel methodModel : thisAsMethodModels )
        {
            methodMixinMappings.put( methodModel.getMethod(), getMixinForMethod( methodModel.getMethod(), compositeModel, methodModel.getMethod() ) );
        }

        return methodMixinMappings;
    }

    private MixinModel getMixinForMethod( Method methodModel, CompositeModel compositeModel, AnnotatedElement annotatedElement )
        throws ResolutionException
    {
        Iterable<MixinModel> mixinModels = compositeModel.getMixinModels();

        // Check non-generic impls first
        // NOTE: a generic mixin may also be non-generic and implement a particular interface at the same time
        for( MixinModel implementation : mixinModels )
        {
            if( methodModel.getDeclaringClass().isAssignableFrom( implementation.getModelClass() ) && appliesTo( implementation, methodModel, annotatedElement, implementation, compositeModel.getCompositeType() ) )
            {
                return implementation;
            }
        }

        // Check generic impls
        for( MixinModel implementation : mixinModels )
        {
            if( implementation.isGeneric() && appliesTo( implementation, methodModel, annotatedElement, implementation, compositeModel.getCompositeType() ) )
            {
                return implementation;
            }
        }

        throw new ResolutionException( "Could not find mixin for method " + methodModel.toGenericString() + " in composite " + compositeModel.getCompositeType().getName() );
    }

    private Iterable<ConcernModel> getConcernsForMethod( CompositeModel compositeModel, Method method, AnnotatedElement annotatedElement, MixinModel mixinModel, Iterable<MixinResolution> usedMixins )
    {
        Class compositeClass = compositeModel.getCompositeType();

        List<ConcernModel> methodModifierModels = new ArrayList<ConcernModel>();

        // 1) Interface concerns
        addModifiers( compositeClass, method, annotatedElement, compositeModel.getConcernModels(), methodModifierModels, mixinModel );

        // 2) MixinModel concerns
        addModifiers( compositeClass, method, annotatedElement, mixinModel.getConcernModels(), methodModifierModels, mixinModel );

        // 3) Concerns from other mixins
        for( MixinResolution usedMixin : usedMixins )
        {
            if( usedMixin.getMixinModel() != mixinModel )
            {
                MixinModel model = (MixinModel) usedMixin.getFragmentModel();
                addModifiers( compositeClass, method, annotatedElement, model.getConcernModels(), methodModifierModels, mixinModel );
            }
        }

        return methodModifierModels;
    }

    private Iterable<SideEffectModel> getSideEffectsForMethod( CompositeModel compositeModel, Method method, AnnotatedElement annotatedElement, MixinModel mixinModel, Iterable<MixinResolution> usedMixins )
    {
        Class compositeClass = compositeModel.getCompositeType();

        List<SideEffectModel> methodModifierModels = new ArrayList<SideEffectModel>();

        // 1) Interface side-effects
        addModifiers( compositeClass, method, annotatedElement, compositeModel.getSideEffectModels(), methodModifierModels, mixinModel );

        // 2) MixinModel side-effects
        addModifiers( compositeClass, method, annotatedElement, mixinModel.getSideEffectModels(), methodModifierModels, mixinModel );

        // 3) Side-effects from other mixins
        for( MixinResolution usedMixin : usedMixins )
        {
            if( usedMixin.getMixinModel() != mixinModel )
            {
                MixinModel model = (MixinModel) usedMixin.getFragmentModel();
                addModifiers( compositeClass, method, annotatedElement, model.getSideEffectModels(), methodModifierModels, mixinModel );
            }
        }

        return methodModifierModels;
    }

    private <K extends ModifierModel> void addModifiers( Class compositeClass, Method method, AnnotatedElement annotatedElement, Iterable<K> possibleModifiers, List<K> aMethodModifierList, MixinModel mixinModel )
    {
        nextmodifier:
        for( K possibleModifier : possibleModifiers )
        {
            if( !aMethodModifierList.contains( possibleModifier ) && appliesTo( possibleModifier, method, annotatedElement, mixinModel, compositeClass ) )
            {
                // ModifierModel is ok!
                aMethodModifierList.add( possibleModifier );
            }
        }
    }

    private boolean appliesTo( FragmentModel fragmentModel, Method method, AnnotatedElement annotatedElement, MixinModel mixinModel, Class compositeClass )
    {
        Collection<Class> appliesToClasses = fragmentModel.getAppliesTo();

        boolean ok = appliesToClasses.isEmpty();
        for( Class appliesTo : appliesToClasses )
        {
            if( appliesToClass( appliesTo, mixinModel, method, annotatedElement, compositeClass, fragmentModel ) )
            {
                ok = true;
                break;
            }
        }

        // The fragment must implement the interface of the methodModel or be generic
        if( !ok )
        {
            return false;
        }

        if( fragmentModel.isAbstract() )
        {
            try
            {
                boolean methodNotAbstract = !Modifier.isAbstract( fragmentModel.getModelClass().getSuperclass().getMethod( method.getName(), method.getParameterTypes() ).getModifiers() );
                return methodNotAbstract;
            }
            catch( NoSuchMethodException e )
            {
                return false;
            }
        }
        else
        {
            return method.getDeclaringClass().isAssignableFrom( fragmentModel.getModelClass() ) || fragmentModel.isGeneric();
        }
    }

    private boolean appliesToClass( Class appliesTo, MixinModel mixinModel, Method method, AnnotatedElement annotatedElement, Class compositeClass, FragmentModel fragmentModel )
    {
        // Check AppliesTo
        if( appliesTo.isAnnotation() )
        {
            // Check if the mixin model somehow has this annotation (methodModel, mixin methodModel, mixin class, methodModel class)
            if( annotatedElement.getAnnotation( appliesTo ) == null )
            {
                return false;
            }
        }
        else
        {
            if( AppliesToFilter.class.isAssignableFrom( appliesTo ) )
            {
                // Instantiate filter
                try
                {
                    AppliesToFilter filter = (AppliesToFilter) appliesTo.newInstance();

                    // Must apply to this methodModel
                    if( !filter.appliesTo( method, mixinModel.getModelClass(), compositeClass, fragmentModel.getModelClass() ) )
                    {
                        return false;
                    }
                }
                catch( InstantiationException e )
                {
                    throw new ResolutionException( "Could not instantiate AppliesToFilter " + appliesTo.getName() );
                }
                catch( IllegalAccessException e )
                {
                    throw new ResolutionException( "Could not instantiate AppliesToFilter " + appliesTo.getName() );
                }

            }
            else
            {
                if( fragmentModel instanceof MixinModel )
                {
                    // Check if methodModel interface is assignable from the AppliesTo class
                    if( !appliesTo.isAssignableFrom( method.getDeclaringClass() ) )
                    {
                        return false;
                    }
                }
                else
                {
                    // Check if the mixin or methodModel interface is assignable from the AppliesTo class
                    if( !appliesTo.isAssignableFrom( method.getDeclaringClass() ) &&
                        !( mixinModel.isGeneric() || appliesTo.isAssignableFrom( mixinModel.getModelClass() ) ) )
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}