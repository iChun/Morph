package me.ichun.mods.morph.common.morph.save;

import me.ichun.mods.morph.api.morph.MorphVariant;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerMorphData
{
    public UUID owner;
    public ArrayList<MorphVariant> morphs;
    public double biomass;

    public PlayerMorphData()
    {
        this.morphs = new ArrayList<>();
    }

    public PlayerMorphData(UUID owner)
    {
        this.owner = owner;
        this.morphs = new ArrayList<>();
        MorphVariant variant = MorphVariant.createPlayerMorph(owner, false);
        variant.variants.get(0).identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;
        this.morphs.add(variant); //add the player as a morph, this should never be deleted.
    }

    public boolean containsVariant(MorphVariant variant)
    {
        for(MorphVariant morph : morphs)
        {
            if(morph.id.equals(variant.id))
            {
                return morph.containsVariant(variant);
            }
        }
        return false;
    }

    public MorphVariant addVariant(MorphVariant variant) //returns the morph it was added to.
    {
        for(MorphVariant morph : morphs)
        {
            //combine variants does the ID checking.
            morph.combineVariants(variant);
            return morph;
        }

        //we it wasn't added, add it.
        morphs.add(variant);
        return variant;
    }

    public void read(CompoundNBT tag)
    {
        owner = tag.getUniqueId("owner");

        morphs.clear();
        int count = tag.getInt("morphCount");
        for(int i = 0; i < count; i++)
        {
            morphs.add(MorphVariant.createFromNBT(tag.getCompound("morph_" + i)));
        }

        biomass = tag.getDouble("biomass");
    }

    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putUniqueId("owner", owner);
        tag.putInt("morphCount", morphs.size());
        for(int i = 0; i < morphs.size(); i++)
        {
            tag.put("morph_" + i, morphs.get(i).write(new CompoundNBT()));
        }

        tag.putDouble("biomass", biomass);
        return tag;
    }
}
