package me.ichun.mods.morph.common.morph;

import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.NBTHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

public class MorphVariant
        implements Comparable<MorphVariant>
{
    public static final int VARIANT_PROTOCOL = 1;
    public static final int IDENTIFIER_LENGTH = 20;

    public static final String PLAYER_MORPH_ID = "PlayerMorph";

    public String entId;
    public String playerName;
    public NBTTagCompound entTag;
    public NBTTagCompound instanceTag;
    public NBTTagCompound morphData;
    public Variant thisVariant;
    public ArrayList<Variant> variants;

    public MorphVariant(String entId)
    {
        this.entId = entId;
        this.playerName = "";
        this.entTag = new NBTTagCompound();
        this.instanceTag = new NBTTagCompound();
        this.morphData = new NBTTagCompound();
        this.thisVariant = new Variant();
        this.variants = new ArrayList<>();
    }

    public MorphVariant setPlayer(EntityPlayer player)
    {
        playerName = player.getName();
        entTag.setString("UUID", player.getGameProfile().getId().toString());
        return this;
    }

    //NEVER RETURNS NULL
    public EntityLivingBase createEntityInstance(World world)
    {
        if(entId.equals(PLAYER_MORPH_ID))
        {
            if(!playerName.isEmpty())
            {
                return world.isRemote ? createPlayer(world, playerName) : new FakePlayer((WorldServer)world, EntityHelper.getGameProfile(playerName));
            }
            else
            {
                thisVariant.invalid = true;
            }
        }
        else
        {
            NBTTagCompound instanceCreator = (NBTTagCompound)instanceTag.copy();
            instanceCreator.tagMap.putAll(entTag.tagMap);
            instanceCreator.tagMap.putAll(thisVariant.variantData.tagMap);
            thisVariant.tagsToRemove.forEach(instanceCreator.tagMap::remove);
            Entity ent = EntityList.createEntityFromNBT(instanceCreator, world);
            if(ent instanceof EntityLivingBase)
            {
                return (EntityLivingBase)ent;
            }
            else
            {
                thisVariant.invalid = true;
            }
        }

        //by this point the variant would have been marked as invalid and a fake pig should be created to return as the entity.

        EntityPig pig = (EntityPig)EntityList.newEntity(EntityPig.class, world);
        NBTTagCompound fakeTag = new NBTTagCompound();
        pig.writeEntityToNBT(fakeTag);
        clean(pig, fakeTag);
        pig.readEntityFromNBT(fakeTag);

        return pig;
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer createPlayer(World world, String player)
    {
        GameProfile gp = EntityHelper.getGameProfile(player);
        if(Minecraft.getMinecraft().getConnection().getPlayerInfo(gp.getId()) == null)
        {
            Minecraft.getMinecraft().getConnection().playerInfoMap.put(gp.getId(), new NetworkPlayerInfo(gp));
        }
        return new EntityOtherPlayerMP(world, gp);
    }

    public ArrayList<MorphVariant> split() //Returns a new copy of all the variants in the list.
    {
        ArrayList<MorphVariant> vars = new ArrayList<>();
        NBTTagCompound tag = write(new NBTTagCompound());
        MorphVariant current = new MorphVariant(entId);
        current.read(tag);
        current.variants.clear();

        vars.add(current);

        for(Variant var : variants)
        {
            MorphVariant variant = new MorphVariant(entId);
            variant.read(tag);
            variant.variants.clear();
            variant.thisVariant = var;
            vars.add(variant);
        }

        return vars;
    }

    public MorphVariant createWithVariant(Variant var)
    {
        NBTTagCompound tag = write(new NBTTagCompound());
        MorphVariant variant = new MorphVariant(entId);
        variant.read(tag);
        variant.variants.clear();
        variant.thisVariant = var;
        return variant;
    }

    public void read(NBTTagCompound tag)
    {
        int varProtocol = tag.getInteger("varProtocol");

        entId = tag.getString("entId");
        playerName = tag.getString("playerName");
        entTag = tag.getCompoundTag("entTag");
        instanceTag = tag.getCompoundTag("instanceTag");
        morphData = tag.getCompoundTag("morphData");
        thisVariant.read(tag.getCompoundTag("thisVariant"));

        int variantSize = tag.getInteger("variantCount");
        for(int i = 0; i < variantSize; i++)
        {
            Variant variant = new Variant();
            variant.read(tag.getCompoundTag("variant_" + i));

            if(!variant.identifier.isEmpty() && !variant.invalid)
            {
                variants.add(variant);
            }
        }

        repair(varProtocol);
    }

    public NBTTagCompound write(NBTTagCompound tag)
    {
        tag.setInteger("varProtocol", VARIANT_PROTOCOL);

        tag.setString("entId", entId);
        tag.setString("playerName", playerName);
        tag.setTag("entTag", entTag);
        tag.setTag("instanceTag", instanceTag);
        tag.setTag("morphData", morphData);

        NBTTagCompound thisVarTag = new NBTTagCompound();
        thisVariant.write(thisVarTag);
        tag.setTag("thisVariant", thisVarTag);

        tag.setInteger("variantCount", variants.size());
        for(int i = 0; i < variants.size(); i++)
        {
            Variant variant = variants.get(i);

            NBTTagCompound varTag = new NBTTagCompound();
            variant.write(varTag);
            tag.setTag("variant_" + i, varTag);
        }

        return tag;
    }

    public void repair(int varProtocol)
    {
        //nothing here yet.
    }

    public Variant getVariantByIdentifier(String ident)
    {
        if(thisVariant.identifier.equals(ident))
        {
            return thisVariant;
        }
        for(Variant variant : variants)
        {
            if(variant.identifier.equals(ident))
            {
                return variant;
            }
        }
        return null;
    }

    public boolean deleteVariant(Variant var) //returns true if all variants have been deleted and this variant needs to be removed from the list
    {
        if(var.identifier.equals(thisVariant.identifier))
        {
            if(variants.isEmpty())
            {
                return true;
            }
            else
            {
                thisVariant = variants.get(0);
                variants.remove(0);
                return false;
            }
        }

        //Not the current variant so we need to find the variant in the variants list and remove it. Return false cause current variant is still valid
        for(int i = variants.size() - 1; i >= 0; i--)
        {
            Variant var1 = variants.get(i);
            if(var.identifier.equals(var1.identifier))
            {
                variants.remove(i);
                break;
            }
        }
        return false;
    }

    public NBTTagCompound getVariantTag()
    {
        NBTTagCompound tag = (NBTTagCompound)entTag.copy();
        tag.tagMap.putAll(thisVariant.variantData.tagMap);
        thisVariant.tagsToRemove.forEach(tag.tagMap::remove);
        return tag;
    }

    public static MorphVariant createVariant(EntityLivingBase living)
    {
        if(living instanceof EntityPlayer)
        {
            return new MorphVariant(PLAYER_MORPH_ID).setPlayer((EntityPlayer)living);
        }

        NBTTagCompound saveData = new NBTTagCompound();

        //Modify this stupid thing
        if(living instanceof EntityHorse)
        {
            EntityHorse horse = (EntityHorse)living;
            horse.setHorseVariant((horse.getHorseVariant() & 255) % 7);
        }
        //End modify stupid thing

        if(!living.writeToNBTOptional(saveData))
        {
            return null;
        }

        MorphVariant variant = new MorphVariant(saveData.getString("id"));
        variant.instanceTag = saveData;
        clean(living, variant.instanceTag);

        living.writeEntityToNBT(variant.entTag);
        clean(living, variant.entTag);

        variant.entTag.setDouble("Morph_HealthBalancing", MathHelper.clamp(living.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue(), 0D, 20D)); //For health balancing reasons for now.

        if(variant.instanceTag.tagMap.containsKey("ForgeData")) // send the ForgeData to the variant tag.
        {
            variant.entTag.tagMap.put("ForgeData", variant.instanceTag.tagMap.get("ForgeData"));
        }
        if (living.getCustomNameTag() != null && living.getCustomNameTag().length() > 0)
        {
            variant.entTag.setString("CustomName", living.getCustomNameTag());
            variant.entTag.setBoolean("CustomNameVisible", living.getAlwaysRenderNameTag());
        }
        variant.instanceTag.removeTag("ForgeData");
        variant.instanceTag.removeTag("CustomName");
        variant.instanceTag.removeTag("CustomNameVisible");

        return variant;
    }

    public static void clean(EntityLivingBase living, NBTTagCompound tag)
    {
        NBTHandler.modifyNBT(living.getClass(), tag);

        //EntityLivingBase tags
        tag.setFloat("HealF", Short.MAX_VALUE);
        tag.setShort("Health", Short.MAX_VALUE);

        //EntityAgeable tags
        tag.setInteger("Age", living.isChild() ? -24000 : 0);

        //EntityTameable tags
        //        tag.removeTag("Sitting"); //we're making an ability for this maybe?

        //EntityLiving tags
        if(living instanceof EntityLiving)
        {
            tag.setBoolean("CanPickUpLoot", true);
            tag.setBoolean("PersistenceRequired", true);
            tag.setBoolean("NoAI", true);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof MorphVariant)
        {
            MorphVariant var = (MorphVariant)o;
            if(entId.equals(PLAYER_MORPH_ID))
            {
                return var.entId.equals(PLAYER_MORPH_ID) && playerName.equals(var.playerName);
            }
            return entId.equals(var.entId) && playerName.equals(var.playerName) && entTag.equals(var.entTag) && thisVariant.identifier.equals(var.thisVariant.identifier); //Do not compare the variant arraylist? >_>
        }
        return false;
    }

    /**
     * Returns false if variants were not combined successfully or if the variant already exists
     * You cannot combine player variants even though they might be categorised together!
     * Returns -2 for failed merge. -1 for thisVariant. 0-X for a index of variants.
     */
    public static int combineVariants(MorphVariant source, MorphVariant variantToMerge)
    {
        if(source.entId.equals(PLAYER_MORPH_ID) || variantToMerge.entId.equals(PLAYER_MORPH_ID) || !source.entId.equals(variantToMerge.entId))
        {
            return -2;
        }

        NBTTagCompound tagCopy = (NBTTagCompound)source.entTag.copy();

        double sourceHealth = tagCopy.getDouble("Morph_HealthBalancing");
        double variantHealth = variantToMerge.entTag.getDouble("Morph_HealthBalancing");
        tagCopy.removeTag("Morph_HealthBalancing");
        variantToMerge.entTag.removeTag("Morph_HealthBalancing");
        if(tagCopy.equals(variantToMerge.entTag)) //Compare variant with current variant
        {
            if(variantHealth > sourceHealth) //Give this variant a new health
            {
                source.entTag.setDouble("Morph_HealthBalancing", variantHealth);
                for(Variant variant : source.variants)
                {
                    if(variant.variantData.getDouble("Morph_HealthBalancing") == variantHealth) //remove this variant since the original has the number and the tag
                    {
                        variant.variantData.removeTag("Morph_HealthBalancing");
                    }
                    else if(!variant.variantData.hasKey("Morph_HealthBalancing")) // this variant didn't have a morph health balancing tag. Add the old one.
                    {
                        variant.variantData.setDouble("Morph_HealthBalancing", sourceHealth);
                    }
                }
            }
            return -1;
        }

        ArrayList<Variant> variants1 = source.variants;
        for(int i = 0; i < variants1.size(); i++)
        {
            Variant variant = variants1.get(i);
            NBTTagCompound tagCopyCopy = (NBTTagCompound)tagCopy.copy();
            tagCopyCopy.tagMap.putAll(variant.variantData.tagMap);
            variant.tagsToRemove.forEach(tagCopyCopy.tagMap::remove);

            sourceHealth = tagCopyCopy.getDouble("Morph_HealthBalancing");
            tagCopyCopy.removeTag("Morph_HealthBalancing");
            if(tagCopyCopy.equals(variantToMerge.entTag))
            {
                if(variantHealth > sourceHealth && sourceHealth > 0D) //Give this variant a new health
                {
                    variant.variantData.setDouble("Morph_HealthBalancing", variantHealth);
                }
                return i;
            }
        }

        //At this point, this variant is considered "unique"
        variantToMerge.entTag.setDouble("Morph_HealthBalancing", variantHealth);

        //Create the variant
        Variant variant = new Variant();
        //Get tags to be removed
        for(Object obj : source.entTag.tagMap.entrySet())
        {
            Map.Entry<String, NBTBase> e = (Map.Entry<String, NBTBase>)obj;
            String key = e.getKey();
            if(!variantToMerge.entTag.tagMap.containsKey(key))
            {
                variant.tagsToRemove.add(key);
            }
        }
        for(Object obj : variantToMerge.entTag.tagMap.entrySet())
        {
            Map.Entry<String, NBTBase> e = (Map.Entry<String, NBTBase>)obj;
            String key = e.getKey();
            if(!source.entTag.tagMap.containsKey(key) || !source.entTag.tagMap.get(key).equals(e.getValue()))
            {
                //IF YOU ARE READING THIS. MY HEAD HURT TRYING TO MAKE THIS CLASS.
                //I HOPE IT MAKES YOUR HEAD HURT TOO.
                variant.variantData.tagMap.put(key, e.getValue());
            }
        }

        //Add the variant to the variants list. Ensure this entry is the last entry added, it is used to update the player of the new variant.
        source.variants.add(variant);

        return source.variants.size() - 1;
    }

    @Override
    public String toString()
    {
        return entId + "_" + playerName + "_" + thisVariant.identifier + "_" + entTag.toString();
    }

    @Override
    public int compareTo(MorphVariant var)
    {
        if(entId.equals(PLAYER_MORPH_ID) && var.entId.equals(PLAYER_MORPH_ID))
        {
            return playerName.compareTo(var.playerName);
        }
        else if(entId.equals(var.entId))
        {
            TreeMap<String, NBTBase> map = new TreeMap<>(Ordering.natural());
            TreeMap<String, NBTBase> varmap = new TreeMap<>(Ordering.natural());
            NBTTagCompound tag = getVariantTag();
            NBTTagCompound vartag = var.getVariantTag();
            map.putAll(tag.tagMap);
            varmap.putAll(vartag.tagMap);
            map.remove("Morph_HealthBalancing");
            varmap.remove("Morph_HealthBalancing");
            if(map.size() == varmap.size())
            {
                Iterator<Map.Entry<String, NBTBase>> ite = map.entrySet().iterator();
                Iterator<Map.Entry<String, NBTBase>> varite = varmap.entrySet().iterator();
                while(ite.hasNext())
                {
                    Map.Entry<String, NBTBase> e = ite.next();
                    Map.Entry<String, NBTBase> vare = varite.next();
                    if(e.getKey().equals(vare.getKey()))
                    {
                        if(e.getValue().toString().equals(vare.getValue().toString()))
                        {
                            continue;
                        }
                        else
                        {
                            return e.getValue().toString().compareTo(vare.getValue().toString());
                        }
                    }
                    else
                    {
                        return e.getKey().compareTo(vare.getKey());
                    }
                }
            }
            return Integer.compare(map.size(), varmap.size());
        }
        else
        {
            return entId.compareTo(var.entId);
        }
    }

    public static class Variant
    {
        public String identifier;
        public NBTTagCompound variantData;
        public ArrayList<String> tagsToRemove;
        public boolean isFavourite;
        public boolean invalid;

        public Variant()
        {
            identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);
            variantData = new NBTTagCompound();
            tagsToRemove = new ArrayList<>();
            isFavourite = false;
            invalid = false;
        }

        public void read(NBTTagCompound tag)
        {
            identifier = tag.getString("ident");
            variantData = tag.getCompoundTag("entData");

            int tagsToRemoveSize = tag.getInteger("tagsToRemove");
            for(int i = 0; i < tagsToRemoveSize; i++)
            {
                tagsToRemove.add(tag.getString("tagToRemove_" + i));
            }
            isFavourite = tag.getBoolean("isFavourite");
            invalid = tag.getBoolean("invalid");
        }

        public void write(NBTTagCompound tag)
        {
            tag.setString("ident", identifier);
            tag.setTag("entData", variantData);

            tag.setInteger("tagsToRemove", tagsToRemove.size());
            for(int i = 0; i < tagsToRemove.size(); i++)
            {
                tag.setString("tagToRemove_" + i, tagsToRemove.get(i));
            }
            tag.setBoolean("isFavourite", isFavourite);
            tag.setBoolean("invalid", invalid);
        }
    }
}
