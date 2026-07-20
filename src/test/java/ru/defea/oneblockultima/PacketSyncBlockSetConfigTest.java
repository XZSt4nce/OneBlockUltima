package ru.defea.oneblockultima;

import io.netty.buffer.Unpooled;
import org.junit.Test;
import ru.defea.oneblockultima.network.PacketSyncBlockSetConfig;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class PacketSyncBlockSetConfigTest {

    private static String getJson(PacketSyncBlockSetConfig packet) throws Exception {
        Field f = PacketSyncBlockSetConfig.class.getDeclaredField("json");
        f.setAccessible(true);
        return (String) f.get(packet);
    }

    @Test
    public void toBytesFromBytesRoundTripPreservesData() throws Exception {
        String json = "{\"sets\":[{\"id\":\"test\",\"blocks\":[],\"mobs\":[]}],\"settings\":{}}";
        PacketSyncBlockSetConfig original = new PacketSyncBlockSetConfig(json);

        io.netty.buffer.ByteBuf buf = Unpooled.buffer();
        original.toBytes(buf);

        PacketSyncBlockSetConfig restored = new PacketSyncBlockSetConfig();
        restored.fromBytes(buf);

        assertEquals(json, getJson(restored));
        buf.release();
    }

    @Test
    public void roundTripPreservesEmptyString() throws Exception {
        String json = "";
        PacketSyncBlockSetConfig original = new PacketSyncBlockSetConfig(json);

        io.netty.buffer.ByteBuf buf = Unpooled.buffer();
        original.toBytes(buf);

        PacketSyncBlockSetConfig restored = new PacketSyncBlockSetConfig();
        restored.fromBytes(buf);

        assertEquals("", getJson(restored));
        buf.release();
    }

    @Test
    public void roundTripPreservesUnicode() throws Exception {
        String json = "{\"name\":\"Тест набор\",\"sets\":[]}";
        PacketSyncBlockSetConfig original = new PacketSyncBlockSetConfig(json);

        io.netty.buffer.ByteBuf buf = Unpooled.buffer();
        original.toBytes(buf);

        PacketSyncBlockSetConfig restored = new PacketSyncBlockSetConfig();
        restored.fromBytes(buf);

        assertEquals(json, getJson(restored));
        buf.release();
    }

    @Test
    public void roundTripPreservesLargePayload() throws Exception {
        StringBuilder sb = new StringBuilder("{\"sets\":[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"id\":\"set_").append(i).append("\",\"blocks\":[],\"mobs\":[]}");
        }
        sb.append("],\"settings\":{}}");
        String json = sb.toString();

        PacketSyncBlockSetConfig original = new PacketSyncBlockSetConfig(json);

        io.netty.buffer.ByteBuf buf = Unpooled.buffer();
        original.toBytes(buf);
        int readableAfterWrite = buf.readableBytes();

        PacketSyncBlockSetConfig restored = new PacketSyncBlockSetConfig();
        restored.fromBytes(buf);

        assertEquals(json, getJson(restored));
        assertTrue("encoded size should be larger than raw bytes", readableAfterWrite > json.getBytes(StandardCharsets.UTF_8).length);
        buf.release();
    }

    @Test
    public void roundTripPreservesSpecialCharacters() throws Exception {
        String json = "{\"key\":\"value\\nwith\\nnewlines\",\"path\":\"C:\\\\Users\\\\test\"}";
        PacketSyncBlockSetConfig original = new PacketSyncBlockSetConfig(json);

        io.netty.buffer.ByteBuf buf = Unpooled.buffer();
        original.toBytes(buf);

        PacketSyncBlockSetConfig restored = new PacketSyncBlockSetConfig();
        restored.fromBytes(buf);

        assertEquals(json, getJson(restored));
        buf.release();
    }

    @Test
    public void actualConfigJsonRoundTrips() throws Exception {
        java.util.List<ru.defea.oneblockultima.config.BlockSetConfig.BlockSetDefinition> saved =
            new java.util.ArrayList<ru.defea.oneblockultima.config.BlockSetConfig.BlockSetDefinition>(
                ru.defea.oneblockultima.config.BlockSetConfig.get().getSets());
        try {
            ru.defea.oneblockultima.config.BlockSetConfig.BlockSetDefinition mini =
                new ru.defea.oneblockultima.config.BlockSetConfig.BlockSetDefinition();
            mini.id = "roundtrip_test";
            java.util.List<ru.defea.oneblockultima.config.BlockSetConfig.BlockSetDefinition> sets =
                new java.util.ArrayList<ru.defea.oneblockultima.config.BlockSetConfig.BlockSetDefinition>();
            sets.add(mini);
            ru.defea.oneblockultima.config.BlockSetConfig.applySets(sets);

            String json = ru.defea.oneblockultima.config.BlockSetConfig.get().toJson();
            assertNotNull(json);
            assertFalse(json.isEmpty());

            PacketSyncBlockSetConfig original = new PacketSyncBlockSetConfig(json);

            io.netty.buffer.ByteBuf buf = Unpooled.buffer();
            original.toBytes(buf);

            PacketSyncBlockSetConfig restored = new PacketSyncBlockSetConfig();
            restored.fromBytes(buf);

            assertEquals(json, getJson(restored));
            buf.release();
        } finally {
            ru.defea.oneblockultima.config.BlockSetConfig.applySets(saved);
        }
    }
}
