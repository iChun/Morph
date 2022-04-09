package me.ichun.mods.morph.common.mode;

import java.util.function.Supplier;

public enum MorphModeType
{
    DEFAULT(DefaultMode::new),
    CLASSIC(ClassicMode::new),
    COMMAND_ONLY(() -> new CommandMode(false)), //Only can acquire/morph via commands
    COMMAND_ALLOW_SELECTION(() -> new CommandMode(true)), //Command only, but allow manual selection of acquired morphs
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
