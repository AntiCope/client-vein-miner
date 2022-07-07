package anticope.clientminer.mixin;

import anticope.clientminer.ClientVeinMiner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
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
        if (ClientVeinMiner.keyBinding.isPressed() && !ClientVeinMiner.miner.working) {
            cir.cancel();
            ClientVeinMiner.miner.onStartMining(blockPos, direction, networkHandler.getWorld());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (client.options.attackKey.isPressed()) {
            ClientVeinMiner.miner.onTick();
        } else {
            ClientVeinMiner.miner.onStopMining();
        }
    }
}
