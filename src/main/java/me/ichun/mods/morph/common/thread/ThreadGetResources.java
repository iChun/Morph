package me.ichun.mods.morph.common.thread;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.ichun.mods.ichunutil.common.core.util.ResourceHelper;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.NBTHandler;
import net.minecraft.entity.EntityLivingBase;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ThreadGetResources extends Thread
{
    public String sitePrefix = "https://raw.github.com/iChun/Morph/1.10.2/src/main/resources/assets/morph/mod/";//TODO change this before release.

    public ThreadGetResources(String prefix)
    {
        if(!prefix.isEmpty())
        {
            sitePrefix = prefix;
        }
        this.setName("Morph Resource Thread");
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        HashMap<String, HashMap<String, String>> json = getResource("nbt_modifiers.json", new TypeToken<HashMap<String, HashMap<String, String>>>() {}.getType());

        int mcNBTModifiers = 0;

        NBTHandler.nbtModifiers.clear();
        for(Map.Entry<String, HashMap<String, String>> e : json.entrySet())
        {
            try
            {
                if(e.getKey().startsWith("example.class."))
                {
                    continue;
                }

                Class clz = Class.forName(e.getKey());

                HashMap<String, Object> modifiers = new HashMap<>();
                HashMap<String, String> map = e.getValue();

                for(Map.Entry<String, String> modifier : map.entrySet())
                {
                    String value = modifier.getValue();
                    Object obj = value;
                    if(value.equalsIgnoreCase("null"))
                    {
                        obj = null;
                    }
                    else if(value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true"))
                    {
                        obj = value.equalsIgnoreCase("true");
                    }
                    else if(value.endsWith("F"))
                    {
                        try
                        {
                            obj = Float.parseFloat(value.substring(0, value.length() - 1));
                        }
                        catch(NumberFormatException ignored){}
                    }
                    else if(value.endsWith("D"))
                    {
                        try
                        {
                            obj = Double.parseDouble(value.substring(0, value.length() - 1));
                        }
                        catch(NumberFormatException ignored){}
                    }
                    else if(value.endsWith("B"))
                    {
                        try
                        {
                            obj = Byte.parseByte(value.substring(0, value.length() - 1));
                        }
                        catch(NumberFormatException ignored){}
                    }
                    else if(value.endsWith("S"))
                    {
                        try
                        {
                            obj = Short.parseShort(value.substring(0, value.length() - 1));
                        }
                        catch(NumberFormatException ignored){}
                    }
                    else if(value.endsWith("L"))
                    {
                        try
                        {
                            obj = Long.parseLong(value.substring(0, value.length() - 1));
                        }
                        catch(NumberFormatException ignored){}
                    }
                    else
                    {
                        try
                        {
                            obj = Integer.parseInt(value);
                        }
                        catch(NumberFormatException ignored){}
                    }
                    modifiers.put(modifier.getKey(), obj);
                }
                if(EntityLivingBase.class.isAssignableFrom(clz) && !modifiers.isEmpty())
                {
                    NBTHandler.nbtModifiers.put(clz, modifiers);
                    if(clz.getName().startsWith("net.minecraft"))
                    {
                        mcNBTModifiers++;
                    }
                    else
                    {
                        Morph.LOGGER.info("Adding NBT modifiers for morphs for class: " + clz.getName());
                    }
                }
            }
            catch(ClassNotFoundException ignored){}
        }

        if(mcNBTModifiers > 0)
        {
            Morph.LOGGER.info("Loaded NBT modifiers for presumably " + mcNBTModifiers + " Minecraft mobs");
        }
        else
        {
            Morph.LOGGER.warn("No NBT modifiers for Minecraft mobs? This might be an issue!");
        }
    }

    public <T> T getResource(String name, Type mapType)
    {
        T objectType;
        Gson gson = new Gson();
        try
        {
            if(Morph.config.useLocalResources == 1)
            {
                InputStream con = new FileInputStream(new File(ResourceHelper.getConfigFolder(), name));
                String data = new String(ByteStreams.toByteArray(con));
                con.close();
                objectType = gson.fromJson(data, mapType);
            }
            else
            {
                Reader fileIn = new InputStreamReader(new URL(sitePrefix + name).openStream());
                objectType = gson.fromJson(fileIn, mapType);
                fileIn.close();
            }
        }
        catch(Exception e)
        {
            if(Morph.config.useLocalResources == 1)
            {
                Morph.LOGGER.warn("Failed to retrieve local resource: " + name);
            }
            else
            {
                Morph.LOGGER.warn("Failed to retrieve " + name + " from " + (Morph.config.customPatchLink.isEmpty() ? "GitHub!" : sitePrefix));
            }
            e.printStackTrace();

            Reader fileIn = new InputStreamReader(Morph.class.getResourceAsStream("/assets/morph/mod/" + name));
            objectType = gson.fromJson(fileIn, mapType);
            try
            {
                fileIn.close();
            }
            catch(IOException ignored){}
        }

        return objectType;
    }
}
