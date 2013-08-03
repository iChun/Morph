package morph.common.morph;

import java.util.ArrayList;

public class MorphHandler 
{

	public static MorphState addOrGetMorphState(ArrayList<MorphState> states, MorphState state)
	{
		for(MorphState mState : states)
		{
			if(mState.identifier.equalsIgnoreCase(state.identifier))
			{
				return mState;
			}
		}
		states.add(state);
		return state;
	}
	
}
