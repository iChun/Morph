package me.ichun.mods.morph.client.gui.window.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ichun.mods.ichunutil.client.gui.bns.window.Fragment;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.Element;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementFertile;
import me.ichun.mods.ichunutil.client.render.RenderHelper;
import me.ichun.mods.morph.api.biomass.BiomassUpgrade;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.client.gui.window.WindowBiomassUpgrades;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.biomass.BiomassUpgradeHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ElementBiomassUpgrades extends ElementFertile<WindowBiomassUpgrades.ViewBiomassUpgrades>
{
    public ArrayList<Element<?>> children = new ArrayList<>();
    public Random rand = new Random();

    public boolean hasInit = false;

    public int offsetX = 0;
    public int offsetY = 0;

    public MousePos pos;

    public ArrayList<Element<?>> ripples = new ArrayList<>();

    public ElementBiomassUpgrades(@Nonnull WindowBiomassUpgrades.ViewBiomassUpgrades parent)
    {
        super(parent);
    }

    @Override
    public void init()
    {
        constraint.apply();
        if(!hasInit)
        {
            hasInit = true;

            ArrayList<ElementUpgradeNode> nodes = new ArrayList<>();

            for(BiomassUpgradeInfo value : BiomassUpgradeHandler.BIOMASS_UPGRADES.values())
            {
                BiomassUpgrade upgrade = Morph.eventHandlerClient.morphData.getBiomassUpgrade(value.id);
                ElementUpgradeNode node = new ElementUpgradeNode(this, value, upgrade, e -> {
                    ArrayList<ElementUpgradeNode> activeNodes = getActiveNodes();
                    for(ElementUpgradeNode activeNode : activeNodes)
                    {
                        activeNode.toggleState = activeNode == e;
                    }
                    ripples.add(e);
                });
                nodes.add(node);
            }

            ElementUpgradeNode theParent = null;
            for(ElementUpgradeNode childNode : nodes)
            {
                if(childNode.upgradeInfo.parentId.equals("root"))
                {
                    theParent = childNode; //omg the responsibility
                    continue;
                }

                for(ElementUpgradeNode parentNode : nodes)
                {
                    if(parentNode.upgradeInfo.id.equals(childNode.upgradeInfo.parentId))
                    {
                        childNode.setParentNode(parentNode);
                        break;
                    }
                }
            }

            if(theParent != null) //it shouldn't!
            {
                theParent.setPos((getWidth() - ElementUpgradeNode.SIZE) / 2, (getHeight() - ElementUpgradeNode.SIZE) / 2);
                theParent.allocateChildPlacements(rand);

                for(ElementUpgradeNode node : nodes)
                {
                    children.add(node);
                }
            }
            else
            {
                Morph.LOGGER.error("Cannot find root upgrade node!");
            }
        }
        getEventListeners().forEach(Fragment::init);
        //TODO float the view towards the upgrade you click on?
    }

    @Override
    public void tick()
    {
        children.removeIf(e -> e instanceof ElementRipple && ((ElementRipple)e).age > 20);

        super.tick();

        for(Element<?> e : ripples)
        {
            children.add(0, new ElementRipple(this).setPos(e.posX + e.width / 2, e.posY + e.height / 2));
        }
        ripples.clear();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTick) //TODO play upgrade sound?
    {
        if(renderMinecraftStyle() > 0)
        {
            bindTexture(resourceHorse());
            cropAndStitch(stack, getLeft() - 1, getTop() - 1, width + 2, height + 2, 2, 79, 17, 90, 54, 256, 256);
        }
        else
        {
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getTop() - 1, width + 2, 1, 0); //top
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getTop() - 1, 1, height + 2, 0); //left
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getLeft() - 1, getBottom(), width + 2, 1, 0); //bottom
            RenderHelper.drawColour(stack, getTheme().elementTreeBorder[0], getTheme().elementTreeBorder[1], getTheme().elementTreeBorder[2], 255, getRight(), getTop() - 1, 1, height + 2, 0); //right
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

//        RenderHelper.drawColour(stack, 0, 0, 0, 255, getLeft(), getTop(), width, height, -100);
        RenderHelper.drawColour(stack, 43, 43, 43, 255, getLeft(), getTop(), width, height, -100);

        setScissor();
        children.forEach(item -> {
            if(item instanceof ElementRipple)
            {
                item.render(stack, mouseX, mouseY, partialTick);
            }
        });

        children.forEach(item -> {
            if(item instanceof ElementUpgradeNode)
            {
                ((ElementUpgradeNode)item).renderLines(stack, partialTick);
            }
        });

        children.forEach(item -> {
            if(item instanceof ElementUpgradeNode && item.getBottom() >= getTop() && item.getTop() < getBottom())
            {
                item.render(stack, mouseX, mouseY, partialTick);
            }
        });
        resetScissorToParent();

        RenderSystem.disableBlend();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(isMouseOver(mouseX, mouseY))
        {
            super.mouseClicked(mouseX, mouseY, button);
            if(getListener() == null)
            {
                pos = new MousePos((int)mouseX, (int)mouseY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double distX, double distY)
    {
        if(pos != null)
        {
            offsetX += (int)mouseX - pos.x;
            offsetY += (int)mouseY - pos.y;

            //TODO cap the offsets by the nodes?

            pos.x = (int)mouseX;
            pos.y = (int)mouseY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        pos = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public ArrayList<ElementUpgradeNode> getActiveNodes()
    {
        ArrayList<ElementUpgradeNode> elementUpgradeNodes = new ArrayList<>();
        List<Element<?>> collect = children.stream().filter(e -> e instanceof ElementUpgradeNode && ((ElementUpgradeNode)e).isActive()).collect(Collectors.toList());
        for(Element<?> element : collect)
        {
            elementUpgradeNodes.add((ElementUpgradeNode)element);
        }
        return elementUpgradeNodes;
    }

    @Override
    public List<Element<?>> getEventListeners()
    {
        return children;
    }

    @Override
    public boolean requireScissor()
    {
        return true;
    }

    @Override
    public boolean changeFocus(boolean direction) //we can't change focus on this
    {
        return false;
    }

    @Override
    public int getBorderSize()
    {
        return 0;
    }
}
