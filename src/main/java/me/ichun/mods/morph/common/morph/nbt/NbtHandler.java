package me.ichun.mods.morph.common.morph.nbt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NbtHandler
{
    private static final HashMap<Class<? extends LivingEntity>, NbtModifier> NBT_MODIFIERS = new HashMap<>();
    private static final HashMap<Class<?>, NbtModifier> NBT_MODIFIERS_INTERFACES = new HashMap<>();

    public static void loadNbtModifiers()
    {
        NBT_MODIFIERS.clear();
        NBT_MODIFIERS_INTERFACES.clear();

        //        serialiseModifiers();

        try
        {
            IOUtil.scourDirectoryForFiles(ResourceHandler.getMorphDir().resolve("nbt"), p -> {
                if(p.getFileName().toString().endsWith(".json"))
                {
                    File file = p.toFile();
                    try
                    {
                        String json = FileUtils.readFileToString(file, "UTF-8");
                        if(readNbtJson(json))
                        {
                            return true;
                        }
                        else
                        {
                            Morph.LOGGER.error("Error reading NBT Modifier file, no forClass: {}", file);
                            return false;
                        }
                    }
                    catch(IOException | JsonSyntaxException e)
                    {
                        Morph.LOGGER.error("Error reading NBT Modifier file: {}", file);
                        e.printStackTrace();
                    }
                    catch(ClassNotFoundException ignored){}
                }
                return false;
            });
        }
        catch(IOException e)
        {
            Morph.LOGGER.error("Error loading NBT Modifier files.", e);
        }

        Morph.LOGGER.info("Loaded {} NBT Modifier(s)", NBT_MODIFIERS.size() + NBT_MODIFIERS_INTERFACES.size());

        setupInterfaceModifiers();
    }

    private static boolean readNbtJson(String json) throws ClassNotFoundException, JsonSyntaxException
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        if(jsonObject.has("forClass"))
        {
            String className = jsonObject.get("forClass").getAsString();

            Class clz = Class.forName(className);

            boolean forInterface = jsonObject.has("isInterface") && jsonObject.get("isInterface").getAsBoolean();

            if(!forInterface && NBT_MODIFIERS.containsKey(clz) || forInterface && NBT_MODIFIERS_INTERFACES.containsKey(clz))
            {
                Morph.LOGGER.warn("We already have another NBT Modifier for {}", clz.getName());
            }

            try
            {
                NbtModifier nbtModifier = ResourceHandler.GSON.fromJson(json, NbtModifier.class);
                if(forInterface)
                {
                    NBT_MODIFIERS_INTERFACES.put(clz, nbtModifier);
                }
                else
                {
                    NBT_MODIFIERS.put(clz, nbtModifier);
                }
            }
            catch(Throwable t)
            {
                Morph.LOGGER.error("Error deserialising NBT Modifier for {}", clz.getName());
                t.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private static void serialiseModifiers()
    {
        Path file = ResourceHandler.getMorphDir().resolve("nbt").resolve("LivingEntity.json");

        NbtModifier modifier = new NbtModifier();
        modifier.forClass = LivingEntity.class.getName();
        String[] strip = new String[] { "Health", "HurtTime", "HurtByTimestamp", "DeathTime", "AbsorptionAmount", "FallFlying", "SleepingX", "SleepingY", "SleepingZ", "Brain" };
        for(String s : strip)
        {
            NbtModifier.Modifier mod = new NbtModifier.Modifier();
            mod.key = s;
            mod.strip = true;
            modifier.modifiers.add(mod);
        }

        NbtModifier.Modifier hatsMod = new NbtModifier.Modifier();
        hatsMod.key = "ForgeCaps";

        NbtModifier.Modifier partMod = new NbtModifier.Modifier();
        partMod.key = "hats:capability_hat";
        partMod.strip = true;

        hatsMod.nestedModifiers = new ArrayList<>();
        hatsMod.nestedModifiers.add(partMod);

        ArrayList<NbtModifier.Modifier> list = new ArrayList<>();
        list.add(hatsMod);

        modifier.modSpecificModifiers.put("hats", list);

        try
        {
            String json = ResourceHandler.GSON.toJson(modifier);
            FileUtils.writeStringToFile(file.toFile(), json, "UTF-8");
        }
        catch(IOException ignored){}
        catch(Throwable e1)
        {
            e1.printStackTrace();
        }
    }

    public static NbtModifier getModifierFor(LivingEntity living)
    {
        NbtModifier modifier = getModifierFor(living.getClass());

        //we're about to use this modifier. Set up the modifier values
        modifier.setupValues();

        return modifier;
    }

    private static NbtModifier getModifierFor(Class clz)
    {
        NbtModifier modifier;
        if(NBT_MODIFIERS.containsKey(clz))
        {
            modifier = NBT_MODIFIERS.get(clz);
            if(modifier.toStrip != null) // it's been set up;
            {
                return modifier;
            }
        }
        else
        {
            modifier = new NbtModifier();
            NBT_MODIFIERS.put(clz, modifier);
        }

        modifier.toStrip = new HashSet<>();
        modifier.keyToModifier = new HashMap<>();

        if(clz != LivingEntity.class)
        {
            //get the parent class's modifier and add their modifiers
            NbtModifier parentModifier = getModifierFor(clz.getSuperclass());

            modifier.toStrip.addAll(parentModifier.toStrip);
            modifier.keyToModifier.putAll(parentModifier.keyToModifier);
        }

        //Check the class' interfaces
        for(Map.Entry<Class<?>, NbtModifier> e : NBT_MODIFIERS_INTERFACES.entrySet())
        {
            if(e.getKey().isAssignableFrom(clz))
            {
                modifier.toStrip.addAll(e.getValue().toStrip);
                modifier.keyToModifier.putAll(e.getValue().keyToModifier);
            }
        }

        //setup adds this class' own modifiers.
        modifier.setup();

        return modifier;
    }

    private static void setupInterfaceModifiers()
    {
        for(Map.Entry<Class<?>, NbtModifier> e : NBT_MODIFIERS_INTERFACES.entrySet())
        {
            e.getValue().toStrip = new HashSet<>();
            e.getValue().keyToModifier = new HashMap<>();

            e.getValue().setup();
        }
    }

    public static void removeEmptyCompoundTags(CompoundNBT tag)
    {
        tag.tagMap.entrySet().removeIf(e -> e.getValue() instanceof CompoundNBT && ((CompoundNBT)e.getValue()).tagMap.isEmpty());
        tag.tagMap.entrySet().stream().filter(e -> e.getValue() instanceof CompoundNBT).forEach(e -> removeEmptyCompoundTags((CompoundNBT)e.getValue()));
    }
}
