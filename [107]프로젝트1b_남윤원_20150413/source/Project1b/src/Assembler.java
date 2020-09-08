import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// ���� ����°� ���� ó���� ���� ���̺귯����
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;

// ���� ���̺귯��
import java.lang.Math;

/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) 
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 *     
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 *  ���� ��� : ����ó�� ������ �߰��Ͽ� �� �����ϰ� �˻縦 �ϴ� ���� ���� ���Դϴ�. ���� ��� �޺κп� �� ���� ÷���߽��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ����*/
	ArrayList<LiteralTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����.   
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;
	ArrayList<String> codeList_loc;

	/** program counter���� ���� */
	/** �� section���� ���α׷��� length�� ���� */
	ArrayList<Integer> lengthList;
	static int pc = 0;
	
	/** section�� ������ ���� */
	static int section = 0;

	/** EXTDEF�� EXTREF �������� ���� */
	ArrayList<ArrayList<String>> EXTDEFS_all;
	ArrayList<ArrayList<String>> EXTREFS_all;

	/** Object Program�� M������ ����ϱ� ����, pass2 �۾��� �ϸ鼭 ���� ���� */
	ArrayList<ArrayList<String>> M_Line;

	
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
		codeList_loc = new ArrayList<String>();
		lengthList = new ArrayList<Integer>();
		EXTDEFS_all = new ArrayList<ArrayList<String>>();
		EXTREFS_all = new ArrayList<ArrayList<String>>();
		M_Line = new ArrayList<ArrayList<String>>();
	}

	/** 
	 * ������� ���� ��ƾ
	 */
	public static void main(String[] args) {

		Assembler assembler = new Assembler("inst.data");
		
		assembler.loadInputFile("input.txt");
		assembler.pass1();

		assembler.printSymbolTable("symtab_20150413");
		assembler.printLiteralTable("literaltab_20150413");
		assembler.pass2();
		assembler.printObjectCode("output_20150413");
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * @param inputFile : input ���� �̸�.
	 * 
	 * 
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		
		// ���پ� �о �����ؾ� ��
		// inputFile�� ������ ������ �� : try-catch ���� ó��
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			while(true) {
				String line = br.readLine();
				if (line == null) break;
				lineList.add(line);
			//	System.out.println(line);
			}
			br.close();
		} 
		catch (IOException e) {
			// ���� ó�� : �ʱ�ȭ�� �����Ͽ��ٴ� ���� ���
			System.out.println("input.txt doesn't find! Initialization failed.\n");
			System.exit(0);

		}
	}

	/** 
	 * pass1 ������ �����Ѵ�.
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����
	 *   2) label�� symbolTable�� ����
	 *   
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		
		// �� lineList ������ŭ for���� ������ ��
		// ���α׷��� section���� �ϳ��� SymbolTable�� TokenTable�� �����ؾ� ��
		
		pc = 0;
		section = 0;
		
		ArrayList<String> Lit_temp = new ArrayList<String>();
		
		for (int i = 0; i < lineList.size(); i++) {
			Token token = new Token(lineList.get(i));
			
			// �ϴ� �� ���� SymbolTable�� TokenTable�� ����� ����
			if (i == 0) {
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(section), instTable));
				TokenList.get(section).literalTab = literaltabList.get(section);
				section++;
			}
			// section�� �ٲ� ������ �ϳ� ����� ����
			else if (token.operator != null && token.operator.equals("CSECT")) {	
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(section), instTable));
				TokenList.get(section).literalTab = literaltabList.get(section);
				section++;
				lengthList.add(pc); // ���α׷��� �� ���̸� ����
				pc = 0;				// pc���� �ٽ� 0���� ����
			}
			
			// ��ū�и��� ���� TokenList�� ������
			TokenList.get(section - 1).putToken(lineList.get(i));
			
			// label�� null�� �ƴ� ��� symboltable�� ����
			if (token.label != null) {
				// symtabList.get(section - 1).putSymbol(token.label, pc);
				symtabList.get(section - 1).symbolList.add(token.label);
				if (!token.operator.equals("EQU") || (token.operand != null && token.operand[0].equals("*"))) {
					symtabList.get(section - 1).locationList.add(pc);
				}
				else if (token.operator.equals("EQU")) {
					// EQU�� ��츸 ���� ó��
					int loc = 0;
					String str[] = token.operand[0].split("-");
					// str[0]�� str[1]�� �ּҸ� ã�Ƽ� �� �־�� ��
					for (int j = 0; j < symtabList.get(section-1).symbolList.size(); j++) {
						if (str[0].equals(symtabList.get(section-1).symbolList.get(j))) {
							loc += symtabList.get(section-1).locationList.get(j);
						}
						else if (str[1].equals(symtabList.get(section-1).symbolList.get(j))) {
								loc -= symtabList.get(section-1).locationList.get(j);
						}
					}
				//	System.out.println("loc�� �� : " + loc);
					symtabList.get(section - 1).locationList.add(loc);
				}
			}
			
			// operand[0]�� ������ =�� ��� �����س��Ҵٰ�, 
			// LTORG�� END�� ������ �׶� literaltable�� ����
			
			if (token.operand != null && token.operand[0].charAt(0) == '=') {
				int sw = 0;
				for (int k = 0; k < Lit_temp.size(); k++) {
					if (Lit_temp.get(k).equals(token.operand[0])) {
						sw = 1;
						break;
					}
				}
				if (sw == 0)
					Lit_temp.add(token.operand[0]);
			}
			
			if (token.operator != null && (token.operator.equals("LTORG") || token.operator.equals("END"))) {
				for (int k = 0; k < Lit_temp.size(); k++) {
					String str[] = Lit_temp.get(k).split("\'");
					literaltabList.get(section - 1).literalList.add(str[1]);
					literaltabList.get(section - 1).locationList.add(pc);
					if (str[0].equals("=X")) {
					//	System.out.println(str[1].length() / 2 + "xx");
						pc += str[1].length() / 2;
					}
					else if (str[0].equals("=C")) {
					//	System.out.println(str[1].length() + "cc");
						pc += str[1].length();
					}
				}
				
				if (token.operator.equals("END")) {
					lengthList.add(pc);
				}
				Lit_temp.clear();
			}
			
			// operator�� null�� �ƴ� ��� �ּҰ�(pc��)�� ������Ŵ
			if (token.operator != null) {
				// ���� �ִٸ� ����
				if (instTable.instMap.containsKey(token.operator)) {
					pc += instTable.instMap.get(token.operator).format;
				}
				else {
					// �� ���� START, LTORG, END, USE, EQU, ...
					// BYTE, WORD�� ���� ��쵵 �����ؾ� ��. ���⼭ ó������!
					if (token.operator.equals("BYTE")) {
						String str[] = token.operand[0].split("\'");
						if (str[0].equals("X")) {
							pc += str[1].length() / 2;
						}
						else if (str[1].equals("C")) {
							pc += str[1].length();
						}
					}
					else if (token.operator.equals("WORD")) {
						int pc_orig = pc;
						String str[] = token.operand[0].split("-");
						// str[0]�� str[1]�� �ּҸ� ã�Ƽ� �� �־�� ��
						for (int j = 0; j < symtabList.get(section-1).symbolList.size(); j++) {
							if (str[0] == symtabList.get(section-1).symbolList.get(j)) {
								pc += symtabList.get(section-1).locationList.get(j);
							}
							else if (str[1] == symtabList.get(section-1).symbolList.get(j)) {
								pc -= symtabList.get(section-1).locationList.get(j);
							}
						}
						// BUFEND-BUFFER�� ���� �� �� ���� ���, pc���� +3 �ڵ� �Ҵ� (��ɾ�� 000000)
						if (pc_orig == pc) {
							pc += 3;
						}
					}
					else if (token.operator.equals("RESW")) {
						pc += Integer.parseInt(token.operand[0]) * 3;
					}
					else if (token.operator.equals("RESB")) {
						pc += Integer.parseInt(token.operand[0]);
					}
				}
			}		
		}
		
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		
		System.out.println("\n<<< THIS IS SYMBOL >>>\n");
		
		File file = new File(fileName);
		FileWriter fw = null;

		try {
			for (int i = 0; i < symtabList.size(); i++) {
				// ������ �� ó�� ���� ������ ���� �����, �� �������ʹ� ������ ���� ����.
				if (i == 0) {
					fw = new FileWriter(file, false);
					System.out.print(symtabList.get(i).symbolList.get(0) + "\t" + Integer.toHexString(symtabList.get(i).locationList.get(0)) + "\n");
					fw.write(symtabList.get(i).symbolList.get(0));
					fw.flush();
					fw.write("\t");
					fw.flush();
					fw.write(Integer.toHexString(symtabList.get(i).locationList.get(0)));
					fw.flush();
					fw.write("\n");
					fw.flush();
					fw.close();
					
					fw = new FileWriter(file, true);
					for (int j = 1; j < symtabList.get(i).symbolList.size(); j++) {
						System.out.print(symtabList.get(i).symbolList.get(j) + "\t" + Integer.toHexString(symtabList.get(i).locationList.get(j)) + "\n");
						fw.write(symtabList.get(i).symbolList.get(j));
						fw.flush();
						fw.write("\t");
						fw.flush();
						fw.write(Integer.toHexString(symtabList.get(i).locationList.get(j)));
						fw.flush();
						fw.write("\n");
						fw.flush();
					}
				}
				else {
					for (int j = 0; j < symtabList.get(i).symbolList.size(); j++) {
						System.out.print(symtabList.get(i).symbolList.get(j) + "\t" + Integer.toHexString(symtabList.get(i).locationList.get(j)) + "\n");
						fw.write(symtabList.get(i).symbolList.get(j));					
						fw.flush();
						fw.write("\t");
						fw.flush();
						fw.write(Integer.toHexString(symtabList.get(i).locationList.get(j)));
						fw.flush();
						fw.write("\n");
						fw.flush();
					}
				}
				
				System.out.println();
				fw.write("\n");
				fw.flush();
				
				if (i == symtabList.size() - 1) {
					fw.close();
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			System.out.println("Error! PrintSymbolTable error!\n");
			System.exit(0);
		}
		
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		
		System.out.println("\n\n<<< THIS IS LITERAL >>>\n");
		
		File file = new File(fileName);
		FileWriter fw = null;

		try {
			for (int i = 0; i < literaltabList.size(); i++) {
				// ������ �� ó�� ���� ������ ���� �����, �� �������ʹ� ������ ���� ����.
				if (i == 0) {
					fw = new FileWriter(file, false);
					System.out.print(literaltabList.get(i).literalList.get(0) + "\t" + Integer.toHexString(literaltabList.get(i).locationList.get(0)) + "\n");
					fw.write(literaltabList.get(i).literalList.get(0));
					fw.flush();
					fw.write("\t");
					fw.flush();
					fw.write(Integer.toHexString(literaltabList.get(i).locationList.get(0)));
					fw.flush();
					fw.write("\n");
					fw.flush();
					fw.close();
					fw = new FileWriter(file, true);
					
					for (int j = 1; j < literaltabList.get(i).literalList.size(); j++) {
						System.out.print(literaltabList.get(i).literalList.get(j) + "\t" + Integer.toHexString(literaltabList.get(i).locationList.get(j)) + "\n");
						fw.write(literaltabList.get(i).literalList.get(j));
						fw.flush();
						fw.write("\t");
						fw.flush();
						fw.write(Integer.toHexString(literaltabList.get(i).locationList.get(j)));
						fw.flush();
						fw.write("\n");
						fw.flush();
					}
				}
				else {
					for (int j = 0; j < literaltabList.get(i).literalList.size(); j++) {
						System.out.print(literaltabList.get(i).literalList.get(j) + "\t" + Integer.toHexString(literaltabList.get(i).locationList.get(j)) + "\n");
						fw.write(literaltabList.get(i).literalList.get(j));					
						fw.flush();
						fw.write("\t");
						fw.flush();
						fw.write(Integer.toHexString(literaltabList.get(i).locationList.get(j)));
						fw.flush();
						fw.write("\n");
						fw.flush();
					}
				}

				if (i == symtabList.size() - 1) {
					fw.close();
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			System.out.println("Error! PrintLiteralTable error!\n");
			System.exit(0);
		}
		
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * pass2 ������ �����Ѵ�.
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		
		int now_section = 0;
		int start_section = 0;
		
		// ���� ���� ����� ��ȯ
		// �켱�� �� section���� ����
		/*
		System.out.println("TokenList ũ�� : " + TokenList.size());
		System.out.println("TokenList.get(0).tokenList.size ũ�� : " + TokenList.get(0).tokenList.size());
		System.out.println("TokenList.get(1).tokenList.size ũ�� : " + TokenList.get(1).tokenList.size());
		System.out.println("TokenList.get(2).tokenList.size ũ�� : " + TokenList.get(2).tokenList.size());
		*/
		for (int i = 0; i < TokenList.size(); i++) {
			// ���ο� section�� �������� �ʱ�ȭ
			EXTDEFS_all.add(new ArrayList<String>());
			EXTREFS_all.add(new ArrayList<String>());
			M_Line.add(new ArrayList<String>());
								
			// ���� ���� ��ȯ��
			if (TokenList.get(i).tokenList == null) continue;
			
			// ���� List �����ϱ� ����
			codeList.add("New List");
			codeList_loc.add("000000");
			
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				String op = TokenList.get(i).tokenList.get(j).operator;
				
				// operator�κ��� NULL�� ��� �ּ��̹Ƿ� ���� �������� ����
				if (op == null)
					continue;
				// START�ٵ� ���� �������� ����
				else if (op.equals("START")) 
					continue;
				else if (op.equals("CSECT")) {
					now_section++;
					continue;
				}
				// RSEW�� ������ ���
				else if (op.equals("RESW")) {
					codeList.add(""); // �� ������ ����, T���� ������ �� �ְ� �Ѵ�.
					codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
				}
				// EXTREF�� ������ ���
				else if (op.equals("EXTREF")) {
					// �̹� ,�� �������� ���� ����
					// String[] str = TokenList.get(i).tokenList.get(j).operand[0].split(",");
					for (int k = 0; k < TokenList.get(i).tokenList.get(j).operand.length; k++) {
						EXTREFS_all.get(now_section).add(TokenList.get(i).tokenList.get(j).operand[k]);
					}
				}
				// EXTDEF�� ������ ���
				else if (op.equals("EXTDEF")) {
					for (int k = 0; k < TokenList.get(i).tokenList.get(j).operand.length; k++) {
						EXTDEFS_all.get(now_section).add(TokenList.get(i).tokenList.get(j).operand[k]);
					}
				}
				// BYTE�� ������ ���
				else if (op.equals("BYTE")) {
					String[] str = TokenList.get(i).tokenList.get(j).operand[0].split("\'");
					if (str[0].equals("X")) {
					//	System.out.println(str[1]);
						codeList.add(str[1]);
						codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
					}
				}
				// WORD�� ������ ��� - COPY ��ɾ���� WORD BUFEND-BUFFER�� �ֱ� ������ �̸��� �����
				else if (op.equals("WORD")) {
					// �ڿ� �ִ� ������ ����� �Ұ����ϴٸ�, 000000�� �߰�
					// ��Ȳ�� ���� M������ �߰��ؾ� ��
				//	System.out.println("000000\n");
					codeList.add("000000");
					codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
					String str = "M";
					str += tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6);
					M_Line.get(now_section).add(str + "06+BUFEND");
					M_Line.get(now_section).add(str + "06-BUFFER");
				}
				// LTORG �Ǵ� END�� ������ ���
				else if (op.equals("LTORG") || op.equals("END")) {
					// ���� section�� �ش��ϴ� ������ ��
					for (int k = start_section; k <= now_section; k++) {
						for (int p = 0; p < literaltabList.get(k).literalList.size(); p++) {
							// EOF, 05�� ���� �͵��� ����ؾ� �ϴµ�
							// EOF�� ���� �ƽ�Ű�ڵ� ��ȯ��
							// 05�� ���� �׳� ����� �ϸ� �ȴ�.
							if (literaltabList.get(k).literalList.get(p).equals("05")) {
								codeList.add(literaltabList.get(k).literalList.get(p));
								codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
							//	System.out.println(literaltabList.get(k).literalList.get(p));
							}
							else if (literaltabList.get(k).literalList.get(p).equals("EOF")) {
								codeList.add("454F46");	// EOF�� �ƽ�Ű�ڵ�
								codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
							//	System.out.println("454F46\n");
							}
						}
					}
					start_section = now_section + 1;
				}
				
				// ���� ���������� ��ɾ�鿡 ���ؼ� ���� ���� ����
				// opcode�� �ش��ϴ� ��� ã��, �� �� ���ĵ� �ν��ؾ� �Ѵ�.
				// ���Ŀ� ���� ���� ���̰� �޶����� ����
				char[] machine = new char[9];
				if (instTable.instMap.containsKey(op)) {
					
				//	if (instTable.instMap.get(op).format == 2) machine = new char[5];
				//	else if (instTable.instMap.get(op).format == 3) machine = new char[7];
				//	else machine = new char[9];

					// ù���ڴ� ������
					machine[0] = instTable.instMap.get(op).opcode.charAt(0);
					
					// �ι�° ���ڸ� �˾Ƴ��� ��
					// �켱 format�� 2�̸�, �װſ� �°� ������ ����
					if (instTable.instMap.get(op).format == 2) {
						// format�� 2�� ���� opcode�� �״�� ������ �ȴ�. n i�� ����� �ʿ� ����
						machine[1] = instTable.instMap.get(op).opcode.charAt(1);
						
						// �������Ϳ� �´� ���� ��ȣ�� �ٿ��ָ� �ȴ�.
						if (TokenList.get(i).tokenList.get(j).operand.length < 1) {
							machine[2] = '0';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[0].equals("X")) {
							machine[2] = '1';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[0].equals("A")) {
							machine[2] = '0';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[0].equals("S")) {
							machine[2] = '4';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[0].equals("T")) {
							machine[2] = '5';
						}
												
						if (TokenList.get(i).tokenList.get(j).operand.length < 2) {
							machine[3] = '0';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[1].equals("X")) {
							machine[3] = '1';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[1].equals("A")) {
							machine[3] = '0';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[1].equals("S")) {
							machine[3] = '4';
						}
						else if (TokenList.get(i).tokenList.get(j).operand[1].equals("T")) {
							machine[3] = '5';
						}
						
						char machine_2[] = new char[4];
						machine_2[0] = machine[0];
						machine_2[1] = machine[1];
						machine_2[2] = machine[2];
						machine_2[3] = machine[3];
						String str = String.valueOf(machine_2);
					//	System.out.println(str);
						codeList.add(str);
						codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
						continue;						
					}
					else {
						// format�� 3 �Ǵ� 4�� ���, ���� �ڵ� 3��° �ڸ������� ����� �����ϴ�.
			            // �ι�° ���ڴ� _ _ n i �ε�, �켱 ���� �� ���ڴ� opcode���� �˾Ƴ��� ��
			            // 1. first�� second���� ä����.
			            int first_1 = 0;      // 2^3
			            int second_1 = 0;     // 2^2
			            int third_1 = 0;      // 2^1
			            int fourth_1 = 0;     // 2^0
			            int opcode1;

			            switch (instTable.instMap.get(op).opcode.charAt(1)) {
			            case 'A': opcode1 = 10; break;
			            case 'B': opcode1 = 11; break;
			            case 'C': opcode1 = 12; break;
			            case 'D': opcode1 = 13; break;
			            case 'E': opcode1 = 14; break;
			            case 'F': opcode1 = 15; break;
			            default:
			                opcode1 = (int)(instTable.instMap.get(op).opcode.charAt(1) - '0'); break;
			            }

			            if (opcode1 - 8 >= 0) {
			                opcode1 -= 8;
			                first_1 = 1;
			            }
			            else {
			                first_1 = 0;
			            }

			            if (opcode1 - 4 >= 0) {
			                opcode1 -= 4;
			                second_1 = 1;
			            }
			            else {
			                second_1 = 0;
			            }
			            
			            // nixbpe�� ���ؼ�, �� ������ �� ��°�� �� �� ����
			            if (TokenList.get(i).tokenList.get(j).getFlag(32) > 0) {
			                third_1 = 1;
			            }
			            
			            if (TokenList.get(i).tokenList.get(j).getFlag(16) > 0) {
			                fourth_1 = 1;
			            }
			            
			            // �� ��° ���� ���߱�
			            opcode1 = first_1 * 8 + second_1 * 4 + third_1 * 2 + fourth_1 * 1;
			            switch (opcode1) {
			            case 15: machine[1] = 'F'; break;
			            case 14: machine[1] = 'E'; break;
			            case 13: machine[1] = 'D'; break;
			            case 12: machine[1] = 'C'; break;
			            case 11: machine[1] = 'B'; break;
			            case 10: machine[1] = 'A'; break;
			            case 9:  machine[1] = '9'; break;
			            case 8:  machine[1] = '8'; break;
			            case 7:  machine[1] = '7'; break;
			            case 6:  machine[1] = '6'; break;
			            case 5:  machine[1] = '5'; break;
			            case 4:  machine[1] = '4'; break;
			            case 3:  machine[1] = '3'; break;
			            case 2:  machine[1] = '2'; break;
			            case 1:  machine[1] = '1'; break;
			            case 0:  machine[1] = '0'; break;
			            default: machine[1] = '0'; break;
			            }
			            
			            // Ư���� ��� ���� ó�� - RSUB
			            if (TokenList.get(i).tokenList.get(j).operator.equals("RSUB")) {
			            	machine[1] = 'F'; // RSUB�� ��� 2��° �ڸ� ��ɾ C�� �ƴ϶� F�̴�.
			                machine[2] = '0'; 
			                machine[3] = '0';
			                machine[4] = '0';
			                machine[5] = '0';
							char machine_2[] = new char[6];
							machine_2[0] = machine[0];
							machine_2[1] = machine[1];
							machine_2[2] = machine[2];
							machine_2[3] = machine[3];		
							machine_2[4] = machine[4];
							machine_2[5] = machine[5];
			                String str = String.valueOf(machine_2);
			                codeList.add(str);
			                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
			            //  System.out.println(str);
			                continue;
			            }
			            
			            // 2. xbpe �ν�
			            int first_2 = 0;      // 2^3 : x
			            int second_2 = 0;     // 2^2 : b
			            int third_2 = 0;      // 2^1 : p
			            int fourth_2 = 0;     // 2^0 : e

			            if ((TokenList.get(i).tokenList.get(j).getFlag(8) > 0) ) {
			            	first_2 = 1;
			            }
			            if ((TokenList.get(i).tokenList.get(j).getFlag(4) > 0) ) {
			            	second_2 = 1;
			            }
			            if ((TokenList.get(i).tokenList.get(j).getFlag(2) > 0) ) {
			            	third_2 = 1;
			            }
			            if ((TokenList.get(i).tokenList.get(j).getFlag(1) > 0) ) {
			            	fourth_2 = 1;
			            }
			            
			            opcode1 = first_2 * 8 + second_2 * 4 + third_2 * 2 + fourth_2 * 1;

			            // �� ��° ���� ���߱�
			            switch (opcode1) {
			            case 15: machine[2] = 'F'; break;
			            case 14: machine[2] = 'E'; break;
			            case 13: machine[2] = 'D'; break;
			            case 12: machine[2] = 'C'; break;
			            case 11: machine[2] = 'B'; break;
			            case 10: machine[2] = 'A'; break;
			            case 9:  machine[2] = '9'; break;
			            case 8:  machine[2] = '8'; break;
			            case 7:  machine[2] = '7'; break;
			            case 6:  machine[2] = '6'; break;
			            case 5:  machine[2] = '5'; break;
			            case 4:  machine[2] = '4'; break;
			            case 3:  machine[2] = '3'; break;
			            case 2:  machine[2] = '2'; break;
			            case 1:  machine[2] = '1'; break;
			            case 0:  machine[2] = '0'; break;
			            default: machine[2] = '0'; break;
			            }
			            
			            // b : base-relative�� ���, ���� ��
			            // p : pc-relative�� ���, ���� ��
			            // �ּҸ� �˾Ƴ�����
			            
			            // 3. PC-relative vs Base-relative
			            // �켱 pc-relative�� �Ǵ��� ���θ� ������� �Ѵ�. �켱 �Ϲ����� ������
			            
			            // n : Indirect Addressing(���� �ּ� ����)
			            if ((TokenList.get(i).tokenList.get(j).getFlag(32) > 0) && TokenList.get(i).tokenList.get(j).getFlag(16) == 0) {
			                // @ �ڿ� �ִ� �ּ��� ���� target�� ��
			                // �� �� �ڸ����� @ �ڿ� ������ ���ڸ� ������� ��
			                // @ �ڿ� ������ ���� ��
			                String[] str = TokenList.get(i).tokenList.get(j).operand[0].split("@");

			                // str[1]�� ������ ����, �̰��� �ּҸ� ã�ƾ� �ϴµ� �̶� sym_table�� �̿���
			                int addr = 0;
			                for (int p = 0; p < symtabList.get(i).symbolList.size(); p++) {
			                	if (symtabList.get(i).symbolList.get(p).equals(str[1])) {
			                		addr = symtabList.get(i).locationList.get(p);
			                	}
			                }
			                
			                // ���� �ּҿ��� PC���� ���� ��
			                addr -= TokenList.get(i).tokenList.get(j+1).location;
			                
			                // ���� addr�� 16���� ���ڿ��� ��ȯ ��, 
			                // ������ 3�̸� 3����, ������ 4�̸� 4���� ������ ��
			                String a16 = Integer.toHexString(addr);
			                char[] arrays = a16.toCharArray();
			                char[] ans = new char[5];
			                if (instTable.instMap.get(op).format == 3) {
			                	ans[3] = '\0';
			                	int p = 0;
			                	for (p = 0; p < arrays.length; p++)
			                		ans[2-p] = arrays[arrays.length - 1 - p];
			                	if (p <= 2) {
			                		while(p <= 2) {
			                			ans[2-p] = '0';
			                			p++;
			                		}
			                	}
			                }
			                else {
			                	ans[4] = '\0';
			                	int p = 0;
			                	for (p = 0; p < arrays.length; p++)
			                		ans[3-p] = arrays[arrays.length - 1 - p];
			                	if (p <= 3) {
			                		while(p <= 3) {
			                			ans[3-p] = '0';
			                			p++;
			                		}
			                	}
			                }
			                machine[3] = ans[0];
			                machine[4] = ans[1];
			                machine[5] = ans[2];
			                machine[6] = ans[3];
		                	machine[7] = ans[4];

			                char machine_2[];
			                if (instTable.instMap.get(op).format == 3) {
			                	machine_2 = new char[6];
			                	machine_2[0] = machine[0];
			                	machine_2[1] = machine[1];
			                	machine_2[2] = machine[2];
			                	machine_2[3] = machine[3];
			                	machine_2[4] = machine[4];
			                	machine_2[5] = machine[5];
				                String strs = String.valueOf(machine_2);
				                codeList.add(strs);
				                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
				            //  System.out.println(strs);
			                }
			                else if (instTable.instMap.get(op).format == 4) {
			                	machine_2 = new char[8];
			                	machine_2[0] = machine[0];
			                	machine_2[1] = machine[1];
			                	machine_2[2] = machine[2];
			                	machine_2[3] = machine[3];
			                	machine_2[4] = machine[4];
			                	machine_2[5] = machine[5];
			                	machine_2[6] = machine[6];
			                	machine_2[7] = machine[7];
				                String strs = String.valueOf(machine_2);
				                codeList.add(strs);
				                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
				            //  System.out.println(strs);
			                }
			                continue;
			            }
			            // i : Immediate Addressing(��� �ּ� ����)
			            if ((TokenList.get(i).tokenList.get(j).getFlag(32) == 0) && TokenList.get(i).tokenList.get(j).getFlag(16) > 0) {
			                // �� �� �ڸ����� # �ڿ� ������ ���ڸ� ������� ��
			                // # �ڿ� ������ ���� ��
			                String[] str = TokenList.get(i).tokenList.get(j).operand[0].split("#");

			                // ���� addr�� 16���� ���ڿ��� ��ȯ ��, 
			                // ������ 3�̸� 3����, ������ 4�̸� 4���� ������ ��

			                char[] arrays = str[1].toCharArray();
			                char[] ans = new char[5];
			                if (instTable.instMap.get(op).format == 3) {
			                	ans[3] = '\0';
			                	int p = 0;
			                	for (p = 0; p < arrays.length; p++)
			                		ans[2-p] = arrays[arrays.length - 1 - p];
			                	if (p <= 2) {
			                		while(p <= 2) {
			                			ans[2-p] = '0';
			                			p++;
			                		}
			                	}
			                }
			                else {
			                	ans[4] = '\0';
			                	int p = 0;
			                	for (p = 0; p < arrays.length; p++)
			                		ans[3-p] = arrays[arrays.length - 1 - p];
			                	if (p <= 3) {
			                		while(p <= 3) {
			                			ans[3-p] = '0';
			                			p++;
			                		}
			                	}
			                }
			                machine[3] = ans[0];
			                machine[4] = ans[1];
			                machine[5] = ans[2];
			                machine[6] = ans[3];
			                machine[7] = ans[4];
			                
			                char machine_2[];
			                if (instTable.instMap.get(op).format == 3) {
			                	machine_2 = new char[6];
			                	machine_2[0] = machine[0];
			                	machine_2[1] = machine[1];
			                	machine_2[2] = machine[2];
			                	machine_2[3] = machine[3];
			                	machine_2[4] = machine[4];
			                	machine_2[5] = machine[5];
				                String strs = String.valueOf(machine_2);
				                codeList.add(strs);
				                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
				            //    System.out.println(strs);
			                }
			                else if (instTable.instMap.get(op).format == 4) {
			                	machine_2 = new char[8];
			                	machine_2[0] = machine[0];
			                	machine_2[1] = machine[1];
			                	machine_2[2] = machine[2];
			                	machine_2[3] = machine[3];
			                	machine_2[4] = machine[4];
			                	machine_2[5] = machine[5];
			                	machine_2[6] = machine[6];
			                	machine_2[7] = machine[7];
				                String strs = String.valueOf(machine_2);
				                codeList.add(strs);
				                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
				            //    System.out.println(strs);
			                }
			                continue;     
			            }
			            else {
			            	// �Ϲ����� ���

			                int target = -1;
			                int displacement = 0;
			                                          
			                // �켱 operand�� EXTREFS(�ܺο��� ���̴� ��)������ ����. �� ���,���� �ּҰ��� ���� 0�� �ȴ�.
			                int sw = 0;
			                for (int pp = 0; pp < EXTREFS_all.get(now_section).size(); pp++) {
			                    if (EXTREFS_all.get(now_section).get(pp).equals(TokenList.get(i).tokenList.get(j).operand[0])) {
			                    	sw = 1;
			                    	break;
			                    }
			                }
			                
			                if (sw == 0) {
			                	// symbolList�� literalList�� Ȯ���ؼ� target �ּҸ� Ȯ������.
			    
			                	for (int pp = 0; pp < symtabList.get(now_section).symbolList.size(); pp++) {
			                		if (symtabList.get(now_section).symbolList.get(pp).equals(TokenList.get(i).tokenList.get(j).operand[0])) {
			                			target = symtabList.get(now_section).locationList.get(pp); // target�� ��� ���� PC �ּ�
			                			break;
			                		}
			                	}
			                	
			                	if (target == -1) {
			                	// �� ��� literal������ Ȯ���Ѵ�.	
			                		String temp_part[] = TokenList.get(i).tokenList.get(j).operand[0].split("\'");	
			                		if (temp_part.length > 1) {
					                	for (int pp = 0; pp < literaltabList.get(now_section).literalList.size(); pp++) {
					                		if (literaltabList.get(now_section).literalList.get(pp).equals(temp_part[1])) {
					                			target = literaltabList.get(now_section).locationList.get(pp);
					                			break;
					                		}
				                		}
			                		}
			                	}
			                	
			                	// symbolList�� literalList �� �� Ȯ���ߴµ��� �ұ��ϰ� target�� -1�̸� ��
			                	if (target == -1) {
			                		continue;
			                	}
			                	else if (target == 0) {
			                		displacement = 0;
			                	}
			                	else {
			                        if (target > TokenList.get(i).tokenList.get(j+1).location)
			                            displacement = target - TokenList.get(i).tokenList.get(j+1).location;
			                        else {
			                            if (instTable.instMap.get(op).format == 3)
			                                displacement = 16 * 16 * 16 * 16 - TokenList.get(i).tokenList.get(j+1).location + target;
			                            else if (instTable.instMap.get(op).format == 4)
			                                displacement = 16 * 16 * 16 * 16 * 16 - TokenList.get(i).tokenList.get(j+1).location + target;
			                        }

			                        // pc-relative���� Ȯ��
			                        if (Math.abs(target - TokenList.get(i).tokenList.get(j).location) >= 16*16*16) {
			                        	continue; // pc-relative�� �ƴ� (Base-relative)
			                        }
			                        else {
			                        	
			                        	char hex[] = new char[10];
			                        	for (int ppp = 0; ppp < 9; ppp++) {
			                        		hex[ppp] = '0';
			                        	}
			                            int pos = 0;

			                            while (true) {
			                                int mod = displacement % 16; // 16���� �������� �� ������
			                                if (mod < 10) {
			                                    // ���� 0�� ASCII �ڵ� �� 48 + ������
			                                    hex[pos] = (char) ('0' + mod);
			                                }
			                                else {
			                                    // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
			                                    hex[pos] = (char) ('A' + mod - 10);
			                                }

			                                displacement = displacement / 16;

			                                pos++;

			                                if (displacement == 0) break;
			                            }

			                            if (instTable.instMap.get(op).format == 3) {
			                                // ���������� ���� ���߱�
			                                machine[6] = '\0';
			                                machine[5] = hex[0];
			                                machine[4] = hex[1];
			                                machine[3] = hex[2];
			                            }
			                            else if (instTable.instMap.get(op).format == 4) {
			                                // ���������� ���� ���߱�
			                                machine[7] = '\0';
			                                machine[6] = hex[0];
			                                machine[5] = hex[1];
			                                machine[4] = hex[2];
			                                machine[3] = hex[3];
			                            }
			                        }
					                String strs = "";
					                for (int ppp = 0; machine[ppp] != '\0'; ppp++)
					                	strs += String.valueOf(machine[ppp]);
					                codeList.add(strs);
					                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
					            //    System.out.println(strs);
					                continue;     
			                	}
			                }
			                // operand�� EXTREFS(�ܺο��� ���̴� ��)�� ���, ���� �ּҰ��� ���� 0�� �ȴ�.
			                // M_Line���� �ִ´�.
			                else {
				                char machine_2[];
				                if (instTable.instMap.get(op).format == 3) {
				                	machine_2 = new char[6];
				                	machine_2[0] = machine[0];
				                	machine_2[1] = machine[1];
				                	machine_2[2] = machine[2];
				                	machine_2[3] = '0';
				                	machine_2[4] = '0';
				                	machine_2[5] = '0';
					                String strs = String.valueOf(machine_2);
					                codeList.add(strs);
					                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
					            //    System.out.println(strs);
					                // format�� 3�� ��� (�ּ� + 1)�������� 3��
					                strs = tenTosixteen(TokenList.get(i).tokenList.get(j).location + 1, 6);
						            strs += "03+";
						            strs += TokenList.get(i).tokenList.get(j).operand[0];
						            M_Line.get(now_section).add("M" + strs);
					            }
				                else if (instTable.instMap.get(op).format == 4) {
				                	machine_2 = new char[8];
				                	machine_2[0] = machine[0];
				                	machine_2[1] = machine[1];
				                	machine_2[2] = machine[2];
				                	machine_2[3] = '0';
				                	machine_2[4] = '0';
				                	machine_2[5] = '0';
				                	machine_2[6] = '0';
				                	machine_2[7] = '0';
					                String strs = String.valueOf(machine_2);
					                codeList.add(strs);
					                codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
					             //   System.out.println(strs);
					                // format�� 4�� ��� (�ּ� + 1)�������� 5��
					                strs = tenTosixteen(TokenList.get(i).tokenList.get(j).location + 1, 6);
						            strs += "05+";
						            strs += TokenList.get(i).tokenList.get(j).operand[0];
						            M_Line.get(now_section).add("M" + strs);
				                }
				               	continue;     
			                }	            	
			            }
					}
					
				}
				// �����ϴ� opcode�� ���� ��� ��� ���� �� ������ continue
				else {
					continue;
				}
			}
		}
	}
	
	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {

		// System.out.println("ũ�� : " + codeList.size());
		/*
		for (int i = 0; i < codeList.size(); i++) {
			System.out.println(codeList.get(i));
		}	
		*/
	
		// TODO Auto-generated method stub
		
		System.out.println("\n\n<<< THIS IS OBJECTCODE >>>\n");
		
		File file = new File(fileName);
		FileWriter fw = null;

		try {
			int now_section = -1;
			String str_T = "T";
			String str_M = "M";
			String str_E = "E";
			String str2 = "";
			for (int i = 0; i < codeList.size(); i++) {
				
				if (i == 0) {
					fw = new FileWriter(file, false);
					fw.write("");
					fw.flush();
					fw.close();

					fw = new FileWriter(file, true);
					fw.write("");
					fw.flush();
				}
				
				/******************************************* H *****************************************/
				if (codeList.get(i).equals("New List")) {
					if (i != 0) {
						str_T += tenTosixteen(str2.length() / 2, 2); // ��ɾ��� �� ����
						str_T += str2;							 // ��ɾ��
						System.out.println(str_T);
						fw.write(str_T + "\n");
						fw.flush();
						str_T = "T";
						str2 = "";
						
						// M����, E������ �����.
						
						// M����
						for (int j = 0; j < M_Line.get(now_section).size(); j++) {
							System.out.println(M_Line.get(now_section).get(j));
							fw.write(M_Line.get(now_section).get(j) + "\n");
							fw.flush();
						}
						
						// E����
						if (now_section == 0) {
							System.out.println(str_E + "000000\n");
							fw.write(str_E + "000000\n\n");
							fw.flush();

						}
						else {
							System.out.println(str_E + "\n");
							fw.write(str_E + "\n\n");
							fw.flush();
						}
						
					}
					
					
					now_section++;
					String str = "H" + symtabList.get(now_section).symbolList.get(0);
					for (int j = str.length(); j < 7; j++) {
						str += " ";
					}
					str += "000000"; // �����ּ� �ֱ�
					str += tenTosixteen(lengthList.get(now_section), 6); // ������ �� ���� �ֱ�
					System.out.println(str);
					fw.write(str + "\n");
					fw.flush();

					
					if (EXTDEFS_all.get(now_section).size() != 0) {
						// ���⿡�� D, R�� ����
						str = "D";
						for (int j = 0; j < EXTDEFS_all.get(now_section).size(); j++) {
							 // ���� �̸�
							str += EXTDEFS_all.get(now_section).get(j);
							if (str.length() < 1 + 6 * (j + 1)) {
								str += " ";
							}
							
							// ���� �ּ� - �̰Ŵ� ��� ã��? -> symtabList���� ã��!
							for (int k = 0; k < symtabList.get(now_section).symbolList.size(); k++) {
								if (EXTDEFS_all.get(now_section).get(j).equals(symtabList.get(now_section).symbolList.get(k))) {
									str2 = tenTosixteen(symtabList.get(now_section).locationList.get(k), 6);
									str += str2;
								}
							}
						}
						System.out.println(str);
						fw.write(str + "\n");
						fw.flush();
					}
					
					if (EXTREFS_all.get(now_section).size() != 0) {
						// ���⿡�� D, R�� ����
						str = "R";
						for (int j = 0; j < EXTREFS_all.get(now_section).size(); j++) {
							 // ���� �̸�
							str += EXTREFS_all.get(now_section).get(j);
							if (str.length() < 1 + 6 * (j + 1)) {
								str += " ";
							}
						}
						System.out.println(str);
						fw.write(str + "\n");
						fw.flush();
					}
					
				}
				else {
					// T�κ� ������
					// T + 6�ڸ�(��������) + 2�ڸ�(����) + 58�ڸ�(��ɾ� ����, 1D = 29, 29*2������ 16���� ��ɾ� ���� ����)
					if (str_T.equals("T")) {
						// ���������� ��������
						str_T += codeList_loc.get(i);
						str2 = ""; // str2 �ʱ�ȭ
					}
					if (str2.length() + codeList.get(i).length() <= 58 && !(codeList.get(i).equals(""))) {
						str2 += codeList.get(i);
					}
					else {
						// �̶� ���ؾ� ��
						if (str2.length() != 0) {
							str_T += tenTosixteen(str2.length() / 2, 2); // ��ɾ��� �� ����
							str_T += str2;							 // ��ɾ��
							System.out.println(str_T);
							fw.write(str_T + "\n");
							fw.flush();
						}
						
						str_T = "T";
						if (!(codeList.get(i).equals(""))) {
							str_T += codeList_loc.get(i); // ��������
							str2 = codeList.get(i); // str2 �ʱ�ȭ
						}
					}
				}	
			}
			
			// �� ������ ���� �����ش�.
			str_T += tenTosixteen(str2.length() / 2, 2); // ��ɾ��� �� ����
			str_T += str2;							 	 // ��ɾ��
			System.out.println(str_T);
			fw.write(str_T + "\n");
			fw.flush();
			
			// M����
			for (int j = 0; j < M_Line.get(now_section).size(); j++) {
				System.out.println(M_Line.get(now_section).get(j));
				fw.write(M_Line.get(now_section).get(j) + "\n");
				fw.flush();
			}
			// E����
			System.out.println("E\n");
			fw.write("E" + "\n\n");
			fw.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		//	System.out.println("Error! PrintSymbolTable printing!\n");
			System.exit(0);
		}
		
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	// 10���� �������� n�ڸ��� 16���� String���� �������ִ� �Լ� 
	private String tenTosixteen(int ten_int, int n) {
		String sixteen_str = "";
		
		// ������ �ѱ���
        char cs_full_length[] = new char[n]; // �� ����

        // 10������ 16������ ��ȯ
        int temp = ten_int;
        int pos = 0;
        char hex[] = new char[n];

        while (true) {
            int mod = temp % 16; // 16���� �������� �� ������
            if (mod < 10) {
                // ���� 0�� ASCII �ڵ� �� 48 + ������
                hex[pos] = (char)(48 + mod);
            }
            else {
                // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
                hex[pos] = (char)(65 + mod - 10);
            }

            temp = temp / 16;
            pos++;
            if (temp == 0) break;
        }
        // ���ڿ��� ���·� ����
        for (int j = 0; j < pos; j++) {
            cs_full_length[n - 1 - j] = hex[j];
        }
        for (int j = n-1-pos; j > -1; j--) {
            cs_full_length[j] = '0';
        }
        sixteen_str += String.copyValueOf(cs_full_length);
		return sixteen_str;
	}
}
