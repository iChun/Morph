package me.ichun.mods.morph.client.render;

import me.ichun.mods.ichunutil.client.model.util.ModelHelper;
import me.ichun.mods.ichunutil.common.core.util.ObfHelper;
import me.ichun.mods.morph.client.model.ModelInfo;
import me.ichun.mods.morph.client.model.ModelMorph;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderPlayerHand extends RenderPlayer
{
    public RenderPlayer parent;

    public MorphInfoClient clientInfo;

    public float renderTick;

    public ModelMorph rightHandInterim;
    public ModelMorph leftHandInterim;

    public RenderPlayerHand()
    {
        super(Minecraft.getMinecraft().getRenderManager());

        rightHandInterim = new ModelMorph();
        leftHandInterim = new ModelMorph();
    }

    public void reset(World world, MorphInfoClient info)
    {
        rightHandInterim.clean();
        leftHandInterim.clean();
        if(info != null && world != null)
        {
            rightHandInterim.prevModels.clear();
            rightHandInterim.nextModels.clear();

            leftHandInterim.prevModels.clear();
            leftHandInterim.nextModels.clear();

            ModelInfo prevModel = info.getPrevStateModel(world);
            ModelInfo nextModel = info.getNextStateModel(world);

            if(prevModel != null && prevModel.modelArms[0] != null)
            {
                rightHandInterim.prevModels.add(prevModel.modelArms[0]);
            }
            if(nextModel.modelArms[0] != null)
            {
                rightHandInterim.nextModels.add(nextModel.modelArms[0]);
            }

            if(prevModel != null && prevModel.modelArms[1] != null)
            {
                leftHandInterim.prevModels.add(prevModel.modelArms[1]);
            }
            if(nextModel.modelArms[1] != null)
            {
                leftHandInterim.nextModels.add(nextModel.modelArms[1]);
            }

            rightHandInterim.prepareReferences();
            leftHandInterim.prepareReferences();

            rightHandInterim.modelList.clear();
            leftHandInterim.modelList.clear();

            if(prevModel != null)
            {
                rightHandInterim.modelList.addAll(ModelHelper.getModelCubesCopy(rightHandInterim.prevModels, rightHandInterim, null));
                leftHandInterim.modelList.addAll(ModelHelper.getModelCubesCopy(leftHandInterim.prevModels, leftHandInterim, null));
            }
        }
    }

    @Override
    public void renderRightArm(AbstractClientPlayer clientPlayer)
    {
        if(clientInfo != null)
        {
            //Do we need to render the arms?
            float morphTransition = clientInfo.getMorphTransitionProgress(renderTick);
            if(morphTransition == 0.0F && clientInfo.getPrevStateModel(clientPlayer.world).modelArms[0] == null || morphTransition == 1.0F && clientInfo.getNextStateModel(clientPlayer.world).modelArms[0] == null)
            {
                return;
            }

            //setup
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            ModelPlayer modelplayer = parent.getMainModel();
            modelplayer.swingProgress = 0.0F;
            modelplayer.isSneak = false;
            modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);

            //render the actual arm. There will always be an arm if we're no in interim here cause of the check above.
            ModelRenderer replacement;
            if(clientInfo.isMorphing() && !(clientInfo.morphTime > Morph.config.morphTime - 10))
            {
                if(clientInfo.morphTime < 10)
                {
                    //render old arm with skin
                    ModelInfo modelInfo = clientInfo.getPrevStateModel(clientPlayer.world);
                    ResourceLocation entTexture = ObfHelper.getEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), clientInfo.prevState.getEntInstance(clientPlayer.world));
                    bindTexture(entTexture);

                    replacement = modelInfo.modelArms[0];

                    ModelRenderer arm = parent.getMainModel().bipedRightArm;
                    parent.getMainModel().bipedRightArm = replacement;

                    //player arms are 12 blocks long
                    int heightDiff = 12 - ModelHelper.getModelHeight(replacement);
                    float rotX = replacement.rotationPointX;
                    float rotY = replacement.rotationPointY;
                    float rotZ = replacement.rotationPointZ;

                    float angX = replacement.rotateAngleX;
                    float angY = replacement.rotateAngleY;
                    float angZ = replacement.rotateAngleZ;

                    replacement.rotationPointX = arm.rotationPointX;
                    replacement.rotationPointY = arm.rotationPointY + heightDiff;
                    replacement.rotationPointZ = arm.rotationPointZ;

                    parent.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
                    parent.getMainModel().bipedRightArm.render(0.0625F);
                    if(clientInfo.prevState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                    {
                        parent.getMainModel().bipedRightArmwear.render(0.0625F);
                    }

                    float skinAlpha = clientInfo.getMorphSkinAlpha(renderTick);
                    if(skinAlpha > 0.0F)
                    {
                        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                        GlStateManager.color(1F, 1F, 1F, skinAlpha);

                        bindTexture(PlayerMorphHandler.morphSkin);

                        parent.getMainModel().bipedRightArm.render(0.0625F);
                        if(clientInfo.prevState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                        {
                            parent.getMainModel().bipedRightArmwear.render(0.0625F);
                        }
                        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                    }

                    parent.getMainModel().bipedRightArm = arm;

                    replacement.rotationPointX = rotX;
                    replacement.rotationPointY = rotY;
                    replacement.rotationPointZ = rotZ;

                    replacement.rotateAngleX = angX;
                    replacement.rotateAngleY = angY;
                    replacement.rotateAngleZ = angZ;
                }
                else
                {
                    //create interim arm with skin
                    if(rightHandInterim.modelList.isEmpty())
                    {
                        return;
                    }

                    rightHandInterim.updateModelList(morphTransition, rightHandInterim.modelList, rightHandInterim.prevModels, rightHandInterim.nextModels, 0);

                    replacement = rightHandInterim.modelList.get(0);

                    bindTexture(PlayerMorphHandler.morphSkin);

                    ModelRenderer arm = parent.getMainModel().bipedRightArm;
                    parent.getMainModel().bipedRightArm = replacement;

                    //player arms are 12 blocks long
                    float heightDiff;
                    if(clientInfo.getPrevStateModel(clientPlayer.world).modelArms[0] == null)
                    {
                        heightDiff = 12 - ModelHelper.getModelHeight(replacement) - (18 * (1F - morphTransition));
                    }
                    else if(clientInfo.getNextStateModel(clientPlayer.world).modelArms[0] == null)
                    {
                        heightDiff = 12 - ModelHelper.getModelHeight(replacement) - (18 * morphTransition);
                    }
                    else
                    {
                        heightDiff = 12 - ModelHelper.getModelHeight(replacement);
                    }
                    float rotX = replacement.rotationPointX;
                    float rotY = replacement.rotationPointY;
                    float rotZ = replacement.rotationPointZ;

                    float angX = replacement.rotateAngleX;
                    float angY = replacement.rotateAngleY;
                    float angZ = replacement.rotateAngleZ;

                    replacement.rotationPointX = arm.rotationPointX;
                    replacement.rotationPointY = arm.rotationPointY + heightDiff;
                    replacement.rotationPointZ = arm.rotationPointZ;

                    parent.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
                    parent.getMainModel().bipedRightArm.render(0.0625F);
                    parent.getMainModel().bipedRightArm = arm;

                    replacement.rotationPointX = rotX;
                    replacement.rotationPointY = rotY;
                    replacement.rotationPointZ = rotZ;

                    replacement.rotateAngleX = angX;
                    replacement.rotateAngleY = angY;
                    replacement.rotateAngleZ = angZ;
                }
            }
            else
            {
                //render new arm with skin
                ModelInfo modelInfo = clientInfo.getNextStateModel(clientPlayer.world);
                ResourceLocation entTexture = ObfHelper.getEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), clientInfo.nextState.getEntInstance(clientPlayer.world));
                bindTexture(entTexture);

                replacement = modelInfo.modelArms[0];

                ModelRenderer arm = parent.getMainModel().bipedRightArm;
                parent.getMainModel().bipedRightArm = replacement;

                //player arms are 12 blocks long
                int heightDiff = 12 - ModelHelper.getModelHeight(replacement);
                float rotX = replacement.rotationPointX;
                float rotY = replacement.rotationPointY;
                float rotZ = replacement.rotationPointZ;

                float angX = replacement.rotateAngleX;
                float angY = replacement.rotateAngleY;
                float angZ = replacement.rotateAngleZ;

                replacement.rotationPointX = arm.rotationPointX;
                replacement.rotationPointY = arm.rotationPointY + heightDiff;
                replacement.rotationPointZ = arm.rotationPointZ;

                parent.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
                parent.getMainModel().bipedRightArm.render(0.0625F);
                if(clientInfo.nextState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                {
                    parent.getMainModel().bipedRightArmwear.render(0.0625F);
                }

                float skinAlpha = clientInfo.getMorphSkinAlpha(renderTick);
                if(skinAlpha > 0.0F)
                {
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                    GlStateManager.color(1F, 1F, 1F, skinAlpha);

                    bindTexture(PlayerMorphHandler.morphSkin);

                    parent.getMainModel().bipedRightArm.render(0.0625F);
                    if(clientInfo.nextState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                    {
                        parent.getMainModel().bipedRightArmwear.render(0.0625F);
                    }
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                }

                parent.getMainModel().bipedRightArm = arm;

                replacement.rotationPointX = rotX;
                replacement.rotationPointY = rotY;
                replacement.rotationPointZ = rotZ;

                replacement.rotateAngleX = angX;
                replacement.rotateAngleY = angY;
                replacement.rotateAngleZ = angZ;
            }

            GlStateManager.disableBlend();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        else
        {
            super.renderRightArm(clientPlayer);
        }
    }

    @Override
    public void renderLeftArm(AbstractClientPlayer clientPlayer)
    {
        if(clientInfo != null)
        {
            //Do we need to render the arms?
            float morphTransition = clientInfo.getMorphTransitionProgress(renderTick);
            if(morphTransition == 0.0F && clientInfo.getPrevStateModel(clientPlayer.world).modelArms[1] == null || morphTransition == 1.0F && clientInfo.getNextStateModel(clientPlayer.world).modelArms[1] == null)
            {
                return;
            }

            //setup
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            ModelPlayer modelplayer = parent.getMainModel();
            modelplayer.swingProgress = 0.0F;
            modelplayer.isSneak = false;
            modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);

            //render the actual arm. There will always be an arm if we're no in interim here cause of the check above.
            ModelRenderer replacement;
            if(clientInfo.isMorphing() && !(clientInfo.morphTime > Morph.config.morphTime - 10))
            {
                if(clientInfo.morphTime < 10)
                {
                    //render old arm with skin
                    ModelInfo modelInfo = clientInfo.getPrevStateModel(clientPlayer.world);
                    ResourceLocation entTexture = ObfHelper.getEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), clientInfo.prevState.getEntInstance(clientPlayer.world));
                    bindTexture(entTexture);

                    replacement = modelInfo.modelArms[1];

                    ModelRenderer arm = parent.getMainModel().bipedLeftArm;
                    parent.getMainModel().bipedLeftArm = replacement;

                    //player arms are 12 blocks long
                    int heightDiff = 12 - ModelHelper.getModelHeight(replacement);
                    float rotX = replacement.rotationPointX;
                    float rotY = replacement.rotationPointY;
                    float rotZ = replacement.rotationPointZ;

                    float angX = replacement.rotateAngleX;
                    float angY = replacement.rotateAngleY;
                    float angZ = replacement.rotateAngleZ;

                    replacement.rotationPointX = arm.rotationPointX;
                    replacement.rotationPointY = arm.rotationPointY + heightDiff;
                    replacement.rotationPointZ = arm.rotationPointZ;

                    parent.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
                    parent.getMainModel().bipedLeftArm.render(0.0625F);
                    if(clientInfo.prevState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                    {
                        parent.getMainModel().bipedLeftArmwear.render(0.0625F);
                    }

                    float skinAlpha = clientInfo.getMorphSkinAlpha(renderTick);
                    if(skinAlpha > 0.0F)
                    {
                        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                        GlStateManager.color(1F, 1F, 1F, skinAlpha);

                        bindTexture(PlayerMorphHandler.morphSkin);

                        parent.getMainModel().bipedLeftArm.render(0.0625F);
                        if(clientInfo.prevState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                        {
                            parent.getMainModel().bipedLeftArmwear.render(0.0625F);
                        }
                        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                    }

                    parent.getMainModel().bipedLeftArm = arm;

                    replacement.rotationPointX = rotX;
                    replacement.rotationPointY = rotY;
                    replacement.rotationPointZ = rotZ;

                    replacement.rotateAngleX = angX;
                    replacement.rotateAngleY = angY;
                    replacement.rotateAngleZ = angZ;
                }
                else
                {
                    //create interim arm with skin
                    if(leftHandInterim.modelList.isEmpty())
                    {
                        return;
                    }

                    leftHandInterim.updateModelList(morphTransition, leftHandInterim.modelList, leftHandInterim.prevModels, leftHandInterim.nextModels, 0);

                    replacement = leftHandInterim.modelList.get(0);

                    bindTexture(PlayerMorphHandler.morphSkin);

                    ModelRenderer arm = parent.getMainModel().bipedLeftArm;
                    parent.getMainModel().bipedLeftArm = replacement;

                    //player arms are 12 blocks long
                    float heightDiff;
                    if(clientInfo.getPrevStateModel(clientPlayer.world).modelArms[1] == null)
                    {
                        heightDiff = 12 - ModelHelper.getModelHeight(replacement) - (18 * (1F - morphTransition));
                    }
                    else if(clientInfo.getNextStateModel(clientPlayer.world).modelArms[1] == null)
                    {
                        heightDiff = 12 - ModelHelper.getModelHeight(replacement) - (18 * morphTransition);
                    }
                    else
                    {
                        heightDiff = 12 - ModelHelper.getModelHeight(replacement);
                    }
                    float rotX = replacement.rotationPointX;
                    float rotY = replacement.rotationPointY;
                    float rotZ = replacement.rotationPointZ;

                    float angX = replacement.rotateAngleX;
                    float angY = replacement.rotateAngleY;
                    float angZ = replacement.rotateAngleZ;

                    replacement.rotationPointX = arm.rotationPointX;
                    replacement.rotationPointY = arm.rotationPointY + heightDiff;
                    replacement.rotationPointZ = arm.rotationPointZ;

                    parent.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
                    parent.getMainModel().bipedLeftArm.render(0.0625F);
                    if(clientInfo.prevState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                    {
                        parent.getMainModel().bipedLeftArmwear.render(0.0625F);
                    }

                    parent.getMainModel().bipedLeftArm = arm;

                    replacement.rotationPointX = rotX;
                    replacement.rotationPointY = rotY;
                    replacement.rotationPointZ = rotZ;

                    replacement.rotateAngleX = angX;
                    replacement.rotateAngleY = angY;
                    replacement.rotateAngleZ = angZ;
                }
            }
            else
            {
                //render new arm with skin
                ModelInfo modelInfo = clientInfo.getNextStateModel(clientPlayer.world);
                ResourceLocation entTexture = ObfHelper.getEntityTexture(modelInfo.entRenderer, modelInfo.entRenderer.getClass(), clientInfo.nextState.getEntInstance(clientPlayer.world));
                bindTexture(entTexture);

                replacement = modelInfo.modelArms[1];

                ModelRenderer arm = parent.getMainModel().bipedLeftArm;
                parent.getMainModel().bipedLeftArm = replacement;

                //player arms are 12 blocks long
                int heightDiff = 12 - ModelHelper.getModelHeight(replacement);
                float rotX = replacement.rotationPointX;
                float rotY = replacement.rotationPointY;
                float rotZ = replacement.rotationPointZ;

                float angX = replacement.rotateAngleX;
                float angY = replacement.rotateAngleY;
                float angZ = replacement.rotateAngleZ;

                replacement.rotationPointX = arm.rotationPointX;
                replacement.rotationPointY = arm.rotationPointY + heightDiff;
                replacement.rotationPointZ = arm.rotationPointZ;

                parent.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
                parent.getMainModel().bipedLeftArm.render(0.0625F);
                if(clientInfo.nextState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                {
                    parent.getMainModel().bipedLeftArmwear.render(0.0625F);
                }

                float skinAlpha = clientInfo.getMorphSkinAlpha(renderTick);
                if(skinAlpha > 0.0F)
                {
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
                    GlStateManager.color(1F, 1F, 1F, skinAlpha);

                    bindTexture(PlayerMorphHandler.morphSkin);

                    parent.getMainModel().bipedLeftArm.render(0.0625F);
                    if(clientInfo.nextState.getEntInstance(clientPlayer.world) instanceof AbstractClientPlayer)
                    {
                        parent.getMainModel().bipedLeftArmwear.render(0.0625F);
                    }
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
                }

                parent.getMainModel().bipedLeftArm = arm;

                replacement.rotationPointX = rotX;
                replacement.rotationPointY = rotY;
                replacement.rotationPointZ = rotZ;

                replacement.rotateAngleX = angX;
                replacement.rotateAngleY = angY;
                replacement.rotateAngleZ = angZ;
            }

            GlStateManager.disableBlend();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
        else
        {
            super.renderLeftArm(clientPlayer);
        }
    }
}
