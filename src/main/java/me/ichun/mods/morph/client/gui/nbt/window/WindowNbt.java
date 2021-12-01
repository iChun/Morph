package me.ichun.mods.morph.client.gui.nbt.window;

import com.google.gson.reflect.TypeToken;
import me.ichun.mods.ichunutil.client.gui.bns.Theme;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.morph.api.mob.nbt.NbtModifier;
import me.ichun.mods.morph.api.morph.MorphState;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.gui.nbt.WorkspaceNbt;
import me.ichun.mods.morph.client.gui.nbt.window.element.ElementRenderEntity;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.nbt.NbtHandler;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class WindowNbt extends Window<WorkspaceNbt>
{
    public WindowNbt(WorkspaceNbt parent)
    {
        super(parent);

        disableBringToFront();
        disableDockingEntirely();
        disableDrag();
        disableDragResize();
        disableTitle();

        setId("windowNbt");

        setView(new ViewNbt(this));
    }

    @Override
    public ViewNbt getCurrentView()
    {
        return (ViewNbt)currentView;
    }

    public static class ViewNbt extends View<WindowNbt>
    {
        public NbtModifier parentModifier;
        public NbtModifier targetModifier;

        public CompoundNBT targetTag;

        public ElementList<?> listKeys;
        public ElementRenderEntity rendModEnt;

        public ViewNbt(@Nonnull WindowNbt parent)
        {
            super(parent, "morph.gui.workspace.nbt.title");

            LivingEntity target = parent.parent.target;

            parentModifier = NbtHandler.getModifierFor(target.getClass().getSuperclass());
            targetModifier = ResourceHandler.GSON.fromJson(ResourceHandler.GSON.toJson(NbtHandler.getModifierFor(target)), NbtModifier.class);

            //Add the parent's modifiers
            targetModifier.toKeep = new HashSet<>(parentModifier.toKeep);
            targetModifier.keyToModifier = ResourceHandler.GSON.fromJson(ResourceHandler.GSON.toJson(parentModifier.keyToModifier), new TypeToken<HashMap<String, NbtModifier.Modifier>>() {}.getType());

            int padding = 6;

            rendModEnt = new ElementRenderEntity(this);
            rendModEnt.setSize(80, 80);
            rendModEnt.constraints().right(this, Constraint.Property.Type.RIGHT, padding).top(this, Constraint.Property.Type.TOP, padding + 10);
            elements.add(rendModEnt);

            ElementRenderEntity rendEnt = new ElementRenderEntity(this);
            rendEnt.setSize(80, 80);
            rendEnt.setEntityToRender(parent.parent.target);
            rendEnt.constraints().right(rendModEnt, Constraint.Property.Type.LEFT, padding).top(rendModEnt, Constraint.Property.Type.TOP, 0);
            elements.add(rendEnt);

            ElementTextWrapper textModEnt = new ElementTextWrapper(this);
            textModEnt.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.asMorphEntity"));
            textModEnt.constraints().left(rendModEnt, Constraint.Property.Type.LEFT, -1).bottom(rendModEnt, Constraint.Property.Type.TOP, 0).right(rendModEnt, Constraint.Property.Type.RIGHT, 0);
            elements.add(textModEnt);

            ElementTextWrapper textEnt = new ElementTextWrapper(this);
            textEnt.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.targetEntity"));
            textEnt.constraints().left(rendEnt, Constraint.Property.Type.LEFT, -1).bottom(rendEnt, Constraint.Property.Type.TOP, 0).right(rendEnt, Constraint.Property.Type.RIGHT, 0);
            elements.add(textEnt);

            ElementButton<?> buttonCancel = new ElementButton<>(this, "gui.cancel", btn -> {
                parent.parent.closeScreen();
            });
            buttonCancel.setSize(60, 20);
            buttonCancel.constraints().bottom(this, Constraint.Property.Type.BOTTOM, padding).right(this, Constraint.Property.Type.RIGHT, padding);
            elements.add(buttonCancel);

            ElementButton<?> buttonExport = new ElementButton<>(this, "morph.gui.workspace.resources.export", btn -> {
                //TODO export
                parent.parent.closeScreen();
            });
            buttonExport.setSize(60, 20);
            buttonExport.constraints().bottom(buttonCancel, Constraint.Property.Type.BOTTOM, 0).right(buttonCancel, Constraint.Property.Type.LEFT, 6);
            elements.add(buttonExport);

            ElementTextWrapper textClass = new ElementTextWrapper(this);
            textClass.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.forClass"));
            textClass.constraints().left(this, Constraint.Property.Type.LEFT, padding - 1).top(textModEnt, Constraint.Property.Type.TOP, 0);
            elements.add(textClass);

            ElementScrollBar<?> svClass = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            svClass.constraints().top(textClass, Constraint.Property.Type.BOTTOM, -1)
                    .bottom(this, Constraint.Property.Type.BOTTOM, padding)
                    .right(textClass, Constraint.Property.Type.LEFT, -100);
            elements.add(svClass);

            ElementList<?> listClass = new ElementList<>(this).setScrollVertical(svClass);
            listClass.setSize(80, 20);
            listClass.constraints().left(textClass, Constraint.Property.Type.LEFT, 0).top(textClass, Constraint.Property.Type.BOTTOM, -1).bottom(this, Constraint.Property.Type.BOTTOM, padding).right(svClass, Constraint.Property.Type.LEFT, 0);
            Class clz = target.getClass();
            while(clz != Entity.class)
            {
                ElementList.Item<Class> item = listClass.addItem(clz).addTextWrapper(clz.getSimpleName());
                if(clz == target.getClass())
                {
                    item.selected = true;
                    listClass.setListener(item);
                }
                ElementTextWrapper text = (ElementTextWrapper)item.elements.get(0);
                text.setNoWrap();
                text.setTooltip(clz.getSimpleName());
                clz = clz.getSuperclass();
            }
            elements.add(listClass);


            ElementScrollBar<?> svKeys = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            svKeys.constraints().top(svClass, Constraint.Property.Type.TOP, 0)
                    .bottom(svClass, Constraint.Property.Type.BOTTOM, 0)
                    .right(rendEnt, Constraint.Property.Type.LEFT, padding);
            elements.add(svKeys);

            listKeys = new ElementList<>(this).setScrollVertical(svKeys);
            listKeys.constraints().left(svClass, Constraint.Property.Type.RIGHT, padding).right(svKeys, Constraint.Property.Type.LEFT, 0).top(listClass, Constraint.Property.Type.TOP, 0).bottom(listClass, Constraint.Property.Type.BOTTOM, 0);
            elements.add(listKeys);

            ElementTextWrapper textKeys = new ElementTextWrapper(this);
            textKeys.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.nbtKeys"));
            textKeys.constraints().left(listKeys, Constraint.Property.Type.LEFT, -1).bottom(listKeys, Constraint.Property.Type.TOP, -1);
            elements.add(textKeys);

            targetTag = new CompoundNBT();
            MorphVariant.writeDefaults(target, targetTag);
            target.writeAdditional(targetTag);

            //Add keys
            addModifierKeys(targetModifier, listKeys);

            updateListKeyColours();

            rendModEnt.setEntityToRender(compileAndApply());
        }

        public void addModifierKeys(NbtModifier modifier, ElementList elementList)
        {
            LinkedHashMap<String, ArrayList<NbtModifier.Modifier>> modifiers = new LinkedHashMap<>();
            modifiers.computeIfAbsent("", k -> {
                ArrayList<NbtModifier.Modifier> list = new ArrayList<>();
                for(String s : modifier.toKeep)
                {
                    NbtModifier.Modifier m = new NbtModifier.Modifier();
                    m.key = s;
                    m.keep = true;
                    list.add(m);
                }

                list.addAll(modifier.keyToModifier.values());

                //Add a dummy modifier that's null all

                NbtModifier.Modifier m = new NbtModifier.Modifier();
                m.key = m.value = "PARENT_BREAK";
                list.add(m);

                list.addAll(modifier.modifiers);

                return list;
            });

            modifiers.putAll(modifier.modSpecificModifiers);

            addModifierInfo(modifiers, targetTag, elementList, null, "");
        }

        public void addModifierInfo(LinkedHashMap<String, ArrayList<NbtModifier.Modifier>> modifiers, CompoundNBT tag, ElementList<?> list, ModifierInfo parentInfo, String prefix)
        {
            for(Map.Entry<String, INBT> e : tag.tagMap.entrySet())
            {
                //Look for a modifier for this key
                boolean handledElsewhere = true;
                String key = e.getKey();
                ModifierInfo info = null;
                for(Map.Entry<String, ArrayList<NbtModifier.Modifier>> me : modifiers.entrySet())
                {
                    String mod = me.getKey().isEmpty() ? null : me.getKey();
                    ArrayList<NbtModifier.Modifier> mods = me.getValue();
                    for(NbtModifier.Modifier modifier : mods)
                    {
                        if(key.equals(modifier.key)) // we found the key
                        {
                            info = new ModifierInfo(parentInfo, key, new ModifierInfo.Info(mod, modifier, handledElsewhere));
                        }
                        else if("PARENT_BREAK".equals(modifier.key) && modifier.key.equals(modifier.value))
                        {
                            handledElsewhere = false;
                        }
                    }

                }

                //if info is null, we haven't found a modifier for this.
                if(info == null)
                {
                    info = new ModifierInfo(parentInfo, e.getKey(), new ModifierInfo.Info(null, null, false));
                }

                if(parentInfo != null)
                {
                    parentInfo.addChild(info);
                }

                String name = prefix.isEmpty() ? e.getKey() : prefix + ": " + e.getKey();
                list.addItem(info).addTextWrapper(name);

                if(e.getValue() instanceof CompoundNBT)
                {
                    LinkedHashMap<String, ArrayList<NbtModifier.Modifier>> babyMods = new LinkedHashMap<>();
                    if(info.currentInfo.modifier != null && info.currentInfo.modifier.nestedModifiers != null)
                    {
                        babyMods.put(info.currentInfo.requiredMod == null ? "" : info.currentInfo.requiredMod, info.currentInfo.modifier.nestedModifiers);
                    }
                    addModifierInfo(babyMods, ((CompoundNBT)e.getValue()), list, info, name);
                }
            }
        }

        private void updateListKeyColours() //TODO add a "HELP" tutorial
        {
            for(ElementList.Item<?> item : listKeys.items)
            {
                ElementTextWrapper text = (ElementTextWrapper)item.elements.get(0);
                ModifierInfo modInfo = (ModifierInfo)item.getObject();

                if(modInfo.currentInfo.modifier == null)
                {
                    text.setColor(0xaaaaaa);
                    text.setTooltip(text.getText().get(0) + "\n\n" + I18n.format("morph.gui.workspace.nbt.stripped"));
                }
                else if(modInfo.currentInfo.handledElsewhere)
                {
                    text.setColor(0xffff55);
                    text.setTooltip(text.getText().get(0) + "\n\n" + I18n.format("morph.gui.workspace.nbt.handledElsewhere"));
                }
                else
                {
                    text.setColor(null);
                    text.setTooltip(text.getText().get(0));
                }
            }
        }

        public LivingEntity compileAndApply()
        {
            LivingEntity target = parentFragment.parent.target;
            Class clz = target.getClass();

            NbtModifier modifier = compileModifier();

            NbtModifier ori = NbtHandler.NBT_MODIFIERS.get(clz);
            NbtHandler.NBT_MODIFIERS.put(clz, modifier);

            MorphVariant variant = MorphHandler.INSTANCE.createVariant(target);
            NbtHandler.NBT_MODIFIERS.put(clz, ori);

            if(variant != null)
            {
                return variant.createEntityInstance(Minecraft.getInstance().player.world, null);
            }
            LivingEntity entInstance = EntityType.PIG.create(target.world);
            entInstance.setCustomName(new StringTextComponent("Invalid Morph Pig"));

            return entInstance;
        }

        public NbtModifier compileModifier()
        {
            LivingEntity target = parentFragment.parent.target;
            Class clz = target.getClass();

            NbtModifier modifier = new NbtModifier();

            modifier.toKeep = new HashSet<>(parentModifier.toKeep);
            modifier.keyToModifier = ResourceHandler.GSON.fromJson(ResourceHandler.GSON.toJson(parentModifier.keyToModifier), new TypeToken<HashMap<String, NbtModifier.Modifier>>() {}.getType());

            //Check the class' interfaces
            for(Map.Entry<Class<?>, NbtModifier> e : NbtHandler.NBT_MODIFIERS_INTERFACES.entrySet())
            {
                if(e.getKey().isAssignableFrom(clz))
                {
                    modifier.toKeep.addAll(e.getValue().toKeep);
                    modifier.keyToModifier.putAll(e.getValue().keyToModifier);
                }
            }

            HashMap<ModifierInfo, NbtModifier.Modifier> modifiers = new HashMap<>();

            for(ElementList.Item<?> item : listKeys.items)
            {
                ModifierInfo modInfo = (ModifierInfo)item.getObject();

                modifiers.put(modInfo, modInfo.currentInfo.modifier);

                if(!modInfo.currentInfo.handledElsewhere && modInfo.currentInfo.modifier != null)
                {
                    if(modInfo.currentInfo.requiredMod == null)
                    {
                        if(modInfo.parent != null) //is nested
                        {
                            ModifierInfo parent = modInfo.parent;
                            while(parent != null)
                            {
                                NbtModifier.Modifier parentMod = modifiers.get(parent);
                                if(parentMod.nestedModifiers == null)
                                {
                                    parentMod.nestedModifiers = new ArrayList<>();
                                }
                                if(!parentMod.nestedModifiers.contains(modInfo.currentInfo.modifier))
                                {
                                    parentMod.nestedModifiers.add(modInfo.currentInfo.modifier);
                                }

                                parent = parent.parent;
                            }
                        }
                        else
                        {
                            modifier.modifiers.add(modInfo.currentInfo.modifier);
                        }
                    }
                    else
                    {
                        ArrayList<NbtModifier.Modifier> modModifiers = modifier.modSpecificModifiers.computeIfAbsent(modInfo.currentInfo.requiredMod, k -> new ArrayList<>());
                        ModifierInfo parent = modInfo.parent;
                        if(parent != null)
                        {
                            NbtModifier.Modifier rootMod = modInfo.currentInfo.modifier;
                            while(parent != null)
                            {
                                NbtModifier.Modifier parentMod = new NbtModifier.Modifier();
                                parentMod.key = parent.key;
                                parentMod.nestedModifiers = new ArrayList<>();
                                parentMod.nestedModifiers.add(rootMod);
                                rootMod = parentMod;
                                parent = parent.parent;
                            }

                            //parent is null, we can add it now
                            modModifiers.add(rootMod);

                            //recombine
                            combineChildMods(rootMod.nestedModifiers);
                        }
                        else
                        {
                            modModifiers.add(modInfo.currentInfo.modifier);
                        }
                    }
                }
            }

            modifier.setup();

            modifier.setupValues();

            return modifier;
        }

        private void combineChildMods(ArrayList<NbtModifier.Modifier> mods)
        {
            HashMap<String, NbtModifier.Modifier> keys = new HashMap<>();
            for(int i = mods.size() - 1; i >= 0; i--)
            {
                NbtModifier.Modifier mod = mods.get(i);
                if(keys.containsKey(mod.key))
                {
                    NbtModifier.Modifier oldMod = keys.get(mod.key);
                    if(oldMod.nestedModifiers != null && mod.nestedModifiers != null)
                    {
                        oldMod.nestedModifiers.addAll(mod.nestedModifiers);
                        mods.remove(i);
                    }
                }
                else
                {
                    keys.put(mod.key, mod);
                }
            }

            for(NbtModifier.Modifier mod : mods)
            {
                if(mod.nestedModifiers != null)
                {
                    combineChildMods(mod.nestedModifiers);
                }
            }
        }

        public static class ModifierInfo
        {
            public ModifierInfo parent;
            public ArrayList<ModifierInfo> children;
            public String key;
            public Info originalInfo;
            public Info currentInfo;

            public ModifierInfo(ModifierInfo parent, String key, Info info)
            {
                this.parent = parent;
                this.children = new ArrayList<>();
                this.key = key;
                this.originalInfo = info;
                this.currentInfo = info;
            }

            public void addChild(ModifierInfo info)
            {
                children.add(info);
            }

            public static class Info
            {
                @Nullable
                public String requiredMod;
                @Nullable
                public NbtModifier.Modifier modifier;
                public boolean handledElsewhere;

                public Info(@Nullable String requiredMod, @Nullable NbtModifier.Modifier modifier, boolean handledElsewhere)
                {
                    this.requiredMod = requiredMod;
                    this.modifier = modifier;
                    this.handledElsewhere = handledElsewhere;
                }
            }
        }
    }
}
