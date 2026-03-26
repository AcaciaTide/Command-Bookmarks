package acaciatide.commandbookmark;

import acaciatide.commandbookmark.data.BookmarkManager;
import acaciatide.commandbookmark.gui.BookmarkListScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandBookmarkClient implements ClientModInitializer {
    public static final String MOD_ID = "commandbookmark";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BookmarkManager MANAGER = new BookmarkManager();
    public static KeyMapping openGuiKeyBinding;

    @Override
    public void onInitializeClient() {
        MANAGER.load();

        openGuiKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.commandbookmark.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyMapping.Category.MISC
        ));

        // キー入力監視
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKeyBinding.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new BookmarkListScreen(null)); // 引数については後で対応
                }
            }
        });

        LOGGER.info("Command Bookmarks client initialized!");
    }
}