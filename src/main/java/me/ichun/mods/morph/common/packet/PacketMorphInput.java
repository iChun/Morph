package me.ichun.mods.morph.common.packet;

import me.ichun.mods.ichunutil.common.network.AbstractPacket;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketMorphInput extends AbstractPacket
{
    public String identifier;
    public boolean inputFavourite;
    public boolean isFavourite;

    public PacketMorphInput(){}

    public PacketMorphInput(String identifier, boolean inputFavourite, boolean isFavourite)
    {
        this.identifier = identifier;
        this.inputFavourite = inputFavourite;
        this.isFavourite = isFavourite;
    }

    @Override
    public void writeTo(PacketBuffer buf)
    {
        buf.writeString(identifier);
        buf.writeBoolean(inputFavourite);
        buf.writeBoolean(isFavourite);
    }

    @Override
    public void readFrom(PacketBuffer buf)
    {
        identifier = readString(buf);
        inputFavourite = buf.readBoolean();
        isFavourite = buf.readBoolean();
    }

    @Override
    public void process(NetworkEvent.Context context)
    {
        context.enqueueWork(() -> {
            PlayerMorphData morphData = MorphHandler.INSTANCE.getPlayerMorphData(context.getSender());
            for(MorphVariant morph : morphData.morphs)
            {
                MorphVariant.Variant variant = morph.getVariantById(identifier);
                if(variant != null)
                {
                    if(inputFavourite)
                    {
                        variant.isFavourite = isFavourite;

                        MorphHandler.INSTANCE.getSaveData().markDirty();
                    }
                    else
                    {
                        MorphHandler.INSTANCE.morphTo(context.getSender(), morph.getAsVariant(variant));
                    }

                    return;
                }
            }
        });
    }
}
