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

package net.pcal.fastback.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.pcal.fastback.ModContext;
import net.pcal.fastback.WorldConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.pcal.fastback.commands.Commands.FAILURE;
import static net.pcal.fastback.commands.Commands.SUCCESS;
import static net.pcal.fastback.commands.Commands.executeStandard;
import static net.pcal.fastback.utils.FileUtils.mkdirs;

public class CreateFileRemoteCommand {

    public static void register(final LiteralArgumentBuilder<ServerCommandSource> argb, final ModContext ctx) {
        final CreateFileRemoteCommand c = new CreateFileRemoteCommand(ctx);
        argb.then(
                literal("create-remote").then(
                        argument("file-path", StringArgumentType.greedyString()).
                                executes(c::setFileRemote))
        );
    }

    private final ModContext ctx;

    private CreateFileRemoteCommand(final ModContext context) {
        this.ctx = requireNonNull(context);
    }

    private int setFileRemote(final CommandContext<ServerCommandSource> cc) {
        return executeStandard(this.ctx, cc, (gitc, wc, log) -> {
            final String targetPath = cc.getArgument("file-path", String.class);
            final Path fupHome = Path.of(targetPath);
            if (fupHome.toFile().exists()) {
                log.notifyError("Directory already exists:");
                log.notifyError(fupHome.toString());
                return FAILURE;
            }
            mkdirs(fupHome);
            try (Git targetGit = Git.init().setBare(wc.isFileRemoteBare()).setDirectory(fupHome.toFile()).call()) {
                final StoredConfig targetGitc = targetGit.getRepository().getConfig();
                targetGitc.setInt("core", null, "compression", 0);
                targetGitc.setInt("pack", null, "window", 0);
                targetGitc.save();
            }
            final String targetUrl = "file://" + fupHome.toAbsolutePath();
            WorldConfig.setRemoteUrl(gitc, targetUrl);
            WorldConfig.setRemoteBackupEnabled(gitc, true);
            gitc.save();
            log.notify("Git repository created at " + targetPath);
            log.notify("Remote backups enabled to:");
            log.notify(targetUrl);
            return SUCCESS;
        });
    }
}
