package me.ichun.mods.morph.client.gui.nbt.window;

import com.google.gson.reflect.TypeToken;
import me.ichun.mods.ichunutil.client.gui.bns.window.Window;
import me.ichun.mods.ichunutil.client.gui.bns.window.WindowPopup;
import me.ichun.mods.ichunutil.client.gui.bns.window.constraint.Constraint;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.View;
import me.ichun.mods.ichunutil.client.gui.bns.window.view.element.*;
import me.ichun.mods.morph.api.mob.nbt.NbtModifier;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.gui.mob.window.WindowMobData;
import me.ichun.mods.morph.client.gui.nbt.WorkspaceNbt;
import me.ichun.mods.morph.client.gui.window.element.ElementRenderEntity;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.nbt.NbtHandler;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

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

        public ElementList<?> listClass;
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
                    WindowPopup.popup(parent.parent, 0.7D, 190, null, I18n.format("morph.gui.workspace.nbt.help"));
                }
            });
            buttonHelp.setSize(20, 20);
            buttonHelp.constraints().bottom(buttonCancel, Constraint.Property.Type.BOTTOM, 0).right(buttonExport, Constraint.Property.Type.LEFT, 6);
            elements.add(buttonHelp);

            ElementTextWrapper textClass = new ElementTextWrapper(this);
            textClass.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.forClass"));
            textClass.constraints().left(this, Constraint.Property.Type.LEFT, padding - 1).top(textModEnt, Constraint.Property.Type.TOP, 0);
            elements.add(textClass);

            ElementScrollBar<?> svClass = new ElementScrollBar<>(this, ElementScrollBar.Orientation.VERTICAL, 0.6F);
            svClass.constraints().top(textClass, Constraint.Property.Type.BOTTOM, -1)
                    .bottom(this, Constraint.Property.Type.BOTTOM, padding)
                    .right(textClass, Constraint.Property.Type.LEFT, -100);
            elements.add(svClass);

            listClass = new ElementList<>(this).setScrollVertical(svClass);
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
                text.setTooltip(clz.getName());
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

            //Add the modifier editors
            ElementTextWrapper textKeep = new ElementTextWrapper(this);
            textKeep.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.keep"));
            textKeep.setTooltip(I18n.format("morph.gui.workspace.nbt.keep.tooltip"));
            textKeep.constraints().left(rendEnt, Constraint.Property.Type.LEFT, 1).top(rendEnt, Constraint.Property.Type.BOTTOM, padding);
            elements.add(textKeep);

            ElementCheckbox<?> checkKeep = new ElementCheckbox<>(this, I18n.format("morph.gui.workspace.nbt.keep.tooltip"), c -> {
                if(!c.toggleState && !((ElementTextField)getById("fieldValue")).getText().isEmpty())
                {
                    c.toggleState = true;
                }
                setModifierFields();
            });
            checkKeep.constraints().left(textKeep, Constraint.Property.Type.RIGHT, 4).top(textKeep, Constraint.Property.Type.TOP, 4);
            checkKeep.setId("checkKeep");
            elements.add(checkKeep);

            ElementTextWrapper textRemove = new ElementTextWrapper(this);
            textRemove.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.forceRemove"));
            textRemove.setTooltip(I18n.format("morph.gui.workspace.nbt.forceRemove.tooltip"));
            textRemove.constraints().left(checkKeep, Constraint.Property.Type.RIGHT, padding).top(textKeep, Constraint.Property.Type.TOP, 0);
            elements.add(textRemove);

            ElementCheckbox<?> checkRemove = new ElementCheckbox<>(this, I18n.format("morph.gui.workspace.nbt.forceRemove.tooltip"), c -> {
                if(c.toggleState)
                {
                    ElementCheckbox<?> keep = getById("checkKeep");
                    keep.toggleState = true;

                    ((ElementTextField)getById("fieldValue")).setText("");
                }
                setModifierFields();
            });
            checkRemove.constraints().left(textRemove, Constraint.Property.Type.RIGHT, 4).top(checkKeep, Constraint.Property.Type.TOP, 0);
            checkRemove.setId("checkRemove");
            elements.add(checkRemove);

            ElementTextWrapper textValue = new ElementTextWrapper(this);
            textValue.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.forceValue"));
            textValue.setTooltip(I18n.format("morph.gui.workspace.nbt.forceValue.tooltip"));
            textValue.constraints().left(textKeep, Constraint.Property.Type.LEFT, 0).top(textKeep, Constraint.Property.Type.BOTTOM, 4);
            elements.add(textValue);

            Consumer<String> responder = (s) -> setModifierFields();

            ElementTextField fieldValue = new ElementTextField(this);
            fieldValue.setTooltip(I18n.format("morph.gui.workspace.nbt.forceValue.tooltip"));
            fieldValue.setId("fieldValue");
            fieldValue.setResponder(responder).setEnterResponder(responder);
            fieldValue.constraints().left(textValue, Constraint.Property.Type.LEFT, 0).top(textValue, Constraint.Property.Type.BOTTOM, 0).right(rendModEnt, Constraint.Property.Type.RIGHT, 0);
            elements.add(fieldValue);

            ElementTextWrapper textMod = new ElementTextWrapper(this);
            textMod.setNoWrap().setText(I18n.format("morph.gui.workspace.nbt.modRequired"));
            textMod.setTooltip(I18n.format("morph.gui.workspace.nbt.modRequired.tooltip"));
            textMod.constraints().left(textValue, Constraint.Property.Type.LEFT, 0).top(fieldValue, Constraint.Property.Type.BOTTOM, 4);
            elements.add(textMod);

            ElementTextField fieldMod = new ElementTextField(this);
            fieldMod.setTooltip(I18n.format("morph.gui.workspace.nbt.modRequired.tooltip"));
            fieldMod.setId("fieldMod");
            fieldMod.setResponder(responder).setEnterResponder(responder);
            fieldMod.constraints().left(textValue, Constraint.Property.Type.LEFT, 0).top(textMod, Constraint.Property.Type.BOTTOM, 0).right(rendModEnt, Constraint.Property.Type.RIGHT, 0);
            elements.add(fieldMod);


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

            addModifierInfo(modifiers, targetTag, elementList, null, 0);
        }

        public void addModifierInfo(LinkedHashMap<String, ArrayList<NbtModifier.Modifier>> modifiers, CompoundNBT tag, ElementList<?> list, ModifierInfo parentInfo, int depth)
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
                            info = new ModifierInfo(parentInfo, key, e.getValue(), new ModifierInfo.Info(mod, modifier, handledElsewhere));
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
                    info = new ModifierInfo(parentInfo, e.getKey(), e.getValue(), new ModifierInfo.Info(null, null, false));
                }

                if(parentInfo != null)
                {
                    parentInfo.addChild(info);
                }

                StringBuilder prefix = new StringBuilder();
                for(int i = 0; i < depth; i++)
                {
                    prefix.append("- ");
                }
                list.addItem(info).addTextWrapper(prefix + e.getKey()).setSelectionHandler(this::updateModifierInputs);

                if(e.getValue() instanceof CompoundNBT)
                {
                    LinkedHashMap<String, ArrayList<NbtModifier.Modifier>> babyMods = new LinkedHashMap<>();
                    if(info.currentInfo.modifier != null && info.currentInfo.modifier.nestedModifiers != null)
                    {
                        babyMods.put(info.currentInfo.requiredMod == null ? "" : info.currentInfo.requiredMod, info.currentInfo.modifier.nestedModifiers);
                    }
                    addModifierInfo(babyMods, ((CompoundNBT)e.getValue()), list, info, depth + 1);
                }
            }
        }

        public void updateModifierInputs(ElementList.Item<ModifierInfo> item)
        {
            if(!item.selected)
            {
                ((ElementCheckbox<?>)getById("checkKeep")).toggleState = false;
                ((ElementCheckbox<?>)getById("checkRemove")).toggleState = false;
                ((ElementTextField)getById("fieldValue")).setText("");
                ((ElementTextField)getById("fieldMod")).setText("");
                return;
            }

            ModifierInfo info = item.getObject();
            NbtModifier.Modifier modifier = info.currentInfo.modifier;
            ((ElementCheckbox<?>)getById("checkKeep")).toggleState = modifier != null && modifier.keep != null || (modifier != null && modifier.value != null);
            ((ElementCheckbox<?>)getById("checkRemove")).toggleState = modifier != null && modifier.keep != null && !modifier.keep;
            ((ElementTextField)getById("fieldValue")).setText((modifier != null && modifier.value != null) ? modifier.value : "");
            ((ElementTextField)getById("fieldMod")).setText((info.currentInfo.requiredMod != null) ? info.currentInfo.requiredMod : "");
        }

        public void setModifierFields()
        {
            String mod = ((ElementTextField)getById("fieldMod")).getText();
            if(mod.isEmpty())
            {
                mod = null;
            }

            NbtModifier.Modifier modifier = new NbtModifier.Modifier();
            modifier.value = ((ElementTextField)getById("fieldValue")).getText();
            if(modifier.value.isEmpty())
            {
                modifier.value = null;
            }
            modifier.keep = modifier.value == null && ((ElementCheckbox<?>)getById("checkKeep")).toggleState ? (((ElementCheckbox<?>)getById("checkRemove")).toggleState ? false : true) : null;

            ModifierInfo.Info newInfo = new ModifierInfo.Info(mod, modifier, false);

            for(ElementList.Item<?> item : listKeys.items)
            {
                if(item.selected)
                {
                    ModifierInfo info = (ModifierInfo)item.getObject();

                    modifier.key = info.key; //match the key

                    if(info.originalInfo.handledElsewhere)
                    {
                        boolean sameMod = info.originalInfo.requiredMod == null && newInfo.requiredMod == null || info.originalInfo.requiredMod != null && info.originalInfo.requiredMod.equals(newInfo.requiredMod);
                        boolean sameValue = info.originalInfo.modifier.value == null && modifier.value == null || info.originalInfo.modifier.value != null && info.originalInfo.modifier.value.equals(modifier.value);
                        if(modifier.keep == null && modifier.value == null || sameMod && info.originalInfo.modifier.keep == modifier.keep && sameValue) //modifier is the same as original, handled elsewhere
                        {
                            info.currentInfo = info.originalInfo;
                            break;
                        }
                    }

                    if(modifier.keep == null && modifier.value == null)
                    {
                        newInfo = new ModifierInfo.Info(mod, null, false);
                    }

                    info.currentInfo = newInfo;

                    break;
                }
            }

            updateListKeyColours();

            rendModEnt.setEntityToRender(compileAndApply());
        }

        //TODO fix eye height when shooting bow

        private void updateListKeyColours()
        {
            for(ElementList.Item<?> item : listKeys.items)
            {
                ElementTextWrapper text = (ElementTextWrapper)item.elements.get(0);
                ModifierInfo modInfo = (ModifierInfo)item.getObject();

                if(modInfo.currentInfo.modifier == null)
                {
                    text.setColor(0xaaaaaa);
                    text.setTooltip(text.getText().get(0) + "\n\n" + I18n.format("morph.gui.workspace.nbt.nbtType") + modInfo.inbt.getType().getName() + "\n\n" + I18n.format("morph.gui.workspace.nbt.stripped"));
                }
                else if(modInfo.currentInfo.handledElsewhere)
                {
                    text.setColor(0xffff55);
                    text.setTooltip(text.getText().get(0) + "\n\n" + I18n.format("morph.gui.workspace.nbt.nbtType") + modInfo.inbt.getType().getName() + "\n\n" + I18n.format("morph.gui.workspace.nbt.handledElsewhere"));
                }
                else
                {
                    text.setColor(null);
                    text.setTooltip(text.getText().get(0) + "\n\n" + I18n.format("morph.gui.workspace.nbt.nbtType") + modInfo.inbt.getType().getName() + "\n\n" + I18n.format("morph.gui.workspace.nbt.kept"));
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
                            NbtModifier.Modifier rootMod = modInfo.currentInfo.modifier;
                            while(parent != null)
                            {
                                NbtModifier.Modifier parentMod = modifiers.computeIfAbsent(parent, parentKey -> {
                                    NbtModifier.Modifier mod = new NbtModifier.Modifier();
                                    mod.key = parentKey.key;
                                    return mod;
                                });
                                if(parentMod.nestedModifiers == null)
                                {
                                    parentMod.nestedModifiers = new ArrayList<>();
                                }
                                if(!parentMod.nestedModifiers.contains(rootMod))
                                {
                                    parentMod.nestedModifiers.add(rootMod);
                                }

                                rootMod = parentMod;
                                parent = parent.parent;
                            }

                            modifier.modifiers.add(rootMod);

                            combineChildMods(modifier.modifiers);
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
                            combineChildMods(modModifiers);
                        }
                        else
                        {
                            modModifiers.add(modInfo.currentInfo.modifier);
                        }
                    }
                }
            }

            if(modifier.modifiers.isEmpty())
            {
                modifier.modifiers = null;
            }

            if(modifier.modSpecificModifiers.isEmpty())
            {
                modifier.modSpecificModifiers = null;
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
                        for(NbtModifier.Modifier nestedModifier : mod.nestedModifiers)
                        {
                            if(!oldMod.nestedModifiers.contains(nestedModifier))
                            {
                                oldMod.nestedModifiers.add(nestedModifier);
                            }
                        }
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

        public boolean export()
        {
            Path dir = ResourceHandler.getMorphDir().resolve("export");
            try
            {
                if(!Files.exists(dir))
                {
                    Files.createDirectory(dir);
                }

                ResourceLocation rl = rendModEnt.entToRender.getType().getRegistryName();

                if(!rl.getNamespace().equals("minecraft"))
                {
                    dir = dir.resolve(WindowMobData.ViewMobData.capitaliseWords(rl.getNamespace(), true));
                    if(!Files.exists(dir))
                    {
                        Files.createDirectory(dir);
                    }
                }

                Class clz = null;
                for(ElementList.Item<?> item : listClass.items)
                {
                    if(item.selected)
                    {
                        clz = (Class)item.getObject();
                        break;
                    }
                }

                if(clz == null)
                {
                    WindowPopup.popup(parentFragment.parent, 0.6D, 180, null, I18n.format("morph.gui.workspace.nbt.classRequired"));
                    return false;
                }

                NbtModifier modifier = compileModifier();

                if(!Minecraft.getInstance().getSession().getUsername().equals("Dev"))
                {
                    modifier.author = Minecraft.getInstance().getSession().getUsername();
                }
                modifier.forClass = clz.getName();

                Path file = dir.resolve(clz.getSimpleName() + ".json");

                String json = ResourceHandler.GSON.toJson(modifier);
                FileUtils.writeStringToFile(file.toFile(), json, "UTF-8");

                return true;
            }
            catch(Throwable e)
            {
                Morph.LOGGER.error("Error exporting NBT Modifier file.", e);
            }

            return false;
        }

        public static class ModifierInfo
        {
            public ModifierInfo parent;
            public ArrayList<ModifierInfo> children;
            public String key;
            public INBT inbt;
            public Info originalInfo;
            public Info currentInfo;

            public ModifierInfo(ModifierInfo parent, String key, INBT inbt, Info info)
            {
                this.parent = parent;
                this.children = new ArrayList<>();
                this.key = key;
                this.inbt = inbt;
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
