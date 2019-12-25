package util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import config.Config;

public class FileUtil {

	public static void writeFile(String FilePath, String str) {
		try {
			FileWriter writer = new FileWriter(FilePath);
			writer.write(str);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File[] getTestJavaFilesArray(String path) {
		int fileNum = 0, folderNum = 0;
		File file = new File(path);
		List<String> javaFilesPath = new ArrayList<>();
		LinkedList<File> list = new LinkedList<>();
		if (file.exists()) {
			if (null == file.listFiles()) {
				return null;
			}
			list.addAll(Arrays.asList(file.listFiles()));
			while (!list.isEmpty()) {
				File[] files = list.removeFirst().listFiles();
				if (null == files) {
					continue;
				}
				for (File f : files) {
					if (f.isDirectory()) {
						list.add(f);
						folderNum++;
					} else {
						String fileName = f.getAbsolutePath();
						if (!containTestFile(fileName)
								&& fileName.substring(fileName.lastIndexOf(".") + 1).equals("java")) {
							javaFilesPath.add(fileName);
							fileNum++;
						}

					}
				}
			}
		} else {
		}
		File[] javaFiles = new File[fileNum];
		for (int i = 0; i < javaFilesPath.size(); i++) {
			String filePath = javaFilesPath.get(i);
			javaFiles[i] = new File(filePath);
		}
		return javaFiles;
	}

	private static boolean containTestFile(String fileName) {
		if (fileName.toLowerCase().contains("test")) {
			return true;
		} else {
			return false;
		}
	}

	public static void zip(String path) {

		BufferedReader in = null;

		BufferedOutputStream out = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(Config.RESULTOUTFILEPATH+Config.GRAPHFILENAME), "UTF-8"));
			out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(
					Config.ZIPOUTPATH+Config.GRAPHFILENAME+".gz")));
			System.out.println("\nout put gzip file...\n");
			int c;
			while ((c = in.read()) != -1) {
				out.write(String.valueOf((char) c).getBytes("UTF-8"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
         System.out.print("gzip task end ");
		}
	}
	
	
	//deep clone
	public static Object deeplyCopy(Serializable obj) {
		try {
			return bytes2object(object2bytes(obj));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static byte[] object2bytes(Serializable obj) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
			baos.close();
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Object bytes2object(byte[] bytes) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
