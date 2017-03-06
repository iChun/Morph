package morph.common.thread;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import morph.api.Ability;
import morph.common.Morph;
import morph.common.ability.AbilityHandler;
import morph.common.morph.MorphHandler;
import net.minecraft.entity.EntityLivingBase;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class ThreadGetOnlineResources extends Thread
{
    public String sitePrefix = "https://raw.github.com/iChun/Morph/master/src/main/resources/assets/morph/mod/";

    public ThreadGetOnlineResources(String prefix)
    {
        if(!prefix.isEmpty())
        {
            sitePrefix = prefix;
        }
        this.setName("Morph Online Resource Thread");
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        try
        {
            if(Morph.config.getInt("abilities") == 1)
            {
                Gson gson = new Gson();
                Map<String, String[]> json;

                Type mapType = new TypeToken<Map<String, String[]>>() {}.getType();

                try
                {
                    if(Morph.config.getInt("useLocalResources") == 1)
                    {
                        InputStream con = new FileInputStream(new File(Morph.configFolder, "AbilitySupport.json"));
                        String data = new String(ByteStreams.toByteArray(con));
                        con.close();
                        json = gson.fromJson(data, mapType);
                    }
                    else
                    {
                        Reader fileIn = new InputStreamReader(new URL(sitePrefix + "AbilitySupport.json").openStream());
                        json = gson.fromJson(fileIn, mapType);
                        fileIn.close();
                    }
                }
                catch(Exception e)
                {
                    if(Morph.config.getInt("useLocalResources") == 1)
                    {
                        Morph.console("Failed to retrieve local mod mob ability mappings.", true);
                    }
                    else
                    {
                        Morph.console("Failed to retrieve mod mob ability mappings from " + (Morph.config.getString("customPatchLink").isEmpty() ? "GitHub!" : sitePrefix), true);
                    }
                    e.printStackTrace();

                    Reader fileIn = new InputStreamReader(Morph.class.getResourceAsStream("/assets/morph/mod/AbilitySupport.json"));
                    json = gson.fromJson(fileIn, mapType);
                    fileIn.close();
                }

                if(json != null)
                {
                    int mcMappings = 0;
                    for(Map.Entry<String, String[]> e : json.entrySet())
                    {
                        try
                        {
                            Class.forName(e.getKey());

                            ArrayList<Ability> abilityObjs = new ArrayList<Ability>();
                            for(String ability : e.getValue()){
                                boolean hasArgs = false;
                                if(ability != null)
                                {
                                    if(ability.contains("|"))
                                    {
                                        ArrayList<String> argVars = new ArrayList<String>();
                                        hasArgs = true;
                                        String args = ability.split("\\|")[1];
                                        ability = ability.split("\\|")[0];
                                        if(args.contains(","))
                                        {
                                            for(String arg : args.split(","))
                                            {
                                                argVars.add(arg.trim());
                                            }
                                        }
                                        else
                                        {
                                            argVars.add(args.trim());
                                        }
                                        try
                                        {
                                            Class abilityClass = AbilityHandler.stringToClassMap.get(ability);
                                            if(abilityClass != null)
                                            {
                                                Ability ab = ((Ability)abilityClass.getConstructor().newInstance());
                                                try
                                                {
                                                    ab.parse(argVars.toArray(new String[0]));
                                                }
                                                catch(Exception e2)
                                                {
                                                    Morph.console("Mappings are erroring! These mappings are probably invalid or outdated: "  + abilityClass.getName() + ", "+ ability + ", args: " + args, true);
                                                }
                                                abilityObjs.add(ab);
                                            }
                                            else
                                            {
                                                Morph.console("Ability \"" + ability + "\" does not exist for: "  + e.getKey() + ", args: " + args, true);
                                            }
                                        }
                                        catch(Exception e2)
                                        {
                                            e2.printStackTrace();
                                        }
                                    }
                                    if(!ability.isEmpty() && !hasArgs)
                                    {
                                        Class abilityClass = AbilityHandler.stringToClassMap.get(ability);
                                        if(abilityClass != null)
                                        {
                                            try
                                            {
                                                abilityObjs.add((Ability)abilityClass.getConstructor().newInstance());
                                            }
                                            catch(Exception e2)
                                            {
                                                e2.printStackTrace();
                                            }
                                        }
                                        else
                                        {
                                            Morph.console("Ability \"" + ability + "\" does not exist for: "  + e.getKey(), true);
                                        }
                                    }
                                }
                            }
                            if(abilityObjs.size() > 0){
                                Class<? extends EntityLivingBase> entityClass = (Class<? extends EntityLivingBase>) Class.forName(e.getKey());
                                if(entityClass != null){
                                    if(entityClass.getName().startsWith("net.minecraft"))
                                    {
                                        mcMappings++;
                                    }
                                    else
                                    {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("Adding ability mappings ");
                                        for(int i = 0; i < abilityObjs.size(); i++)
                                        {
                                            Ability a = abilityObjs.get(i);
                                            sb.append(a.getType());
                                            if(i != abilityObjs.size() - 1)
                                            {
                                                sb.append(", ");
                                            }
                                        }
                                        sb.append(" to ");
                                        sb.append(entityClass);

                                        Morph.console(sb.toString(), false);
                                    }
                                    AbilityHandler.mapAbilities(entityClass, abilityObjs.toArray(new Ability[abilityObjs.size()]));
                                }
                            }
                        }
                        catch(ClassNotFoundException e1)
                        {
                        }
                    }
                    Morph.console("Found and mapped ability mappings for " + mcMappings + " presumably Minecraft mobs.", false);
                }
            }

            if(Morph.config.getInt("NBTStripper") == 1)
            {
                Gson gson = new Gson();
                Map<String, String[]> json;

                Type mapType = new TypeToken<Map<String, String[]>>() {}.getType();

                try
                {
                    if(Morph.config.getInt("useLocalResources") == 1)
                    {
                        InputStream con = new FileInputStream(new File(Morph.configFolder, "NBTStripper.json"));
                        String data = new String(ByteStreams.toByteArray(con));
                        con.close();
                        json = gson.fromJson(data, mapType);
                    }
                    else
                    {
                        Reader fileIn = new InputStreamReader(new URL(sitePrefix + "NBTStripper.json").openStream());
                        json = gson.fromJson(fileIn, mapType);
                        fileIn.close();
                    }
                }
                catch(Exception e)
                {
                    if(Morph.config.getInt("useLocalResources") == 1)
                    {
                        Morph.console("Failed to retrieve local NBT stripper mappings.", true);
                    }
                    else
                    {
                        Morph.console("Failed to retrieve NBT stripper mappings from " + (Morph.config.getString("customPatchLink").isEmpty() ? "GitHub!" : sitePrefix), true);
                    }
                    e.printStackTrace();

                    Reader fileIn = new InputStreamReader(Morph.class.getResourceAsStream("/assets/morph/mod/NBTStripper.json"));
                    json = gson.fromJson(fileIn, mapType);
                    fileIn.close();
                }

                if(json != null)
                {
                    for(Map.Entry<String, String[]> e : json.entrySet())
                    {
                        try
                        {
                            addStripperMappings((Class<? extends EntityLivingBase>) Class.forName(e.getKey()), e.getValue());
                        }
                        catch(Exception e1)
                        {
                        }
                    }
                }
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void addStripperMappings(Class<? extends EntityLivingBase> clz, String...tagNames)
    {
        ArrayList<String> mappings = MorphHandler.getNBTTagsToStrip(clz);
        for(String tagName : tagNames)
        {
            if(!mappings.contains(tagName))
            {
                mappings.add(tagName);
            }
        }
    }
}
