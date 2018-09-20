package it.polito.translator.symnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Post-Process of SymNet Translator. It takes the Rule_NF.scala generated
 * automatically by the parser. It reads each line of the file to replace
 * symbolic values. <br>
 * For example,<br>
 * postParsef -> :==:
 * 
 * @author s211483
 * @version 1.0 01/07/2018
 *
 */
public class PostProcess {
	public static void main(String[] args) {

		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedWriter out = null;

		try {
			if (args.length != 2) {
				System.err.println("Usage: Post_Parser <NF.scala_path>");
				System.exit(-1);
			}
			fstream = new FileInputStream(args[0]);
			in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine, strNew;
			StringBuilder fileContent = new StringBuilder();
			while ((strLine = br.readLine()) != null) {
				strNew = strLine;
				if (strLine.contains("postParsef")) {
					strNew = strLine.replaceAll("postParsef", ":==:");
				}
				if (strNew.contains("Fchiocciola")) {
					String r = ":" + "@";
					strNew = strNew.replaceAll("Fchiocciola", r);
				}
				if (strNew.contains(" Array[ConfigParameter]")) {
					strNew = strLine.replace(" Array[ConfigParameter]", "List[ConfigParameter]");
				}
				fileContent.append(strNew);
				fileContent.append(System.getProperty("line.separator"));
			}

			FileWriter fstreamWrite = new FileWriter(args[1]);
			out = new BufferedWriter(fstreamWrite);
			out.write(fileContent.toString());
		} catch (

		Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fstream.close();
				out.flush();
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
