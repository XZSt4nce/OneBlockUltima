package ru.defea.oneblockultima.gui;

public class DonateMethod
{
    public enum Type
    {
        TEXT,
        LINK
    }

    public final Type type;
    public final String text;
    public final String value;

    public DonateMethod(Type type, String text, String value)
    {
        this.type = type;
        this.text = text;
        this.value = value;
    }

    public static final DonateMethod[] METHODS = new DonateMethod[]
    {
        new DonateMethod(Type.TEXT, "Bitcoin", "bc1qdra5454kw9wncg8s6dtngswxs2musaqpnqdr4k"),
        new DonateMethod(Type.TEXT, "Ethereum", "0x5f0864a5687b845200cC1fCb987E1F671E0feecd"),
        new DonateMethod(Type.LINK, "Steam Trade", "https://steamcommunity.com/tradeoffer/new/?partner=1094904831&token=FGQy9z8F")
    };
}
