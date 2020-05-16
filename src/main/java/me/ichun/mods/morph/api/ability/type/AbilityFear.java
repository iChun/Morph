package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

// Most of this class is by Lomeli12. Thanks! -iChun
public class AbilityFear extends Ability
{
    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fear.png");
    public ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
    public int radius;
    public double runSpeed;

    public AbilityFear() { }

    public AbilityFear(int radius, double speed, Class<?>... entityClass) {
        this.radius = radius;
        this.runSpeed = speed;
        for (Class<?> clazz : entityClass) {
            if(EntityCreature.class.isAssignableFrom(clazz))
            {
                this.classList.add(clazz);
            }
        }
    }

    @Override
    public String getType() {
        return "fear";
    }

    @Override
    public Ability parse(String[] args)
    {
        this.radius = Integer.parseInt(args[0]);
        this.runSpeed = Double.parseDouble(args[1]);
        for(int i = 2; i < args.length; i++)
        {
            try
            {
                Class clz = Class.forName(args[i]);
                if(EntityCreature.class.isAssignableFrom(clz))
                {
                    this.classList.add(clz);
                }
            }
            catch(ClassNotFoundException e) { }
        }
        return this;
    }

    @Override
    public void tick()
    {
        if(getParent().getEntityWorld().getTotalWorldTime() % 22L == 0)
        {
            List<Entity> entityList = getParent().getEntityWorld().getEntitiesWithinAABBExcludingEntity(getParent(), getParent().getEntityBoundingBox().grow(radius, radius, radius));
            if(entityList.isEmpty())
                return;
            for(Entity entity : entityList)
            {
                if(entity instanceof EntityCreature)
                {
                    EntityCreature creature = (EntityCreature) entity;
                    for(Class clz : classList)
                    {
                        if(clz.isInstance(creature))
                        {
                            boolean canRun = false;
                            Vec3d vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(creature, 16, 7, new Vec3d(getParent().posX, getParent().posY, getParent().posZ));
                            if(vec3 != null && !(getParent().getDistanceSq(vec3.x, vec3.y, vec3.z) < getParent().getDistanceSq(creature)))
                            {
                                Path newPath = new Path(new PathPoint[]{new PathPoint((int) vec3.x, (int) vec3.y, (int) vec3.z)});
                                creature.getNavigator().setPath(newPath, 1D);
                                canRun = true;
                            }
                            if(canRun)
                                creature.getNavigator().setSpeed(runSpeed);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Ability clone()
    {
        if (classList.isEmpty())
            return new AbilityFear();
        else {
            Class<?>[] classArray = new Class<?>[classList.size()];
            for (int i = 0; i < classArray.length; i++) {
                classArray[i] = classList.get(i);
            }
            return new AbilityFear(radius, runSpeed, classArray);
        }
    }

    @Override
    public ResourceLocation getIcon() {
        return iconResource;
    }
}
