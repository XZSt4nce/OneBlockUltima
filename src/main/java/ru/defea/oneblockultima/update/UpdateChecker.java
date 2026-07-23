package ru.defea.oneblockultima.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import ru.defea.oneblockultima.OneBlockUltima;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class UpdateChecker
{
    private static final String VERSIONS_URL = "https://raw.githubusercontent.com/XZSt4nce/OneBlockUltima/main/versions.json";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory()
    {
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, "OneBlockUltima-UpdateChecker");
            t.setDaemon(true);
            return t;
        }
    });

    private static String cachedRecommendedVersion;
    private static String cachedDownloadUrl;
    private static boolean checkDone = false;
    private static boolean updateAvailable = false;

    public static void checkForUpdates(final EntityPlayerMP player)
    {
        if (checkDone)
        {
            if (updateAvailable && player != null)
            {
                notifyPlayer(player);
            }
            return;
        }

        executor.submit(new Runnable()
        {
            public void run()
            {
                try
                {
                    String mcVersion = Loader.instance().getMCVersionString().substring(10);
                    String promoKey = mcVersion + "-recommended";

                    ModContainer mod = Loader.instance().getIndexedModList().get(OneBlockUltima.MODID);
                    if (mod == null) return;
                    String currentVersion = mod.getVersion();

                    URL url = new URL(VERSIONS_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "OneBlockUltima/" + currentVersion);

                    if (conn.getResponseCode() != 200)
                    {
                        OneBlockUltima.getLogger().warn("[UpdateChecker] Failed to fetch versions.json: HTTP {}", conn.getResponseCode());
                        checkDone = true;
                        return;
                    }

                    java.io.InputStream is = conn.getInputStream();
                    String encoding = conn.getContentEncoding();
                    if ("gzip".equalsIgnoreCase(encoding))
                    {
                        is = new GZIPInputStream(is);
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    JsonObject root = new JsonParser().parse(sb.toString()).getAsJsonObject();
                    JsonObject promos = root.getAsJsonObject("promos");
                    if (promos == null || !promos.has(promoKey))
                    {
                        OneBlockUltima.getLogger().warn("[UpdateChecker] No promo key '{}' found", promoKey);
                        checkDone = true;
                        return;
                    }

                    String recommendedVersion = promos.get(promoKey).getAsString();
                    String homepage = root.has("homepage") ? root.get("homepage").getAsString() : null;

                    cachedRecommendedVersion = recommendedVersion;
                    cachedDownloadUrl = homepage;
                    checkDone = true;

                    if (!currentVersion.equals(recommendedVersion))
                    {
                        OneBlockUltima.getLogger().info("[UpdateChecker] New version available: {} (current: {})", new Object[]{recommendedVersion, currentVersion});
                        updateAvailable = true;
                        if (player != null)
                        {
                            notifyPlayer(player);
                        }
                    }
                    else
                    {
                        OneBlockUltima.getLogger().info("[UpdateChecker] Mod is up to date: {}", currentVersion);
                    }
                }
                catch (Exception e)
                {
                    OneBlockUltima.getLogger().warn("[UpdateChecker] Failed to check for updates: {}", e.getMessage());
                    checkDone = true;
                }
            }
        });
    }

    private static void notifyPlayer(EntityPlayerMP player)
    {
        String currentVersion;
        ModContainer mod = Loader.instance().getIndexedModList().get(OneBlockUltima.MODID);
        if (mod != null)
        {
            currentVersion = mod.getVersion();
        }
        else
        {
            currentVersion = "???";
        }

        player.addChatMessage(new ChatComponentTranslation(
                "oneblockultima.update.available",
                cachedRecommendedVersion,
                currentVersion));

        if (cachedDownloadUrl != null)
        {
            player.addChatMessage(new ChatComponentTranslation(
                    "oneblockultima.update.link",
                    cachedDownloadUrl));
        }
    }

    public static void shutdown()
    {
        executor.shutdown();
        try
        {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException ignored)
        {
        }
    }
}
