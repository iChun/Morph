package me.ichun.mods.morph.common.handler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class NBTHandler
{
    public static HashMap<Class<? extends EntityLivingBase>, HashMap<String, Object>> nbtModifiers = new HashMap<Class<? extends EntityLivingBase>, HashMap<String, Object>>();

    public static void modifyNBT(Class<? extends EntityLivingBase> clz, NBTTagCompound tag)
    {
        HashMap<String, Object> modifiers = getModifiers(clz);
        for(Map.Entry<String, Object> e : modifiers.entrySet())
        {
            Object obj = e.getValue();
            if(obj == null)
            {
                tag.removeTag(e.getKey());
            }
            else if(obj instanceof Boolean)
            {
                tag.setBoolean(e.getKey(), (Boolean)obj);
            }
            else if(obj instanceof String)
            {
                tag.setString(e.getKey(), (String)obj);
            }
            else if(obj instanceof Float)
            {
                tag.setFloat(e.getKey(), (Float)obj);
            }
            else if(obj instanceof Double)
            {
                tag.setDouble(e.getKey(), (Double)obj);
            }
            else if(obj instanceof Integer)
            {
                tag.setInteger(e.getKey(), (Integer)obj);
            }
            else if(obj instanceof Byte)
            {
                tag.setByte(e.getKey(), (Byte)obj);
            }
            else if(obj instanceof Short)
            {
                tag.setShort(e.getKey(), (Short)obj);
            }
            else if(obj instanceof Long)
            {
                tag.setLong(e.getKey(), (Long)obj);
            }
        }
    }

    public static HashMap<String, Object> getModifiers(Class<? extends EntityLivingBase> entClz)
    {
        HashMap<String, Object> modifiers = new HashMap<String, Object>();
        while(entClz != EntityLivingBase.class)
        {
            if(nbtModifiers.containsKey(entClz))
            {
                modifiers.putAll(nbtModifiers.get(entClz));
            }
            entClz = (Class<? extends EntityLivingBase>)entClz.getSuperclass();
        }
        return modifiers;
    }
}
