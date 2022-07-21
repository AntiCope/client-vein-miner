package anticope.clientminer;

import net.minecraftforge.fml.common.Mod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

@Mod("clientminer")
public class ClientVeinMiner {
    static {
        KeyMappingRegistry.register(Constants.veinKey);
        KeyMappingRegistry.register(Constants.stopKey);
        KeyMappingRegistry.register(Constants.configKey);
    }

    public ClientVeinMiner() {
        AutoConfig.register(ClientMinerConfig.class, Toml4jConfigSerializer::new);
        Constants.config = AutoConfig.getConfigHolder(ClientMinerConfig.class).getConfig();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientVeinMiner::registerScreen);
    }

    public static void registerScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
            return AutoConfig.getConfigScreen(ClientMinerConfig.class, parent).get();
        }));
    }
}
