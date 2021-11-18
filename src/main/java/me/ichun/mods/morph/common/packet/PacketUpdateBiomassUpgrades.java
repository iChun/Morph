package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.api.biomass.BiomassUpgrade;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;

public class PacketUpdateBiomassUpgrades extends AbstractPacket
{
    public ArrayList<BiomassUpgrade> upgrades;

    public PacketUpdateBiomassUpgrades(){}

    public PacketUpdateBiomassUpgrades(Collection<BiomassUpgrade> upgrades)
    {
        this.upgrades = new ArrayList<>(upgrades);
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeInt(upgrades.size());

        for(BiomassUpgrade upgrade : upgrades)
        {
            buf.writeCompoundTag(upgrade.write(new CompoundNBT()));
        }
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        upgrades = new ArrayList<>();

        int count = buf.readInt();
        for(int i = 0; i < count; i++)
        {
            upgrades.add(BiomassUpgrade.createFromNBT(buf.readCompoundTag()));
        }
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            Morph.eventHandlerClient.morphData.upgrades = upgrades;

            Morph.eventHandlerClient.hudHandler.update(Morph.eventHandlerClient.morphData);
        });
    }
}
