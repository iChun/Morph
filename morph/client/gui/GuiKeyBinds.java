package morph.client.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import morph.common.Morph;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.GuiControlsScrollPanel;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiKeyBinds extends GuiScreen
{
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    private GuiScreen parentScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "morph.gui.keybinds.title";

    /** The ID of the  button that has been pressed. */
    private int buttonId = -1;

    public GuiKeyBinds(GuiScreen par1GuiScreen)
    {
        this.parentScreen = par1GuiScreen;
    }

    /**
     * Gets the distance from the left border of the window to left border of the controls screen
     */
    private int getLeftBorder()
    {
        return this.width / 2 - 155;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
    	this.buttonList.add(new GuiButton(0 , 1 * (width - 320) / 5, 40, 80, 20, GameSettings.getKeyDisplayString(Morph.keySelectorUp)));
    	this.buttonList.add(new GuiButton(1 , 1 * (width - 320) / 5, 65, 80, 20, StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorUpHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorUpHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorUpHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"))));
    	
    	this.buttonList.add(new GuiButton(10, 2 * (width - 320) / 5 + 80, 40, 80, 20, GameSettings.getKeyDisplayString(Morph.keySelectorDown)));
    	this.buttonList.add(new GuiButton(11, 2 * (width - 320) / 5 + 80, 65, 80, 20, StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorDownHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorDownHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorDownHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"))));
    	
    	this.buttonList.add(new GuiButton(20, 3 * (width - 320) / 5 + 160, 40, 80, 20, GameSettings.getKeyDisplayString(Morph.keySelectorLeft)));
    	this.buttonList.add(new GuiButton(21, 3 * (width - 320) / 5 + 160, 65, 80, 20, StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorLeftHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorLeftHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorLeftHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"))));
    	
    	this.buttonList.add(new GuiButton(30, 4 * (width - 320) / 5 + 240, 40, 80, 20, GameSettings.getKeyDisplayString(Morph.keySelectorRight)));
    	this.buttonList.add(new GuiButton(31, 4 * (width - 320) / 5 + 240, 65, 80, 20, StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorRightHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorRightHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorRightHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"))));
    	
    	this.buttonList.add(new GuiButton(40 , 1 * (width - 320) / 5, 110, 80, 20, GameSettings.getKeyDisplayString(Morph.keySelectorSelect)));
    	this.buttonList.add(new GuiButton(50, 2 * (width - 320) / 5 + 80, 110, 80, 20, GameSettings.getKeyDisplayString(Morph.keySelectorCancel)));
    	this.buttonList.add(new GuiButton(60, 3 * (width - 320) / 5 + 160, 110, 80, 20, GameSettings.getKeyDisplayString(Morph.keySelectorRemoveMorph)));
    	this.buttonList.add(new GuiButton(70, 4 * (width - 320) / 5 + 240, 110, 80, 20, GameSettings.getKeyDisplayString(Morph.keyFavourite)));

        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height - 28, I18n.getString("gui.done")));
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton btn)
    {
    	if(btn.id % 10 == 0)
    	{
    		buttonId = btn.id;
    		btn.displayString = ">???<";
    	}
    	else if(btn.id % 10 == 1)
    	{
    		if(btn.id == 1)
    		{
    			Morph.keySelectorUpHold++;
    			if(Morph.keySelectorUpHold > 3)
    			{
    				Morph.keySelectorUpHold = 0;
    			}
    			btn.displayString = StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorUpHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorUpHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorUpHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"));
    		}
    		else if(btn.id == 11)
    		{
    			Morph.keySelectorDownHold++;
    			if(Morph.keySelectorDownHold > 3)
    			{
    				Morph.keySelectorDownHold = 0;
    			}
    			btn.displayString = StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorDownHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorDownHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorDownHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"));
    		}
    		else if(btn.id == 21)
    		{
    			Morph.keySelectorLeftHold++;
    			if(Morph.keySelectorLeftHold > 3)
    			{
    				Morph.keySelectorLeftHold = 0;
    			}
    			btn.displayString = StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorLeftHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorLeftHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorLeftHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"));
    		}
    		else if(btn.id == 31)
    		{
    			Morph.keySelectorRightHold++;
    			if(Morph.keySelectorRightHold > 3)
    			{
    				Morph.keySelectorRightHold = 0;
    			}
    			btn.displayString = StatCollector.translateToLocal("morph.gui.keybinds.hold") + ": " + (Morph.keySelectorRightHold == 0 ? StatCollector.translateToLocal("morph.gui.keybinds.none") : Morph.keySelectorRightHold == 1 ? StatCollector.translateToLocal("morph.gui.keybinds.shift") : Morph.keySelectorRightHold == 2 ? StatCollector.translateToLocal("morph.gui.keybinds.ctrl") : StatCollector.translateToLocal("morph.gui.keybinds.alt"));
    		}
    		Morph.saveConfig();
    	}
        if (btn.id == 200)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int par1, int par2, int par3)
    {
        for (int l = 0; l < this.buttonList.size(); ++l)
        {
            GuiButton guibutton = (GuiButton)this.buttonList.get(l);

            if (buttonId == -1 && par3 == 0 && guibutton.mousePressed(this.mc, par1, par2))
            {
                this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
               	this.actionPerformed(guibutton);
            }
            else if(buttonId % 10 == 0 && buttonId == guibutton.id)
            {
            	this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
            	
            	int i = par3 - 100;
        		if(buttonId == 0)
        		{
        			Morph.keySelectorUp = i;
        		}
        		else if(buttonId == 10)
        		{
        			Morph.keySelectorDown = i;
        		}
        		else if(buttonId == 20)
        		{
        			Morph.keySelectorLeft = i;
        		}
        		else if(buttonId == 30)
        		{
        			Morph.keySelectorRight = i;
        		}
        		else if(buttonId == 40)
        		{
        			Morph.keySelectorSelect = i;
        		}
        		else if(buttonId == 50)
        		{
        			Morph.keySelectorCancel = i;
        		}
        		else if(buttonId == 60)
        		{
        			Morph.keySelectorRemoveMorph = i;
        		}
        		else if(buttonId == 70)
        		{
        			Morph.keyFavourite = i;
        		}
        		buttonId = -1;
        		guibutton.displayString = GameSettings.getKeyDisplayString(par3 - 100);
            }
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char c, int i)
    {
    	if(buttonId != -1)
    	{
    		for(int k = 0; k < buttonList.size(); k++)
    		{
    			GuiButton btn = (GuiButton)buttonList.get(k);
    			if(btn.id == buttonId)
    			{
    				btn.displayString = GameSettings.getKeyDisplayString(i);
    				break;
    			}
    		}
    		if(buttonId == 0)
    		{
    			Morph.keySelectorUp = i;
    		}
    		else if(buttonId == 10)
    		{
    			Morph.keySelectorDown = i;
    		}
    		else if(buttonId == 20)
    		{
    			Morph.keySelectorLeft = i;
    		}
    		else if(buttonId == 30)
    		{
    			Morph.keySelectorRight = i;
    		}
    		else if(buttonId == 40)
    		{
    			Morph.keySelectorSelect = i;
    		}
    		else if(buttonId == 50)
    		{
    			Morph.keySelectorCancel = i;
    		}
    		else if(buttonId == 60)
    		{
    			Morph.keySelectorRemoveMorph = i;
    		}
    		else if(buttonId == 70)
    		{
    			Morph.keyFavourite = i;
    		}
    		buttonId = -1;
    		
    		Morph.saveConfig();
    	}
    	else
    	{
    		super.keyTyped(c, i);
    	}
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        drawCenteredString(fontRenderer, StatCollector.translateToLocal(screenTitle), width / 2, 4, 0xffffff);
        
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.selector") + " " + StatCollector.translateToLocal("morph.gui.keybinds.up"),  1 * (width - 320) / 5 + 40, 30, 0xffffff);
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.selector") + " " + StatCollector.translateToLocal("morph.gui.keybinds.down"),  2 * (width - 320) / 5 + 80 + 40, 30, 0xffffff);
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.selector") + " " + StatCollector.translateToLocal("morph.gui.keybinds.left"),  3 * (width - 320) / 5 + 160 + 40, 30, 0xffffff);
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.selector") + " " + StatCollector.translateToLocal("morph.gui.keybinds.right"),  4 * (width - 320) / 5 + 240 + 40, 30, 0xffffff);
        
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.selector") + " " + StatCollector.translateToLocal("morph.gui.keybinds.choose"),  1 * (width - 320) / 5 + 40, 100, 0xffffff);
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.selector") + " " + StatCollector.translateToLocal("morph.gui.keybinds.cancel"),  2 * (width - 320) / 5 + 80 + 40, 100, 0xffffff);
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.selector") + " " + StatCollector.translateToLocal("morph.gui.keybinds.remove"),  3 * (width - 320) / 5 + 160 + 40, 100, 0xffffff);
        drawCenteredString(fontRenderer, StatCollector.translateToLocal("morph.gui.keybinds.favorite"),  4 * (width - 320) / 5 + 240 + 40, 100, 0xffffff);

        super.drawScreen(par1, par2, par3);
    }
}
