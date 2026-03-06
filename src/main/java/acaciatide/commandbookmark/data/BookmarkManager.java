package acaciatide.commandbookmark.data;

import acaciatide.commandbookmark.CommandBookmarkClient;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookmarkManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("command_bookmark.json");
    private static final int CURRENT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private List<Bookmark> bookmarks = new ArrayList<>();

    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            bookmarks = new ArrayList<>();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("bookmarks")) {
                bookmarks = new ArrayList<>();
                return;
            }

            JsonArray arr = root.getAsJsonArray("bookmarks");
            bookmarks = new ArrayList<>();
            for (JsonElement el : arr) {
                bookmarks.add(GSON.fromJson(el, Bookmark.class));
            }
        } catch (IOException | JsonParseException e) {
            CommandBookmarkClient.LOGGER.error("Failed to load bookmarks", e);
            bookmarks = new ArrayList<>();
            // パースエラー時などでファイルが壊れている場合、.bakとしてバックアップを作成
            if (Files.exists(CONFIG_PATH)) {
                try {
                    Path backupPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".bak");
                    Files.copy(CONFIG_PATH, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    CommandBookmarkClient.LOGGER.warn("Corrupted config file backed up to " + backupPath);
                } catch (IOException ex) {
                    CommandBookmarkClient.LOGGER.error("Failed to create backup file", ex);
                }
            }
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("version", CURRENT_VERSION);
        root.add("bookmarks", GSON.toJsonTree(bookmarks));

        try {
            if (!Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            CommandBookmarkClient.LOGGER.error("Failed to save bookmarks", e);
        }
    }

    public void add(Bookmark bookmark) {
        bookmarks.add(bookmark);
        save();
    }

    public void remove(String id) {
        bookmarks.removeIf(b -> b.getId().equals(id));
        save();
    }

    public void update(Bookmark updatedBookmark) {
        for (int i = 0; i < bookmarks.size(); i++) {
            if (bookmarks.get(i).getId().equals(updatedBookmark.getId())) {
                bookmarks.set(i, updatedBookmark);
                save();
                return;
            }
        }
    }

    public List<Bookmark> getBookmarks() {
        return Collections.unmodifiableList(bookmarks);
    }
}
