package ru.defea.oneblockultima;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.defea.oneblockultima.command.*;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.gui.GuiHandler;
import ru.defea.oneblockultima.gui.GuiSetsConfig;
import ru.defea.oneblockultima.network.ModMessages;
import ru.defea.oneblockultima.tile.ModTileEntities;
import ru.defea.oneblockultima.update.UpdateChecker;
import ru.defea.oneblockultima.world.OneBlockWorldType;

@Mod(
        modid = OneBlockUltima.MODID,
        name = OneBlockUltima.NAME,
        version = "2.1.1"
)
public class OneBlockUltima
{
    public static final String MODID = "oneblockultima";
    public static final String NAME = "OneBlockUltima";

    public static final CreativeTabs modTab = new ModTab(NAME);

    @Mod.Instance(MODID)
    public static OneBlockUltima instance;

    @SidedProxy(clientSide = "ru.defea.oneblockultima.ClientProxy", serverSide = "ru.defea.oneblockultima.CommonProxy")
    public static CommonProxy proxy;

    private static Logger logger;

    public static Logger getLogger()
    {
        if (logger == null)
        {
            logger = LogManager.getLogger(MODID);
        }
        return logger;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        BlockSetConfig.load(event.getModConfigurationDirectory());
        ModTileEntities.register();
        ModMessages.register();
        OneBlockWorldType.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        proxy.preInit();

        if (cpw.mods.fml.common.FMLCommonHandler.instance().getSide() == cpw.mods.fml.relauncher.Side.CLIENT)
        {
            GuiSetsConfig.loadStaticCustomNames();
        }
        logger.info("{} is loading...", NAME);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent ignoredEvent)
    {
        proxy.init();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandAddUltimaBalance());
        event.registerServerCommand(new CommandInviteGeneratorMember());
        event.registerServerCommand(new CommandAcceptGeneratorInvite());
        event.registerServerCommand(new CommandDeclineGeneratorInvite());
        event.registerServerCommand(new CommandSetOwner());
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        UpdateChecker.shutdown();
    }
}
