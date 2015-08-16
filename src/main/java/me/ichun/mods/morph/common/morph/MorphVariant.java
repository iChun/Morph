package me.ichun.mods.morph.common.morph;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.RandomStringUtils;
import us.ichun.mods.ichunutil.common.core.EntityHelperBase;

import java.util.ArrayList;
import java.util.Map;

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
    public Variant thisVariant;
    public ArrayList<Variant> variants;

    public MorphVariant(String entId)
    {
        this.entId = entId;
        this.playerName = "";
        this.entTag = new NBTTagCompound();
        this.instanceTag = new NBTTagCompound();
        this.thisVariant = new Variant();
        this.variants = new ArrayList<Variant>();
    }

    public MorphVariant setPlayerName(String name)
    {
        playerName = name;
        return this;
    }

    //NEVER RETURNS NULL
    public EntityLivingBase createEntityInstance(World world)
    {
        if(entId.equals(PLAYER_MORPH_ID))
        {
            if(!playerName.isEmpty())
            {
                return world.isRemote ? createPlayer(world, playerName) : new FakePlayer((WorldServer)world, EntityHelperBase.getSimpleGameProfileFromName(playerName));
            }
            else
            {
                thisVariant.invalid = true;
            }
        }
        else
        {
            //TODO should I try...catch here to prevent crashes?
            NBTTagCompound instanceCreator = (NBTTagCompound)instanceTag.copy();
            instanceCreator.tagMap.putAll(entTag.tagMap);
            instanceCreator.tagMap.putAll(thisVariant.variantData.tagMap);
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

        EntityPig pig = (EntityPig)EntityList.createEntityByName("Pig", world);
        NBTTagCompound fakeTag = new NBTTagCompound();
        pig.writeEntityToNBT(fakeTag);
        clean(pig, fakeTag);
        pig.readEntityFromNBT(fakeTag);

        return pig;
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer createPlayer(World world, String player)
    {
        return new EntityOtherPlayerMP(world, EntityHelperBase.getFullGameProfileFromName(player));
    }

    public ArrayList<MorphVariant> split()
    {
        ArrayList<MorphVariant> vars = new ArrayList<MorphVariant>();
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
            variant.entTag.tagMap.putAll(var.variantData.tagMap);
            variant.thisVariant = var;
            vars.add(variant);
        }

        return vars;
    }

    public void read(NBTTagCompound tag)
    {
        int varProtocol = tag.getInteger("varProtocol");

        entId = tag.getString("entId");
        playerName = tag.getString("playerName");
        entTag = tag.getCompoundTag("entTag");
        instanceTag = tag.getCompoundTag("instanceTag");
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

    public static MorphVariant createVariant(EntityLivingBase living)
    {
        if(living instanceof EntityPlayer)
        {
            return new MorphVariant(PLAYER_MORPH_ID).setPlayerName(living.getCommandSenderName());
        }

        NBTTagCompound saveData = new NBTTagCompound();

        if(!living.writeToNBTOptional(saveData))
        {
            return null;
        }

        MorphVariant variant = new MorphVariant(saveData.getString("id"));
        variant.instanceTag = saveData;
        clean(living, variant.instanceTag);

        living.writeEntityToNBT(variant.entTag);
        clean(living, variant.entTag);

        variant.entTag.setDouble("Morph_HealthBalancing", MathHelper.clamp_double(living.getEntityAttribute(SharedMonsterAttributes.maxHealth).getBaseValue(), 0D, 20D)); //For health balancing reasons for now.

        if(variant.instanceTag.tagMap.containsKey("ForgeData")) // send the ForgeData to the variant tag.
        {
            variant.entTag.tagMap.put("ForgeData", variant.instanceTag.tagMap.get("ForgeData"));
        }

        return variant;
    }

    public static void clean(EntityLivingBase living, NBTTagCompound tag)
    {
        //Entity tags
        tag.removeTag("Fire");
        tag.removeTag("Riding");

        //EntityLivingBase tags
        tag.setFloat("HealF", Short.MAX_VALUE);
        tag.setShort("Health", (short)Short.MAX_VALUE);
        tag.removeTag("HurtTime");
        tag.removeTag("HurtByTimestamp");
        tag.removeTag("DeathTime");
        tag.removeTag("AbsorptionAmount");
        tag.removeTag("Attributes");
        tag.removeTag("ActiveEffects");

        //EntityPigZombie tags
        tag.removeTag("Anger");
        tag.removeTag("HurtBy");
        tag.removeTag("CanBreakDoors");

        //EntityAgeable tags
        tag.setInteger("Age", living.isChild() ? -24000 : 0);
        tag.removeTag("ForcedAge");
        tag.removeTag("InLove");

        //EntityLiving tags
        if(living instanceof EntityLiving)
        {
            tag.setBoolean("CanPickUpLoot", true);
            tag.setBoolean("PersistenceRequired", true);
            tag.setBoolean("NoAI", true);
        }
        tag.removeTag("Equipment");
        tag.removeTag("DropChances");
        tag.removeTag("Leashed");
        tag.removeTag("Leash");

        //TODO modify NBT tags here.
        tag.removeTag("bukkit");
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof MorphVariant)
        {
            MorphVariant var = (MorphVariant)o;
            return entId.equals(var.entId) && playerName.equals(var.playerName) && entTag.equals(var.entTag) && thisVariant.identifier.equals(var.thisVariant.identifier); //Do not compare the variant arraylist? >_>
        }
        return false;
    }

    /**
     * Returns false if variants were not combined successfully or if the variant already exists
     * You cannot combine player variants even though they might be categorised together!
     */
    public static boolean combineVariants(MorphVariant source, MorphVariant variantToMerge)
    {
        if(source.entId.equals(PLAYER_MORPH_ID) || variantToMerge.entId.equals(PLAYER_MORPH_ID) || !source.entId.equals(variantToMerge.entId))
        {
            return false;
        }

        NBTTagCompound tagCopy = (NBTTagCompound)source.entTag.copy();

        if(tagCopy.equals(variantToMerge.entTag)) //Compare variant with current variant
        {
            return false;
        }

        for(Variant variant : source.variants)
        {
            NBTTagCompound tagCopyCopy = (NBTTagCompound)tagCopy.copy();
            tagCopyCopy.tagMap.putAll(variant.variantData.tagMap);

            if(tagCopyCopy.equals(variantToMerge.entTag))
            {
                return false;
            }
        }

        //At this point, this variant is considered "unique"

        //Create the variant
        Variant variant = new Variant();
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

        //Add the variant to the variants list.
        source.variants.add(variant);

        return true;
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
        else
        {
            return entId.compareTo(var.entId);
        }
    }

    public static class Variant
    {
        public String identifier;
        public NBTTagCompound variantData;
        public NBTTagCompound morphData;
        public boolean invalid;

        public Variant()
        {
            identifier = RandomStringUtils.randomAscii(IDENTIFIER_LENGTH);
            variantData = new NBTTagCompound();
            morphData = new NBTTagCompound();
            invalid = false;
        }

        public void read(NBTTagCompound tag)
        {
            identifier = tag.getString("ident");
            variantData = tag.getCompoundTag("entData");
            morphData = tag.getCompoundTag("morphData");
            invalid = tag.getBoolean("invalid");
        }

        public void write(NBTTagCompound tag)
        {
            tag.setString("ident", identifier);
            tag.setTag("entData", variantData);
            tag.setTag("morphData", morphData);
            tag.setBoolean("invalid", invalid);
        }
    }
}
