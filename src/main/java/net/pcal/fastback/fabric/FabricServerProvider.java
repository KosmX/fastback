package net.pcal.fastback.fabric;

import net.pcal.fastback.logging.Message;

import java.nio.file.Path;

/**
 * @author pcal
 * @since 0.1.0
 */
public class FabricServerProvider extends FabricProvider {

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public Path getSnapshotRestoreDir() {
        return null;
    }
}
