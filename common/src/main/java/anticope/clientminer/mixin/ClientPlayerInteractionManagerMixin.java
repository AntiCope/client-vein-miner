package anticope.clientminer.mixin;

import anticope.clientminer.ClientMinerConfig;
import anticope.clientminer.Constants;
import me.shedaniel.autoconfig.AutoConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin  {
    @Shadow
    private ClientPlayNetworkHandler networkHandler;

    @Shadow
    private MinecraftClient client;

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (Constants.veinKey.isPressed() && !Constants.miner.working) {
            cir.cancel();
            Constants.miner.onStartMining(blockPos, direction, networkHandler.getWorld());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (Constants.miner.working) {
            if (client.options.attackKey.isPressed()) {
                Constants.miner.onTick();
            } else {
                if (Constants.config.automine && Constants.miner.working) client.options.attackKey.setPressed(true);
                else Constants.miner.onStopMining();
            }
            if (Constants.stopKey.isPressed()) {
                Constants.miner.onStopMining();
            }
        }
        if (Constants.configKey.isPressed()) {
            try {
                client.setScreen(AutoConfig.getConfigScreen(ClientMinerConfig.class, client.currentScreen).get());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
