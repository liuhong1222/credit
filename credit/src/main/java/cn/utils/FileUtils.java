package cn.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class FileUtils {

	private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * 生成报表
	 * 
	 * @param fileName
	 * @param filePath
	 * @param dataList
	 */
	public static void createCvsFile(String fileName, String filePath, List<List<Object>> dataList, Object[] head) {

		BufferedWriter csvWtriter = null;
		File csvFile = null;

		try {
			List<Object> headList = Arrays.asList(head);

			csvFile = new File(filePath + fileName);
			File parent = csvFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			csvFile.createNewFile();
			// GB2312使正确读取分隔符","
			csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 1024);
			int num = headList.size() / 2;
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < num; i++) {
				buffer.append(" ,");
			}

			csvWtriter.write(buffer.toString() + fileName + buffer.toString());
			csvWtriter.newLine();

			// 写入文件头部
			writeRow(headList, csvWtriter);
			// 写入文件内容
			for (List<Object> row : dataList) {
				writeRow(row, csvWtriter);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("生成报表文件异常：" + e.getMessage());
		} finally {
			try {
				csvWtriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 生成报表
	 *
	 * @param fileName
	 * @param filePath
	 * @param dataList
	 */
	public static void createTxtFile(String fileName, String filePath, List<List<Object>> dataList, Object[] head) {

		BufferedWriter csvWtriter = null;
		File csvFile = null;

		try {
			csvFile = new File(filePath + fileName);
			File parent = csvFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			csvFile.createNewFile();
			csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 1024);
			// 写入文件内容
			for (List<Object> row : dataList) {
				writeTxtRow(row, csvWtriter);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("生成报表文件异常：{}" , e);
		} finally {
			try {
				csvWtriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 生成报表
	 * 
	 * @param fileName
	 * @param filePath
	 * @param dataList
	 */
	public static void createCvsFileByMap(String fileName, String filePath, List<Map<String, Object>> dataList,
			Object[] head) {

		BufferedWriter csvWtriter = null;
		File csvFile = null;

		try {
			List<Object> headList = Arrays.asList(head);

			csvFile = new File(filePath + fileName);
			File parent = csvFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			csvFile.createNewFile();
			// GB2312使正确读取分隔符","
			csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "gbk"), 1024);
			int num = headList.size() / 2;
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < num; i++) {
				buffer.append(" ,");
			}

			csvWtriter.write(buffer.toString() + fileName + buffer.toString());
			csvWtriter.newLine();

			// 写入文件头部
			writeRow(headList, csvWtriter);
			// 写入文件内容
			for (Map<String, Object> row : dataList) {
				writeRowByMap(row.get("mobile").toString(), csvWtriter);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("生成报表文件异常：" + e.getMessage());
		} finally {
			try {
				csvWtriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * 生成报表
	 * 
	 * @param fileName
	 * @param filePath
	 * @param dataList
	 */
	public static void createCvsFileByMap2(String fileName, String filePath, List<Map<String, Object>> dataList,
			Object[] head) {

		BufferedWriter csvWtriter = null;
		File csvFile = null;

		try {
			List<Object> headList = Arrays.asList(head);

			csvFile = new File(filePath + fileName);
			File parent = csvFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}

			csvFile.createNewFile();
			// GB2312使正确读取分隔符","
			csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "gbk"), 1024);
			int num = headList.size() / 2;
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < num; i++) {
				buffer.append(" ,");
			}

			csvWtriter.write(buffer.toString() + fileName + buffer.toString());
			csvWtriter.newLine();

			// 写入文件头部
			writeRow(headList, csvWtriter);
			// 写入文件内容
			for (Map<String, Object> row : dataList) {
				writeRowByMap(row.get("mobile").toString(), csvWtriter);
				writeRowByMap(row.get("delivd").toString(), csvWtriter);
				writeRowByMap(row.get("reportTime").toString(), csvWtriter);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("生成报表文件异常：" + e.getMessage());
		} finally {
			try {
				csvWtriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 写入文件
	 * 
	 * @param row
	 * @param csvWriter
	 * @throws IOException
	 */
	private static void writeRow(List<Object> row, BufferedWriter csvWriter) throws IOException {
		for (Object data : row) {
			StringBuffer sb = new StringBuffer();
			String rowStr = sb.append("\"").append(data).append("\",").toString();
			csvWriter.write(rowStr);
		}
		csvWriter.newLine();
	}

	private static void writeTxtRow(List<Object> row, BufferedWriter csvWriter) throws IOException {
		for (Object data : row) {
			StringBuffer sb = new StringBuffer();
			String rowStr = sb.append(data).toString();
			csvWriter.write(rowStr);
		}
		csvWriter.newLine();
	}

	/**
	 * 写入文件
	 * 
	 * @param row
	 * @param csvWriter
	 * @throws IOException
	 */
	private static void writeRowByMap(String row, BufferedWriter csvWriter) throws IOException {
		StringBuffer sb = new StringBuffer();
		String rowStr = sb.append("\"").append(row).append("\",").toString();
		csvWriter.write(rowStr);
		csvWriter.newLine();
	}

	/**
	 * 生成 zip打包文件
	 * 
	 * @param files
	 * @param strZipName
	 */
	public static void createZip(List<File> list, String strZipName) {
		try {
			byte[] buffer = new byte[1024];

			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(strZipName));

			// 需要同时下载的两个文件result.txt ，source.txt

			for (File file : list) {
				FileInputStream fis = new FileInputStream(file);

				out.putNextEntry(new ZipEntry(file.getName()));

				int len;

				// 读入需要下载的文件的内容，打包到zip文件

				while ((len = fis.read(buffer)) > 0) {

					out.write(buffer, 0, len);

				}

				out.closeEntry();

				fis.close();
			}

			out.close();

			System.out.println("生成Demo.zip成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("打包文件异常：" + e.getMessage());
		}
	}
	
	/**
	 * 生成 zip打包文件(带密码)
	 * 
	 * @param files
	 * @param strZipName
	 */
	public static void createZipByPassword(List<File> list, String strZipName,String password) {
		if (list == null || list.size() == 0) {
			return;
		}
 
		ZipParameters parameters = new ZipParameters();
		// 压缩方式
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		// 压缩级别
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		// 加密方式
		if (StringUtils.isNotBlank(password)) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			parameters.setPassword(password);
		}
 
		ArrayList<File> existFileList = new ArrayList<File>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) != null) {
				existFileList.add(list.get(i));
			}
		}
		
		try {
			ZipFile zipFile = new ZipFile(strZipName);
			zipFile.addFiles(existFileList, parameters);
			System.out.println("生成加密文件" + strZipName + "成功");
		} catch (net.lingala.zip4j.exception.ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("生成加密文件" + strZipName + "异常，异常信息为：" + e.getMessage());
		}
	}

	/**
	 * 获取文件大小
	 * 
	 * @param path
	 */
	@SuppressWarnings("resource")
	public static String getFileSize11(String path) {
		String size = null;
		try {
			size = new FileInputStream(new File(path)).available() / 1024 / 1024 + "M";
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("打包文件异常：" + e.getMessage());
		}
		return size;
	}

	public static String getFileSize(String path) {
		File file = new File(path);
		String size = "";
		if (file.exists() && file.isFile()) {
			long fileS = file.length();
			DecimalFormat df = new DecimalFormat("#.00");
			if (fileS < 1024) {
				size = "1KB";
			} else {
				size = df.format((double) fileS / 1024) + "KB";
			}
		} else if (file.exists() && file.isDirectory()) {
			size = "";
		} else {
			size = "0KB";
		}
		return size;
	}

	/**
	 * 获取上传文件开始行到结束行的内容
	 * 
	 * @return
	 */
	public static String getFileMenu(String fielUrl, int startLine, int endLine) {
		String mobiles = "";
		try {
			File file = new File(fielUrl);// 文件路径
			FileReader fileReader = new FileReader(file);
			LineNumberReader reader = new LineNumberReader(fileReader);
			String txt = "";

			int lines = 0;
			while (txt != null) {
				lines++;
				txt = reader.readLine();
				if (lines > startLine && lines <= endLine) {
					mobiles = mobiles + txt + ",";
				}
			}
			reader.close();
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取文件内容异常：" + e.getMessage());
		}

		return mobiles;
	}
	
	public static int getFileLines(String fileUrl){
		try {
			LineNumberReader rf = null;
			int lines = 0;
			File test = new File(fileUrl);
			File file1 = new File(fileUrl);
			if (file1.isFile() && file1.exists()) {
				long fileLength = test.length();
				rf = new LineNumberReader(new FileReader(test));

				if (rf != null) {
					rf.skip(fileLength);
					lines = rf.getLineNumber();
					rf.close();
				}
			}
			return lines;
		} catch (Exception e) {
			logger.error("获取文件行数异常：" + e.getMessage());
		}
		return 0;
	}
	
	/**
	 * 生成 zip打包文件
	 * 
	 * @param files
	 * @param strZipName
	 */
	public static void batchCreateZip(List<File> list, String strZipName) {
		try {
			byte[] buffer = new byte[1024];

			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(strZipName));
			for (File file : list) {
				FileInputStream fis = new FileInputStream(file);
				String tempName = file.getName();
				String fileName = tempName.substring(0, tempName.length()-4)+"_"+DateUtils.getDate() +  
						(int)((Math.random() * 9 + 1) * 100000) + tempName.substring(tempName.length()-4, file.getName().length());
				out.putNextEntry(new ZipEntry(fileName));

				int len;
				// 读入需要下载的文件的内容，打包到zip文件
				while ((len = fis.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				
				out.closeEntry();
				fis.close();
			}

			out.close();
			System.out.println("生成压缩结果文件" + strZipName + "成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("打包文件异常：" + e.getMessage());
		}
	}
	
	/**
	 * 生成 zip打包文件(带密码)
	 * 
	 * @param files
	 * @param strZipName
	 */
	public static void batchCreateZipByPassword(List<File> list, String strZipName,String password) {
		if (list == null || list.size() == 0) {
			return;
		}
 
		ZipParameters parameters = new ZipParameters();
		// 压缩方式
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		// 压缩级别
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		// 加密方式
		if (StringUtils.isNotBlank(password)) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			parameters.setPassword(password);
		}
 
		ArrayList<File> existFileList = new ArrayList<File>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) != null) {
				existFileList.add(list.get(i));
			}
		}
		
		try {
			ZipFile zipFile = new ZipFile(strZipName);
			zipFile.addFiles(existFileList, parameters);
			System.out.println("生成加密文件" + strZipName + "成功");
		} catch (net.lingala.zip4j.exception.ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("生成加密文件" + strZipName + "异常，异常信息为：" + e.getMessage());
		}
	}

	public static void main(String[] args) {
//		 List<File> fileList = new ArrayList<>();
//		 fileList.add(new File("D:\\test\\1000001.txt"));
//		 fileList.add(new File("D:\\testtxt\\大数据测试\\1000001.txt"));
//		 fileList.add(new File("D:\\testtxt\\1000001.txt"));
//		 FileUtils.batchCreateZipByPassword(fileList,"D:\\testtxt\\Demo.zip","123");

		System.out.println("18611764784".hashCode());
	}

	// String therefileName = "thereCSV.csv";// 文件名称
	// String thereFilePath = "c:/test/"; // 文件路径
	//
	// String sixfileName = "sixCSV.csv";// 文件名称
	// String sixFilePath = "c:/test/"; // 文件路径
	//
	// String unkonwnfileName = "unkonwnCSV.csv";// 文件名称
	// String unknownFilePath = "c:/test/"; // 文件路径
	//

	// 文件下载，使用如下代码
	// response.setContentType("application/csv;charset=gb18030");
	// response.setHeader("Content-disposition", "attachment; filename="
	// + URLEncoder.encode(fileName, "UTF-8"));
	// ServletOutputStream out = response.getOutputStream();
	// csvWtriter = new BufferedWriter(new OutputStreamWriter(out,
	// "GB2312"), 1024);
}
