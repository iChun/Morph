package me.ichun.mods.morph.common.morph.save;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MorphSavedData extends WorldSavedData
{
    public static final String ID = "morph_save";
    public HashMap<UUID, PlayerMorphData> playerMorphs = new HashMap<>();

    public MorphSavedData()
    {
        super(ID);
    }

    @Override
    public void read(CompoundNBT tag)
    {
        playerMorphs.clear();

        int count = tag.getInt("count");
        for(int i = 0; i < count; i++)
        {
            PlayerMorphData playerData = new PlayerMorphData();
            playerData.read(tag.getCompound("hats_" + i));

            playerMorphs.put(playerData.owner, playerData);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putInt("count", playerMorphs.size());

        int i = 0;
        for(Map.Entry<UUID, PlayerMorphData> entry : playerMorphs.entrySet())
        {
            tag.put("hats_" + i, entry.getValue().write(new CompoundNBT()));
            i++;
        }

        return tag;
    }
}
