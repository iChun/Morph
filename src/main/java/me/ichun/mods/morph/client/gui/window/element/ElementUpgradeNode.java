package me.ichun.mods.morph.client.gui.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementToggleTextured;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.api.biomass.BiomassUpgrade;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Consumer;

public class ElementUpgradeNode extends ElementToggleTextured<ElementUpgradeNode>
{
    public static final RenderType MORPH_LINES = RenderType.makeType("morph_lines", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.of(4D))).texture(RenderState.NO_TEXTURE).transparency(RenderState.TRANSLUCENT_TRANSPARENCY).writeMask(RenderState.COLOR_DEPTH_WRITE).build(false));
    //TODO FINAL this
    public static int SIZE = 16;
    public static int TOLERANCE_MIN = 2 * SIZE;
    public static int TOLERANCE_MAX = 4 * SIZE;

    private ElementBiomassUpgrades parent;
    public ElementUpgradeNode parentNode; //null == anchored
    public ArrayList<ElementUpgradeNode> childNodes = new ArrayList<>();
    public int showTime;
    public double lastX;
    public double lastY;
    public double pushX;
    public double pushY;

    @Nonnull
    public BiomassUpgradeInfo upgradeInfo;

    @Nullable
    public BiomassUpgrade actualUpgrade;

    public ElementUpgradeNode(@Nonnull ElementBiomassUpgrades parent, BiomassUpgradeInfo upgradeInfo, BiomassUpgrade actualUpgrade, Consumer<ElementUpgradeNode> callback)
    {
        super(parent, "", upgradeInfo.getTextureLocation(), callback);
        this.parent = parent;
        this.showTime = -1;
        setWarping();
        this.upgradeInfo = upgradeInfo;
        this.actualUpgrade = actualUpgrade;
    }

    public void setParentNode(ElementUpgradeNode parent)
    {
        this.parentNode = parent;
        parent.childNodes.add(this);
    }

    public void allocateChildPlacements(Random rand)
    {
        for(ElementUpgradeNode childNode : childNodes)
        {
            rand.setSeed(Math.abs(childNode.upgradeInfo.id.hashCode() + Minecraft.getInstance().getSession().getPlayerID().hashCode()) * 42069L); //Blame Harogna for haha funny number
            childNode.setPos(posX + rand.nextInt(SIZE * 4), posY + rand.nextInt(SIZE * 4));
            childNode.allocateChildPlacements(rand);
        }
    }

    @Override
    public Element<?> setPos(int x, int y)
    {
        lastX = x;
        lastY = y;
        return super.setPos(x, y);
    }

    @Override
    public void tick()
    {
        if(parentNode != null)
        {
            lastX += pushX;
            lastY += pushY;

            pushX *= 0.98F;
            pushY *= 0.98F;

            if(Math.abs(pushX) < 0.001D)
            {
                pushX = 0D;
            }

            if(Math.abs(pushY) < 0.001D)
            {
                pushY = 0D;
            }

            posX = (int)Math.round(lastX);
            posY = (int)Math.round(lastY);
        }

        if(shouldShow())
        {
            showTime++;

            if(parentNode != null)
            {
                pushAwayFromOthers();
            }
        }
    }

    public void pushAwayFromOthers()
    {
        ArrayList<ElementUpgradeNode> activeNodes = parent.getActiveNodes();
        for(ElementUpgradeNode node : activeNodes)
        {
            if(node == this)
            {
                continue;
            }

            Vector3d diff = getAsVector().subtract(node.getAsVector());
            if(diff.equals(Vector3d.ZERO))
            {
                diff = new Vector3d(parent.rand.nextGaussian() * 3D, parent.rand.nextGaussian() * 3D, 0D); //add some difference
            }
            double dist = diff.length();
            if(dist < TOLERANCE_MIN)
            {
                Vector3d normal = diff.normalize();
                double mag = (TOLERANCE_MIN - dist) / TOLERANCE_MIN * 0.1D;
                Vector3d mul = normal.mul(mag, mag, mag);

                pushX += mul.x;
                pushY += mul.y;
            }
            else if(node == parentNode && dist > TOLERANCE_MAX)
            {
                Vector3d normal = diff.normalize();
                double mag = (TOLERANCE_MAX - dist) / TOLERANCE_MAX * 0.1D;
                Vector3d mul = normal.mul(mag, mag, mag);

                pushX += mul.x;
                pushY += mul.y;
            }
        }
    }

    public Vector3d getAsVector()
    {
        return new Vector3d(getCenterX(), getCenterY(), 0D);
    }

    public void renderLines(MatrixStack stack, float partialTick)
    {
        if(parentNode != null)
        {
            float prog = EntityHelper.sineifyProgress(MathHelper.clamp((showTime + partialTick) / 10, 0F, 1F));
            if(prog > 0F)
            {
                IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
                IVertexBuilder builder = bufferSource.getBuffer(MORPH_LINES);
                Matrix4f matrix = stack.getLast().getMatrix();
                float parX = parentNode.getLeft() + parentNode.width / 2F;
                float parY = parentNode.getTop() + parentNode.height / 2F;
                float diffX = (getLeft() + width / 2F) - parX;
                float diffY = (getTop() + height / 2F) - parY;
                builder.pos(matrix, parX, parY, 0F).color(255, 255, 255, 200).endVertex();
                builder.pos(matrix, parX + diffX * prog, parY + diffY * prog, 0F).color(255, 255, 255, 120).endVertex();
                bufferSource.finish();
            }
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        float prog = EntityHelper.sineifyProgress(MathHelper.clamp((showTime + partialTick) / 10, 0F, 1F));
        if(prog > 0F)
        {
            float curSize = SIZE * prog;

            if(curSize >= 4)
            {
                int preX = posX;
                int preY = posY;

                posX = (int)((float)posX + (SIZE - curSize) / 2F);
                posY = (int)((float)posY + (SIZE - curSize) / 2F);

                width = (int)curSize;
                height = (int)curSize;

                super.render(stack, mouseX, mouseY, partialTick);

                //TODO renders for if can afford or maxed etc

                posX = preX;
                posY = preY;
            }
        }
    }

    public boolean shouldShow()
    {
        return true;
    }

    public boolean isActive()
    {
        return showTime >= 0;
    }

    public int getCenterX()
    {
        return posX + width / 2;
    }

    public int getCenterY()
    {
        return posY + height / 2;
    }

    @Nullable
    @Override
    public String tooltip(double mouseX, double mouseY)
    {
        return upgradeInfo.id;//TODO debug//super.tooltip(mouseX, mouseY);
    }

    @Override
    public int getLeft()
    {
        return super.getLeft() + parent.offsetX;
    }

    @Override
    public int getRight()
    {
        return super.getRight() + parent.offsetX;
    }

    @Override
    public int getTop()
    {
        return super.getTop() + parent.offsetY;
    }

    @Override
    public int getBottom()
    {
        return super.getBottom() + parent.offsetY;
    }

    @Override
    public int getMaxWidth()
    {
        return SIZE;
    }

    @Override
    public int getMaxHeight()
    {
        return SIZE;
    }

    @Override
    public int getMinWidth()
    {
        return SIZE;
    }

    @Override
    public int getMinHeight()
    {
        return SIZE;
    }
}
