package processor;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;



public class PassII {
	String filePath;
	File outputFile,aptabFile;
	FileWriter output,APTAB;
	BufferedReader mnt,mdt,nomacro,kpdtab;
	String line;
	LinkedHashMap<Integer,String> aptab;
	LinkedHashMap<String,String> kpdt;
	int apcnt=1,pp,kp,mdtp,kpdtp;
	
	public PassII() {
		aptab = new LinkedHashMap<>();
		kpdt = new LinkedHashMap<>();
	}

	public void performPassII(String SRC,String MNTPath,String MDTPath,String noMacroFilePath,String KPDTPath) throws IOException{
		String md;
		this.filePath = SRC.replaceAll("src.asm","");
		try {
		mnt = new BufferedReader(new FileReader(MNTPath));
		mdt = new BufferedReader(new FileReader(MDTPath));
		kpdtab = new BufferedReader(new FileReader(KPDTPath));
		nomacro = new BufferedReader(new FileReader(noMacroFilePath));
		outputFile = new File(filePath + "output.txt");
		if(outputFile.createNewFile()) {print("Output file created!");}else print("Output file already created!");
		output = new FileWriter(outputFile);		
		
		aptabFile = new File(filePath + "aptab.txt");
		if(aptabFile.createNewFile()) {print("APTAB file created!");}else print("APTAB file already created!");
		APTAB = new FileWriter(aptabFile);		
		
		}catch(IOException io) {io.printStackTrace();}
		mnt.mark(40);
		//read nomacro
		ArrayList<String> macrodef = new ArrayList<>();
		while((md = mnt.readLine())!=null) {String parts[] = md.split("\\s+");macrodef.add(parts[0]);}
		mnt.reset();
		
		while((line = nomacro.readLine())!= null) {
			
			String parts[] = line.split("\\s+");
			
			if(macrodef.contains(parts[0])) {
				//create APTAB
				mnt.mark(macrodef.indexOf(parts[0]));
				String macrodetails[] = mnt.readLine().split("\\s+");
				pp = Integer.parseInt(macrodetails[1]);
				kp = Integer.parseInt(macrodetails[2]);
				mdtp = Integer.parseInt(macrodetails[3]);
				kpdtp = Integer.parseInt(macrodetails[4]);
				apcnt = 1;
				for(int i=0;i<pp;i++) {//pp comes first 
					parts[apcnt] = parts[apcnt].replaceAll(",", "");
					aptab.put(apcnt, parts[apcnt]);
					
					APTAB.write(aptab.get(apcnt)  + "\n");
					apcnt++;
				}

				for(int i=apcnt;i<=parts.length-1;i++) {
					parts[i] = parts[i].replaceAll("&", "");
					parts[i] = parts[i].replace(",", "");
					String kparam[] = parts[i].split("=");
					kpdt.put(kparam[0], kparam[1]);

					
				}
				
				String li;
				kpdtab.mark(kpdtp);
				for(int i=0;i<kp;i++) {
					li = kpdtab.readLine();
					String kpdEntry[] = li.split("\\s+");
					
					if(kpdt.containsKey(kpdEntry[0])) {//given value by programmer
						APTAB.write(kpdt.get(kpdEntry[0]) + "\n");
						aptab.put(apcnt, kpdt.get(kpdEntry[0]));
						apcnt++;
						
					}else {//default value						
						APTAB.write(kpdEntry[1] + "\n");
						aptab.put(apcnt,kpdEntry[1]);
						apcnt++;
					}
					
				}
				//APTAB created! start reading mdt
				mdt.mark(mdtp);
				String mline;
				
				while((mline = mdt.readLine())!=null) {
					int index;
					String mdtparts[] = mline.split("\\s+");
					
					if(mdtparts[0].equalsIgnoreCase("MEND")) {break;}
					else {
						output.write("+ ");
						for(int i=0;i<mdtparts.length;i++) {
							if(mdtparts[i].contains("(P,")) {
								mdtparts[i] = mdtparts[i].replaceAll("[^0-9]", "");
								index = Integer.parseInt(mdtparts[i]);
								output.write(aptab.get(index) + "\t");
							}else {
								output.write(mdtparts[i] + "\t");
							}
						}
						output.write("\n");
					}
				}
				aptab.clear();
			}else {output.write("  " + line + "\n");}//normal assembly code
		}
		
		try {
			output.close();
			mnt.close();
			mdt.close();
			nomacro.close();
			kpdtab.close();
			APTAB.close();
		}catch(IOException io) {
			io.printStackTrace();
		}
	}
	public static void print(String m) {System.out.println(m);}
	public String getOutputAsmPath() {return outputFile.getAbsolutePath();}


}
