package morph.common.thread;

public class ThreadGetOnlineResources extends Thread
{
    public ThreadGetOnlineResources()
    {
        this.setName("Morph Online Resource Thread");
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        try
        {

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
