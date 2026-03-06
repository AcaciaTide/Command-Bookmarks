package acaciatide.commandbookmark;

import acaciatide.commandbookmark.data.BookmarkManager;
import acaciatide.commandbookmark.gui.BookmarkListScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandBookmarkClient implements ClientModInitializer {
    public static final String MOD_ID = "commandbookmark";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final BookmarkManager MANAGER = new BookmarkManager();
    public static KeyBinding openGuiKeyBinding;

    @Override
    public void onInitializeClient() {
        MANAGER.load();

        openGuiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.commandbookmark.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyBinding.Category.MISC
        ));

        // キー入力監視
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new BookmarkListScreen(null)); // 引数については後で対応
                }
            }
        });

        LOGGER.info("Command Bookmark client initialized!");
    }
}