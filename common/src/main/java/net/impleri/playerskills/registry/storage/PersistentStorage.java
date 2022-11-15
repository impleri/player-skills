package net.impleri.playerskills.registry.storage;

import java.io.File;
import java.util.List;

interface PersistentStorage {
    List<String> read(File file);

    void write(File file, List<String> skills);
}
