package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphVariant;

import java.util.ArrayList;
import java.util.HashMap;

public class TickHandlerServer
{
    public HashMap<String, MorphInfo> morphsActive = new HashMap<String, MorphInfo>();
    public HashMap<String, ArrayList<MorphVariant>> playerMorphs = new HashMap<String, ArrayList<MorphVariant>>();//For clients a morph state is created from a Morph Variant. Hmm. WE SHALL SEE.
}
