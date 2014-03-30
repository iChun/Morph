package morph.common.ability;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import morph.api.Ability;
import morph.common.Morph;

public class AbilityFear extends Ability {

    private List<Class<?>> classList = new ArrayList<Class<?>>();
    private int radius;
    private double runSpeed;

    public AbilityFear() {
    }

    public AbilityFear(int radius, double speed, Class<?>... entityClass) {
        this.radius = radius;
        this.runSpeed = speed;
        for (Class<?> clazz : entityClass) {
            this.classList.add(clazz);
        }
    }

    @Override
    public String getType() {
        return "fear";
    }

    @Override
    public void tick() {
        if (getParent() instanceof EntityPlayer && Morph.fearAbilityEnabled) {
            @SuppressWarnings("unchecked")
            List<Entity> entityList = ((EntityPlayer) getParent()).getEntityWorld().getEntitiesWithinAABBExcludingEntity(getParent(), ((EntityPlayer) getParent()).boundingBox.expand(radius, radius, radius));
            if (!entityList.isEmpty()) {
                for (Entity entity : entityList) {
                    if (entity instanceof EntityCreature) {
                        EntityCreature creature = (EntityCreature) entity;
                        if (classList.contains(creature.getClass())) {
                            boolean canRun = false;
                            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(creature, 16, 7, creature.worldObj.getWorldVec3Pool().getVecFromPool(getParent().posX, getParent().posY, getParent().posZ));
                            if (vec3 != null && !(getParent().getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord) < getParent().getDistanceSqToEntity(creature))) {
                                PathEntity newPath = new PathEntity(new PathPoint[]{new PathPoint((int) vec3.xCoord, (int) vec3.yCoord, (int) vec3.zCoord)});
                                creature.getNavigator().setPath(newPath, 1D);
                                canRun = true;
                            }
                            if (canRun)
                                creature.getNavigator().setSpeed(runSpeed);
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