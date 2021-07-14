package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;

public class PacketSessionSync extends AbstractPacket
{
    public ArrayList<BiomassUpgradeInfo> upgrades;

    public PacketSessionSync(){}

    public PacketSessionSync(Collection<BiomassUpgradeInfo> upgrades)
    {
        this.upgrades = new ArrayList<>(upgrades);
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(upgrades.size());

        for(BiomassUpgradeInfo upgrade : upgrades)
        {
            buf.writeString(ResourceHandler.GSON_MINIFY.toJson(upgrade));
        }
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        upgrades = new ArrayList<>();

        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            upgrades.add(ResourceHandler.GSON_MINIFY.fromJson(readString(buf), BiomassUpgradeInfo.class));
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            MorphHandler.BIOMASS_UPGRADES.clear();
            for(BiomassUpgradeInfo upgrade : upgrades)
            {
                MorphHandler.BIOMASS_UPGRADES.put(upgrade.id, upgrade);
            }
        });
    }
}
