package me.ichun.mods.morph.api.morph;

import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class MorphVariant implements Comparable<MorphVariant>
{
    public static final int IDENTIFIER_LENGTH = 20;
    public static final String IDENTIFIER_DEFAULT_PLAYER_STATE = "default_player_state";

    @Nonnull
    public ResourceLocation id; // the ID of the morph
    @Nonnull
    public CompoundNBT nbtMorph; //special morph specific NBT
    public CompoundNBT nbtCommon; //common nbt tags shared by all variants
    public ArrayList<Variant> variants; //empty if it is not of a save. if populated, thisVariant should not be used.

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
        for(Map.Entry<ResourceLocation, Boolean> e : MorphApi.getApiImpl().getSupportedAttributes().entrySet())
        {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(e.getKey());
            if(attribute != null && living.getAttributeManager().hasAttributeInstance(attribute))
            {
                nbtMorph.putDouble("attr_" + e.getKey().toString(), living.getAttributeValue(attribute));
            }
        }
    }

    public void writeDefaults(LivingEntity living, CompoundNBT tag) //taken from Entity.writeWithoutTypeId
    {
        CompoundNBT defs = new CompoundNBT();

        living.writeWithoutTypeId(defs); //because I can't copy out serialiseCaps

        String[] tagsToTake = new String[] { "CustomName", "CustomNameVisible", "ForgeCaps", "ForgeData" };

        for(String s : tagsToTake)
        {
            if(defs.tagMap.containsKey(s))
            {
                tag.tagMap.put(s, defs.tagMap.get(s));
            }
        }
    }

    public void writeSpecialTags(LivingEntity living, CompoundNBT tag)
    {
        if(living instanceof AgeableEntity) //ForcedAge is only called when eating, useless for keeping a mob a baby.
        {
            tag.putInt("Age", living.isChild() ? -24000 : 0);
        }
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

        HashSet<String> uncommons = new HashSet<>();

        //compare with our commons first to see what doesn't match.
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

        //add the now uncommon to the existing variants
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

        return true;
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
        else
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

        Map<ResourceLocation, Boolean> supportedAttributes = MorphApi.getApiImpl().getSupportedAttributes();

        for(Map.Entry<String, INBT> e : nbtMorph.tagMap.entrySet())
        {
            String key = e.getKey();
            if(key.startsWith("attr_")) //it's an attribute key
            {
                ResourceLocation id = new ResourceLocation(key.substring(5));
                if(supportedAttributes.containsKey(id))
                {
                    if(supportedAttributes.get(id)) //more is better
                    {
                        if(nbtMorph.getDouble(key) < tag.getDouble(key))
                        {
                            nbtMorph.putDouble(key, tag.getDouble(key));
                            flag = true;
                        }
                    }
                    else //less is better
                    {
                        if(nbtMorph.getDouble(key) > tag.getDouble(key))
                        {
                            nbtMorph.putDouble(key, tag.getDouble(key));
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

    @Nonnull
    public LivingEntity createEntityInstance(World world)
    {
        LivingEntity entInstance = null;
        EntityType<?> value = ForgeRegistries.ENTITIES.getValue(id);
        if(value != null)
        {
            if(value.equals(EntityType.PLAYER))
            {
                //TODO special handling for the player
            }
            else
            {
                CompoundNBT tags = getCumulativeTags();

                Entity ent = value.create(world);
                if(ent instanceof LivingEntity)
                {
                    ent.read(tags);

                    entInstance = (LivingEntity)ent;
                    entInstance.setEntityId(MorphInfo.getNextEntId()); //to prevent ID collision
                }
            }
        }

        if(entInstance == null) //we can't find the entity type or errored out somewhere... have a pig.
        {
            MorphApi.getLogger().error("Cannot find entity type: " + id);
            entInstance = EntityType.PIG.create(world);
        }

        return entInstance;
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
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(id);
        EntityType<?> otherType = ForgeRegistries.ENTITIES.getValue(o.id);
        if(type != null)
        {
            if(otherType != null)
            {
                return type.getName().getUnformattedComponentText().compareTo(otherType.getName().getUnformattedComponentText());
            }
            return 1; //we have a type, we're greater
        }
        else
        {
            if(otherType == null)
            {
                return 0; //they also don't have a type, no comparator
            }
            return -1;//we don't have a type
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
    }
}
