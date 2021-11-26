package me.ichun.mods.morph.common.mode;

import java.util.function.Supplier;

public enum MorphModeType
{
    DEFAULT(DefaultMode::new),
    CLASSIC(ClassicMode::new),
    DISGUISE(DisguiseMode::new);

    private final Supplier<MorphMode> modeSupplier;

    private MorphModeType(Supplier<MorphMode> modeSupplier)
    {
        this.modeSupplier = modeSupplier;
    }

    public MorphMode createMode()
    {
        return modeSupplier.get();
    }
}
