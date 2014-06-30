package morph.common.ability;

import morph.api.Ability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

// Most of this class is by Lomeli12. Thanks! -iChun
public class AbilityFear extends Ability {

    public ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
    public int radius;
    public double runSpeed;

    public AbilityFear() {
    }

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
            catch(ClassNotFoundException e)
            {
            }
        }
        return this;
    }

    @Override
    public String getType() {
        return "fear";
    }

    @Override
    public void tick() {
        if (getParent().worldObj.getWorldTime() % 22L == 0) {
            @SuppressWarnings("unchecked")
            List<Entity> entityList = ((EntityPlayer) getParent()).getEntityWorld().getEntitiesWithinAABBExcludingEntity(getParent(), getParent().boundingBox.expand(radius, radius, radius));
            if (!entityList.isEmpty()) {
                for (Entity entity : entityList) {
                    if (entity instanceof EntityCreature) {
                        EntityCreature creature = (EntityCreature) entity;
                        for(Class clz : classList)
                        {
                            if(clz.isInstance(creature))
                            {
                                boolean canRun = false;
                                Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(creature, 16, 7, Vec3.createVectorHelper(getParent().posX, getParent().posY, getParent().posZ));
                                if(vec3 != null && !(getParent().getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord) < getParent().getDistanceSqToEntity(creature)))
                                {
                                    PathEntity newPath = new PathEntity(new PathPoint[] { new PathPoint((int)vec3.xCoord, (int)vec3.yCoord, (int)vec3.zCoord) });
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
    }

    @Override
    public void kill() {

    }

    @Override
    public Ability clone() {
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
    public void save(NBTTagCompound tag) {
    }

    @Override
    public void load(NBTTagCompound tag) {
    }

    @Override
    public void postRender() {

    }

    @Override
    public ResourceLocation getIcon() {
        return iconResource;
    }

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fear.png");
}