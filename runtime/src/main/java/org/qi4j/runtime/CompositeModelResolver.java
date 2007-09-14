package org.qi4j.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.InvalidDependencyException;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.CompositeResolution;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.MixinResolution;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.api.model.ModifierResolution;

/**
 * TODO
 */
public final class CompositeModelResolver
{
    ModifierModelResolver modifierModelResolver;
    MixinModelResolver mixinModelResolver;

    public CompositeModelResolver( ModifierModelResolver modifierModelResolver, MixinModelResolver mixinModelResolver )
    {
        this.modifierModelResolver = modifierModelResolver;
        this.mixinModelResolver = mixinModelResolver;
    }

    public <T extends Composite> CompositeResolution<T> resolveCompositeModel( CompositeModel<T> compositeModel )
        throws CompositeResolutionException
    {
        // First figure out which interface map to which mixins
        Map<Class, MixinResolution> mixinsForInterfaces = mapInterfacesToMixins( compositeModel );

        // Determine list of mixin resolutions. Remove all duplicates
        Set<MixinResolution> usedMixinModels = getUsedMixins( mixinsForInterfaces );

        // Map modifiers to methods
        Map<Method, List<ModifierResolution>> modifiersForMethod = mapModifiersToMethods( compositeModel, mixinsForInterfaces );

        return new CompositeResolution<T>( compositeModel, usedMixinModels, mixinsForInterfaces, modifiersForMethod );
    }

    private Set<MixinResolution> getUsedMixins( Map<Class, MixinResolution> mixinsForInterfaces )
    {
        Set<MixinResolution> usedMixins = new LinkedHashSet<MixinResolution>();

        for( MixinResolution mixinResolution : mixinsForInterfaces.values() )
        {
            if( !usedMixins.contains( mixinResolution ) )
            {
                usedMixins.add( mixinResolution );
            }
        }

        return usedMixins;
    }

    private Map<Method, List<ModifierResolution>> mapModifiersToMethods( CompositeModel compositeModel, Map<Class, MixinResolution> mixinMappings )
        throws CompositeResolutionException
    {
        Map<Method, List<ModifierResolution>> methodModifiers = new HashMap<Method, List<ModifierResolution>>();
        Method[] methods = compositeModel.getCompositeClass().getMethods();
        for( Method method : methods )
        {
            // Find modifiers for method
            Iterable<ModifierModel> modifiers = mapModifiersToMethod( compositeModel, method, mixinMappings );

            // Resolve modifiers
            List<ModifierResolution> resolutions = new ArrayList<ModifierResolution>();
            for( ModifierModel modifier : modifiers )
            {
                try
                {
                    ModifierResolution resolution = modifierModelResolver.resolveModifierModel( modifier );
                    resolutions.add( resolution );
                }
                catch( InvalidDependencyException e )
                {
                    throw new CompositeResolutionException( "Could not resolve modifier " + modifier.getFragmentClass() + "in composite " + compositeModel.getCompositeClass().getName(), e );
                }
            }

            methodModifiers.put( method, resolutions );
        }

        return methodModifiers;
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
        MixinResolution mixinResolution;

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
                mixinResolution = mixinModelResolver.resolveMixinModel( possibleMixinModel );
            }
            catch( InvalidDependencyException e )
            {
                ex = e;
            }

            mixinResolutions.put( possibleMixinModel, mixinResolution );
            return mixinResolution;
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
                Class fragmentClass = implementation.getFragmentClass();
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

    private Iterable<ModifierModel> mapModifiersToMethod( CompositeModel compositeModel, Method method, Map<Class, MixinResolution> mixinMappings )
    {
        Class compositeClass = compositeModel.getCompositeClass();
        Iterable<ModifierModel> modifierModels = compositeModel.getModifierModels();

        List<ModifierModel> methodModifiers = new ArrayList<ModifierModel>();

        // 1) Interface modifiers
        addModifiers( compositeClass, method, modifierModels, methodModifiers, mixinMappings );

        // 2) MixinModel modifiers
        Class<?> methodClass = method.getDeclaringClass();
        MixinResolution mixinResolution = mixinMappings.get( methodClass );

        if( mixinResolution != null )
        {
            MixinModel mixinModel = (MixinModel) mixinResolution.getFragmentModel();
            addModifiers( compositeClass, method, mixinModel.getModifiers(), methodModifiers, mixinMappings );
        }

        // 3) Modifiers from other mixins
        for( Map.Entry<Class, MixinResolution> mapping : mixinMappings.entrySet() )
        {
            // TODO What about AppliesTo?
            if( !methodClass.equals( mapping.getKey() ) )
            {
                MixinModel model = (MixinModel) mapping.getValue().getFragmentModel();
                addModifiers( compositeClass, method, model.getModifiers(), methodModifiers, mixinMappings );
            }
        }

        return methodModifiers;
    }

    private void addModifiers( Class compositeClass, Method method, Iterable<ModifierModel> aModifierList, List<ModifierModel> aMethodModifierList, Map<Class, MixinResolution> mixinMappings )
    {
        // TODO This needs to be refactored to be easier to read

        for( ModifierModel modifierModel : aModifierList )
        {
            if( !aMethodModifierList.contains( modifierModel ) )
            {
                processModel( modifierModel, mixinMappings, method, compositeClass, aMethodModifierList );
            }
        }
    }

    private void processModel( ModifierModel modifierModel, Map<Class, MixinResolution> mixinMappings, Method method, Class compositeClass, List<ModifierModel> aMethodModifierList )
    {
        // Check AppliesTo
        if( checkForAppliesTo( modifierModel, mixinMappings, method, compositeClass ) )
        {
            return;
        }

        // Check interface
        if( !modifierModel.isGeneric() )
        {
            if( !method.getDeclaringClass().isAssignableFrom( modifierModel.getFragmentClass() ) )
            {
                return;
            }
        }
        // ModifierModel is ok!
        aMethodModifierList.add( modifierModel );
    }

    private boolean checkForAppliesTo( ModifierModel modifierModel, Map<Class, MixinResolution> mixinMappings, Method method, Class compositeClass )
    {
        Class appliesTo = modifierModel.getAppliesTo();
        if( appliesTo != null )
        {
            // Check AppliesTo
            if( appliesTo.isAnnotation() )
            {
                MixinModel mixinModel = (MixinModel) mixinMappings.get( method.getDeclaringClass() ).getFragmentModel();
                if( checkMixinImplAnnotated( mixinModel, appliesTo, method ) )
                {
                    return true;
                }
            }
            else
            {
                Class<?> methodDeclaringClass = method.getDeclaringClass();
                MixinModel mixin = (MixinModel) mixinMappings.get( methodDeclaringClass ).getFragmentModel();
                if( mixin == null )
                {
                    throw new InvalidCompositeException( methodDeclaringClass + " has no implementation.", compositeClass );
                }
                Class fragmentClass = mixin.getFragmentClass();
                if( !appliesTo.isAssignableFrom( fragmentClass ) && !appliesTo.isAssignableFrom( methodDeclaringClass ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkMixinImplAnnotated( MixinModel mixinModel, Class appliesTo, Method method )
    {
        // Check the Mixin implementation class to see if it is annotated.
        if( mixinModel.getFragmentClass().getAnnotation( appliesTo ) == null )
        {
            // The Mixin Implementation class was not annotated with the @AppliesTo.

            // Check method
            if( !mixinModel.isGeneric() )
            {
                try
                {
                    Method implMethod = mixinModel.getFragmentClass().getMethod( method.getName(), method.getParameterTypes() );
                    if( implMethod.getAnnotation( appliesTo ) == null )
                    {
                        return true;
                    }
                }
                catch( NoSuchMethodException e )
                {
                    return true;
                }
            }
            else
            {
                return true;
            }
        }
        return false;
    }
}