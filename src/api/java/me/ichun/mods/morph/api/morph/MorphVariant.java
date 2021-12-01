package me.ichun.mods.morph.api.morph;

import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class MorphVariant implements Comparable<MorphVariant>
{
    public static final int IDENTIFIER_LENGTH = 20;
    public static final String IDENTIFIER_DEFAULT_PLAYER_STATE = "default_player_state";
    public static final String NBT_PLAYER_ID = "Morph_Player_ID";
    public static String[] TAGS_TO_TAKE = new String[] { "CustomName", "CustomNameVisible", "ForgeCaps", "ForgeData" }; //Intentionally non-final. If you're going to be adding to this please remember to include tthe originals!

    @Nonnull
    public ResourceLocation id; // the ID of the morph
    @Nonnull
    public CompoundNBT nbtMorph; //special morph specific NBT
    public CompoundNBT nbtCommon; //common nbt tags shared by all variants
    public ArrayList<Variant> variants; //if populated, thisVariant should not be used.

    public Variant thisVariant; //this is set for a specific variant/render. variants should be left empty.

    public MorphVariant(ResourceLocation id)
    {
        this.id = id;
        this.nbtMorph = new CompoundNBT();
        this.variants = new ArrayList<>();
    }

    private MorphVariant()
    {
        this.variants = new ArrayList<>();
    }

    public void setLiving(CompoundNBT tag) //Not used for PLAYERS
    {
        nbtCommon = tag;
    }

    public void writeSupportedAttributes(LivingEntity living)
    {
        for(Map.Entry<ResourceLocation, AttributeConfig> e : MorphApi.getApiImpl().getSupportedAttributes().entrySet())
        {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(e.getKey());
            if(attribute != null && living.getAttributeManager().hasAttributeInstance(attribute))
            {
                AttributeConfig attributeConfig = e.getValue();
                double value = living.getAttributeValue(attribute);
                if(attributeConfig.moreIsBetter) //more is better
                {
                    if(attributeConfig.cap != null && value > attributeConfig.cap)
                    {
                        value = attributeConfig.cap;
                    }
                }
                else //less is better
                {
                    if(attributeConfig.cap != null && value < attributeConfig.cap)
                    {
                        value = attributeConfig.cap;
                    }
                }

                nbtMorph.putDouble("attr_" + e.getKey().toString(), value);
            }
        }
    }

    public void writeDefaults(LivingEntity living, CompoundNBT tag) //taken from Entity.writeWithoutTypeId
    {
        CompoundNBT defs = new CompoundNBT();

        living.writeWithoutTypeId(defs); //because I can't copy out serialiseCaps

        for(String s : TAGS_TO_TAKE)
        {
            if(defs.tagMap.containsKey(s))
            {
                tag.tagMap.put(s, defs.tagMap.get(s));
            }
        }
    }

    public boolean hasVariants()
    {
        return !variants.isEmpty();
    }

    public boolean combineVariants(MorphVariant variant)
    {
        if(!isSameMorphType(variant))
        {
            return false;
        }

        //special handling for players
        if(id.equals(EntityType.PLAYER.getRegistryName()))
        {
            variants.add(variant.thisVariant);
            return true;
        }

        //Compare the tags for living entities.
        addBetterMorphData(variant.nbtMorph);

        CompoundNBT variantTag = variant.getCumulativeTags();

        //compare with our commons first to see what doesn't match.
        HashSet<String> uncommons = new HashSet<>();
        for(Map.Entry<String, INBT> e : nbtCommon.tagMap.entrySet())
        {
            INBT varNBT = variantTag.tagMap.get(e.getKey());

            if(varNBT == null || !varNBT.equals(e.getValue())) //uncommon, mark for cloning in all the other variants
            {
                uncommons.add(e.getKey());
            }
            else //common value, remove it from their variants.
            {
                variantTag.tagMap.remove(e.getKey());
            }
        }

        //add the now uncommon to the existing variants, and remove the previous common, it's not common anymore.
        for(String key : uncommons)
        {
            INBT nbt = nbtCommon.get(key);
            for(Variant aVariant : variants)
            {
                aVariant.nbtVariant.tagMap.put(key, nbt.copy());
            }
            nbtCommon.remove(key);
        }

        //the commons have been stripped so what remains is the variant.
        variant.thisVariant.nbtVariant = variantTag;

        variants.add(variant.thisVariant);

        //we've fixed the uncommons... now get the new commons.
        gatherNewCommons();

        return true;
    }

    public boolean removeVariant(Variant variant)
    {
        boolean flag = false;
        for(int i = variants.size() - 1; i >= 0; i--)
        {
            if(variants.get(i).identifier.equals(variant.identifier))
            {
                variants.remove(i);
                flag = true;
            }
        }

        if(flag && !id.equals(EntityType.PLAYER.getRegistryName())) //player morphs don't have commons
        {
            if(variants.size() >= 2)
            {
                gatherNewCommons();
            }
            else if(!variants.isEmpty()) //Only one variant left
            {
                variants.get(0).nbtVariant.tagMap.putAll(nbtCommon.tagMap);
                nbtCommon.tagMap.clear(); // no more commons
            }
            else //no more variants, aka no more common tags.
            {
                nbtCommon.tagMap.clear();
            }
        }

        return flag;
    }

    public Variant getVariantById(String id)
    {
        for(Variant variant : variants)
        {
            if(variant.identifier.equals(id))
            {
                return variant;
            }
        }

        if(thisVariant != null && thisVariant.identifier.equals(id))
        {
            return thisVariant;
        }

        return null;
    }

    public void gatherNewCommons()
    {
        HashMap<String, INBT> commons = new HashMap<>();

        //add all the tags we know of first
        for(Variant variant : variants)
        {
            commons.putAll(variant.nbtVariant.tagMap);
        }

        //now we compare
        commons.entrySet().removeIf(e -> {
            for(Variant variant : variants)
            {
                if(!variant.nbtVariant.tagMap.containsKey(e.getKey()) || !e.getValue().equals(variant.nbtVariant.tagMap.get(e.getKey())))
                {
                    return true;
                }
            }
            return false;
        });

        //remove from the variants
        nbtCommon.tagMap.putAll(commons);
        for(String s : commons.keySet())
        {
            for(Variant variant : variants)
            {
                variant.nbtVariant.tagMap.remove(s);
            }
        }
    }

    public boolean containsVariant(MorphVariant variant)
    {
        //special handling for players
        if(id.equals(EntityType.PLAYER.getRegistryName()))
        {
            for(Variant aVariant : variants)
            {
                if(aVariant.playerUUID.equals(variant.thisVariant.playerUUID))
                {
                    return true;
                }
            }
        }
        else if(!variant.id.equals(EntityType.PLAYER.getRegistryName()))
        {
            CompoundNBT variantTags = variant.getCumulativeTags();

            for(Variant aVariant : variants)
            {
                CompoundNBT aVariantTags = getCumulativeTagsWithVariant(aVariant);

                if(variantTags.equals(aVariantTags))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isSameMorphType(MorphVariant variant)
    {
        return id.equals(variant.id);
    }

    private boolean addBetterMorphData(CompoundNBT tag) //returns true when the data is better.
    {
        boolean flag = false;

        Map<ResourceLocation, AttributeConfig> supportedAttributes = MorphApi.getApiImpl().getSupportedAttributes();

        for(Map.Entry<String, INBT> e : nbtMorph.tagMap.entrySet())
        {
            String key = e.getKey();
            if(key.startsWith("attr_")) //it's an attribute key
            {
                ResourceLocation id = new ResourceLocation(key.substring(5));
                if(supportedAttributes.containsKey(id))
                {
                    AttributeConfig attributeConfig = supportedAttributes.get(id);
                    final double value = tag.getDouble(key);
                    if(attributeConfig.moreIsBetter) //more is better
                    {
                        if(nbtMorph.getDouble(key) < value)
                        {
                            nbtMorph.putDouble(key, value);

                            if(attributeConfig.cap != null && value > attributeConfig.cap)
                            {
                                nbtMorph.putDouble(key, attributeConfig.cap);
                            }
                            flag = true;
                        }
                    }
                    else //less is better
                    {
                        if(nbtMorph.getDouble(key) > value)
                        {
                            nbtMorph.putDouble(key, value);

                            if(attributeConfig.cap != null && value < attributeConfig.cap)
                            {
                                nbtMorph.putDouble(key, attributeConfig.cap);
                            }
                            flag = true;
                        }
                    }
                }
            }
        }

        for(Map.Entry<String, INBT> e : tag.tagMap.entrySet())
        {
            if(e.getKey().startsWith("attr_") && !nbtMorph.contains(e.getKey()))
            {
                nbtMorph.tagMap.put(e.getKey(), e.getValue());
                flag = true;
            }
        }
        return flag;
    }

    public boolean hasFavourite()
    {
        for(Variant variant : variants)
        {
            if(variant.isFavourite)
            {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public LivingEntity createEntityInstance(World world, @Nullable UUID playerId)
    {
        LivingEntity entInstance = null;
        EntityType<?> value = ForgeRegistries.ENTITIES.getValue(id);
        if(value != null)
        {
            try
            {
                if(value.equals(EntityType.PLAYER))
                {
                    entInstance = world.isRemote ? createPlayer(world, thisVariant.playerUUID) : new FakePlayer((ServerWorld)world, MorphApi.getApiImpl().getGameProfile(thisVariant.playerUUID, null));
                }
                else
                {
                    CompoundNBT tags = getCumulativeTags();
                    Entity ent = value.create(world);
                    if(ent instanceof LivingEntity)
                    {
                        ent.read(tags);

                        entInstance = (LivingEntity)ent;

                        for(BiConsumer<LivingEntity, CompoundNBT> consumer : MorphApi.getApiImpl().getVariantNbtTagReaders())
                        {
                            consumer.accept(entInstance, tags);
                        }
                    }
                }
            }
            catch(Throwable t)
            {
                MorphApi.getLogger().error("Error creating Morph entity for ID: {}", id);
                t.printStackTrace();
            }
        }

        if(entInstance == null) //we can't find the entity type or errored out somewhere... have a pig.
        {
            MorphApi.getLogger().error("Cannot find entity type {} have a pig instead!", id);
            entInstance = EntityType.PIG.create(world);
            entInstance.setCustomName(new StringTextComponent("Invalid Morph Pig"));
        }

        entInstance.setEntityId(MorphInfo.getNextEntId()); //to prevent ID collision

        if(playerId != null)
        {
            entInstance.getPersistentData().putUniqueId(NBT_PLAYER_ID, playerId);
        }

        return entInstance;
    }

    @OnlyIn(Dist.CLIENT)
    private PlayerEntity createPlayer(World world, UUID uuid)
    {
        RemoteClientPlayerEntity player = new RemoteClientPlayerEntity((ClientWorld)world, MorphApi.getApiImpl().getGameProfile(uuid, null));
        player.getDataManager().set(PlayerEntity.PLAYER_MODEL_FLAG, (byte)127); //All model parts shown
        return player;
    }

    public MorphVariant getAsVariant(Variant variant)
    {
        MorphVariant morph = createFromNBT(write(new CompoundNBT()));
        morph.variants.clear();
        morph.thisVariant = variant;

        return morph;
    }

    public CompoundNBT getCumulativeTags()
    {
        return getCumulativeTagsWithVariant(thisVariant);
    }

    public CompoundNBT getCumulativeTagsWithVariant(Variant variant)
    {
        CompoundNBT tags = new CompoundNBT();

        tags.tagMap.putAll(nbtCommon.tagMap);
        tags.tagMap.putAll(variant.nbtVariant.tagMap);

        return tags;
    }

    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putString("id", id.toString());
        tag.put("nbtMorph", nbtMorph);
        if(!id.equals(EntityType.PLAYER.getRegistryName()))
        {
            tag.put("nbtCommon", nbtCommon);
        }

        tag.putInt("variantCount", variants.size());
        for(int i = 0; i < variants.size(); i++)
        {
            tag.put("variant_" + i, variants.get(i).write(new CompoundNBT()));
        }

        if(thisVariant != null)
        {
            tag.put("thisVariant", thisVariant.write(new CompoundNBT()));
        }
        return tag;
    }

    public void read(CompoundNBT tag)
    {
        id = new ResourceLocation(tag.getString("id"));
        nbtMorph = tag.getCompound("nbtMorph");
        if(!id.equals(EntityType.PLAYER.getRegistryName()))
        {
            nbtCommon = tag.getCompound("nbtCommon");
        }

        variants.clear();
        int count = tag.getInt("variantCount");
        for(int i = 0; i < count; i++)
        {
            Variant variant = new Variant();
            variant.read(tag.getCompound("variant_" + i));
            variants.add(variant);
        }

        if(tag.contains("thisVariant"))
        {
            Variant variant = new Variant();
            variant.read(tag.getCompound("thisVariant"));
            thisVariant = variant;
        }
    }

    @Override
    public boolean equals(Object obj) //only used for a single variant
    {
        if(obj instanceof MorphVariant)
        {
            MorphVariant variant = (MorphVariant)obj;

            if(id.equals(variant.id) && thisVariant != null && variant.thisVariant != null)
            {
                if(id.equals(EntityType.PLAYER.getRegistryName()))
                {
                    return thisVariant.playerUUID.equals(variant.thisVariant.playerUUID);
                }
                else
                {
                    return getCumulativeTags().equals(variant.getCumulativeTags());
                }
            }
        }
        return false;
    }

    @Override
    public int compareTo(MorphVariant o)
    {
        if(id.equals(EntityType.PLAYER.getRegistryName()) && !id.equals(o.id)) //this is a player morph. always first
        {
            return -1; //we're before...
        }
        else if(o.id.equals(EntityType.PLAYER.getRegistryName()) && !id.equals(o.id))
        {
            return 1;
        }

        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
        EntityType<?> otherType = ForgeRegistries.ENTITIES.getValue(o.id);
        if(type != null)
        {
            if(otherType != null)
            {
                if(EffectiveSide.get().isClient())
                {
                    return I18n.format(type.getTranslationKey()).compareTo(I18n.format(otherType.getTranslationKey()));
                }
                return type.getTranslationKey().compareTo(otherType.getTranslationKey());
            }
            return -1; //we have a type, we're before
        }
        else
        {
            if(otherType == null)
            {
                return 0; //they also don't have a type, no comparator
            }
            return 1;//we don't have a type
        }
    }

    public static MorphVariant createFromNBT(CompoundNBT tag)
    {
        MorphVariant variant = new MorphVariant();
        variant.read(tag);
        return variant;
    }

    public static MorphVariant createPlayerMorph(UUID owner, boolean isVariant) //creates the base morph + variant of the player.
    {
        MorphVariant variant = new MorphVariant(EntityType.PLAYER.getRegistryName());
        Variant var = new Variant();
        var.playerUUID = owner;
        if(isVariant)
        {
            variant.thisVariant = var;
        }
        else
        {
            variant.variants.add(var);
        }

        return variant;
    }

    @Override
    public int hashCode()
    {
        return thisVariant != null ? thisVariant.hashCode() : super.hashCode();
    }

    public static class Variant
    {
        public String identifier;
        public UUID playerUUID; // for player morphs
        public CompoundNBT nbtVariant;
        public boolean isFavourite;

        public Variant()
        {
            this.identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);
            this.nbtVariant = new CompoundNBT();
            this.isFavourite = false;
        }

        public CompoundNBT write(CompoundNBT tag)
        {
            tag.putString("identifier", identifier);
            if(playerUUID != null)
            {
                tag.putUniqueId("playerUUID", playerUUID);
            }
            else
            {
                tag.put("nbtVariant", nbtVariant);
            }
            tag.putBoolean("isFavourite", isFavourite);
            return tag;
        }

        public void read(CompoundNBT tag)
        {
            identifier = tag.getString("identifier");
            if(tag.contains("playerUUID"))
            {
                playerUUID = tag.getUniqueId("playerUUID");
            }
            else
            {
                nbtVariant = tag.getCompound("nbtVariant");
            }
            isFavourite = tag.getBoolean("isFavourite");
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof Variant)
            {
                return playerUUID != null ? playerUUID.equals(((Variant)obj).playerUUID) : nbtVariant.equals(((Variant)obj).nbtVariant);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return identifier.hashCode();
        }
    }
}
