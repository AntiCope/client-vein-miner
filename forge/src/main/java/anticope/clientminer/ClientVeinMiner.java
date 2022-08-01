package anticope.clientminer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

@Mod("clientminer")
public class ClientVeinMiner {
    public ClientVeinMiner() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        AutoConfig.register(ClientMinerConfig.class, Toml4jConfigSerializer::new);
        Constants.config = AutoConfig.getConfigHolder(ClientMinerConfig.class).getConfig();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientVeinMiner.registerScreen();

            bus.addListener(this::registerKeyBinding);
        });
    }

    public static void registerScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
            return AutoConfig.getConfigScreen(ClientMinerConfig.class, parent).get();
        }));
    }

    private void registerKeyBinding(final RegisterKeyMappingsEvent event) {
        event.register(Constants.configKey);
        event.register(Constants.stopKey);
        event.register(Constants.veinKey);
    }
}
