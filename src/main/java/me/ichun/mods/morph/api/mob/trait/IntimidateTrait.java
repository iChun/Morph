package me.ichun.mods.morph.api.mob.trait;

import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class IntimidateTrait extends Trait<IntimidateTrait>
{
    public String idToIntimidate;
    public String classToIntimidate;
    public Float distance;
    public Double farRunSpeed;
    public Double nearRunSpeed;

    public transient EntityType<?> idIntimidate;
    public transient Class<? extends LivingEntity> classIntimidate;

    public IntimidateTrait()
    {
        type = "traitIntimidate";
    }

    @Override
    public void addHooks()
    {
        if(idToIntimidate != null)
        {
            idIntimidate = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(idToIntimidate));
        }
        else if(classToIntimidate != null)
        {
            try
            {
                Class clz = Class.forName(classToIntimidate);
                if(LivingEntity.class.isAssignableFrom(clz))
                {
                    classIntimidate = clz;
                }
                else
                {
                    Morph.LOGGER.warn("Found class to intimidate that is not a LivingEntity class: {}", classToIntimidate);
                }
            }
            catch(ClassNotFoundException e)
            {
                Morph.LOGGER.warn("Could not find class to intimidate: {}", classToIntimidate);
            }
        }

        if(idIntimidate != null || classIntimidate != null)
        {
            if(distance == null)
            {
                distance = 6F;
            }
            if(farRunSpeed == null)
            {
                farRunSpeed = 1.0D;
            }
            if(nearRunSpeed == null)
            {
                nearRunSpeed = 1.0D;
            }
        }
    }

    @Override
    public void tick(float strength)
    {
        if(strength == 1F && (idIntimidate != null || classIntimidate != null))
        {
            List<?> entitiesIntimidated = idIntimidate != null ? player.world.getEntitiesWithinAABB(idIntimidate, player.getBoundingBox().expand(distance, 3D, distance), p -> p instanceof CreatureEntity) : player.world.getEntitiesWithinAABB(classIntimidate, player.getBoundingBox().expand(distance, 3D, distance), p -> p instanceof CreatureEntity);
            for(Object o : entitiesIntimidated)
            {
                CreatureEntity creature = (CreatureEntity)o;

                //if the creature has no path, or the target path is < distance, make the creature run.
                if(!creature.getNavigator().noPath() || player.getDistanceSq(creature.getNavigator().getTargetPos().getX(), creature.getNavigator().getTargetPos().getY(), creature.getNavigator().getTargetPos().getZ()) < distance * distance)
                {
                    Vector3d vector3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(creature, 16, 7, player.getPositionVec());

                    if(vector3d != null && player.getDistanceSq(vector3d) > player.getDistanceSq(creature))
                    {
                        Path path = creature.getNavigator().pathfind(vector3d.x, vector3d.y, vector3d.z, 0);

                        if(path != null)
                        {
                            double speed = creature.getDistanceSq(player) < 49D ? nearRunSpeed : farRunSpeed;
                            creature.getNavigator().setPath(path, speed);
                        }
                    }
                }
                else //the creature is still running away from us
                {
                    double speed = creature.getDistanceSq(player) < 49D ? nearRunSpeed : farRunSpeed;
                    creature.getNavigator().setSpeed(speed);
                }
            }
        }
    }

    @Override
    public IntimidateTrait copy()
    {
        IntimidateTrait trait = new IntimidateTrait();
        trait.idToIntimidate = this.idToIntimidate;
        trait.classToIntimidate = this.classToIntimidate;
        trait.distance = this.distance;
        trait.farRunSpeed = this.farRunSpeed;
        trait.nearRunSpeed = this.nearRunSpeed;
        return trait;
    }
}
