package anticope.clientminer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class ClientVeinMiner implements ClientModInitializer {
    public static KeyBinding keyBinding;
    public static final Miner miner = new Miner();

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientminer.vein",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "category.clientminer.cat"
        ));
    }
}
