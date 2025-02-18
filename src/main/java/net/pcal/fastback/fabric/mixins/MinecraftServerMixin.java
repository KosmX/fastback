/*
 * FastBack - Fast, incremental Minecraft backups powered by Git.
 * Copyright (C) 2022 pcal.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */
package net.pcal.fastback.fabric.mixins;

import net.minecraft.server.MinecraftServer;
import net.pcal.fastback.fabric.FabricProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author pcal
 * @since 0.0.1
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    /**
     * Intercept the call to saveAll that triggers on autobacks, pass it through and then send out notification that
     * the autoback is done.
     */
    @Redirect(method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;saveAll(ZZZ)Z"))
    public boolean autoback_saveAll(MinecraftServer instance, boolean suppressLogs, boolean flush, boolean force) {
        boolean result = instance.saveAll(suppressLogs, flush, force);
        FabricProvider.getInstance().autoSaveCompleted();
        return result;
    }

    /**
     * Intercept save so we can hard-disable saving during critical parts of the backup.
     */
    @Inject(at = @At("HEAD"), method = "save(ZZZ)Z", cancellable = true)
    public void save(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> ci) {
        synchronized (this) {
            final FabricProvider ctx = FabricProvider.getInstance();
            if (ctx.isWorldSaveEnabled()) {
                ctx.getLogger().debug("world saves are enabled, doing requested save");
            } else {
                ctx.getLogger().warn("Skipping requested save because a backup is in progress.");
                ci.setReturnValue(false);
                ci.cancel();
            }
        }
    }

    /**
     * Intercept saveAll so we can hard-disable saving during critical parts of the backup.
     */
    @Inject(at = @At("HEAD"), method = "saveAll(ZZZ)Z", cancellable = true)
    public void saveAll(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> ci) {
        synchronized (this) {
            final FabricProvider ctx = FabricProvider.getInstance();
            if (ctx.isWorldSaveEnabled()) {
                ctx.getLogger().debug("world saves are enabled, doing requested saveAll");
                //TODO should call save here to ensure all synced?
            } else {
                ctx.getLogger().warn("Skipping requested saveAll because a backup is in progress.");
                ci.setReturnValue(false);
                ci.cancel();
            }
        }
    }
}
