package acaciatide.commandbookmark.data;

import acaciatide.commandbookmark.CommandBookmarkClient;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookmarkManager {
    private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("command_bookmark.json");
    private static final Path BASE_DIR = FabricLoader.getInstance().getConfigDir().resolve("commandbookmark");
    private static final Path GLOBAL_CONFIG_PATH = BASE_DIR.resolve("global.json");
    private static final Path STATE_CONFIG_PATH = BASE_DIR.resolve("state.json");
    
    private static final int CURRENT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private List<Bookmark> globalBookmarks = new ArrayList<>();
    private List<Bookmark> localBookmarks = new ArrayList<>();
    
    private boolean isGlobalMode = true;
    private String currentLocalId = null;

    public void load() {
        // Migration from legacy config
        if (Files.exists(LEGACY_CONFIG_PATH)) {
            try {
                if (!Files.exists(BASE_DIR)) Files.createDirectories(BASE_DIR);
                Files.move(LEGACY_CONFIG_PATH, GLOBAL_CONFIG_PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                CommandBookmarkClient.LOGGER.info("Migrated legacy config to global.json");
            } catch (IOException e) {
                CommandBookmarkClient.LOGGER.error("Failed to migrate legacy config", e);
            }
        }

        loadState();
        globalBookmarks = loadBookmarksFromFile(GLOBAL_CONFIG_PATH);
    }

    private void loadState() {
        if (Files.exists(STATE_CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(STATE_CONFIG_PATH)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (root != null && root.has("isGlobalMode")) {
                    this.isGlobalMode = root.get("isGlobalMode").getAsBoolean();
                }
            } catch (Exception e) {
                CommandBookmarkClient.LOGGER.error("Failed to load command bookmark state", e);
            }
        }
    }

    public void saveState() {
        JsonObject root = new JsonObject();
        root.addProperty("isGlobalMode", this.isGlobalMode);
        try {
            if (!Files.exists(BASE_DIR)) Files.createDirectories(BASE_DIR);
            try (Writer writer = Files.newBufferedWriter(STATE_CONFIG_PATH)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            CommandBookmarkClient.LOGGER.error("Failed to save command bookmark state", e);
        }
    }

    private List<Bookmark> loadBookmarksFromFile(Path path) {
        List<Bookmark> list = new ArrayList<>();
        if (!Files.exists(path)) return list;

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("bookmarks")) return list;

            JsonArray arr = root.getAsJsonArray("bookmarks");
            for (JsonElement el : arr) {
                list.add(GSON.fromJson(el, Bookmark.class));
            }
        } catch (Exception e) {
            CommandBookmarkClient.LOGGER.error("Failed to load bookmarks from " + path, e);
            if (Files.exists(path)) {
                try {
                    Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
                    Files.copy(path, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    CommandBookmarkClient.LOGGER.warn("Corrupted config file backed up to " + backupPath);
                } catch (IOException ex) {
                    CommandBookmarkClient.LOGGER.error("Failed to create backup file", ex);
                }
            }
        }
        return list;
    }

    private void saveBookmarksToFile(List<Bookmark> list, Path path) {
        JsonObject root = new JsonObject();
        root.addProperty("version", CURRENT_VERSION);
        root.add("bookmarks", GSON.toJsonTree(list));

        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            CommandBookmarkClient.LOGGER.error("Failed to save bookmarks to " + path, e);
        }
    }

    public void updateLocalContext() {
        String worldId = fetchCurrentWorldId();
        if (worldId == null) {
            this.localBookmarks = new ArrayList<>();
            this.currentLocalId = null;
            if (!this.isGlobalMode) {
                this.isGlobalMode = true; // 強制的にグローバルモードへ戻す
            }
        } else if (!worldId.equals(this.currentLocalId)) {
            this.currentLocalId = worldId;
            Path localPath = BASE_DIR.resolve("local").resolve(worldId + ".json");
            this.localBookmarks = loadBookmarksFromFile(localPath);
        }
    }

    private String fetchCurrentWorldId() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer() && client.getServer() != null) {
            return "local_" + client.getServer().getSaveProperties().getLevelName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        } else if (client.getCurrentServerEntry() != null) {
            return "server_" + client.getCurrentServerEntry().address.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        }
        return null;
    }

    public void save() {
        if (isGlobalMode) {
            saveBookmarksToFile(globalBookmarks, GLOBAL_CONFIG_PATH);
        } else {
            if (currentLocalId != null) {
                Path localPath = BASE_DIR.resolve("local").resolve(currentLocalId + ".json");
                saveBookmarksToFile(localBookmarks, localPath);
            }
        }
    }

    private List<Bookmark> getActiveBookmarks() {
        return isGlobalMode ? globalBookmarks : localBookmarks;
    }

    public void add(Bookmark bookmark) {
        getActiveBookmarks().add(bookmark);
        save();
    }

    public void remove(String id) {
        getActiveBookmarks().removeIf(b -> b.getId().equals(id));
        save();
    }

    public void update(Bookmark updatedBookmark) {
        List<Bookmark> list = getActiveBookmarks();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedBookmark.getId())) {
                list.set(i, updatedBookmark);
                save();
                return;
            }
        }
    }

    public void moveUp(Bookmark bookmark) {
        List<Bookmark> list = getActiveBookmarks();
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(bookmark.getId())) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            Collections.swap(list, index, index - 1);
            save();
        }
    }

    public void moveDown(Bookmark bookmark) {
        List<Bookmark> list = getActiveBookmarks();
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(bookmark.getId())) {
                index = i;
                break;
            }
        }
        if (index >= 0 && index < list.size() - 1) {
            Collections.swap(list, index, index + 1);
            save();
        }
    }

    public List<Bookmark> getBookmarks() {
        return Collections.unmodifiableList(getActiveBookmarks());
    }

    public boolean isGlobalMode() {
        return isGlobalMode;
    }

    public void setGlobalMode(boolean globalMode) {
        this.isGlobalMode = globalMode;
        saveState();
    }

    public String getCurrentLocalId() {
        return currentLocalId;
    }
}
