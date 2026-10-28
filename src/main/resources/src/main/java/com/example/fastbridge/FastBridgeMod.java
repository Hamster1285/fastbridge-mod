package com.example.fastbridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod("fastbridge")
public class FastBridgeMod {

    private static KeyBinding bridgeKey;
    private boolean wasBridging = false;

    public FastBridgeMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setupClient(final FMLClientSetupEvent event) {
        // Кнопка V по умолчанию (можно перебиндить в настройках игры)
        bridgeKey = new KeyBinding("key.fastbridge.hold", GLFW.GLFW_KEY_V, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(bridgeKey);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        if (bridgeKey.isDown()) {
            wasBridging = true;

            // Проверяем, держит ли игрок блок в руке
            boolean holdingBlock = mc.player.getMainHandItem().getItem() instanceof BlockItem
                    || mc.player.getOffhandItem().getItem() instanceof BlockItem;

            if (holdingBlock) {
                // 1. Убираем стандартную задержку в 4 тика между установкой блоков (FastPlace)
                try {
                    ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, mc, 0, "field_71467_ac");
                } catch (Exception ignored) {}

                // 2. Автоматически зажимаем правую кнопку мыши
                mc.options.keyUse.setDown(true);
            }

            // 3. Проверяем, находится ли под ногами пустота (край блока), и зажимаем Shift
            BlockPos posBelow = new BlockPos(mc.player.getX(), mc.player.getY() - 0.5D, mc.player.getZ());
            boolean isOverAir = mc.level.isEmptyBlock(posBelow);
            mc.options.keyShift.setDown(isOverAir && mc.player.isOnGround());

        } else if (wasBridging) {
            // Когда отпустили кнопку — отжимаем Shift и ПКМ, чтобы персонаж не залипал
            wasBridging = false;
            mc.options.keyShift.setDown(false);
            mc.options.keyUse.setDown(false);
        }
    }
}
