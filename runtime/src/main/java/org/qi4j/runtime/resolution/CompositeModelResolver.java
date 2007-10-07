package org.qi4j.runtime.resolution;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.scope.ThisAs;
import org.qi4j.api.model.AssertionModel;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.DependencyKey;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodModel;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.ParameterDependency;
import org.qi4j.api.model.SideEffectModel;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class CompositeModelResolver
{
    AssertionModelResolver assertionModelResolver;
    SideEffectModelResolver sideEffectModelResolver;
    MixinModelResolver mixinModelResolver;

    public CompositeModelResolver( AssertionModelResolver assertionModelResolver, SideEffectModelResolver sideEffectModelResolver, MixinModelResolver mixinModelResolver )
    {
        this.assertionModelResolver = assertionModelResolver;
        this.sideEffectModelResolver = sideEffectModelResolver;
        this.mixinModelResolver = mixinModelResolver;
    }

    public <T extends Composite> CompositeResolution<T> resolveCompositeModel( CompositeModel<T> compositeModel )
        throws CompositeResolutionException
    {
        // First figure out which interface map to which mixins
        Map<Class, MixinResolution> mixinsForInterfaces = mapInterfacesToMixins( compositeModel );

        // Determine list of mixin resolutions. Remove all duplicates
        Set<MixinResolution> usedMixinModels = getUsedMixins( mixinsForInterfaces, compositeModel );

        // Map assertions to methods
        List<MethodResolution> methodResolutions = getMethodResolutions( compositeModel, mixinsForInterfaces, usedMixinModels );

        CompositeResolution resolution = new CompositeResolution<T>( compositeModel, usedMixinModels, mixinsForInterfaces, methodResolutions );
        return resolution;
    }

    private <T extends Composite> Set<MixinResolution> getUsedMixins( Map<Class, MixinResolution> mixinsForInterfaces, CompositeModel<T> compositeModel )
    {
        Set<MixinResolution> usedMixins = new LinkedHashSet<MixinResolution>();

        // Build set of used mixins
        usedMixins.addAll( mixinsForInterfaces.values() );

        // If a mixin A uses mixin B as a @ThisAs parameter dependency, then ensure that the order is correct.
        Set<MixinResolution> orderedUsedMixins = new LinkedHashSet<MixinResolution>();
        dependencyCheck:
        while( !usedMixins.isEmpty() )
        {
            Iterator<MixinResolution> iterator = usedMixins.iterator();
            MixinResolution resolution = iterator.next();

            // Check if any of the other resolutions depends on this one
            while( iterator.hasNext() )
            {
                MixinResolution otherResolution = iterator.next();
                if( dependsOn( resolution, otherResolution, mixinsForInterfaces ) )
                {
                    // Check cyclic dependency
                    if( dependsOn( otherResolution, resolution, mixinsForInterfaces ) )
                    {
                        throw new InvalidCompositeException( "Cyclic dependency between mixins " + resolution.getMixinModel().getModelClass().getName() + " and " + otherResolution.getMixinModel().getModelClass().getName(), compositeModel.getCompositeClass() );
                    }
                    else
                    {
                        // Move to last
                        usedMixins.remove( resolution );
                        usedMixins.add( resolution );
                        continue dependencyCheck;
                    }
                }
            }

            // No dependencies from this resolution to the remaining resolutions
            usedMixins.remove( resolution );
            orderedUsedMixins.add( resolution );
        }


        return orderedUsedMixins;
    }

    private boolean dependsOn( MixinResolution resolution, MixinResolution otherResolution, Map<Class, MixinResolution> mixinsForInterfaces )
    {
        Iterable<ConstructorDependencyResolution> constructorDependencies = resolution.getConstructorResolutions();
        for( ConstructorDependencyResolution constructorDependency : constructorDependencies )
        {
            for( ParameterDependency parameterDependency : constructorDependency.getConstructorDependency().getParameterDependencies() )
            {
                DependencyKey key = parameterDependency.getKey();
                if( key.getAnnotationType().equals( ThisAs.class ) )
                {
                    MixinResolution dependencyResolution = mixinsForInterfaces.get( key.getDependencyType() );
                    if( dependencyResolution == otherResolution )
                    {
                        return true;
                    }
                }
            }
        }

        Iterable<MethodDependencyResolution> methodDependencies = resolution.getMethodResolutions();
        for( MethodDependencyResolution methodDependency : methodDependencies )
        {
            for( ParameterDependency parameterDependency : methodDependency.getMethodDependency().getParameterDependencies() )
            {
                DependencyKey key = parameterDependency.getKey();
                if( key.getAnnotationType().equals( ThisAs.class ) )
                {
                    MixinResolution dependencyResolution = mixinsForInterfaces.get( key.getDependencyType() );
                    if( dependencyResolution == otherResolution )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private List<MethodResolution> getMethodResolutions( CompositeModel compositeModel, Map<Class, MixinResolution> mixinMappings, Iterable<MixinResolution> usedMixins )
        throws CompositeResolutionException
    {
        List<MethodResolution> methodResolutions = new ArrayList<MethodResolution>();

        Iterable<MethodModel> methodModels = compositeModel.getMethodModels();
        for( MethodModel methodModel : methodModels )
        {
            MixinResolution mixinResolution = mixinMappings.get( methodModel.getMethod().getDeclaringClass() );

            // Find assertions for method
            Iterable<AssertionModel> assertions = getAssertionsForMethod( compositeModel, methodModel.getMethod(), mixinResolution, usedMixins );

            // Resolve assertions
            List<AssertionResolution> assertionResolutions = new ArrayList<AssertionResolution>();
            for( AssertionModel assertion : assertions )
            {
                try
                {
                    AssertionResolution resolution = assertionModelResolver.resolveModel( assertion );
                    assertionResolutions.add( resolution );
                }
                catch( InvalidDependencyException e )
                {
                    throw new CompositeResolutionException( "Could not resolve assertion " + assertion.getModelClass() + " in composite " + compositeModel.getCompositeClass().getName(), e );
                }
            }

            // Find side-effects for method
            Iterable<SideEffectModel> sideEffects = getSideEffectsForMethod( compositeModel, methodModel.getMethod(), mixinResolution, usedMixins );

            // Resolve assertions
            List<SideEffectResolution> sideEffectResolutions = new ArrayList<SideEffectResolution>();
            for( SideEffectModel modifier : sideEffects )
            {
                try
                {
                    SideEffectResolution resolution = sideEffectModelResolver.resolveModel( modifier );
                    sideEffectResolutions.add( resolution );
                }
                catch( InvalidDependencyException e )
                {
                    throw new CompositeResolutionException( "Could not resolve modifier " + modifier.getModelClass() + " in composite " + compositeModel.getCompositeClass().getName(), e );
                }
            }

            MethodResolution methodResolution = new MethodResolution( methodModel, assertionResolutions, sideEffectResolutions, mixinResolution );
            methodResolutions.add( methodResolution );
        }

        return methodResolutions;
    }

    private Map<Class, MixinResolution> mapInterfacesToMixins( CompositeModel compositeModel )
        throws CompositeResolutionException
    {
        Method[] methods = compositeModel.getCompositeClass().getMethods();
        Map<Class, MixinResolution> mixinMappings = new HashMap<Class, MixinResolution>();
        Map<MixinModel, MixinResolution> mixinResolutions = new HashMap<MixinModel, MixinResolution>();
        for( Method method : methods )
        {
            // Find mixin
            Class<?> mixinType = method.getDeclaringClass();
            if( mixinMappings.get( mixinType ) == null )
            {
                MixinResolution mixinResolution = mapInterfaceToMixin( mixinType, compositeModel.getMixinModels(), mixinResolutions );
                mixinMappings.put( mixinType, mixinResolution );
            }
        }
        return mixinMappings;
    }

    private MixinResolution mapInterfaceToMixin( Class mixinType, Iterable<MixinModel> mixins, Map<MixinModel, MixinResolution> mixinResolutions )
        throws CompositeResolutionException
    {
        MixinResolution mixinResolution = null;

        List<MixinModel> possibleMixinModels = getImplementations( mixinType, mixins );

        InvalidDependencyException ex = null;
        for( MixinModel possibleMixinModel : possibleMixinModels )
        {
            // Check if already resolved
            mixinResolution = mixinResolutions.get( possibleMixinModel );
            if( mixinResolution != null )
            {
                return mixinResolution;
            }

            // Check if this mixinModel can be resolved
            try
            {
                mixinResolution = mixinModelResolver.resolveModel( possibleMixinModel );
                mixinResolutions.put( possibleMixinModel, mixinResolution );

                return mixinResolution;
            }
            catch( InvalidDependencyException e )
            {
                ex = e;
            }
        }

        if( ex != null )
        {
            throw new CompositeResolutionException( "Could not resolve composite", ex );
        }
        else
        {
            throw new CompositeResolutionException( "Could not resolve mixin for interface " + mixinType );
        }
    }

    private List<MixinModel> getImplementations( Class aType, Iterable<MixinModel> mixinModels )
    {
        List<MixinModel> impls = new ArrayList<MixinModel>();

        // Check non-generic impls first
        for( MixinModel implementation : mixinModels )
        {
            if( !implementation.isGeneric() )
            {
                Class fragmentClass = implementation.getModelClass();
                if( aType.isAssignableFrom( fragmentClass ) )
                {
                    impls.add( implementation );
                }
            }
        }

        // Check generic impls
        for( MixinModel implementation : mixinModels )
        {
            if( implementation.isGeneric() )
            {
                // Check AppliesTo
                Class appliesTo = implementation.getAppliesTo();
                if( appliesTo == null || appliesTo.isAssignableFrom( aType ) )
                {
                    impls.add( implementation ); // This generic mixin can handle the given type
                }
            }
        }

        return impls;
    }

    private Iterable<AssertionModel> getAssertionsForMethod( CompositeModel compositeModel, Method method, MixinResolution mixinResolution, Iterable<MixinResolution> usedMixins )
    {
        Class compositeClass = compositeModel.getCompositeClass();

        List<AssertionModel> methodModifierModels = new ArrayList<AssertionModel>();

        // 1) Interface assertions
        addModifiers( compositeClass, method, compositeModel.getAssertionModels(), methodModifierModels, mixinResolution );

        // 2) MixinModel assertions
        MixinModel mixinModel = (MixinModel) mixinResolution.getFragmentModel();
        addModifiers( compositeClass, method, mixinModel.getAssertions(), methodModifierModels, mixinResolution );

        // 3) Assertions from other mixins
        for( MixinResolution usedMixin : usedMixins )
        {
            if( usedMixin != mixinResolution )
            {
                MixinModel model = (MixinModel) usedMixin.getFragmentModel();
                addModifiers( compositeClass, method, model.getAssertions(), methodModifierModels, usedMixin );
            }
        }

        return methodModifierModels;
    }

    private Iterable<SideEffectModel> getSideEffectsForMethod( CompositeModel compositeModel, Method method, MixinResolution mixinResolution, Iterable<MixinResolution> usedMixins )
    {
        Class compositeClass = compositeModel.getCompositeClass();

        List<SideEffectModel> methodModifierModels = new ArrayList<SideEffectModel>();

        // 1) Interface side-effects
        addModifiers( compositeClass, method, compositeModel.getSideEffectModels(), methodModifierModels, mixinResolution );

        // 2) MixinModel side-effects
        MixinModel mixinModel = (MixinModel) mixinResolution.getFragmentModel();
        addModifiers( compositeClass, method, mixinModel.getSideEffects(), methodModifierModels, mixinResolution );

        // 3) Side-effects from other mixins
        for( MixinResolution usedMixin : usedMixins )
        {
            if( usedMixin != mixinResolution )
            {
                MixinModel model = (MixinModel) usedMixin.getFragmentModel();
                addModifiers( compositeClass, method, model.getSideEffects(), methodModifierModels, usedMixin );
            }
        }

        return methodModifierModels;
    }

    private <K extends ModifierModel> void addModifiers( Class compositeClass, Method method, Iterable<K> aModifierList, List<K> aMethodModifierList, MixinResolution mixinResolution )
    {
        nextmodifier:
        for( K modifierModel : aModifierList )
        {
            if( !aMethodModifierList.contains( modifierModel ) && appliesTo( modifierModel, mixinResolution, method, compositeClass ) )
            {
                // Check interface
                if( !modifierModel.isGeneric() )
                {
                    if( !method.getDeclaringClass().isAssignableFrom( modifierModel.getModelClass() ) )
                    {
                        continue; // ModifierModel does not implement interface of this method
                    }
                }

                // ModifierModel is ok!
                aMethodModifierList.add( modifierModel );
            }
        }
    }

    private boolean appliesTo( ModifierModel modifierModel, MixinResolution mixinResolution, Method method, Class compositeClass )
    {
        // Check AppliesTo
        Class appliesTo = modifierModel.getAppliesTo();
        if( appliesTo != null )
        {
            // Check AppliesTo
            if( appliesTo.isAnnotation() )
            {
                MixinModel mixinModel = (MixinModel) mixinResolution.getFragmentModel();

                // Check the Mixin implementation class to see if it is annotated.
                if( mixinModel.getModelClass().getAnnotation( appliesTo ) == null )
                {
                    // The Mixin Implementation class was not annotated with the @AppliesTo.

                    // Check method
                    if( !mixinModel.isGeneric() )
                    {
                        try
                        {
                            Method implMethod = mixinModel.getModelClass().getMethod( method.getName(), method.getParameterTypes() );
                            if( implMethod.getAnnotation( appliesTo ) == null )
                            {
                                return false;
                            }
                        }
                        catch( NoSuchMethodException e )
                        {
                            return false;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            else
            {
                Class<?> methodDeclaringClass = method.getDeclaringClass();
                MixinModel mixin = (MixinModel) mixinResolution.getFragmentModel();
                if( mixin == null )
                {
                    throw new InvalidCompositeException( methodDeclaringClass + " has no implementation.", compositeClass );
                }
                Class fragmentClass = mixin.getModelClass();
                if( !appliesTo.isAssignableFrom( fragmentClass ) && !appliesTo.isAssignableFrom( methodDeclaringClass ) )
                {
                    return false;
                }
            }
        }
        return true;
    }
}