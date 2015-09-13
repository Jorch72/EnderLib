package com.enderio.lib.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.util.StringTranslate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

@UtilityClass
public class EnderFileUtils {
  public final FileFilter pngFilter = FileFilterUtils.suffixFileFilter(".png");
  public final FileFilter langFilter = FileFilterUtils.suffixFileFilter(".lang");

  /**
   * @param jarClass
   *          - A class from the jar in question
   * @param filename
   *          - Name of the file to copy, automatically prepended with
   *          "/assets/"
   * @param to
   *          - File to copy to
   */
  public void copyFromJar(Class<?> jarClass, String filename, File to) {
    Log.info("Copying file " + filename + " from jar");
    URL url = jarClass.getResource("/assets/" + filename);

    try {
      FileUtils.copyURLToFile(url, to);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @author Ilias Tsagklis
   *         <p>
   *         From <a href=
   *         "http://examples.javacodegeeks.com/core-java/util/zip/extract-zip-file-with-subdirectories/"
   *         > this site.</a>
   * 
   * @param zip
   *          - The zip file to extract
   * 
   * @return The folder extracted to
   */
  @NonNull
  public File extractZip(File zip) {
    String zipPath = zip.getParent() + "/extracted";
    File temp = new File(zipPath);
    temp.mkdir();

    ZipFile zipFile = null;

    try {
      zipFile = new ZipFile(zip);

      // get an enumeration of the ZIP file entries
      Enumeration<? extends ZipEntry> e = zipFile.entries();

      while (e.hasMoreElements()) {
        ZipEntry entry = e.nextElement();

        File destinationPath = new File(zipPath, entry.getName());

        // create parent directories
        destinationPath.getParentFile().mkdirs();

        // if the entry is a file extract it
        if (entry.isDirectory()) {
          continue;
        } else {
          Log.info("Extracting file: " + destinationPath);

          BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

          int b;
          byte buffer[] = new byte[1024];

          FileOutputStream fos = new FileOutputStream(destinationPath);

          BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

          while ((b = bis.read(buffer, 0, 1024)) != -1) {
            bos.write(buffer, 0, b);
          }

          bos.close();
          bis.close();
        }
      }
    } catch (IOException e) {
      Log.error("Error opening zip file" + e);
    } finally {
      try {
        if (zipFile != null) {
          zipFile.close();
        }
      } catch (IOException e) {
        Log.error("Error while closing zip file" + e);
      }
    }

    return temp;
  }

  /**
   * @author McDowell - http://stackoverflow.com/questions/1399126/java-util-zip
   *         -recreating-directory-structure
   * 
   * @param directory
   *          The directory to zip the contents of. Content structure will be
   *          preserved.
   * @param zipfile
   *          The zip file to output to.
   * @throws IOException
   */
  @SuppressWarnings("resource")
  public void zipFolderContents(File directory, File zipfile) throws IOException {
    URI base = directory.toURI();
    Deque<File> queue = new LinkedList<File>();
    queue.push(directory);
    OutputStream out = new FileOutputStream(zipfile);
    Closeable res = out;
    try {
      ZipOutputStream zout = new ZipOutputStream(out);
      res = zout;
      while (!queue.isEmpty()) {
        directory = queue.pop();
        for (File child : directory.listFiles()) {
          String name = base.relativize(child.toURI()).getPath();
          if (child.isDirectory()) {
            queue.push(child);
            name = name.endsWith("/") ? name : name + "/";
            zout.putNextEntry(new ZipEntry(name));
          } else {
            zout.putNextEntry(new ZipEntry(name));
            copy(child, zout);
            zout.closeEntry();
          }
        }
      }
    } finally {
      res.close();
    }
  }

  /** @see #zipFolderContents(File, File) */
  private void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    while (true) {
      int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }
  }

  /** @see #zipFolderContents(File, File) */
  private void copy(File file, OutputStream out) throws IOException {
    InputStream in = new FileInputStream(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  @NonNull
  public File writeToFile(String filepath, String json) {
    File file = new File(filepath);

    try {
      file.createNewFile();
      FileWriter fw = new FileWriter(file);
      fw.write(json);
      fw.flush();
      fw.close();
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  public void safeDelete(File file) {
    try {
      file.delete();
    } catch (Exception e) {
      Log.error("Deleting file " + file.getAbsolutePath() + " failed.");
    }
  }

  @NonNull
  public void safeDeleteDirectory(File file) {
    try {
      FileUtils.deleteDirectory(file);
    } catch (Exception e) {
      Log.error("Deleting directory " + file.getAbsolutePath() + " failed.");
    }
  }

  @SneakyThrows
  @NonNull
  public void loadLangFiles(File directory) {
    for (File file : directory.listFiles(langFilter)) {
      StringTranslate.inject(new FileInputStream(file));
    }
  }
}
