package me.ichun.mods.morph.common.morph.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NbtModifier
{
    //Used for serialisation
    @Nonnull
    public String forClass = "Class Name Here";
    public ArrayList<Modifier> modifiers = new ArrayList<>();
    public HashMap<String, ArrayList<Modifier>> modSpecificModifiers = new HashMap<>();

    //used in runtime
    public transient HashSet<String> toStrip;
    public transient HashMap<String, Modifier> keyToModifier;
    public transient boolean valuesSetup;

    public void setup()
    {
        //set up the runtime stuff
        for(Modifier modifier : modifiers)
        {
            addModifier(modifier);
        }

        modSpecificModifiers.forEach((k, v) -> {
            if(ModList.get().isLoaded(k))
            {
                for(Modifier modifier : v)
                {
                    addModifier(modifier);
                }
            }
        });
    }

    private void addModifier(Modifier modifier)
    {
        if(modifier.strip != null) //set to strip
        {
            if(modifier.strip)
            {
                toStrip.add(modifier.key);
            }
            else
            {
                toStrip.remove(modifier.key);
            }
        }
        else if(keyToModifier.containsKey(modifier.key) && keyToModifier.get(modifier.key).nestedModifiers != null && modifier.nestedModifiers != null) //both have nested modifiers
        {
            keyToModifier.get(modifier.key).addNestedModifiers(modifier);
        }
        else // override
        {
            keyToModifier.put(modifier.key, modifier.copy());
        }
    }

    public void setupValues()
    {
        if(!valuesSetup)
        {
            keyToModifier.entrySet().removeIf(e -> !e.getValue().setupValue());
            valuesSetup = true;
        }
    }

    public void apply(CompoundNBT tag)
    {
        for(String s : toStrip)
        {
            tag.remove(s);
        }

        keyToModifier.forEach((k, v) -> {
            v.apply(tag);
        });
    }

    public static class Modifier
    {
        @Nonnull
        public String key = "UNSET";
        public Boolean strip;
        public ArrayList<Modifier> nestedModifiers;
        public String value;

        public transient INBT nbtValue;

        public Modifier copy()
        {
            Modifier copy = new Modifier();
            copy.key = key;
            copy.strip = strip;
            if(nestedModifiers != null)
            {
                copy.nestedModifiers = new ArrayList<>();
                for(Modifier nestedModifier : nestedModifiers)
                {
                    copy.nestedModifiers.add(nestedModifier.copy());
                }
            }
            copy.value = value;
            return copy;
        }

        public boolean setupValue()
        {
            if(strip != null && strip)
            {
                return true;
            }

            if(nestedModifiers != null)
            {
                for(Modifier nestedModifier : nestedModifiers)
                {
                    if(!nestedModifier.setupValue())
                    {
                        return false;
                    }
                }
            }

            if(value != null)
            {
                //Taken from JsonToNBT
                JsonToNBT jsonToNBT = new JsonToNBT(new StringReader(value));
                try
                {
                    nbtValue = jsonToNBT.readValue();
                    return true;
                }
                catch(CommandSyntaxException e)
                {
                    Morph.LOGGER.error("Error parsing Modifier key {} of value {}. Removing modifier.", key, value);
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        public void apply(CompoundNBT tag)
        {
            if(strip != null)
            {
                if(strip) //this will allow an override to NOT strip.
                {
                    tag.tagMap.remove(key);
                }
                return;
            }

            if(nestedModifiers != null && !nestedModifiers.isEmpty() && tag.tagMap.get(key) instanceof CompoundNBT)
            {
                CompoundNBT compoundNBT = (CompoundNBT)tag.tagMap.get(key);
                for(Modifier nestedModifier : nestedModifiers)
                {
                    nestedModifier.apply(compoundNBT);
                }
            }

            if(nbtValue != null)
            {
                INBT ori = tag.tagMap.get(key);

                if(ori != null && ori.getType() != nbtValue.getType())
                {
                    Morph.LOGGER.error("Error applying Modifier key {} of value {}. Incompatible INBT type of {} against {}.", key, value, nbtValue.getType(), ori.getType());
                    return;
                }

                tag.tagMap.put(key, nbtValue.copy());
            }
        }

        public void addNestedModifiers(Modifier modifier) //check that the keys are the same before calling func
        {
            ArrayList<Modifier> newNesteds = new ArrayList<>(modifier.nestedModifiers);
            for(int i = nestedModifiers.size() - 1; i >= 0; i--)
            {
                Modifier nested = nestedModifiers.get(i);
                for(int j = newNesteds.size() - 1; j >= 0; j--)
                {
                    Modifier newNested = newNesteds.get(j);
                    if(nested.key.equals(newNested.key)) //same key
                    {
                        newNesteds.remove(j); //we found a match, remove from the list

                        if(nested.nestedModifiers != null && newNested.nestedModifiers != null) //both have nested modifiers
                        {
                            nested.addNestedModifiers(newNested);
                        }
                        else //remove the old modifier and add the new one
                        {
                            nestedModifiers.remove(i);
                            nestedModifiers.add(i, newNested);
                        }

                        break; //match found, stop processing.
                    }
                }
            }
            nestedModifiers.addAll(newNesteds); //add all the modifiers with no matches.

            value = modifier.value;
        }
    }
}
