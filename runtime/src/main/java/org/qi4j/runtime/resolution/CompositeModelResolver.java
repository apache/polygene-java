package org.qi4j.runtime.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.Composite;
import org.qi4j.annotation.AppliesToFilter;
import org.qi4j.annotation.ConstraintDeclaration;
import org.qi4j.dependency.InvalidDependencyException;
import org.qi4j.model.CompositeModel;
import org.qi4j.model.ConcernModel;
import org.qi4j.model.ConstraintDeclarationModel;
import org.qi4j.model.FragmentModel;
import org.qi4j.model.InvalidCompositeException;
import org.qi4j.model.MethodConstraint;
import org.qi4j.model.MethodModel;
import org.qi4j.model.MixinModel;
import org.qi4j.model.ModifierModel;
import org.qi4j.model.ParameterConstraint;
import org.qi4j.model.SideEffectModel;

/**
 * TODO
 */
public class CompositeModelResolver
{
    ConstraintModelResolver ConstraintModelResolver;
    ConcernModelResolver concernModelResolver;
    SideEffectModelResolver sideEffectModelResolver;
    MixinModelResolver mixinModelResolver;

    public CompositeModelResolver( ConcernModelResolver concernModelResolver, SideEffectModelResolver sideEffectModelResolver, MixinModelResolver mixinModelResolver )
    {
        this.concernModelResolver = concernModelResolver;
        this.sideEffectModelResolver = sideEffectModelResolver;
        this.mixinModelResolver = mixinModelResolver;
    }

    public <T extends Composite> CompositeResolution<T> resolveCompositeModel( CompositeModel<T> compositeModel )
        throws CompositeResolutionException
    {
        // First figure out which interfaces map to which methods
        Map<Method, MixinModel> mixinsForMethods = getMixinsForMethods( compositeModel );

        // Determine list of mixin resolutions. Remove all duplicates
        Map<MixinModel, MixinResolution> resolvedMixins = getResolvedMixins( mixinsForMethods, compositeModel );

        // Get mixin resolutions for all methods
        Map<Method, MixinResolution> mixinResolutionsForMethods = getResolvedMethodMixins( mixinsForMethods, resolvedMixins );

        // Map concerns and side-effects to methods
        List<MethodResolution> methodResolutions = getMethodResolutions( compositeModel, mixinResolutionsForMethods, resolvedMixins.values() );

        CompositeResolution resolution = new CompositeResolution<T>( compositeModel, methodResolutions );
        return resolution;
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

    private <T extends Composite> Map<MixinModel, MixinResolution> getResolvedMixins( Map<Method, MixinModel> mixinsForMethods, CompositeModel<T> compositeModel )
    {
        // Build set of used mixins
        Set<MixinModel> usedMixins = new LinkedHashSet<MixinModel>();
        usedMixins.addAll( mixinsForMethods.values() );

        // If a mixin A uses mixin B as a @ThisCompositeAs parameter dependency, then ensure that the order is correct.
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
                        throw new InvalidCompositeException( "Cyclic dependency between mixins " + model.getModelClass().getName() + " and " + otherMixinModel.getModelClass().getName(), compositeModel.getCompositeClass() );
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
        Map<MixinModel, MixinResolution> mixinResolutions = new HashMap<MixinModel, MixinResolution>();
        for( MixinModel orderedUsedMixin : orderedUsedMixins )
        {
            try
            {
                MixinResolution mixinResolution = mixinModelResolver.resolveModel( orderedUsedMixin );
                mixinResolutions.put( orderedUsedMixin, mixinResolution );
            }
            catch( InvalidDependencyException e )
            {
                throw new CompositeResolutionException( "Could not resolve mixin " + orderedUsedMixin.getModelClass().getName(), e );
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

    private List<MethodResolution> getMethodResolutions( CompositeModel compositeModel, Map<Method, MixinResolution> methodMixins, Collection<MixinResolution> mixinResolutions )
        throws CompositeResolutionException
    {
        List<MethodResolution> methodResolutions = new ArrayList<MethodResolution>();
        Map<ConcernModel, ConcernResolution> concernResolutions = new HashMap<ConcernModel, ConcernResolution>();
        Map<SideEffectModel, SideEffectResolution> sideEffectResolutions = new HashMap<SideEffectModel, SideEffectResolution>();

        // Set up annotation -> constraint model mappings
        Iterable<ConstraintDeclarationModel> constraintModels = compositeModel.getConstraintModels();
        Map<Class<? extends Annotation>, ConstraintDeclarationModel> constraintModelMappings = new HashMap<Class<? extends Annotation>, ConstraintDeclarationModel>();
        for( ConstraintDeclarationModel constraintModel : constraintModels )
        {
            constraintModelMappings.put( constraintModel.getAnnotationType(), constraintModel );
        }

        Collection<MethodModel> methodModels = compositeModel.getMethodModels();
        resolveMethods( methodModels, methodMixins, compositeModel, mixinResolutions, concernResolutions, sideEffectResolutions, constraintModelMappings, methodResolutions );

        Iterable<MethodModel> thisAsMethodModels = compositeModel.getThisCompositeAsModels();
        resolveMethods( thisAsMethodModels, methodMixins, compositeModel, mixinResolutions, concernResolutions, sideEffectResolutions, constraintModelMappings, methodResolutions );

        return methodResolutions;
    }

    private void resolveMethods( Iterable<MethodModel> methodModels, Map<Method, MixinResolution> methodMixins, CompositeModel compositeModel, Collection<MixinResolution> mixinResolutions, Map<ConcernModel, ConcernResolution> concernResolutions, Map<SideEffectModel, SideEffectResolution> sideEffectResolutions, Map<Class<? extends Annotation>, ConstraintDeclarationModel> constraintModelMappings, List<MethodResolution> methodResolutions )
    {
        for( MethodModel methodModel : methodModels )
        {
            // Find concerns for method
            MixinResolution mixinResolution = methodMixins.get( methodModel.getMethod() );
            Iterable<ConcernModel> concerns = getConcernsForMethod( compositeModel, methodModel.getMethod(), mixinResolution.getMixinModel(), mixinResolutions );

            // Resolve concerns
            List<ConcernResolution> methodConcernResolutions = new ArrayList<ConcernResolution>();
            for( ConcernModel concern : concerns )
            {
                ConcernResolution resolution = concernResolutions.get( concern );
                if( resolution == null )
                {
                    try
                    {
                        resolution = concernModelResolver.resolveModel( concern );
                        concernResolutions.put( concern, resolution );
                    }
                    catch( InvalidDependencyException e )
                    {
                        throw new CompositeResolutionException( "Could not resolve concern " + concern.getModelClass() + " in composite " + compositeModel.getCompositeClass().getName(), e );
                    }
                }
                methodConcernResolutions.add( resolution );
            }

            // Find side-effects for method
            Iterable<SideEffectModel> sideEffects = getSideEffectsForMethod( compositeModel, methodModel.getMethod(), mixinResolution.getMixinModel(), mixinResolutions );

            // Resolve side-effects
            List<SideEffectResolution> methodSideEffectResolutions = new ArrayList<SideEffectResolution>();
            for( SideEffectModel sideEffect : sideEffects )
            {
                SideEffectResolution resolution = sideEffectResolutions.get( sideEffect );
                if( resolution == null )
                {
                    try
                    {
                        resolution = sideEffectModelResolver.resolveModel( sideEffect );
                        sideEffectResolutions.put( sideEffect, resolution );
                    }
                    catch( InvalidDependencyException e )
                    {
                        throw new CompositeResolutionException( "Could not resolve side-effect " + sideEffect.getModelClass() + " in composite " + compositeModel.getCompositeClass().getName(), e );
                    }
                }
                methodSideEffectResolutions.add( resolution );
            }

            MethodConstraintResolution methodConstraintResolution = resolveMethodConstraints( compositeModel.getCompositeClass(), methodModel.getMethodConstraint(), constraintModelMappings );

            MethodResolution methodResolution = new MethodResolution( methodModel, methodConstraintResolution, methodConcernResolutions, methodSideEffectResolutions, mixinResolution );
            methodResolutions.add( methodResolution );
        }
    }

    private MethodConstraintResolution resolveMethodConstraints( Class compositeClass, MethodConstraint methodConstraint, Map<Class<? extends Annotation>, ConstraintDeclarationModel> constraintModelMappings )
    {
        Iterable<ParameterConstraint> parameterConstraints = methodConstraint.getParameterConstraints();
        List<ParameterConstraintResolution> parameterConstraintResolutions = new ArrayList<ParameterConstraintResolution>();
        for( ParameterConstraint parameterConstraint : parameterConstraints )
        {
            Iterable<Annotation> constraints = parameterConstraint.getConstraints();
            Set<ConstraintResolution> declarationModels = new LinkedHashSet<ConstraintResolution>();
            addConstraintDeclarations( constraints, constraintModelMappings, compositeClass, declarationModels );
            parameterConstraintResolutions.add( new ParameterConstraintResolution( parameterConstraint, declarationModels ) );
        }
        return new MethodConstraintResolution( methodConstraint, parameterConstraintResolutions );
    }

    private void addConstraintDeclarations( Iterable<Annotation> constraints, Map<Class<? extends Annotation>, ConstraintDeclarationModel> constraintModelMappings, Class compositeClass, Set<ConstraintResolution> constraintResolutions )
    {
        for( Annotation constraint : constraints )
        {
            Class annotationType = constraint.annotationType();
            if( isConstraintDeclaration( annotationType ) )
            {
                // Match annotation with constraint declarations
                ConstraintDeclarationModel constraintDeclaration = constraintModelMappings.get( constraint.annotationType() );
                if( constraintDeclaration == null )
                {
                    // No declaration found, but it's a composite constraint so try subconstraints
                    Annotation[] annotations = annotationType.getAnnotations();
                    List<Annotation> constraintAnnotations = new ArrayList<Annotation>();
                    for( Annotation annotation : annotations )
                    {
                        if( isConstraintDeclaration( annotation.annotationType() ) )
                        {
                            constraintAnnotations.add( annotation );
                        }
                    }

                    if( constraintAnnotations.size() > 0 )
                    {
                        addConstraintDeclarations( Arrays.asList( annotations ), constraintModelMappings, compositeClass, constraintResolutions );
                    }
                    else
                    {
                        throw new InvalidCompositeException( "No constraint implementation found for constraint annotation " + constraint.annotationType().getName(), compositeClass );
                    }
                }
                else
                {
                    constraintResolutions.add( new ConstraintResolution( constraintDeclaration, constraint ) );
                }
            }
        }
    }

    private boolean isConstraintDeclaration( Class annotationType )
    {
        return annotationType.getAnnotation( ConstraintDeclaration.class ) != null;
    }

    private Map<Method, MixinModel> getMixinsForMethods( CompositeModel compositeModel )
        throws CompositeResolutionException
    {
        Iterable<MethodModel> methodModels = compositeModel.getMethodModels();
        Map<Method, MixinModel> methodMixinMappings = new HashMap<Method, MixinModel>();

        // Map methods in composite type to mixins
        for( MethodModel methodModel : methodModels )
        {
            methodMixinMappings.put( methodModel.getMethod(), getMixinForMethod( methodModel.getMethod(), compositeModel ) );
        }

        // Map methods in internal @ThisCompositeAs dependencies to mixins
        Iterable<MethodModel> thisAsMethodModels = compositeModel.getThisCompositeAsModels();
        for( MethodModel methodModel : thisAsMethodModels )
        {
            methodMixinMappings.put( methodModel.getMethod(), getMixinForMethod( methodModel.getMethod(), compositeModel ) );
        }

        return methodMixinMappings;
    }

    private MixinModel getMixinForMethod( Method methodModel, CompositeModel compositeModel )
        throws CompositeResolutionException
    {
        Iterable<MixinModel> mixinModels = compositeModel.getMixinModels();

        // Check non-generic impls first
        // NOTE: a generic mixin may also be non-generic and implement a particular interface at the same time
        for( MixinModel implementation : mixinModels )
        {
            if( ( !implementation.isGeneric() || methodModel.getDeclaringClass().isAssignableFrom( implementation.getModelClass() ) ) && appliesTo( implementation, methodModel, implementation, compositeModel.getCompositeClass() ) )
            {
                return implementation;
            }
        }

        // Check generic impls
        for( MixinModel implementation : mixinModels )
        {
            if( implementation.isGeneric() && appliesTo( implementation, methodModel, implementation, compositeModel.getCompositeClass() ) )
            {
                return implementation;
            }
        }

        throw new CompositeResolutionException( "Could not find mixin for method " + methodModel.toGenericString() );
    }

    private Iterable<ConcernModel> getConcernsForMethod( CompositeModel compositeModel, Method method, MixinModel mixinModel, Iterable<MixinResolution> usedMixins )
    {
        Class compositeClass = compositeModel.getCompositeClass();

        List<ConcernModel> methodModifierModels = new ArrayList<ConcernModel>();

        // 1) Interface concerns
        addModifiers( compositeClass, method, compositeModel.getConcernModels(), methodModifierModels, mixinModel );

        // 2) MixinModel concerns
        addModifiers( compositeClass, method, mixinModel.getConcerns(), methodModifierModels, mixinModel );

        // 3) Concerns from other mixins
        for( MixinResolution usedMixin : usedMixins )
        {
            if( usedMixin.getMixinModel() != mixinModel )
            {
                MixinModel model = (MixinModel) usedMixin.getFragmentModel();
                addModifiers( compositeClass, method, model.getConcerns(), methodModifierModels, mixinModel );
            }
        }

        return methodModifierModels;
    }

    private Iterable<SideEffectModel> getSideEffectsForMethod( CompositeModel compositeModel, Method method, MixinModel mixinModel, Iterable<MixinResolution> usedMixins )
    {
        Class compositeClass = compositeModel.getCompositeClass();

        List<SideEffectModel> methodModifierModels = new ArrayList<SideEffectModel>();

        // 1) Interface side-effects
        addModifiers( compositeClass, method, compositeModel.getSideEffectModels(), methodModifierModels, mixinModel );

        // 2) MixinModel side-effects
        addModifiers( compositeClass, method, mixinModel.getSideEffects(), methodModifierModels, mixinModel );

        // 3) Side-effects from other mixins
        for( MixinResolution usedMixin : usedMixins )
        {
            if( usedMixin.getMixinModel() != mixinModel )
            {
                MixinModel model = (MixinModel) usedMixin.getFragmentModel();
                addModifiers( compositeClass, method, model.getSideEffects(), methodModifierModels, mixinModel );
            }
        }

        return methodModifierModels;
    }

    private <K extends ModifierModel> void addModifiers( Class compositeClass, Method method, Iterable<K> possibleModifiers, List<K> aMethodModifierList, MixinModel mixinModel )
    {
        nextmodifier:
        for( K possibleModifier : possibleModifiers )
        {
            if( !aMethodModifierList.contains( possibleModifier ) && appliesTo( possibleModifier, method, mixinModel, compositeClass ) )
            {
                // ModifierModel is ok!
                aMethodModifierList.add( possibleModifier );
            }
        }
    }

    private boolean appliesTo( FragmentModel fragmentModel, Method method, MixinModel mixinModel, Class compositeClass )
    {
        Collection<Class> appliesToClasses = fragmentModel.getAppliesTo();

        boolean ok = appliesToClasses.isEmpty();
        for( Class appliesTo : appliesToClasses )
        {
            if( appliesToClass( appliesTo, mixinModel, method, compositeClass, fragmentModel ) )
            {
                ok = true;
                break;
            }
        }

        // The fragment must implement the interface of the method or be generic
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

    private boolean appliesToClass( Class appliesTo, MixinModel mixinModel, Method method, Class compositeClass, FragmentModel fragmentModel )
    {
        // Check AppliesTo
        if( appliesTo.isAnnotation() )
        {
            // Check if the mixin model somehow has this annotation (method, mixin method, mixin class, method class)
            if( mixinModel.getAnnotation( appliesTo, method ) == null )
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

                    // Must apply to this method
                    if( !filter.appliesTo( method, mixinModel.getModelClass(), compositeClass, fragmentModel.getModelClass() ) )
                    {
                        return false;
                    }
                }
                catch( InstantiationException e )
                {
                    throw new CompositeResolutionException( "Could not instantiate AppliesToFilter " + appliesTo.getName() );
                }
                catch( IllegalAccessException e )
                {
                    throw new CompositeResolutionException( "Could not instantiate AppliesToFilter " + appliesTo.getName() );
                }

            }
            else
            {
                if( fragmentModel instanceof MixinModel )
                {
                    // Check if method interface is assignable from the AppliesTo class
                    if( !appliesTo.isAssignableFrom( method.getDeclaringClass() ) )
                    {
                        return false;
                    }
                }
                else
                {
                    // Check if the mixin or method interface is assignable from the AppliesTo class
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