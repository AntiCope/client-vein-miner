package anticope.clientminer;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.option.KeyBinding;

public class Constants {
    public static KeyBinding veinKey = new KeyBinding(
        "key.clientminer.vein",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_GRAVE_ACCENT,
        "category.clientminer.cat"
    );
    public static KeyBinding stopKey = new KeyBinding(
        "key.clientminer.stop",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_APOSTROPHE,
        "category.clientminer.cat"
    );
    public static KeyBinding configKey = new KeyBinding(
        "key.clientminer.config",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        "category.clientminer.cat"
    );

    public static final Miner miner = new Miner();
    public static ClientMinerConfig config;
}
