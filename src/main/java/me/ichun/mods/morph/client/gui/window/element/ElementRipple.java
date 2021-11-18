package me.ichun.mods.morph.client.gui.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.common.entity.util.EntityHelper;
import me.ichun.mods.morph.common.morph.MorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ElementRipple extends Element<ElementBiomassUpgrades>
{
    public final RenderType RIPPLE = RenderType.makeType("ripple", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_TRIANGLE_STRIP, 256, true, false, RenderType.State.getBuilder().texture(new RenderState.TextureState(MorphHandler.INSTANCE.getMorphSkinTexture(), false, false)).cull(RenderState.CULL_ENABLED).lightmap(RenderState.LIGHTMAP_DISABLED).transparency(RenderState.TRANSLUCENT_TRANSPARENCY).build(false));

    public int age;

    public ElementRipple(@Nonnull ElementBiomassUpgrades parent)
    {
        super(parent);
    }

    @Override
    public void tick()
    {
        age++;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick)
    {
        float prog = 10F * MathHelper.clamp((age + partialTick) / 20, 0F, 1F);
        double dist = ElementUpgradeNode.SIZE * 3.5D;

        //log x = (x^2) / 100 when x == 10, y = 1
        if(prog >= 1F)
        {
            float alpha = 1F - EntityHelper.sineifyProgress(MathHelper.clamp((age - 10 + partialTick) / 10F, 0F, 1F));

            double travDist = dist * Math.log10(prog);
            int slices = getWorkspace().getMinecraft().gameSettings.graphicFanciness == GraphicsFanciness.FAST ? 30 : 100;

            Matrix4f matrix = stack.getLast().getMatrix();
            IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            IVertexBuilder bufferbuilder = bufferSource.getBuffer(RIPPLE);
            float texScale = 100F;
            for(int i = 0; i <= slices; i++)
            {
                double angle = Math.PI * 2 * i / slices;
                float logX = (float)(Math.cos(angle) * travDist);
                float logY = (float)(Math.sin(angle) * travDist);
                bufferbuilder.pos(matrix, getLeft() + logX, getTop() + logY, 0).color(1F, 1F, 1F, alpha).tex(logX / texScale, logY / texScale).endVertex();
                bufferbuilder.pos(matrix, getLeft(), getTop(), 0).color(1F, 1F, 1F, alpha).tex(0, 0).endVertex();
            }
            bufferSource.finish();

            float nextProg = 10F * MathHelper.clamp((age + 1 + partialTick) / 20, 0F, 1F);
            double nextTravDist = dist * Math.log10(nextProg);

            Vector3d ourVec = getAsVector();
            ArrayList<ElementUpgradeNode> activeNodes = parentFragment.getActiveNodes();
            for(ElementUpgradeNode node : activeNodes)
            {
                double nodeDist = ourVec.distanceTo(node.getAsVector());
                if(nodeDist < dist && nodeDist < nextTravDist && nodeDist > travDist && nodeDist > ElementUpgradeNode.SIZE) //will be hit by ripple next tick, and is not within our source
                {
                    Vector3d diff = getAsVector().subtract(node.getAsVector());
                    Vector3d normal = diff.normalize();
                    double mag = (dist - nodeDist) / dist * 0.5D;
                    Vector3d mul = normal.mul(mag, mag, mag);
                    node.pushX -= mul.x;
                    node.pushY -= mul.y;
                }
            }
        }
    }

    public Vector3d getAsVector()
    {
        return new Vector3d(posX, posY, 0D);
    }


    @Override
    public int getLeft()
    {
        return super.getLeft() + parentFragment.offsetX;
    }

    @Override
    public int getRight()
    {
        return super.getRight() + parentFragment.offsetX;
    }

    @Override
    public int getTop()
    {
        return super.getTop() + parentFragment.offsetY;
    }

    @Override
    public int getBottom()
    {
        return super.getBottom() + parentFragment.offsetY;
    }

}
