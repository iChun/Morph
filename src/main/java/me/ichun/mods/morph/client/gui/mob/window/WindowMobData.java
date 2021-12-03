package me.ichun.mods.morph.client.gui.mob.window;

import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementList;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementScrollBar;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.ElementTextWrapper;
import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.gui.mob.WorkspaceMobData;
import me.ichun.mods.morph.client.gui.window.element.ElementRenderEntity;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.mob.MobDataHandler;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class WindowMobData extends Window<WorkspaceMobData>
{
    public WindowMobData(WorkspaceMobData parent)
    {
        super(parent);

        disableBringToFront();
        disableDockingEntirely();
        disableDrag();
        disableDragResize();
        disableTitle();

        setId("windowMobData");

        setView(new ViewTraits(this));
    }

    @Override
    public ViewTraits getCurrentView()
    {
        return (ViewTraits)currentView;
    }

    public static class ViewTraits extends View<WindowMobData>
    {
        public ElementList<?> listMorphs;
        public ElementList<?> listTraits;
        public ElementList<?> listTraitInfo;
        public ElementTextWrapper textFieldDesc;

        public MobData selectedMobData;

        public ViewTraits(@Nonnull WindowMobData parent)
        {
            super(parent, "morph.gui.workspace.mobData.title");

            int padding = 6;

            ElementButton<?> buttonCancel = new ElementButton<>(this, "gui.cancel", btn -> {
                parent.parent.closeScreen();
            });
            buttonCancel.setSize(60, 20);
            buttonCancel.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(buttonCancel);

            ElementButton<?> buttonExport = new ElementButton<>(this, "morph.gui.workspace.resources.export", btn -> {
                if(export())
                {
                    if(Screen.hasShiftDown())
                    {
                        Path dir = ResourceHandler.getMorphDir().resolve("export");
                        Util.getOSType().openFile(dir.toFile());
                    }
                    parent.parent.closeScreen();
                }
            });
            buttonExport.setSize(60, 20);
            buttonExport.constraints().bottom(buttonCancel, Constraint.Property.Type.BOTTOM, 0).right(buttonCancel, Constraint.Property.Type.LEFT, 6);
            elements.add(buttonExport);

            ElementButton<?> buttonHelp = new ElementButton<>(this, "?", btn -> {
                if(Screen.hasShiftDown())
                {
                    Path dir = ResourceHandler.getMorphDir().resolve("export");
                    if(!Files.exists(dir))
                    {
                        dir = ResourceHandler.getMorphDir();
                    }
                    Util.getOSType().openFile(dir.toFile());
                }
                else
                {
                    WindowPopup.popup(parent.parent, 0.7D, 190, null, I18n.format("morph.gui.workspace.mobData.help"));
                }
            });
            buttonHelp.setSize(20, 20);
            buttonHelp.constraints().bottom(buttonCancel, Constraint.Property.Type.BOTTOM, 0).right(buttonExport, Constraint.Property.Type.LEFT, 6);
            elements.add(buttonHelp);

            ElementTextWrapper textClass = new ElementTextWrapper(this);
            textClass.setNoWrap().setText(I18n.format("morph.gui.workspace.mobData.morphs"));
            textClass.setColor(TextFormatting.AQUA.getColor());
            textClass.constraints().left(this, Constraint.Property.Type.LEFT, padding - 1).top(this, Constraint.Property.Type.TOP, padding);
            elements.add(textClass);

            ElementScrollBar<?> svClass = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            svClass.constraints().top(textClass, Constraint.Property.Type.BOTTOM, -1)
                    .bottom(this, Constraint.Property.Type.BOTTOM, padding)
                    .right(textClass, Constraint.Property.Type.LEFT, -64);
            elements.add(svClass);

            listMorphs = new ElementList<>(this).setScrollVertical(svClass);
            listMorphs.setSize(50, 20);
            listMorphs.constraints().left(textClass, Constraint.Property.Type.LEFT, 0).top(textClass, Constraint.Property.Type.BOTTOM, -1).bottom(this, Constraint.Property.Type.BOTTOM, padding).right(svClass, Constraint.Property.Type.LEFT, 0);
            if(Morph.eventHandlerClient.morphData != null)
            {
                for(MorphVariant morph : Morph.eventHandlerClient.morphData.morphs)
                {
                    if(!morph.id.equals(EntityType.PLAYER.getRegistryName()) && morph.hasVariants())
                    {
                        ElementList.Item<MorphVariant> item = listMorphs.addItem(morph);

                        MorphVariant renderVariant = morph.getAsVariant(morph.variants.get(0));
                        EntityType<?> value = ForgeRegistries.ENTITIES.getValue(renderVariant.id);
                        String key = value != null ? value.getTranslationKey() : "morph.morph.type.unknown";
                        ElementRenderEntity rend = new ElementRenderEntity(item, 0.5F);
                        rend.setSize(40, 40);
                        rend.setConstraint(Constraint.matchParent(rend, item, 4).bottom(null, Constraint.Property.Type.BOTTOM, 0));
                        rend.setTooltip(I18n.format(key));
                        rend.setEntityToRender(renderVariant.createEntityInstance(Minecraft.getInstance().world, null));
                        item.addElement(rend);

                        item.setSelectionHandler(theItem -> {
                            MobData data = MobDataHandler.getMobData(theItem.getObject().id);
                            if(data == null)
                            {
                                selectedMobData = new MobData();
                                selectedMobData.forEntity = theItem.getObject().id.toString();
                            }
                            else
                            {
                                selectedMobData = ResourceHandler.GSON.fromJson(ResourceHandler.GSON.toJson(data), MobData.class);
                            }

                            updateTraitsList();
                        });
                    }
                }
            }
            elements.add(listMorphs);

            int traitsWidth = 100;

            ElementScrollBar<?> svTraits = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            svTraits.constraints().top(textClass, Constraint.Property.Type.BOTTOM, 16)
                    .bottom(this, Constraint.Property.Type.BOTTOM, padding)
                    .right(svClass, Constraint.Property.Type.LEFT, -(padding + traitsWidth));
            elements.add(svTraits);

            listTraits = new ElementList<>(this).setScrollVertical(svTraits);
            listTraits.setSize(traitsWidth, 20);
            listTraits.constraints().left(svClass, Constraint.Property.Type.RIGHT, padding).top(svTraits, Constraint.Property.Type.TOP, 0).right(svTraits, Constraint.Property.Type.LEFT, 0).bottom(svTraits, Constraint.Property.Type.BOTTOM, 0);
            elements.add(listTraits);

            ElementTextWrapper textTraits = new ElementTextWrapper(this);
            textTraits.setNoWrap().setText(I18n.format("morph.gui.workspace.mobData.traits"));
            textTraits.constraints().left(listTraits, Constraint.Property.Type.LEFT, 0).bottom(listTraits, Constraint.Property.Type.TOP, 0);
            elements.add(textTraits);

            ElementTextWrapper textMorphId = new ElementTextWrapper(this);
            textMorphId.setId("textMorphId");
            textMorphId.setColor(TextFormatting.AQUA.getColor());
            textMorphId.setNoWrap().setText(I18n.format("morph.gui.workspace.mobData.selectAMorph"));
            textMorphId.constraints().left(listTraits, Constraint.Property.Type.LEFT, 0).top(textClass, Constraint.Property.Type.TOP, 0);
            elements.add(textMorphId);

            ElementButton<?> buttonRemove = new ElementButton<>(this, "-", btn -> {
                for(ElementList.Item<?> item : listTraits.items)
                {
                    if(item.selected)
                    {
                        Trait<?> trait = (Trait<?>)item.getObject();
                        selectedMobData.traits.remove(trait);

                        updateTraitsList();

                        updateTraitInfoList(null);
                        break;
                    }
                }
            });
            buttonRemove.setSize(16, 16);
            buttonRemove.constraints().right(svTraits, Constraint.Property.Type.RIGHT, 0).bottom(svTraits, Constraint.Property.Type.TOP, 0);
            elements.add(buttonRemove);

            ElementButton<?> buttonAdd = new ElementButton<>(this, "+", btn -> {
                Window<?> window = new WindowAddTrait(getWorkspace(), trait -> {
                    selectedMobData.traits.add(trait);

                    updateTraitsList();

                    updateTraitInfoList(null);
                });
                getWorkspace().openWindowInCenter(window, 0.6D, 0.9D, true);
                window.init();
            });
            buttonAdd.setSize(16, 16);
            buttonAdd.constraints().right(buttonRemove, Constraint.Property.Type.LEFT, 0).bottom(buttonRemove, Constraint.Property.Type.BOTTOM, 0);
            elements.add(buttonAdd);

            textFieldDesc = new ElementTextWrapper(this);
            textFieldDesc.setId("textFieldDesc");
            textFieldDesc.setText(" ");
            textFieldDesc.constraints().left(svTraits, Constraint.Property.Type.RIGHT, padding).right(buttonCancel, Constraint.Property.Type.RIGHT, 0);
            textFieldDesc.constraint.bottom(this, Constraint.Property.Type.BOTTOM, 12);
            elements.add(textFieldDesc);

            ElementScrollBar<?> svInfo = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            svInfo.constraints().top(listTraits, Constraint.Property.Type.TOP, 0)
                    .bottom(textFieldDesc, Constraint.Property.Type.TOP, padding)
                    .right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(svInfo);

            listTraitInfo = new ElementList<>(this).setScrollVertical(svInfo);
            listTraitInfo.constraints().left(svTraits, Constraint.Property.Type.RIGHT, padding).right(svInfo, Constraint.Property.Type.LEFT, 0).top(svInfo, Constraint.Property.Type.TOP, 0).bottom(svInfo, Constraint.Property.Type.BOTTOM, 0);
            elements.add(listTraitInfo);

            ElementTextWrapper textInfo = new ElementTextWrapper(this);
            textInfo.setId("textInfo");
            textInfo.setNoWrap().setText(I18n.format("morph.gui.workspace.mobData.additionalTraitInfo"));
            textInfo.constraints().left(listTraitInfo, Constraint.Property.Type.LEFT, 0).bottom(listTraitInfo, Constraint.Property.Type.TOP, 0);
            elements.add(textInfo);
        }

        public void updateTraitsList()
        {
            EntityType<?> value = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(selectedMobData.forEntity));
            String key = value != null ? value.getTranslationKey() : "morph.morph.type.unknown";

            ((ElementTextWrapper)getById("textMorphId")).setText(I18n.format(key));

            listTraits.items.clear();
            listTraits.setListener(null);

            selectedMobData.traits.sort(null);

            for(Trait<?> trait : selectedMobData.traits)
            {
                ElementList.Item<? extends Trait<?>> item = listTraits.addItem(trait);
                item.addTextWrapper(I18n.format(trait.getTranslationKeyRoot() + ".name"));

                ElementTextWrapper text = (ElementTextWrapper)item.elements.get(0);
                if(!trait.isAbility())
                {
                    text.setColor(Theme.getAsHex(getTheme().fontChat));
                }

                item.setSelectionHandler(theItem -> {
                    if(theItem.selected)
                    {
                        updateTraitInfoList(trait);
                    }
                    else
                    {
                        updateTraitInfoList(null);
                    }
                });
            }

            listTraits.init();
        }

        public void updateTraitInfoList(Trait<?> trait)
        {
            listTraitInfo.items.clear();
            listTraitInfo.setListener(null);
            textFieldDesc.setText(" ");
            textFieldDesc.constraint.bottom(this, Constraint.Property.Type.BOTTOM, 12);
            ((ElementTextWrapper)getById("textInfo")).setText("");

            if(trait != null)
            {
                ((ElementTextWrapper)getById("textInfo")).setText(I18n.format(trait.getTranslationKeyRoot() + ".name"));

                Field[] fields = trait.getClass().getDeclaredFields();
                for(Field f : fields)
                {
                    f.setAccessible(true);

                    if(Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()) || f.getName().equals("type"))
                    {
                        continue;
                    }

                    ElementList.Item<Field> fItem = listTraitInfo.addItem(f).addTextWrapper("");
                    updateItemName(fItem, trait, f);
                    fItem.setSelectionHandler(fieldItem -> {
                        if(fieldItem.selected)
                        {
                            textFieldDesc.setText(I18n.format(trait.getTranslationKeyRoot() + "." + f.getName() + ".desc"));
                        }
                        else
                        {
                            textFieldDesc.setText(" ");
                        }
                        textFieldDesc.constraint.bottom(this, Constraint.Property.Type.BOTTOM, 28);
                        this.resize(parentFragment.parent.getMinecraft(), getWidth(), getHeight());
                    });

                    fItem.setDoubleClickHandler(fieldItem -> {
                        if(f.getType() == Boolean.class)
                        {
                            try
                            {
                                Boolean bool = (Boolean)f.get(trait);
                                f.set(trait, bool != null && bool ? null : true);
                            }
                            catch(IllegalAccessException e){}

                            updateItemName(fieldItem, trait, f);

                            this.resize(parentFragment.parent.getMinecraft(), getWidth(), getHeight());
                        }
                        else if(f.getType() == String.class)
                        {
                            try
                            {
                                String s = (String)f.get(trait);

                                getWorkspace().openWindowInCenter(new WindowEditString(parentFragment.parent, s == null ? "" : s, f.getName(), input -> {
                                    try
                                    {
                                        f.set(trait, input);
                                    }
                                    catch(IllegalAccessException e){}

                                    updateItemName(fieldItem, trait, f);

                                    this.resize(parentFragment.parent.getMinecraft(), getWidth(), getHeight());
                                }), 0.4D, 100, true);
                            }
                            catch(IllegalAccessException e){}
                        }
                        else if(f.getType() == Integer.class || f.getType() == Double.class || f.getType() == Float.class)
                        {
                            getWorkspace().openWindowInCenter(new WindowEditNumber(parentFragment.parent, f.getName(), ignored -> {

                                updateItemName(fieldItem, trait, f);

                                this.resize(parentFragment.parent.getMinecraft(), getWidth(), getHeight());

                            }, trait, f), 0.4D, 100, true);
                        }
                    });

                    fItem.setRightClickConsumer((mX, mY, fieldItem) -> {
                        try
                        {
                            fieldItem.getObject().set(trait, null);
                        }
                        catch(IllegalAccessException e){}

                        updateItemName(fieldItem, trait, f);

                        this.resize(parentFragment.parent.getMinecraft(), getWidth(), getHeight());
                    });
                }

            }

            this.resize(parentFragment.parent.getMinecraft(), getWidth(), getHeight());
        }

        public void updateItemName(ElementList.Item<Field> item, Trait<?> trait, Field f)
        {
            StringTextComponent text = new StringTextComponent(TextFormatting.GOLD + I18n.format(trait.getTranslationKeyRoot() + "." + f.getName() + ".name"));
            String fhName = "";
            try
            {
                Object o = f.get(trait);
                if(o == null)
                {
                    fhName = TextFormatting.GRAY + "null";
                }
                else
                {
                    fhName = o.toString();
                }
            }
            catch(IllegalAccessException ignored){}
            text.appendSibling(new StringTextComponent(TextFormatting.RESET + ": " + fhName + TextFormatting.RESET));

            ((ElementTextWrapper)item.elements.get(0)).setText(text.getString());
            item.init();
        }

        public boolean export()
        {
            if(selectedMobData == null)
            {
                return false;
            }

            Path dir = ResourceHandler.getMorphDir().resolve("export");
            try
            {
                if(!Files.exists(dir))
                {
                    Files.createDirectory(dir);
                }

                ResourceLocation rl = new ResourceLocation(selectedMobData.forEntity);

                if(!rl.getNamespace().equals("minecraft"))
                {
                    dir = dir.resolve(rl.getNamespace());
                    if(!Files.exists(dir))
                    {
                        Files.createDirectory(dir);
                    }
                }

                if(!Minecraft.getInstance().getSession().getUsername().equals("Dev"))
                {
                    selectedMobData.author = Minecraft.getInstance().getSession().getUsername();
                }

                String name = rl.getPath().toUpperCase(Locale.ROOT).charAt(0) + rl.getPath().substring(1);

                Path file = dir.resolve(name + ".json");

                String json = ResourceHandler.GSON.toJson(selectedMobData);
                FileUtils.writeStringToFile(file.toFile(), json, "UTF-8");

                return true;
            }
            catch(Throwable e)
            {
                Morph.LOGGER.error("Error exporting Mob Data file.", e);
            }

            return false;
        }
    }
}
