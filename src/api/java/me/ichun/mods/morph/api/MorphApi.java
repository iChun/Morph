package me.ichun.mods.morph.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MorphApi
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static IApi apiImpl = new IApi(){};

    /**
     * Get the IApi implementation for Morph.
     * @return returns the IApi implementation from morph. Will not be an anonymous class if Morph has loaded
     */
    public static IApi getApiImpl()
    {
        return apiImpl;
    }

    /**
     * Sets the IApi implementation for Morph.
     * For use of Morph, so please don't actually use this.
     * @param apiImpl API implementation to set.
     */
    public static void setApiImpl(IApi apiImpl)
    {
        MorphApi.apiImpl = apiImpl;
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }
}
