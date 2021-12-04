package me.ichun.mods.morph.api.mob.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.ichun.mods.morph.api.MorphApi;
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
    public String author; //For Credit
    @Nonnull
    public String forClass = "Class Name Here";
    public Boolean isInterface;
    public ArrayList<Modifier> modifiers = new ArrayList<>();
    public HashMap<String, ArrayList<Modifier>> modSpecificModifiers = new HashMap<>();

    //used in runtime
    public transient HashSet<String> toKeep;
    public transient HashMap<String, Modifier> keyToModifier;
    public transient boolean valuesSetup;

    public void setup()
    {
        //set up the runtime stuff
        if(modifiers != null)
        {
            for(Modifier modifier : modifiers)
            {
                addModifier(modifier);
            }
        }

        if(modSpecificModifiers != null)
        {
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
    }

    private void addModifier(Modifier modifier)
    {
        if(modifier.keep != null) //set to strip
        {
            if(modifier.keep)
            {
                toKeep.add(modifier.key);
            }
            else
            {
                toKeep.remove(modifier.key);
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

    public void apply(CompoundNBT tag) //This is the parent function. This calls all its passed modifiers
    {
        //Collect all the keys we want to keep with this tag
        HashSet<String> keysKept = new HashSet<>(toKeep);
        keysKept.addAll(keyToModifier.keySet());

        //Check the keys the tags have, if we don't have it in our set, remove it.
        HashSet<String> tagKeys = new HashSet<>(tag.keySet());
        for(String s : tagKeys)
        {
            if(!keysKept.contains(s))
            {
                tag.remove(s);
            }
        }

        //Apply the specific keyToModifiers
        keyToModifier.forEach((k, v) -> {
            v.apply(tag);
        });
    }

    public static class Modifier
    {
        @Nonnull
        public String key = "UNSET";
        public Boolean keep;
        public ArrayList<Modifier> nestedModifiers;
        public String value;

        public transient INBT nbtValue;

        public Modifier copy()
        {
            Modifier copy = new Modifier();
            copy.key = key;
            copy.keep = keep;
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
            if(keep != null)
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
                    MorphApi.getLogger().error("Error parsing Modifier key {} of value {}. Removing modifier.", key, value);
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        public void apply(CompoundNBT tag)
        {
            //We're told the keep command, handle the keep and return, do nothing more.
            if(keep != null)
            {
                //If the modifier is told *NOT* to keep, we strip it
                if(!keep)
                {
                    tag.tagMap.remove(key);
                }
                return;
            }

            //We have nested modifiers. Check if this is a Compound Tag and strip the rest.
            if(nestedModifiers != null && !nestedModifiers.isEmpty() && tag.tagMap.get(key) instanceof CompoundNBT)
            {
                HashSet<String> keep = new HashSet<>();
                CompoundNBT compoundNBT = (CompoundNBT)tag.tagMap.get(key);
                for(Modifier nestedModifier : nestedModifiers)
                {
                    keep.add(nestedModifier.key); //add this key to the ones we wanna keep since we're modifying this
                    nestedModifier.apply(compoundNBT);
                }

                //Check the keys the tags have, if we don't have it in our set, remove it.
                HashSet<String> tagKeys = new HashSet<>(compoundNBT.keySet());
                for(String s : tagKeys)
                {
                    if(!keep.contains(s))
                    {
                        compoundNBT.remove(s);
                    }
                }
            }

            //We're modifying the value, do it.
            if(nbtValue != null)
            {
                INBT ori = tag.tagMap.get(key);

                if(ori != null && ori.getType() != nbtValue.getType())
                {
                    MorphApi.getLogger().error("Error applying Modifier key {} of value {}. Incompatible INBT type of {} against {}.", key, value, nbtValue.getType(), ori.getType());
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

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof Modifier)
            {
                Modifier mod = (Modifier)obj;
                if(this.key != null && this.key.equals(mod.key))
                {
                    return (this.keep != null && this.keep.equals(mod.keep) || this.keep == null && mod.keep == null) && areNestedModifiersEqual(mod.nestedModifiers) && (this.value != null && this.value.equals(mod.value) || this.value == null && mod.value == null);
                }
            }
            return false;
        }

        private boolean areNestedModifiersEqual(ArrayList<Modifier> nested)
        {
            if(this.nestedModifiers == null && nested == null)
            {
                return true;
            }
            else if(this.nestedModifiers != null && nested != null && this.nestedModifiers.size() == nested.size())
            {
                boolean equal = true;
                for(int i = 0; i < nestedModifiers.size(); i++)
                {
                    if(!nestedModifiers.get(i).equals(nested.get(i)))
                    {
                        equal = false;
                        break;
                    }
                }
                return equal;
            }
            return false;
        }
    }
}
