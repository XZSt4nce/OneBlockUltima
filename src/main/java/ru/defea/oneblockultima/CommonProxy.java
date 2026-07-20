package ru.defea.oneblockultima;

import ru.defea.oneblockultima.event.ModEvents;

public class CommonProxy
{
    public void preInit()
    {
    }

    public void init()
    {
        ModEvents.register();
    }
}
