package com.hysteryale.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class WatchFolderUtil {


    public void trackingFolder(String folderPath) throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();

        Path path = Paths.get(folderPath);
        registerAll(path, watchService);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println(
                        "Event kind:" + event.kind()
                                + ". File affected: " + event.context());
            }
            key.reset();
        }
    }


    private void registerAll(final Path start, WatchService watcher) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                dir.register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                return FileVisitResult.CONTINUE;
            }
        });

    }
}

