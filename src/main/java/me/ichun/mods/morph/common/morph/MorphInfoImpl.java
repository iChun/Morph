package me.ichun.mods.morph.common.morph;

import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.entity.EntityBiomassAbility;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.packet.PacketInvalidateClientHealth;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class MorphInfoImpl extends MorphInfo
{
    private final Random rand = new Random();

    @OnlyIn(Dist.CLIENT)
    public MorphRenderHandler.MorphTransitionState transitionState;
    @OnlyIn(Dist.CLIENT)
    public EntityBiomassAbility entityBiomassAbility;

    public MorphInfoImpl(PlayerEntity player)
    {
        super(player);
    }

    @Override
    public void tick()
    {
        if(!isMorphed())
        {
            return;
        }

        float transitionProgress = getTransitionProgressLinear(1F);

        if(firstTick)
        {
            firstTick = false;
            player.recalculateSize();
            applyAttributeModifiers(transitionProgress);
        }

        //TODO check player resize on sleeping

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
            prevState.tick(player, transitionProgress > 0F);
            nextState.tick(player, true);

            float prevStateTraitStrength = 1F - MathHelper.clamp(transitionProgress / 0.5F, 0F, 1F);
            float nextStateTraitStrength = MathHelper.clamp((transitionProgress - 0.5F) / 0.5F, 0F, 1F);

            ArrayList<Trait<?>> prevTraits = new ArrayList<>(prevState.traits);
            for(Trait trait : nextState.traits)
            {
                boolean foundTranslatableTrait = false;
                for(int i = prevTraits.size() - 1; i >= 0; i--)
                {
                    Trait<?> prevTrait = prevTraits.get(i);
                    if(prevTrait.canTransitionTo(trait))
                    {
                        prevTraits.remove(i); //remove it

                        foundTranslatableTrait = true;

                        trait.doTransitionalTick(prevTrait, transitionProgress);

                        break;
                    }
                }

                if(!foundTranslatableTrait) //only nextState has this trait
                {
                    trait.doTick(nextStateTraitStrength);
                }
            }

            for(Trait<?> value : prevTraits)
            {
                value.doTick(prevStateTraitStrength);
            }
        }
        else
        {
            nextState.tick(player, false);
            nextState.tickTraits();
        }

        morphTime++;
        if(morphTime <= morphingTime) //still morphing
        {
            player.recalculateSize();
            applyAttributeModifiers(transitionProgress);
        }
        else if(Morph.configServer.aggressiveSizeRecalculation)
        {
            player.recalculateSize();
        }

        if(morphTime == morphingTime)
        {
            removeAttributeModifiersFromPrevState();
            setPrevState(null); //bye bye last state. We don't need you anymore.

            if(player.world.isRemote)
            {
                endMorphOnClient();
            }

            if(nextState.variant.id.equals(EntityType.PLAYER.getRegistryName()) && nextState.variant.thisVariant.identifier.equals(MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE))
            {
                setNextState(null);
            }
        }

        if(player.world.isRemote)
        {
            if(entityBiomassAbility != null && entityBiomassAbility.removed)
            {
                entityBiomassAbility = null; //have it, GC
            }
        }
    }

    public void applyAttributeModifiers(float transitionProgress)
    {
        if(player.world.isRemote) //we don't touch the attributes on the client
        {
            return;
        }

        HashMap<Attribute, Double> attributeModifierAmount = new HashMap<>();

        //Add the next state's attribute modifier amounts
        for(Map.Entry<String, INBT> e : nextState.variant.nbtMorph.tagMap.entrySet())
        {
            String key = e.getKey();
            if(key.startsWith("attr_")) //it's an attribute key
            {
                ResourceLocation id = new ResourceLocation(key.substring(5));
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                if(attribute != null)
                {
                    final ModifiableAttributeInstance playerAttribute = player.getAttribute(attribute);
                    if(playerAttribute != null)
                    {
                        double baseValue = player.getBaseAttributeValue(attribute);
                        double modifierValue = nextState.variant.nbtMorph.getDouble(key) - baseValue;
                        attributeModifierAmount.put(attribute, modifierValue);
                    }
                }
            }
        }

        if(transitionProgress < 1.0F) //we still have a prev state, aka still morphing
        {
            HashSet<Attribute> prevStateAttrs = new HashSet<>();
            for(Map.Entry<String, INBT> e : prevState.variant.nbtMorph.tagMap.entrySet())
            {
                String key = e.getKey();
                if(key.startsWith("attr_")) //it's an attribute key
                {
                    ResourceLocation id = new ResourceLocation(key.substring(5));
                    Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                    if(attribute != null)
                    {
                        final ModifiableAttributeInstance playerAttribute = player.getAttribute(attribute);
                        if(playerAttribute != null)
                        {
                            double baseValue = player.getBaseAttributeValue(attribute);
                            double modifierValue = prevState.variant.nbtMorph.getDouble(key) - baseValue;

                            if(attributeModifierAmount.containsKey(attribute)) //the nextState also has this attribute
                            {
                                double val = modifierValue + (attributeModifierAmount.get(attribute) - modifierValue) * transitionProgress;
                                attributeModifierAmount.put(attribute, val);
                            }
                            else
                            {
                                attributeModifierAmount.put(attribute, modifierValue * (1F - transitionProgress)); //the strength of the attribute approaches 0
                            }
                            prevStateAttrs.add(attribute);
                        }
                    }
                }
            }

            for(Map.Entry<Attribute, Double> e : attributeModifierAmount.entrySet())
            {
                if(!prevStateAttrs.contains(e.getKey())) //this is added by nextState, we need to decrease the modifier since we're still transitioning
                {
                    e.setValue(e.getValue() * transitionProgress);
                }
            }
        }

        //add these modifiers to the player
        for(Map.Entry<Attribute, Double> e : attributeModifierAmount.entrySet())
        {
            final ModifiableAttributeInstance playerAttribute = player.getAttribute(e.getKey());
            if(playerAttribute != null)
            {
                rand.setSeed(Math.abs("MorphAttr".hashCode() * 1231543 + e.getKey().getRegistryName().toString().hashCode() * 268));
                UUID uuid = MathHelper.getRandomUUID(rand);

                double lastRatio = 0D;

                if(!player.world.isRemote && playerAttribute.getAttribute().equals(Attributes.MAX_HEALTH)) //special casing for the max health
                {
                    lastRatio = player.getHealth() / player.getMaxHealth();
                }

                //you can't reapply the same modifier, so lets remove it
                playerAttribute.removePersistentModifier(uuid);

                if(e.getValue() != 0) //if the modifier is non-zero, add it
                {
                    playerAttribute.applyPersistentModifier(new AttributeModifier(uuid, "MorphAttributeModifier:" + e.getKey().getRegistryName().toString(), e.getValue(), AttributeModifier.Operation.ADDITION));

                    if(lastRatio > 0D) //we're doing the max health
                    {
                        double currentRatio = player.getHealth() / player.getMaxHealth();

                        if(currentRatio != lastRatio) //if ratio is different, change the health
                        {
                            double targetHealth = lastRatio * player.getMaxHealth();
                            double extraHealth = targetHealth - player.getHealth();

                            Morph.channel.sendTo(new PacketInvalidateClientHealth(), (ServerPlayerEntity)player);
                            player.setHealth(player.getHealth() + (float)extraHealth); //I think this would work?
                        }
                    }
                }
            }
        }
    }

    public void removeAttributeModifiersFromPrevState()
    {
        if(prevState != null) //just in case?
        {
            HashSet<Attribute> attributesToRemove = new HashSet<>();

            //Add the prev state's attributes
            for(Map.Entry<String, INBT> e : prevState.variant.nbtMorph.tagMap.entrySet())
            {
                String key = e.getKey();
                if(key.startsWith("attr_")) //it's an attribute key
                {
                    ResourceLocation id = new ResourceLocation(key.substring(5));
                    Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                    if(attribute != null)
                    {
                        attributesToRemove.add(attribute);
                    }
                }
            }

            //Add the prev state's attributes
            for(Map.Entry<String, INBT> e : nextState.variant.nbtMorph.tagMap.entrySet())
            {
                String key = e.getKey();
                if(key.startsWith("attr_")) //it's an attribute key
                {
                    ResourceLocation id = new ResourceLocation(key.substring(5));
                    Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                    if(attribute != null)
                    {
                        attributesToRemove.remove(attribute);
                    }
                }
            }

            for(Attribute attribute : attributesToRemove)
            {
                final ModifiableAttributeInstance playerAttribute = player.getAttribute(attribute);
                if(playerAttribute != null)
                {
                    rand.setSeed(Math.abs("MorphAttr".hashCode() * 1231543 + attribute.getRegistryName().toString().hashCode() * 268));
                    UUID uuid = MathHelper.getRandomUUID(rand);
                    playerAttribute.removePersistentModifier(uuid);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void endMorphOnClient()
    {
        if(transitionState != null)
        {
            transitionState = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected float getAbilitySkinAlpha(float partialTick)
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
}
