package org.qi4j.runtime.resolution;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.InvalidCompositeException;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.spi.dependency.InvalidDependencyException;

/**
 * TODO
 */
public class CompositeModelResolver
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

        CompositeResolution resolution = new CompositeResolution<T>( compositeModel, usedMixinModels, mixinsForInterfaces, modifiersForMethod );
        return resolution;
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
                    throw new CompositeResolutionException( "Could not resolve modifier " + modifier.getModelClass() + "in composite " + compositeModel.getCompositeClass().getName(), e );
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

        nextmodifier:
        for( ModifierModel modifierModel : aModifierList )
        {
            if( !aMethodModifierList.contains( modifierModel ) )
            {
                // Check AppliesTo
                Class appliesTo = modifierModel.getAppliesTo();
                if( appliesTo != null )
                {
                    // Check AppliesTo
                    if( appliesTo.isAnnotation() )
                    {
                        MixinModel mixinModel = (MixinModel) mixinMappings.get( method.getDeclaringClass() ).getFragmentModel();

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
                                        continue; // Skip this modifierModel
                                    }
                                }
                                catch( NoSuchMethodException e )
                                {
                                    continue; // Skip this modifierModel
                                }
                            }
                            else
                            {
                                continue; // Skip this modifierModel
                            }
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
                        Class fragmentClass = mixin.getModelClass();
                        if( !appliesTo.isAssignableFrom( fragmentClass ) && !appliesTo.isAssignableFrom( methodDeclaringClass ) )
                        {
                            continue; // Skip this modifierModel
                        }
                    }
                }

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
}