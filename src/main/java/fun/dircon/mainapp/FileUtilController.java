package fun.dircon.mainapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


import biz.ui.controller.utils.ControllerUtils;

public class FileUtilController extends ControllerUtils {

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	protected List<File> getExtractFiles(File parent, List<File> files) {
		if (parent.isDirectory() && parent.canRead()) {
			File[] children = parent.listFiles();
			for (File file : children) {
				getExtractFiles(file, files);
			}

		} else {
			System.out.println("Adding File: " + parent.getAbsolutePath());
			files.add(parent);
		}
		return files;
	}

	protected List<File> consolidateFiles(List<String> exts, File parent, List<File> files) {
		if (parent.isDirectory() && parent.canRead()) {
			File[] children = parent.listFiles();
			for (File file : children) {
				consolidateFiles(exts, file, files);
			}

		} else {
			for (String ext : exts) {
				System.out.println("Adding File: " + parent.getAbsolutePath());
				if (parent.getAbsolutePath().endsWith(ext))
					files.add(parent);
			}

		}

		return files;
	}

	protected void moveFilesToDir(File directory, List<File> files) throws FileNotFoundException, IOException {
		for (File file : files) {
			System.out.println("Exporting File: " + file.getAbsolutePath());
			String path = directory.getAbsolutePath().replace(Character.toString(File.pathSeparatorChar), "/") + "/"
					+ file.getName();
			file.renameTo(new File(path));
		}

	}

	protected void copyFilesToDir(File directory, List<File> files) throws FileNotFoundException, IOException {
		for (File file : files) {
			System.out.println("Exporting File: " + file.getAbsolutePath());
			String path = directory.getAbsolutePath().replace(Character.toString(File.pathSeparatorChar), "/") + "/"
					+ file.getName();

			InputStream inStream = null;
			OutputStream outStream = null;

			try {
				inStream = new FileInputStream(file);
				outStream = new FileOutputStream(new File(path));

				byte[] buffer = new byte[1024];

				int length;
				// copy the file content in bytes
				while ((length = inStream.read(buffer)) > 0) {

					outStream.write(buffer, 0, length);

				}

				inStream.close();
				outStream.close();
				System.out.println("File is copied successful!");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
