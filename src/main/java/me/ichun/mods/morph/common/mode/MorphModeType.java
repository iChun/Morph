package me.ichun.mods.morph.common.mode;

import me.ichun.mods.morph.common.Morph;

import java.util.function.Supplier;

public enum MorphModeType
{
    DEFAULT(DefaultMode::new),
    CLASSIC(ClassicMode::new),
    COMMAND_ONLY(() -> new CommandMode(Morph.configServer.commandAllowSelector)), //Only can acquire/morph via commands
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
