package me.ichun.mods.morph.api.ability;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AbilityApi
{
    /**
     * GSON instance used to read and deserialize abilities from their JSON form.
     */
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static IAbilityHandler apiImpl = new AbilityHandlerDummy();

    /**
     * Get the IAbilityHandler implementation for Morph.
     *
     * @return returns the IAbilityHandler implementation from world portals. May be the AbilityHandlerDummy if Morph has not loaded.
     */
    public static IAbilityHandler getApiImpl() { return apiImpl; }

    /**
     * Sets the IAbilityHandler implementation for Morph Abilities.
     * For use of Morph, so please don't actually use this.
     *
     * @param api API implementation to set.
     */
    public static void setApiImpl(IAbilityHandler api)
    {
        apiImpl = api;
    }
}
