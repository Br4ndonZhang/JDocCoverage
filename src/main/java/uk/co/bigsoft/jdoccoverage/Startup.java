/* =========================================================
 * JavaDoc Coverage : a free tool for the Java(tm) platform
 * =========================================================
 *
 * Copyright (C) 2006, by Volker Berlin
 *
 * Project Info:  http://JDocCoverage.sourceforge.net/
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * Created on 07.03.2006
 */
package uk.co.bigsoft.jdoccoverage;

import java.io.*;
import java.util.*;

/**
 * @author Volker Berlin
 */
public class Startup {

	private static ArrayList results = new ArrayList();
	private static String directory = "JDocCoverage";

	public static void main(String args[]) throws Exception {
		if (args.length == 0) {
			help(null);
		}
		int a = 0;
		ArrayList sources = new ArrayList();
		while (a < args.length) {
			String arg = args[a++];
			if ("-d".equals(arg) && a < args.length) {
				directory = args[a++];
			} else if (arg.startsWith("-")) {
				help("Unknown option:" + arg);
			}
			sources.add(arg);
		}
		if (sources.size() == 0) {
			help("No source files enter");
		}
		FileScanner scanner = new FileScanner(sources);
		scanner.scan();
		ArrayList files = scanner.getFiles();
		if (files.size() == 0) {
			help("No source files selected");
		}
		File parent = new File(directory);
		for (int i = 0; i < files.size(); i++) {
			String source = (String) files.get(i);
			handleFile(source);
		}
		writeResults(parent);
		copyStylesheet(parent);
	}

	static void help(String msg) {
		if (msg != null)
			System.out.println(msg);
		System.out.println("JavaDoc and Comment Coverage version 0.10");
		System.out.println();
		System.out.println("  usage: java -jar jdoccoverage.jar <options> <source files>");
		System.out.println("  where possible options include:");
		System.out.println("    -d <directory>\tSpecify where to place generated coverage result");
		System.out.println();
		System.out.println("  java -jar jdoccoverage.jar src/");
		System.out.println("  java -jar jdoccoverage.jar src/**/*.java src2/?abc*.java");
		System.out.println("  java -jar jdoccoverage.jar -d output src/");
		System.exit(0);
	}

	static void handleFile(String fileName) {
		File file = new File(fileName);
		if (file.isDirectory()) {
			handleDirectory(file);
		} else {
			if (fileName.endsWith(".java")) {
				System.out.println(fileName);
				String content = loadSourceFile(fileName);
				Parser parser = new Parser(content, fileName);
				CoverageResult result = parser.parse();
				results.add(result);
			}
		}
	}

	static void handleDirectory(File dir) {
		String[] fileNames = dir.list();
		Arrays.sort(fileNames);
		for (int f = 0; f < fileNames.length; f++) {
			handleFile(dir.getPath() + File.separatorChar + fileNames[f]);
		}
	}

	static String loadSourceFile(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			DataInputStream dis = new DataInputStream(fis);
			byte[] puffer = new byte[fis.available()];
			dis.readFully(puffer);
			return new String(puffer);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static void writeResults(File parent) throws Exception {
		ResultWriter w = new ResultWriter();
		HashMap packageMap = new HashMap();
		if (parent.exists()) {
			deltree(parent);
		}
		parent.mkdir();
		CoverageResult allResult = new CoverageResult("index");
		PrintStream allOutput = w.createPrintStream(allResult, parent);
		w.writeResultFileHeader(allOutput, allResult);
		w.writeResultHeader(allOutput);

		for (int r = 0; r < results.size(); r++) {
			CoverageResult result = (CoverageResult) results.get(r);

			PrintStream output = w.createPrintStream(result, parent);
			w.writeResultFileHeader(output, result);
			w.writeResultHeader(output);
			w.writeResultLine(output, result, false);
			w.writeResultFileFooter(output, result);

			String packageName = result.packageName;
			CoverageResult packageResult = (CoverageResult) packageMap.get(packageName);
			if (packageResult == null) {
				packageResult = new CoverageResult(packageName);
				packageMap.put(packageName, packageResult);
			}
			packageResult.accumulate(result);
			allResult.accumulate(result);
		}
		w.writeResultLine(allOutput, allResult, false);

		// write the package data
		Iterator packages = packageMap.keySet().iterator();
		while (packages.hasNext()) {
			Object packageName = packages.next();
			CoverageResult packageResult = (CoverageResult) packageMap.get(packageName);
			PrintStream output = w.createPrintStream(packageResult, parent);
			w.writeResultFileHeader(output, packageResult);
			w.writeResultHeader(output);
			w.writeResultLine(output, packageResult, false);
			for (int r = 0; r < results.size(); r++) {
				CoverageResult result = (CoverageResult) results.get(r);
				if (packageName.equals(result.packageName)) {
					w.writeResultLine(output, result, true);
				}
			}
			w.writeResultFileFooter(output, packageResult);

			w.writeResultLine(allOutput, packageResult, true);
		}

		w.writeResultFileFooter(allOutput, allResult);
	}

	private static void deltree(File parent) {
		if (parent.isDirectory()) {
			File[] files = parent.listFiles();
			for (int f = 0; f < files.length; f++) {
				deltree(files[f]);
			}
		}
		parent.delete();
	}

	private static void copyStylesheet(File parent) throws IOException {
		InputStream input = Startup.class.getResourceAsStream("/default.css");
		if (input == null) {
			System.out.println("Startup.class.getResourceAsStream('default.css') failed");
			return;
		}
		File file = new File(parent, "default.css");
		FileOutputStream fos = new FileOutputStream(file);
		byte[] puffer = new byte[1024];
		while (true) {
			int count = input.read(puffer);
			if (count < 0)
				break;
			fos.write(puffer, 0, count);
		}
		fos.flush();
		fos.close();
	}
}
