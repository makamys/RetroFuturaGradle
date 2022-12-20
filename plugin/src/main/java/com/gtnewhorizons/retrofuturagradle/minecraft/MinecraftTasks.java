package com.gtnewhorizons.retrofuturagradle.minecraft;

import com.gtnewhorizons.retrofuturagradle.Constants;
import com.gtnewhorizons.retrofuturagradle.MinecraftExtension;
import de.undercouch.gradle.tasks.download.Download;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

/**
 * Registers vanilla Minecraft-related gradle tasks
 */
public final class MinecraftTasks {
    private final MinecraftExtension mcExt;

    private final File allVersionsManifestLocation;
    private final TaskProvider<Download> taskDownloadLauncherAllVersionsManifest;

    private final File versionManifestLocation;
    private final TaskProvider<Download> taskDownloadLauncherVersionManifest;

    public MinecraftTasks(Project project, MinecraftExtension mcExt) {
        this.mcExt = mcExt;
        allVersionsManifestLocation = new File(project.getBuildDir(), "all_versions_manifest.json");
        taskDownloadLauncherAllVersionsManifest = project.getTasks()
                .register("downloadLauncherAllVersionsManifest", Download.class, task -> {
                    task.src(Constants.URL_LAUNCHER_VERSION_MANIFEST);
                    task.onlyIfModified(true);
                    task.useETag(true);
                    task.dest(allVersionsManifestLocation);
                });

        versionManifestLocation = new File(project.getBuildDir(), "mc_version_manifest.json");
        taskDownloadLauncherVersionManifest = project.getTasks()
                .register("downloadLauncherVersionManifest", Download.class, task -> {
                    task.dependsOn(taskDownloadLauncherAllVersionsManifest);
                    task.src(project.getProviders().provider(() -> {
                        final String mcVersion = mcExt.getMcVersion().get();
                        final String allVersionsManifestJson;
                        try {
                            allVersionsManifestJson =
                                    FileUtils.readFileToString(allVersionsManifestLocation, StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return LauncherManifest.getVersionManifestUrl(allVersionsManifestJson, mcVersion);
                    }));
                    task.onlyIfModified(true);
                    task.useETag(true);
                    task.dest(versionManifestLocation);
                });
    }

    public File getAllVersionsManifestLocation() {
        return allVersionsManifestLocation;
    }

    public TaskProvider<Download> getTaskDownloadLauncherAllVersionsManifest() {
        return taskDownloadLauncherAllVersionsManifest;
    }

    public File getVersionManifestLocation() {
        return versionManifestLocation;
    }

    public TaskProvider<Download> getTaskDownloadLauncherVersionManifest() {
        return taskDownloadLauncherVersionManifest;
    }
}