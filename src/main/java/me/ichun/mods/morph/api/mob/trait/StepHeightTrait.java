package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.util.math.MathHelper;

public class StepHeightTrait extends Trait<StepHeightTrait>
{
    public Double amount;

    public StepHeightTrait()
    {
        type = "traitStepHeight";
    }

    @Override
    public void removeHooks()
    {
        player.stepHeight = 0.6F; //return to default, next trait will handle it... hopefully.
    }

    @Override
    public void tick(float strength)
    {
        if(amount != null)
        {
            setStepHeight(amount * strength);
        }
    }

    @Override
    public void transitionalTick(StepHeightTrait prevTrait, float transitionProgress)
    {
        if(prevTrait.amount != null && amount != null)
        {
            setStepHeight(MathHelper.lerp(transitionProgress, prevTrait.amount, amount));
        }
    }

    private void setStepHeight(double amount)
    {
        player.stepHeight = (float)amount; //Step heights below the default of 0.6 are allowed
    }

    @Override
    public StepHeightTrait copy()
    {
        StepHeightTrait trait = new StepHeightTrait();
        trait.amount = this.amount;
        return trait;
    }
}
