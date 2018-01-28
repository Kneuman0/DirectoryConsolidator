package fun.dircon.mainapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FileJob {
	
	private List<File> files;
	private AtomicInteger percentComplete;
	private AtomicLong bytesInFileJob;
	private long bytesMoved;
	private double bytesMovedPerSecond;
	private File parentDirectory;
	private File exportLocation;
	private String[] exts;
	private FileJobListener listener;
	
	public FileJob(File directory, File exportLocation, String... exts) {
		this.files = new ArrayList<>();
		this.parentDirectory = directory;
		this.exportLocation = exportLocation;
		this.exts = exts != null ? new String[0] : exts;
		this.bytesMoved = 0;
		
		if(exts.length == 0) {
			files = getExtractFiles();
		}else {
			files = consolidateFiles();
		}
		this.percentComplete = new AtomicInteger(0);
		
		long bytes = 0;
		for(File file : files) {
			if(file.exists()) bytes += file.length();
		}
		
		this.bytesInFileJob = new AtomicLong(bytes);
		this.bytesMovedPerSecond = 0;
		
	}
	
	public FileJob(File directory, File exportLocation,
			FileJobListener listener, String... exts) {
		this(directory,exportLocation, exts);
		this.listener = listener;
	}
	
	protected List<File> getExtractFiles() {
		return getExtractFiles(parentDirectory, files);
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

	protected List<File> consolidateFiles(){
		return consolidateFiles(Arrays.asList(exts), parentDirectory, files);
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
	
	public void moveFilesToDir() throws FileNotFoundException, IOException {
		moveFilesToDir(files);
	}

	protected void moveFilesToDir(List<File> files) throws FileNotFoundException, IOException {
		for (File file : files) {
			long start = System.nanoTime();
			listener.notifyCurrentFile("Exporting File: " + file.getAbsolutePath());
			String path = exportLocation.getAbsolutePath().replace(Character.toString(File.pathSeparatorChar), "/") + "/"
					+ file.getName();
			System.out.println("Exporting File: " + file.getAbsolutePath() + "To: " + path);
//			Files.move(file.toPath(), new File(path).toPath());
			file.renameTo(new File(path));
			bytesMoved += file.length();
			int percentComplete = (int)(((double)bytesMoved/(double)this.bytesInFileJob.get()) * 100);
			this.percentComplete.set(percentComplete);
			long end = System.nanoTime();
			double elapsedSeconds = (double)(end - start)/1000000000.0;
			this.bytesMovedPerSecond = file.length()/elapsedSeconds;
			System.out.println("File moved successfully!");
			System.out.println(String.format("Rate of transfer: %.2fkb/s", bytesMovedPerSecond/1000.0));
			System.out.println(String.format("Percent Complete: %d", percentComplete));
			listener.notifyProgress(
					String.format("File is copied successful!\n"
					+ "Rate of transfer: %.2fkb/s\n"
					+ "Percent Complete: %d\n", 
					bytesMovedPerSecond/1000.0, percentComplete));
			
		}

	}

	public void copyFilesToDir() throws FileNotFoundException, IOException {
		copyFilesToDir(exportLocation, files);
	}
	
	protected void copyFilesToDir(File directory, List<File> files) throws FileNotFoundException, IOException {
		for (File file : files) {
			long start = System.nanoTime();
			System.out.println("Exporting File: " + file.getAbsolutePath());
			listener.notifyCurrentFile("Exporting File: " + file.getAbsolutePath());
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
				bytesMoved += file.length();
				int percentComplete = (int)(((double)bytesMoved/(double)this.bytesInFileJob.get()) * 100);
				this.percentComplete.set(percentComplete);
				long end = System.nanoTime();
				double elapsedSeconds = (double)(end - start)/1000000000.0;
				this.bytesMovedPerSecond = file.length()/elapsedSeconds;
				System.out.println("File copied successfully!");
				System.out.println(String.format("Rate of transfer: %.2fkb/s", bytesMovedPerSecond/1000.0));
				System.out.println(String.format("Percent Complete: %d", percentComplete));
				listener.notifyProgress(
						String.format("File is copied successful!\n"
						+ "Rate of transfer: %.2fkb/s\n"
						+ "Percent Complete: %d\n", 
						bytesMovedPerSecond/1000.0, percentComplete));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public AtomicInteger getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(AtomicInteger percentComplete) {
		this.percentComplete = percentComplete;
	}

	public AtomicLong getBytesInFileJob() {
		return bytesInFileJob;
	}

	public void setBytesInFileJob(AtomicLong bytesInFileJob) {
		this.bytesInFileJob = bytesInFileJob;
	}

	public double getBytesMovedPerSecond() {
		return bytesMovedPerSecond;
	}

	public void setBytesMovedPerSecond(double bytesMovedPerSecond) {
		this.bytesMovedPerSecond = bytesMovedPerSecond;
	}

	public File getDirectory() {
		return parentDirectory;
	}

	public void setDirectory(File directory) {
		this.parentDirectory = directory;
	}

	public File getExportLocation() {
		return exportLocation;
	}

	public void setExportLocation(File exportLocation) {
		this.exportLocation = exportLocation;
	}

	public String[] getExts() {
		return exts;
	}

	public void setExts(String[] exts) {
		this.exts = exts;
	}
	
	public ExecuteCopy executeCopy() {
		return new ExecuteCopy();
	}
	
	public ExecuteMove executeMove() {
		return new ExecuteMove();
	}
	
	public class ExecuteCopy extends Thread{
		
		private FileJob fileJob;
		
		public ExecuteCopy() {
			this.fileJob = FileJob.this;
		}
		
		@Override
		public void run() {
			try {
				copyFilesToDir();
			} catch (IOException e) {
				listener.notifyCurrentFile("An error occured: " + e.getMessage());
				listener.notifyProgress("Execution Terminated");
				e.printStackTrace();
			}
		}
		
		public FileJob getFileJob() {
			return this.fileJob;
		}
	}
	
	public class ExecuteMove extends Thread{
		
		private FileJob fileJob;
		
		public ExecuteMove() {
			this.fileJob = FileJob.this;
		}

		@Override
		public void run() {
			try {
				moveFilesToDir();
			} catch (IOException e) {
				listener.notifyCurrentFile("An error occured: " + e.getMessage());
				listener.notifyProgress("Execution Terminated");
				e.printStackTrace();
			}
		}
				
		public FileJob getFileJob() {
			return this.fileJob;
		}
		
	}
	
	public interface FileJobListener {
		void notifyProgress(String progressInfo);
		void notifyCurrentFile(String currentFile);
	}
	
	
	

}
