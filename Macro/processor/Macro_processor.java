package processor;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class Macro_processor implements Runnable{
	private String sourceFile,line;
	private String MDT,MNT,noMacroFile,outputFile,KPDT;
	
	Macro_processor(String src) {
		sourceFile = src;
		MDT = MNT = noMacroFile = outputFile = KPDT = null;
	}
	private void MacroPassI() {
		
		try {
			System.out.println("Preparing required data structures...\n");
			PassI p1 = new PassI(sourceFile);
			p1.performPassI(sourceFile);
			MDT = p1.getMDTPath();	//to get macro definition
			MNT = p1.getMNTPath(); //to get total parameters in macro
			noMacroFile = p1.getNoMacroFilePath(); //to process pass II
			KPDT = p1.getKPDTABPath();
		}
		catch(IOException e) {e.printStackTrace();}
	
	}
	private void MacroPassII() {
		System.out.println("\n--------------------------------\n");
		System.out.println("PASS II started...");
		try {
			PassII p2 = new PassII();
			System.out.println("Preparing APTAB...\n");
			p2.performPassII(sourceFile,MNT, MDT, noMacroFile,KPDT);
			outputFile = p2.getOutputAsmPath();
		}catch(IOException e) {
			e.printStackTrace();}
		
		System.out.println("\nPass II of macro processer completed!");
		System.out.println("\n--------------------------------\n");
		
	}
	private void printOutput() throws IOException{
		try {
		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		while((line = br.readLine())!=null) {System.out.println(line);}
		br.close();
		}catch(IOException e) {e.printStackTrace();}
		
	}
	public void run() {
		try {
		System.out.println("--------------------------------\n");
		System.out.println("PASS I started...");Thread.sleep(2000);
		this.MacroPassI();Thread.sleep(2000);
		System.out.println("\nPass I of macro processer completed!");
		
		System.out.println("--------------------------------\n");
		System.out.println("PASS II started...");Thread.sleep(2000);
		this.MacroPassII();Thread.sleep(2000);
		System.out.println("\nPass II of macro processer completed!");
		System.out.println("Macro preprocessor Completed!");
		this.printOutput();
		
		}catch(Exception e){e.printStackTrace();}
		
		System.out.println("Path of output file: " + this.outputFile);
	}
	
	public static void main(String[] args) throws IOException{
		
		String src = "E:\\JAVA\\Eclipse\\lp1_Assignment03_macro\\src\\files\\src.asm";
		Thread t1 = new Thread(new Macro_processor(src));
		t1.start();
		
	
	}
}
