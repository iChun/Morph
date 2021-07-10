package me.ichun.mods.morph.api.morph;

import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.mixin.LivingEntityInvokerMixin;
import me.ichun.mods.morph.client.entity.EntityBiomassAbility;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class MorphInfo
{
    @CapabilityInject(MorphInfo.class)
    public static Capability<MorphInfo> CAPABILITY_INSTANCE;
    public static final ResourceLocation CAPABILITY_IDENTIFIER = new ResourceLocation("morph", "capability_morph_state");
    private static final AtomicInteger NEXT_ENTITY_ID = new AtomicInteger(-70000000);// -70 million. We reduce even further as we use this more, negative ent IDs prevent collision with real entities (with positive IDs starting with 0)

    public final PlayerEntity player; //TODO do I need to set a new one when change dimension?

    @Nullable
    public MorphState prevState;
    @Nullable
    public MorphState nextState;

    public int morphTime; //this is the time the player has been morphing for.

    public int morphingTime; //this is the time it takes for a player to Morph

    public boolean firstTick = true;
    public int playSoundTime = -1;

    public boolean requested; //Never checked on server.

    @OnlyIn(Dist.CLIENT)
    public MorphRenderHandler.MorphTransitionState transitionState;
    @OnlyIn(Dist.CLIENT)
    public EntityBiomassAbility entityBiomassAbility;

    public MorphInfo(PlayerEntity player)
    {
        this.player = player;
    }

    public void tick() //returns true if the player is considered "morphed"
    {
        if(!isMorphed())
        {
            return;
        }

        if(firstTick)
        {
            firstTick = false;
            player.recalculateSize();
        }

        //TODO check player resize on sleeping

        //TODO remember to set attributes
        float transitionProgress = getTransitionProgressLinear(1F);
        if(transitionProgress < 1.0F) // is morphing
        {
            if(!player.world.isRemote)
            {
                if(playSoundTime < 0)
                {
                    playSoundTime = Math.max(0, (int)((morphingTime - 60) / 2F)); // our sounds are 3 seconds long. play it in the middle of the morph
                }

                if(morphTime == playSoundTime)
                {
                    player.world.playMovingSound(null, player, Morph.Sounds.MORPH.get(), player.getSoundCategory(), 1.0F, 1.0F);
                }
            }
            prevState.tick(player);
        }
        nextState.tick(player);

        morphTime++;
        if(morphTime <= morphingTime || Morph.configServer.aggressiveSizeRecalculation) //still morphing
        {
            player.recalculateSize();
        }

        if(morphTime == morphingTime)
        {
            prevState = null; //bye bye last state. We don't need you anymore.

            if(player.world.isRemote)
            {
                if(transitionState != null)
                {
                    transitionState = null;
                }
            }

            if(nextState.variant.id.equals(EntityType.PLAYER.getRegistryName()) && nextState.variant.thisVariant.identifier.equals(MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE))
            {
                nextState = null;
            }
        }

        if(player.world.isRemote)
        {
            if(entityBiomassAbility != null && entityBiomassAbility.removed)
            {
                entityBiomassAbility = null; //have it, GC
            }
        }

        //TODO do stuff
    }

    public boolean isMorphed()
    {
        return nextState != null;
    }

    public float getMorphProgress(float partialTick)
    {
        if(prevState == null || nextState == null || morphingTime <= 0)
        {
            return 1.0F;
        }

        return MathHelper.clamp((morphTime + partialTick) / morphingTime, 0F, 1F);
    }

    public float getTransitionProgressLinear(float partialTick) //10 - 60 - 10 : fade to black - transition - fade to ent
    {
        float morphProgress = getMorphProgress(partialTick);
        return MathHelper.clamp((morphProgress - 0.125F) / 0.75F, 0F, 1F);
    }

    public float getTransitionProgressSine(float partialTick)
    {
        return sineifyProgress(getTransitionProgressLinear(partialTick));
    }

    public EntitySize getMorphSize(float partialTick)
    {
        float morphProgress = getMorphProgress(partialTick);
        if(morphProgress < 1F)
        {
            float transitionProgress = getTransitionProgressSine(partialTick);
            if(transitionProgress <= 0F)
            {
                LivingEntity prevInstance = prevState.getEntityInstance(player.world, player.getGameProfile().getId());
                prevInstance.recalculateSize();
                return prevInstance.size;
            }
            else if(transitionProgress >= 1F)
            {
                LivingEntity nextInstance = nextState.getEntityInstance(player.world, player.getGameProfile().getId());
                nextInstance.recalculateSize();
                return nextInstance.size;
            }
            else
            {
                LivingEntity prevInstance = prevState.getEntityInstance(player.world, player.getGameProfile().getId());
                prevInstance.recalculateSize();
                LivingEntity nextInstance = nextState.getEntityInstance(player.world, player.getGameProfile().getId());
                nextInstance.recalculateSize();
                EntitySize prevSize = prevInstance.size;
                EntitySize nextSize = nextInstance.size;
                return EntitySize.flexible(prevSize.width + (nextSize.width - prevSize.width) * transitionProgress, prevSize.height + (nextSize.height - prevSize.height) * transitionProgress);
            }
        }
        else
        {
            LivingEntity nextInstance = nextState.getEntityInstance(player.world, player.getGameProfile().getId());
            nextInstance.recalculateSize();
            return nextInstance.size;
        }
    }

    public float getMorphEyeHeight(float partialTick)
    {
        float morphProgress = getMorphProgress(partialTick);
        if(morphProgress < 1F)
        {
            float transitionProgress = getTransitionProgressSine(partialTick);
            if(transitionProgress <= 0F)
            {
                return prevState.getEntityInstance(player.world, player.getGameProfile().getId()).getEyeHeight();
            }
            else if(transitionProgress >= 1F)
            {
                return nextState.getEntityInstance(player.world, player.getGameProfile().getId()).getEyeHeight();
            }
            else
            {
                float prevHeight = prevState.getEntityInstance(player.world, player.getGameProfile().getId()).getEyeHeight();
                float nextHeight = nextState.getEntityInstance(player.world, player.getGameProfile().getId()).getEyeHeight();
                return prevHeight + (nextHeight - prevHeight) * transitionProgress;
            }
        }
        else
        {
            return nextState.getEntityInstance(player.world, player.getGameProfile().getId()).getEyeHeight();
        }
    }

    public void setNextState(MorphState state, int morphingTime) //sets the morph. If null, sets to no morph.
    {
        if(state != null)
        {
            if(nextState != null) //morphing from one morph to another
            {
                prevState = nextState;
            }
            else //just started morphing
            {
                MorphVariant variant = MorphVariant.createPlayerMorph(player.getGameProfile().getId(), true);
                variant.thisVariant.identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;
                prevState = new MorphState(variant);
            }

            this.morphTime = 0;
            this.morphingTime = morphingTime;
        }
        else
        {
            this.prevState = null;
            this.morphTime = 0;
            this.morphingTime = 0;
        }
        nextState = state;
        playSoundTime = -1; //default
        player.recalculateSize();
    }

    public boolean isCurrentlyThisVariant(@Nonnull MorphVariant.Variant variant)
    {
        return (nextState != null && nextState.variant.thisVariant.identifier.equals(variant.identifier) || !isMorphed() && variant.identifier.equals(MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE));
    }

    public CompoundNBT write(CompoundNBT tag)
    {
        if(prevState != null)
        {
            tag.put("prevState", prevState.write(new CompoundNBT()));
        }

        if(nextState != null)
        {
            tag.put("nextState", nextState.write(new CompoundNBT()));
        }

        tag.putInt("morphTime", morphTime);
        tag.putInt("morphingTime", morphingTime);
        return tag;
    }

    public void read(CompoundNBT tag)
    {
        playSoundTime = -1; //default

        if(tag.contains("prevState"))
        {
            MorphState state = MorphState.createFromNbt(tag.getCompound("prevState"));
            if(state.variant.thisVariant != null)
            {
                if(nextState != null && nextState.equals(state) && tag.contains("nextState"))
                {
                    prevState = nextState;
                }
                else
                {
                    prevState = state;
                }
            }
        }
        else
        {
            prevState = null;
        }

        if(tag.contains("nextState"))
        {
            nextState = MorphState.createFromNbt(tag.getCompound("nextState"));
            if(nextState.variant.thisVariant == null) //MorphState variants should ALWAYS have a thisVariant.
            {
                prevState = nextState = null;
            }
        }
        else
        {
            nextState = null;
        }

        morphTime = tag.getInt("morphTime");
        morphingTime = tag.getInt("morphingTime");

        player.recalculateSize();
    }

    public LivingEntity getActiveMorphEntity()
    {
        if(getMorphProgress(1F) < 0.5F)
        {
            return prevState.getEntityInstance(player.world, player.getGameProfile().getId());
        }
        else if(nextState != null)
        {
            return nextState.getEntityInstance(player.world, player.getGameProfile().getId());
        }
        return null;
    }

    private LivingEntity getActiveMorphEntityOrPlayer()
    {
        LivingEntity activeMorph = getActiveMorphEntity();
        if(activeMorph == null)
        {
            activeMorph = player;
        }

        return activeMorph;
    }

    public LivingEntity getActiveAppearanceEntity(float partialTick)
    {
        if(getMorphProgress(partialTick) < 1F) //morphing
        {
            float transitionProg = getTransitionProgressLinear(partialTick);
            if(transitionProg <= 0F)
            {
                return prevState.getEntityInstance(player.world, player.getGameProfile().getId());
            }
            else if(transitionProg >= 1F)
            {
                return nextState.getEntityInstance(player.world, player.getGameProfile().getId());
            }
            return null; //mid transition, no active appearance.
        }
        else if(nextState != null) //is morphed
        {
            return nextState.getEntityInstance(player.world, player.getGameProfile().getId());
        }
        else
        {
            return player;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getMorphSkinAlpha(float partialTick)
    {
        return Math.max(getMorphingSkinAlpha(partialTick), getAbilitySkinAlpha(partialTick));
    }

    @OnlyIn(Dist.CLIENT)
    private float getMorphingSkinAlpha(float partialTick) //similar code in MorphRenderHelper.renderMorphInfo
    {
        float morphProgress = getMorphProgress(partialTick);
        if(morphProgress < 1F)
        {
            float transitionProgress = getTransitionProgressSine(partialTick);
            if(transitionProgress <= 0F)
            {
                return sineifyProgress(morphProgress / 0.125F);
            }
            else if(transitionProgress >= 1F)
            {
                return 1F - sineifyProgress((morphProgress - 0.875F) / 0.125F);
            }
            return 1F;
        }

        return 0F;
    }

    @OnlyIn(Dist.CLIENT)
    private float getAbilitySkinAlpha(float partialTick)
    {
        if(entityBiomassAbility != null)
        {
            float alpha;
            if(entityBiomassAbility.age < entityBiomassAbility.fadeTime)
            {
                alpha = EntityHelper.sineifyProgress(MathHelper.clamp((entityBiomassAbility.age + partialTick) / entityBiomassAbility.fadeTime, 0F, 1F));
            }
            else if(entityBiomassAbility.age >= entityBiomassAbility.fadeTime + entityBiomassAbility.solidTime)
            {
                alpha = EntityHelper.sineifyProgress(1F - MathHelper.clamp((entityBiomassAbility.age - (entityBiomassAbility.fadeTime + entityBiomassAbility.solidTime) + partialTick) / entityBiomassAbility.fadeTime, 0F, 1F));
            }
            else
            {
                alpha = 1F;
            }
            return alpha;
        }
        return 0F;
    }

    @Nullable
    public SoundEvent getHurtSound(DamageSource source) {
        return ((LivingEntityInvokerMixin)getActiveMorphEntityOrPlayer()).callGetHurtSound(source);
    }

    @Nullable
    public SoundEvent getDeathSound() {
        return ((LivingEntityInvokerMixin)getActiveMorphEntityOrPlayer()).callGetDeathSound();
    }

    public SoundEvent getFallSound(int height) {
        return ((LivingEntityInvokerMixin)getActiveMorphEntityOrPlayer()).callGetFallSound(height);
    }

    public SoundEvent getDrinkSound(ItemStack stack) {
        return ((LivingEntityInvokerMixin)getActiveMorphEntityOrPlayer()).callGetDrinkSound(stack);
    }

    public SoundEvent getEatSound(ItemStack stack) {
        return getActiveMorphEntityOrPlayer().getEatSound(stack);
    }

    public float getSoundVolume()
    {
        if(nextState != null)
        {
            if(prevState != null)
            {
                float transitionProg = getTransitionProgressLinear(1F);

                float prevVolume = ((LivingEntityInvokerMixin)prevState.getEntityInstance(player.world, player.getGameProfile().getId())).callGetSoundVolume();
                float nextVolume = ((LivingEntityInvokerMixin)nextState.getEntityInstance(player.world, player.getGameProfile().getId())).callGetSoundVolume();

                return prevVolume + (nextVolume - prevVolume) * transitionProg;
            }
            else
            {
                return ((LivingEntityInvokerMixin)nextState.getEntityInstance(player.world, player.getGameProfile().getId())).callGetSoundVolume();
            }
        }

        return 1F;
    }

    public float getSoundPitch()
    {
        if(nextState != null)
        {
            if(prevState != null)
            {
                float transitionProg = getTransitionProgressLinear(1F);

                float prevPitch = ((LivingEntityInvokerMixin)prevState.getEntityInstance(player.world, player.getGameProfile().getId())).callGetSoundPitch();
                float nextPitch = ((LivingEntityInvokerMixin)nextState.getEntityInstance(player.world, player.getGameProfile().getId())).callGetSoundPitch();

                return prevPitch + (nextPitch - prevPitch) * transitionProg;
            }
            else
            {
                return ((LivingEntityInvokerMixin)nextState.getEntityInstance(player.world, player.getGameProfile().getId())).callGetSoundPitch();
            }
        }

        return 1F;
    }

    public static class CapProvider implements ICapabilitySerializable<CompoundNBT>
    {
        private final MorphInfo state;
        private final LazyOptional<MorphInfo> optional;

        public CapProvider(MorphInfo state)
        {
            this.state = state;
            this.optional = LazyOptional.of(() -> state);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
        {
            if(cap == CAPABILITY_INSTANCE)
            {
                return optional.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            return state.write(new CompoundNBT());
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt)
        {
            state.read(nbt);
        }
    }

    //TAKEN FROM ClientEntityTracker
    public static int getNextEntId()
    {
        return NEXT_ENTITY_ID.getAndDecrement();
    }

    public static float sineifyProgress(float progress) //0F - 1F; Yay math
    {
        return ((float)Math.sin(Math.toRadians(-90F + (180F * progress))) / 2F) + 0.5F;
    }
}
