/*
 * Hoot215's plugin auto-updater.
 * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.hoot215.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.plugin.java.JavaPlugin;

public class AutoUpdater implements Runnable
  {
    private final JavaPlugin plugin;
    private final String pluginName;
    private final URL versionUrl;
    private final String downloadUrl;
    private final String localVersion;
    private String remoteVersion;
    private final String site;
    private final AtomicBoolean upToDate = new AtomicBoolean(true);
    private final AutoUpdaterPlayerListener playerListener;
    private final boolean download = true;
    private final AtomicBoolean downloaded = new AtomicBoolean(false);
    
    public AutoUpdater(JavaPlugin instance) throws MalformedURLException
      {
        plugin = instance;
        pluginName = plugin.getName();
        versionUrl =
            new URL("http://dl.dropbox.com/u/56151340/BukkitPlugins/"
                + pluginName + "/latest");
        downloadUrl =
            "http://dl.dropbox.com/u/56151340/BukkitPlugins/" + pluginName
                + "/v{version}/" + pluginName + ".jar";
        localVersion = plugin.getDescription().getVersion();
        String site = plugin.getDescription().getWebsite();
        this.site = site == null ? "" : site;
        playerListener = new AutoUpdaterPlayerListener(plugin, this);
      }
    
    public String getPluginName ()
      {
        return pluginName;
      }
    
    public String getNewestVersion ()
      {
        return remoteVersion;
      }
    
    public String getSite ()
      {
        return site;
      }
    
    public boolean isUpToDate ()
      {
        return upToDate.get();
      }
    
    public boolean canDownload ()
      {
        return download;
      }
    
    public boolean hasDownloaded ()
      {
        return downloaded.get();
      }
    
    public void start ()
      {
        plugin.getServer().getPluginManager()
            .registerEvents(playerListener, plugin);
        new Thread(this).start();
      }
    
    public void run ()
      {
        while (true)
          {
            if ( !plugin.isEnabled())
              break;
            try
              {
                if (this.updateCheck() && download)
                  {
                    this.downloadLatestVersion();
                  }
                Thread.sleep(3600000L);
              }
            catch (InterruptedException e)
              {
                e.printStackTrace();
              }
          }
      }
    
    public synchronized boolean updateCheck ()
      {
        Scanner s = null;
        try
          {
            s = new Scanner(versionUrl.openStream());
            remoteVersion = s.nextLine();
            if (this.compareVersions())
              {
                upToDate.set(false);
                s.close();
                return true;
              }
            upToDate.set(true);
          }
        catch (MalformedURLException e)
          {
            e.printStackTrace();
          }
        catch (IOException e)
          {
          }
        catch (NoSuchElementException e)
          {
          }
        finally
          {
            if (s != null)
              {
                s.close();
              }
          }
        return false;
      }
    
    public synchronized void downloadLatestVersion ()
      {
        FileOutputStream fos = null;
        try
          {
            if (remoteVersion == null)
              {
                this.updateCheck();
              }
            plugin.getLogger().info("Downloading latest version...");
            ReadableByteChannel rbc =
                Channels.newChannel(new URL(downloadUrl.replace("{version}",
                    remoteVersion)).openStream());
            fos =
                new FileOutputStream("plugins" + File.separator + pluginName
                    + ".jar");
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
          }
        catch (MalformedURLException e)
          {
            e.printStackTrace();
          }
        catch (IOException e)
          {
            e.printStackTrace();
          }
        finally
          {
            if (fos != null)
              {
                try
                  {
                    fos.close();
                    plugin.getLogger().info(
                        "Latest version has finished downloading");
                    plugin.getLogger().info(
                        "Changes will take effect on restart");
                  }
                catch (IOException e)
                  {
                    e.printStackTrace();
                  }
              }
            downloaded.set(true);
          }
      }
    
    private boolean compareVersions ()
      {
        String[] local = localVersion.split("\\.");
        String[] remote = remoteVersion.split("\\.");
        int longestNumber =
            local.length > remote.length ? local.length : remote.length;
        for (int i = 0; i < longestNumber; i++)
          {
            int l = 0;
            int r = 0;
            try
              {
                try
                  {
                    l = Integer.valueOf(local[i]);
                  }
                catch (ArrayIndexOutOfBoundsException e)
                  {
                  }
                try
                  {
                    r = Integer.valueOf(remote[i]);
                  }
                catch (ArrayIndexOutOfBoundsException e)
                  {
                  }
              }
            catch (NumberFormatException e)
              {
              }
            if (r > l)
              return true;
          }
        return false;
      }
  }
