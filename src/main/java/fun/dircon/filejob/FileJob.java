package fun.dircon.filejob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class FileJob {

	protected List<File> files;
	protected AtomicInteger percentComplete;
	protected AtomicLong bytesInFileJob;
	protected long bytesMoved;
	protected double bytesMovedPerSecond;
	protected File parentDirectory;
	protected File exportLocation;
	protected String[] exts;
	protected FileJobProgressListener progressListener;
	protected FileJobCompleteListener jobCompleteListener;
	protected boolean terminate;
	protected Mean mean;

	public FileJob(File directory, File exportLocation, String... exts) {
		this.files = new ArrayList<>();
		this.parentDirectory = directory;
		this.exportLocation = exportLocation;
		this.exts = exts != null ? new String[0] : exts;
		this.bytesMoved = 0;
		this.mean = new Mean();
		this.terminate = false;

//		if (exts.length == 0) {
//			files = getExtractFiles();
//		} else {
//			files = consolidateFiles();
//		}
		this.percentComplete = new AtomicInteger(0);

		long bytes = 0;
		for (File file : files) {
			if (file.exists())
				bytes += file.length();
		}

		this.bytesInFileJob = new AtomicLong(bytes);
		this.bytesMovedPerSecond = 0;

	}

	public FileJob(File directory, File exportLocation, FileJobProgressListener listener, String... exts) {
		this(directory, exportLocation, exts);
		this.progressListener = listener;
	}

	public FileJob(File directory, File exportLocation, FileJobCompleteListener jobCompletelistener, String... exts) {
		this(directory, exportLocation, exts);
		this.jobCompleteListener = jobCompletelistener;
	}

	public FileJob(File directory, File exportLocation, FileJobProgressListener listener,
			FileJobCompleteListener jobCompleteListener, String... exts) {
		this(directory, exportLocation, exts);
		this.progressListener = listener;
		this.jobCompleteListener = jobCompleteListener;
	}

	protected List<File> getExtractFiles() {
		return getExtractFiles(parentDirectory, files);
	}

	protected List<File> getExtractFiles(File parent, List<File> files) {
		if (progressListener != null)
			progressListener.notifyProgress("Finding Files\nTime Remaining: Unknown");
		if (parent.isDirectory() && parent.canRead()) {
			File[] children = parent.listFiles();
			for (File file : children) {
				getExtractFiles(file, files);
			}

		} else {
			System.out.println("Adding File: " + parent.getAbsolutePath());
			long totalBytes = bytesInFileJob.get() + parent.length();
			
			this.bytesInFileJob.set(totalBytes);
			System.out.println("Bytes To Move: " + this.bytesInFileJob.get());
			files.add(parent);
		}
		return files;
	}

	protected List<File> consolidateFiles() {
		return consolidateFiles(Arrays.asList(exts), parentDirectory, files);
	}

	protected List<File> consolidateFiles(List<String> exts, File parent, List<File> files) {
		if (progressListener != null)
			progressListener.notifyProgress("Finding Files\nTime Remaining: Unknown");
		if (parent.isDirectory() && parent.canRead()) {
			File[] children = parent.listFiles();
			for (File file : children) {
				consolidateFiles(exts, file, files);
			}

		} else {
			for (String ext : exts) {
				System.out.println("Adding File: " + parent.getAbsolutePath());
				if (parent.getAbsolutePath().endsWith(ext)) {
					long totalBytes = bytesInFileJob.get() + parent.length();
					this.bytesInFileJob.set(totalBytes);
					files.add(parent);
				}
			}

		}

		return files;
	}

	public void moveFilesToDir() throws FileNotFoundException, IOException {
		moveFilesToDir(files);
	}

	protected void moveFilesToDir(List<File> files) throws FileNotFoundException, IOException {
		for (File file : files) {
			if(this.terminate) {
				break;
			}
			long start = System.nanoTime();
			long bytesInFile = file.length();
			if (progressListener != null)
				progressListener.notifyCurrentFile("Exporting File: " + file.getAbsolutePath());
			String path = exportLocation.getAbsolutePath().replace(Character.toString(File.pathSeparatorChar), "/")
					+ "/" + file.getName();
//			System.out.println("Exporting File: " + file.getAbsolutePath() + "To: " + path);
			// Files.move(file.toPath(), new File(path).toPath());
			bytesMoved += bytesInFile;
			
			file.renameTo(new File(path));
//			System.out.println("Bytes moved: " + bytesMoved);
//			System.out.println("Bytes in file: " + this.bytesInFileJob.get());
			int percentComplete = (int) (((double) bytesMoved / (double) this.bytesInFileJob.get()) * 100);
			this.percentComplete.set(percentComplete);
			long end = System.nanoTime();
			double elapsedSeconds = (double) (end - start) / 1000000000.0;
			this.bytesMovedPerSecond = (double)bytesInFile / (double)elapsedSeconds;
			this.recalculateProcessSpeed(bytesMovedPerSecond/1000.0);
			System.out.println("File copied successfully!");
			System.out.println(String.format("Rate of transfer: %.2fkb/s", bytesMovedPerSecond / 1000.0));
			System.out.println("Average Rate of Transfer: " + getAverageRateOfTransfer());
			System.out.println(String.format("Percent Complete: %d", percentComplete));
			if (progressListener != null)
				progressListener.notifyProgress(String.format(
						"Average Rate of transfer: %s\n"
				+ "Percent Complete: %d\n"
				+ "Time Remaining: %s",
						getAverageRateOfTransfer(), percentComplete, getTimeRemaining()));

		}

		if (jobCompleteListener != null)
			jobCompleteListener.notifyJobComplete();

	}

	public void copyFilesToDir() throws FileNotFoundException, IOException {
		copyFilesToDir(exportLocation, files);
	}

	protected void copyFilesToDir(File directory, List<File> files) throws FileNotFoundException, IOException {
		for (File file : files) {
			if(this.terminate) {
				break;
			}
			long start = System.nanoTime();
			System.out.println("Exporting File: " + file.getAbsolutePath());
			bytesMoved += file.length()/2;
			if (progressListener != null)
				progressListener.notifyCurrentFile("Exporting File: " + file.getAbsolutePath());
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
				
				int percentComplete = (int) (((double) bytesMoved / (double) this.bytesInFileJob.get()) * 100);
				this.percentComplete.set(percentComplete);
				long end = System.nanoTime();
				double elapsedSeconds = (double) (end - start) / 1000000000.0;
				this.bytesMovedPerSecond = (double)bytesMoved / (double)elapsedSeconds;
				this.recalculateProcessSpeed(bytesMovedPerSecond/1000.0);
				System.out.println("File copied successfully!");
				System.out.println(String.format("Rate of transfer: %.2fkb/s", bytesMovedPerSecond / 1000.0));
				System.out.println("Average Rate of Transfer: " + getAverageRateOfTransfer());
				System.out.println(String.format("Percent Complete: %d", percentComplete));
				if (progressListener != null)
					progressListener.notifyProgress(String.format(
							"Average Rate of transfer: %s\n"
					+ "Percent Complete: %d\n"
					+ "Time Remaining: %s",
							getAverageRateOfTransfer(), percentComplete, getTimeRemaining()));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (jobCompleteListener != null)
			jobCompleteListener.notifyJobComplete();

	}
	
	public void recalculateProcessSpeed(double newRateKBPerSecond) {
		this.mean.increment(newRateKBPerSecond);
	}
	
	public String getTimeRemaining() {
		int secondsRemaining = (int)(((this.bytesInFileJob.get() - this.bytesMoved) / 1000.0)
				* (1.0/mean.getResult()));
		if(secondsRemaining < 1) {
			return "less than a second";
		}else if(secondsRemaining < 60) {
			return secondsRemaining + " seconds";
		}else if (secondsRemaining < 3600) {
			return (secondsRemaining/60) + " minutes";
		}else if(secondsRemaining < (3600 * 24)) {
			return (secondsRemaining/3600) + " hours";
		}else {
			return (secondsRemaining/(3600 * 24)) + " days";
		}
	}
	
	public String getAverageRateOfTransfer() {
		if(this.mean.getResult() < 1000) {
			return String.format("%.2f KB/s", this.mean.getResult());
		}else if(this.mean.getResult() < 1000000) {
			return String.format("%.2f MB/s", this.mean.getResult()/1000.0);
		}else {
			return String.format("%.2f GB/s", this.mean.getResult()/1000000.0);
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

	public long getBytesMoved() {
		return bytesMoved;
	}

	public void setBytesMoved(long bytesMoved) {
		this.bytesMoved = bytesMoved;
	}

	public File getParentDirectory() {
		return parentDirectory;
	}

	public void setParentDirectory(File parentDirectory) {
		this.parentDirectory = parentDirectory;
	}

	public FileJobProgressListener getListener() {
		return progressListener;
	}
	
	/**
	 * @return the progressListener
	 */
	public FileJobProgressListener getProgressListener() {
		return progressListener;
	}

	/**
	 * @param progressListener the progressListener to set
	 */
	public void setProgressListener(FileJobProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	/**
	 * @return the jobCompleteListener
	 */
	public FileJobCompleteListener getJobCompleteListener() {
		return jobCompleteListener;
	}

	/**
	 * @param jobCompleteListener the jobCompleteListener to set
	 */
	public void setJobCompleteListener(FileJobCompleteListener jobCompleteListener) {
		this.jobCompleteListener = jobCompleteListener;
	}
	
	

	/**
	 * @return the terminate
	 */
	public boolean isTerminate() {
		return terminate;
	}

	/**
	 * @param terminate the terminate to set
	 */
	public void setTerminate(boolean terminate) {
		this.terminate = terminate;
	}

	/**
	 * @return the mean
	 */
	public Mean getMean() {
		return mean;
	}

	/**
	 * @param mean the mean to set
	 */
	public void setMean(Mean mean) {
		this.mean = mean;
	}

	public ExecuteCopy executeCopy() {
		return new ExecuteCopy();
	}

	public ExecuteMove executeMove() {
		return new ExecuteMove();
	}

	public class ExecuteCopy extends Thread {

		private FileJob fileJob;

		public ExecuteCopy() {
			this.fileJob = FileJob.this;
		}

		@Override
		public void run() {
			try {
				if(exts.length == 0) {
					getExtractFiles();
				}else {
					consolidateFiles();
				}
				copyFilesToDir();
			} catch (IOException e) {
				progressListener.notifyCurrentFile("An error occured: " + e.getMessage());
				progressListener.notifyProgress("Execution Terminated");
				e.printStackTrace();
			}
		}

		public FileJob getFileJob() {
			return this.fileJob;
		}
	}

	public class ExecuteMove extends Thread {

		private FileJob fileJob;

		public ExecuteMove() {
			this.fileJob = FileJob.this;
		}

		@Override
		public void run() {
			try {
				if(exts.length == 0) {
					getExtractFiles();
				}else {
					consolidateFiles();
				}
				moveFilesToDir();
			} catch (IOException e) {
				progressListener.notifyCurrentFile("An error occured: " + e.getMessage());
				progressListener.notifyProgress("Execution Terminated");
				e.printStackTrace();
			}
		}

		public FileJob getFileJob() {
			return this.fileJob;
		}

	}

	public interface FileJobProgressListener {
		void notifyProgress(String progressInfo);

		void notifyCurrentFile(String currentFile);
	}

	public interface FileJobCompleteListener {
		void notifyJobComplete();
	}

}
