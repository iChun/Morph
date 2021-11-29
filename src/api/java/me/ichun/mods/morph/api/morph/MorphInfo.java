package me.ichun.mods.morph.api.morph;

import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
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

public abstract class MorphInfo
{
    @CapabilityInject(MorphInfo.class)
    public static Capability<MorphInfo> CAPABILITY_INSTANCE;
    public static final ResourceLocation CAPABILITY_IDENTIFIER = new ResourceLocation("morph", "capability_morph_state");
    private static final AtomicInteger NEXT_ENTITY_ID = new AtomicInteger(-70000000);// -70 million. We reduce even further as we use this more, negative ent IDs prevent collision with real entities (with positive IDs starting with 0)

    public final PlayerEntity player;

    @Nullable
    public MorphState prevState;
    @Nullable
    public MorphState nextState;

    public int morphTime; //this is the time the player has been morphing for.

    public int morphingTime; //this is the time it takes for a player to Morph

    public boolean firstTick = true;
    public int playSoundTime = -1;

    public boolean requested; //Never checked on server.

    protected MorphInfo(PlayerEntity player)
    {
        this.player = player;
    }

    public abstract void tick();

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

    protected void setPrevState(@Nullable MorphState state)
    {
        if(prevState != null)
        {
            prevState.deactivateHooks();
        }

        prevState = state;

        if(prevState != null)
        {
            prevState.activateHooks();
        }
    }

    protected void setNextState(@Nullable MorphState state)
    {
        if(nextState != null && nextState != prevState) //check to make sure prevState was not set to our current nextState
        {
            nextState.deactivateHooks();
        }

        nextState = state;

        if(nextState != null)
        {
            nextState.activateHooks();
        }
    }

    public void setNextState(MorphState state, int morphingTime) //sets the morph. If null, sets to no morph.
    {
        if(state != null)
        {
            if(nextState != null) //morphing from one morph to another
            {
                setPrevState(nextState);
            }
            else //just started morphing
            {
                MorphVariant variant = MorphVariant.createPlayerMorph(player.getGameProfile().getId(), true);
                variant.thisVariant.identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;
                setPrevState(new MorphState(variant, player));
            }

            this.morphTime = 0;
            this.morphingTime = morphingTime;
        }
        else
        {
            setPrevState(null);
            this.morphTime = 0;
            this.morphingTime = 0;
        }
        setNextState(state);
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
            state.traits = MorphApi.getApiImpl().getTraitsForVariant(state.variant, player);
            if(state.variant.thisVariant != null)
            {
                if(nextState != null && nextState.equals(state) && tag.contains("nextState"))
                {
                    setPrevState(nextState);
                }
                else
                {
                    setPrevState(state);
                }
            }
        }
        else
        {
            setPrevState(null);
        }

        if(tag.contains("nextState"))
        {
            MorphState state = MorphState.createFromNbt(tag.getCompound("nextState"));
            state.traits = MorphApi.getApiImpl().getTraitsForVariant(state.variant, player);
            setNextState(state);
            if(nextState.variant.thisVariant == null) //MorphState variants should ALWAYS have a thisVariant.
            {
                setPrevState(null);
                setNextState(null);
            }
        }
        else
        {
            setNextState(null);
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

    protected LivingEntity getActiveMorphEntityOrPlayer()
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
    protected float getAbilitySkinAlpha(float partialTick)
    {
        return 0F;
    }

    //Entity sound functions
    public abstract void playStepSound(BlockPos pos, BlockState blockState);

    public abstract void playSwimSound(float volume);

    public abstract float playFlySound(float volume);

    //Living Entity sound functions
    @Nullable
    public abstract SoundEvent getHurtSound(DamageSource source);

    @Nullable
    public abstract SoundEvent getDeathSound();

    public abstract SoundEvent getFallSound(int height);

    public abstract SoundEvent getDrinkSound(ItemStack stack);

    public abstract SoundEvent getEatSound(ItemStack stack);

    public abstract float getSoundVolume();

    public abstract float getSoundPitch();

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
