package org.qi4j.runtime.resolution;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.annotation.AppliesToFilter;
import org.qi4j.api.model.AssertionModel;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.FragmentModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MethodModel;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
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
        // First figure out which interfaces map to which methods
        Map<MethodModel, MixinModel> mixinsForMethods = getMixinsForMethods( compositeModel );

        // Determine list of mixin resolutions. Remove all duplicates
        Map<MixinModel, MixinResolution> resolvedMixins = getResolvedMixins( mixinsForMethods, compositeModel );

        // Get mixin resolutions for all methods
        Map<MethodModel, MixinResolution> mixinResolutionsForMethods = getResolvedMethodMixins( mixinsForMethods, resolvedMixins );

        // Map assertions and side-effects to methods
        List<MethodResolution> methodResolutions = getMethodResolutions( compositeModel, mixinResolutionsForMethods, resolvedMixins.values() );

        CompositeResolution resolution = new CompositeResolution<T>( compositeModel, methodResolutions );
        return resolution;
    }

    private Map<MethodModel, MixinResolution> getResolvedMethodMixins( Map<MethodModel, MixinModel> mixinsForMethods, Map<MixinModel, MixinResolution> resolvedMixins )
    {
        Map<MethodModel, MixinResolution> resolvedMethodMixins = new HashMap<MethodModel, MixinResolution>();
        for( Map.Entry<MethodModel, MixinModel> entry : mixinsForMethods.entrySet() )
        {
            resolvedMethodMixins.put( entry.getKey(), resolvedMixins.get( entry.getValue() ) );
        }
        return resolvedMethodMixins;
    }

    private <T extends Composite> Map<MixinModel, MixinResolution> getResolvedMixins( Map<MethodModel, MixinModel> mixinsForMethods, CompositeModel<T> compositeModel )
    {
        // Build set of used mixins
        Set<MixinModel> usedMixins = new LinkedHashSet<MixinModel>();
        usedMixins.addAll( mixinsForMethods.values() );

        // If a mixin A uses mixin B as a @ThisAs parameter dependency, then ensure that the order is correct.
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

    private boolean dependsOn( MixinModel model, MixinModel otherModel, Map<MethodModel, MixinModel> mixinsForMethods )
    {
        Set<MethodModel> methods = model.getThisAsMethods();
        for( MethodModel methodModel : methods )
        {
            if( mixinsForMethods.get( methodModel ).equals( otherModel ) )
            {
                return true;
            }
        }

        return false;
    }

    private List<MethodResolution> getMethodResolutions( CompositeModel compositeModel, Map<MethodModel, MixinResolution> methodMixins, Collection<MixinResolution> mixinResolutions )
        throws CompositeResolutionException
    {
        List<MethodResolution> methodResolutions = new ArrayList<MethodResolution>();
        Map<AssertionModel, AssertionResolution> assertionResolutions = new HashMap<AssertionModel, AssertionResolution>();
        Map<SideEffectModel, SideEffectResolution> sideEffectResolutions = new HashMap<SideEffectModel, SideEffectResolution>();

        for( Map.Entry<MethodModel, MixinResolution> entry : methodMixins.entrySet() )
        {
            // Find assertions for method
            Iterable<AssertionModel> assertions = getAssertionsForMethod( compositeModel, entry.getKey().getMethod(), entry.getValue().getMixinModel(), mixinResolutions );

            // Resolve assertions
            List<AssertionResolution> methodAssertionResolutions = new ArrayList<AssertionResolution>();
            for( AssertionModel assertion : assertions )
            {
                AssertionResolution resolution = assertionResolutions.get( assertion );
                if( resolution == null )
                {
                    try
                    {
                        resolution = assertionModelResolver.resolveModel( assertion );
                        assertionResolutions.put( assertion, resolution );
                    }
                    catch( InvalidDependencyException e )
                    {
                        throw new CompositeResolutionException( "Could not resolve assertion " + assertion.getModelClass() + " in composite " + compositeModel.getCompositeClass().getName(), e );
                    }
                }
                methodAssertionResolutions.add( resolution );
            }

            // Find side-effects for method
            Iterable<SideEffectModel> sideEffects = getSideEffectsForMethod( compositeModel, entry.getKey().getMethod(), entry.getValue().getMixinModel(), mixinResolutions );

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

            MethodResolution methodResolution = new MethodResolution( entry.getKey(), methodAssertionResolutions, methodSideEffectResolutions, methodMixins.get( entry.getKey() ) );
            methodResolutions.add( methodResolution );
        }

        return methodResolutions;
    }

    private Map<MethodModel, MixinModel> getMixinsForMethods( CompositeModel compositeModel )
        throws CompositeResolutionException
    {
        Iterable<MethodModel> methodModels = compositeModel.getMethodModels();
        Map<MethodModel, MixinModel> methodMixinMappings = new HashMap<MethodModel, MixinModel>();

        // Map methods in composite type to mixins
        for( MethodModel methodModel : methodModels )
        {
            methodMixinMappings.put( methodModel, getMixinForMethod( methodModel, compositeModel ) );
        }

        // Map methods in internal @ThisAs dependencies to mixins
        Iterable<MethodModel> thisAsMethodModels = compositeModel.getThisAsModels();
        for( MethodModel methodModel : thisAsMethodModels )
        {
            methodMixinMappings.put( methodModel, getMixinForMethod( methodModel, compositeModel ) );
        }

        return methodMixinMappings;
    }

    private void resolveMethodMixin( MethodModel methodModel, CompositeModel compositeModel, Map<MixinModel, MixinResolution> mixinResolutions, Map<MethodModel, MixinResolution> methodMixinMappings )
    {
        // Find mixin for method
        MixinModel mixinModel = getMixinForMethod( methodModel, compositeModel );

        // Resolve it
        MixinResolution mixinResolution = mixinResolutions.get( mixinModel );
        if( mixinResolution == null )
        {
            try
            {
                mixinResolution = mixinModelResolver.resolveModel( mixinModel );
                mixinResolutions.put( mixinModel, mixinResolution );
            }
            catch( InvalidDependencyException e )
            {
                throw new CompositeResolutionException( "Could not resolve mixin " + mixinModel.getModelClass().getName() + " for method " + methodModel.getMethod().toGenericString(), e );
            }
        }

        methodMixinMappings.put( methodModel, mixinResolution );
    }

    private MixinModel getMixinForMethod( MethodModel methodModel, CompositeModel compositeModel )
        throws CompositeResolutionException
    {
        Iterable<MixinModel> mixinModels = compositeModel.getMixinModels();

        // Check non-generic impls first
        // NOTE: a generic mixin may also be non-generic and implement a particular interface at the same time
        for( MixinModel implementation : mixinModels )
        {
            if( appliesTo( implementation, methodModel.getMethod(), implementation, compositeModel.getCompositeClass() ) )
            {
                return implementation;
            }
        }

        // Check generic impls
        for( MixinModel implementation : mixinModels )
        {
            if( appliesTo( implementation, methodModel.getMethod(), implementation, compositeModel.getCompositeClass() ) )
            {
                return implementation;
            }
        }

        throw new CompositeResolutionException( "Could not find mixin for method " + methodModel.getMethod().toGenericString() );
    }

    private Iterable<AssertionModel> getAssertionsForMethod( CompositeModel compositeModel, Method method, MixinModel mixinModel, Iterable<MixinResolution> usedMixins )
    {
        Class compositeClass = compositeModel.getCompositeClass();

        List<AssertionModel> methodModifierModels = new ArrayList<AssertionModel>();

        // 1) Interface assertions
        addModifiers( compositeClass, method, compositeModel.getAssertionModels(), methodModifierModels, mixinModel );

        // 2) MixinModel assertions
        addModifiers( compositeClass, method, mixinModel.getAssertions(), methodModifierModels, mixinModel );

        // 3) Assertions from other mixins
        for( MixinResolution usedMixin : usedMixins )
        {
            if( usedMixin.getMixinModel() != mixinModel )
            {
                MixinModel model = (MixinModel) usedMixin.getFragmentModel();
                addModifiers( compositeClass, method, model.getAssertions(), methodModifierModels, mixinModel );
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
        Class appliesTo = fragmentModel.getAppliesTo();

        // Check AppliesTo
        if( appliesTo != null )
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
                        if( !filter.appliesTo( method, mixinModel.getModelClass(), compositeClass ) )
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
        }

        // The fragment must implement the interface of the method or be generic
        return ( method.getDeclaringClass().isAssignableFrom( fragmentModel.getModelClass() ) || fragmentModel.isGeneric() );
    }
}