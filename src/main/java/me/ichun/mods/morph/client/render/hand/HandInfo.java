package me.ichun.mods.morph.client.render.hand;

import com.mojang.blaze3d.matrix.MatrixStack;
import cpw.mods.modlauncher.api.INameMappingService;
import me.ichun.mods.ichunutil.api.common.head.HeadInfo;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class HandInfo
{
    public String author; //Used for credit.
    public String forClass = "THIS SHOULD BE FILLED UP";

    public ModelRendererMarker[] leftHandParts;
    public ModelRendererMarker[] rightHandParts;

    public transient Class<? extends EntityModel> modelClass;

    public boolean setup()
    {
        if(modelClass == null)
        {
            try
            {
                Class clz = Class.forName(forClass);
                if(!(EntityModel.class.isAssignableFrom(clz)))
                {
                    Morph.LOGGER.error("{} does not extend EntityModel!", clz.getSimpleName());
                    return false;
                }

                modelClass = clz;

                if(leftHandParts != null)
                {
                    for(ModelRendererMarker leftHandPart : leftHandParts)
                    {
                        if(!leftHandPart.setupFields(clz))
                        {
                            return false;
                        }
                    }
                }
                if(rightHandParts != null)
                {
                    for(ModelRendererMarker rightHandPart : rightHandParts)
                    {
                        if(!rightHandPart.setupFields(clz))
                        {
                            return false;
                        }
                    }
                }

                return true;
            }
            catch(ClassNotFoundException ignored)
            {
                return false;
            }
        }
        return true; //model class has been set, we've been set up.
    }

    public ModelRenderer[] getHandParts(HandSide side, EntityModel model) //Objects in array should not be null but can be
    {
        ModelRendererMarker[] markers = side == HandSide.LEFT ? leftHandParts : rightHandParts;

        ModelRenderer[] parts = new ModelRenderer[markers.length];

        for(int i = 0; i < parts.length; i++)
        {
            parts[i] = markers[i].getModelRenderer(model);
        }

        return parts;
    }

    public MatrixStack[] getPlacementCorrectors(HandSide side) //Objects in array can be null
    {
        ModelRendererMarker[] markers = side == HandSide.LEFT ? leftHandParts : rightHandParts;

        MatrixStack[] stacks = new MatrixStack[markers.length];

        for(int i = 0; i < stacks.length; i++)
        {
            stacks[i] = markers[i].getPlacementCorrectorStack();
        }

        return stacks;
    }

    @OnlyIn(Dist.CLIENT)
    public static class ModelRendererMarker
    {
        private String fieldName = null;
        private HeadInfo.PlacementCorrector[] placementCorrectors;

        private transient Field field;
        private transient ArrayList<Integer> fieldIndices;

        private transient MatrixStack stackPlacementCorrector;

        public boolean setupFields(Class<? extends EntityModel> clz)
        {
            if(field == null)
            {
                String fieldNameFull = this.fieldName;
                String fieldName = fieldNameFull;
                ArrayList<Integer> indices = new ArrayList<>();

                if(fieldName.contains("[")) //it is an array, list, or get child, or worse, multiples.
                {
                    fieldName = fieldNameFull.substring(0, fieldNameFull.indexOf("["));

                    String indicesString = fieldNameFull.substring(fieldNameFull.indexOf("["));
                    while(indicesString.startsWith("["))
                    {
                        int closeBracketIndex = indicesString.indexOf("]");
                        //do magic
                        try
                        {
                            indices.add(Integer.parseInt(indicesString.substring(1, closeBracketIndex))); //we look for -1 and higher to confirm parsing.
                        }
                        catch(NumberFormatException | StringIndexOutOfBoundsException e)
                        {
                            Morph.LOGGER.error("Error parsing field of {} of model {}", fieldNameFull, clz.getSimpleName());
                            return false; //we errored.
                        }

                        indicesString = indicesString.substring(closeBracketIndex + 1);
                    }
                }
                else
                {
                    indices.add(-1);
                }
                Field field = HeadInfo.findField(clz, ObfuscationReflectionHelper.remapName(INameMappingService.Domain.FIELD, fieldName));
                if(field != null)
                {
                    field.setAccessible(true);
                    this.field = field;
                    this.fieldIndices = indices;
                    return true;
                }

                Morph.LOGGER.error("Error finding field of {} from {} of model {}", fieldName, fieldNameFull, clz.getSimpleName());
                return false;
            }
            return true;
        }

        public void correctPlacement(MatrixStack stack)
        {
            if(placementCorrectors != null && placementCorrectors.length > 0)
            {
                if(stackPlacementCorrector == null)
                {
                    stackPlacementCorrector = new MatrixStack();

                    for(HeadInfo.PlacementCorrector renderCorrector : placementCorrectors)
                    {
                        renderCorrector.apply(stackPlacementCorrector);
                    }
                }

                RenderHelper.multiplyStackWithStack(stack, stackPlacementCorrector);
            }
        }

        public ModelRenderer getModelRenderer(EntityModel model)
        {
            try
            {
                Object o = field.get(model);

                Object modelAtIndex = o;

                for(Integer index : fieldIndices)
                {
                    modelAtIndex = HeadInfo.digForModelRendererWithIndex(modelAtIndex, index);
                }

                if(modelAtIndex instanceof ModelRenderer)
                {
                    return (ModelRenderer)modelAtIndex;
                }
            }
            catch(NullPointerException | IllegalAccessException | ArrayIndexOutOfBoundsException e)
            {
                Morph.LOGGER.error("Error getting model renderer from field {} of class {} in model {}", field.getName(), field.getDeclaringClass().getSimpleName(), model);
                e.printStackTrace();
            }
            return null;
        }

        public MatrixStack getPlacementCorrectorStack()
        {
            if(placementCorrectors != null && placementCorrectors.length > 0)
            {
                if(stackPlacementCorrector == null)
                {
                    stackPlacementCorrector = new MatrixStack();

                    for(HeadInfo.PlacementCorrector renderCorrector : placementCorrectors)
                    {
                        renderCorrector.apply(stackPlacementCorrector);
                    }
                }

                return stackPlacementCorrector;
            }
            return null;
        }

        @Nullable
        private ModelRenderer getModel(Field field, ArrayList<Integer> indices, EntityModel model)
        {
            field.setAccessible(true);
            try
            {
                Object o = field.get(model);

                Object modelAtIndex = o;

                for(Integer index : indices)
                {
                    modelAtIndex = HeadInfo.digForModelRendererWithIndex(modelAtIndex, index);
                }

                if(modelAtIndex instanceof ModelRenderer)
                {
                    return (ModelRenderer)modelAtIndex;
                }
            }
            catch(NullPointerException | IllegalAccessException | ArrayIndexOutOfBoundsException e)
            {
                Morph.LOGGER.error("Error getting head info of {} for {} in {}", field.getName(), field.getDeclaringClass().getSimpleName(), model);
                e.printStackTrace();
            }
            return null;
        }
    }
}
