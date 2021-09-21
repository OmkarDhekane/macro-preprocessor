package processor;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.util.Iterator;


public class PassI {
	String filePath;
	String line,macroname;
	BufferedReader readsrc;
	Scanner sc = new Scanner(System.in);
	
	FileWriter MNT,MDT,KPDTAB,ir,PNT;//to write into files
	File MNTFile,MDTFile,KPDFile,noMacroFile,sourceFile;//to store in files
	LinkedHashMap<String,Integer> PNTAB = new LinkedHashMap<>();
	int kp=0,pp=0,mdtp=1,kpdtp=0;
	int pcnt=1;
	

	public PassI(String SRC) throws IOException{
		line = macroname = null;
		this.filePath = SRC.replaceAll("src.asm","");
		try {
			//initalizing and creating all files and their writer objects
			MNTFile = new File(filePath + "mnt.txt");
			if(MNTFile.createNewFile())print("MNT created!");
			else print("MNT already created!");
			
			MDTFile = new File(filePath + "mdt.txt");
			if(MDTFile.createNewFile())print("MDT created!");
			else print("MDT already created!");
			
			noMacroFile = new File(filePath + "noMacroFile.txt");
			if(noMacroFile.createNewFile())print("IR created");
			else print("IR already created!");
			
			KPDFile = new File(filePath + "kpdtab.txt");
			if(KPDFile.createNewFile())print("KPDT created!");
			else print("KPDT already created!");
			
			
			MNT = new FileWriter(MNTFile);
			MDT = new FileWriter(MDTFile);
			KPDTAB = new FileWriter(KPDFile);
			ir = new FileWriter(noMacroFile);
			PNT = new FileWriter(filePath + "pnt.txt");
			
		}catch(IOException io) {io.printStackTrace();}
		

	}
	public static void readFile(String path) throws IOException{	
		String l;
		BufferedReader f = new BufferedReader(new FileReader(path));
		while((l = f.readLine()) != null) {System.out.print(l + "\n");}
		f.close();
	}
	public void writeFile(String fname) throws IOException{	
		FileWriter f = new FileWriter((filePath + fname),true);		
		String l = "";
		while(!(l = sc.nextLine()).equals("stop") && sc.hasNextLine()){f.write(l+"\n");}
		f.close();
	}	
	public static void print(String message) {System.out.println(message);}
	
	public String getMNTPath() {return MNTFile.getAbsolutePath();}
	public String getMDTPath() {return MDTFile.getAbsolutePath();}
	public String getNoMacroFilePath() {return noMacroFile.getAbsolutePath();}
	public String getKPDTABPath() {return KPDFile.getAbsolutePath();}
	
	public void performPassI(String srcfile)throws IOException {
		this.sourceFile= new File(srcfile);
		this.readsrc  = new BufferedReader(new FileReader(sourceFile));

//		this.readFile("src.asm");
		String line;
		boolean flag = false;
		while((line = readsrc.readLine()) != null) 
		{	
			String parts[] = line.split("\\s+");
			if(parts[0].equalsIgnoreCase("MACRO")) {//its macro header
				flag = true;
				line = readsrc.readLine();
				String subparts[] = line.split("\\s+");
				macroname = subparts[0];
				
				if(subparts.length<=1) {//processing macro prototype statement
					MNT.write(macroname+"\t"+pp+"\t"+kp+"\t"+mdtp+"\t"+(kp==0?kpdtp:(kpdtp+1))+"\n");
					continue;
				}
				
				//process parameters
				for(int i=1;i<subparts.length;i++) {
					subparts[i] = subparts[i].replaceAll("[&,]", "");
					if(subparts[i].contains("=")) {//its a keyword parameter
						kp++;
						String keywordparam[] = subparts[i].split("=");
						
						PNTAB.put(keywordparam[0], pcnt++);
						
						if(keywordparam.length == 2) //keyword with value given
						{KPDTAB.write(keywordparam[0] + "\t" + keywordparam[1] + "\n");}
						else {KPDTAB.write(keywordparam[0] + "\t" + "-\n");}
						
					}
					else {//its a positional parameter
						PNTAB.put(subparts[i], pcnt++);
						pp++;
					}
				}
				MNT.write(macroname+"\t"+pp+"\t"+kp+"\t"+mdtp+"\t"+(kp==0?kpdtp:(kpdtp+1))+"\n");
				kpdtp = kpdtp + kp;
			}
			else if(parts[0].equalsIgnoreCase("MEND")) //its mend keyword
			{
				//reset all parameters
				pp=kp=0;
				flag = false;
				pcnt=1;
				MDT.write(parts[0] + "\n");
				mdtp++;
				PNT.write(macroname +":\t");
				Iterator<String> it = PNTAB.keySet().iterator();
				while(it.hasNext()) {PNT.write(it.next() + "\t");}
				PNT.write("\n");
				PNTAB.clear();
			}
			else if(flag == true) {//its macro body
				
				for(int i=0;i<parts.length;i++) {
					if(parts[i].contains("&")) {			//its a paramter
						parts[i] = parts[i].replaceAll("[&,]", "");
						MDT.write("(P,"+PNTAB.get(parts[i]) + ")\t");
					}else {
						MDT.write(parts[i] + "\t\t");
					}
				}
				MDT.write("\n");
				mdtp++;
			}
			else {//its asm code w/o macro
				ir.write(line + "\n");
			}
		}

		try{
			MNT.close();
			MDT.close();
			KPDTAB.close();
			readsrc.close();
			ir.close();
			sc.close();
			PNT.close();
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}