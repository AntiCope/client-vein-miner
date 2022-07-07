package anticope.clientminer;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ClientVeinMiner implements ClientModInitializer {
    public static KeyBinding keyBinding;
    public static final Miner miner = new Miner();
    public static ClientMinerConfig config;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ClientMinerConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ClientMinerConfig.class).getConfig();

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientminer.vein",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "category.clientminer.cat"
        ));
    }
}
