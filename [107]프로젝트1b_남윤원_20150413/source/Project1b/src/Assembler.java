import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 파일 입출력과 예외 처리를 위한 라이브러리들
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;

// 수학 라이브러리
import java.lang.Math;

/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 *  개선 방법 : 예외처리 문구를 추가하여 더 엄격하게 검사를 하는 것이 좋아 보입니다. 보고서 결론 뒷부분에 이 내용 첨부했습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	ArrayList<String> codeList_loc;

	/** program counter값을 저장 */
	/** 각 section별로 프로그램의 length를 저장 */
	ArrayList<Integer> lengthList;
	static int pc = 0;
	
	/** section의 개수를 저장 */
	static int section = 0;

	/** EXTDEF와 EXTREF 변수명을 저장 */
	ArrayList<ArrayList<String>> EXTDEFS_all;
	ArrayList<ArrayList<String>> EXTREFS_all;

	/** Object Program의 M라인을 출력하기 위해, pass2 작업을 하면서 같이 저장 */
	ArrayList<ArrayList<String>> M_Line;

	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
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
	 * 어셈블러의 메인 루틴
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
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 * 
	 * 
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		
		// 한줄씩 읽어서 저장해야 함
		// inputFile이 없으면 에러를 냄 : try-catch 예외 처리
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
			// 예외 처리 : 초기화에 실패하였다는 문구 출력
			System.out.println("input.txt doesn't find! Initialization failed.\n");
			System.exit(0);

		}
	}

	/** 
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		
		// 총 lineList 개수만큼 for문을 돌려야 함
		// 프로그램의 section별로 하나씩 SymbolTable과 TokenTable을 선언해야 함
		
		pc = 0;
		section = 0;
		
		ArrayList<String> Lit_temp = new ArrayList<String>();
		
		for (int i = 0; i < lineList.size(); i++) {
			Token token = new Token(lineList.get(i));
			
			// 일단 한 개의 SymbolTable과 TokenTable은 만들어 놓음
			if (i == 0) {
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(section), instTable));
				TokenList.get(section).literalTab = literaltabList.get(section);
				section++;
			}
			// section이 바뀔 때마다 하나 만들어 놓음
			else if (token.operator != null && token.operator.equals("CSECT")) {	
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(section), instTable));
				TokenList.get(section).literalTab = literaltabList.get(section);
				section++;
				lengthList.add(pc); // 프로그램의 총 길이를 저장
				pc = 0;				// pc값을 다시 0으로 설정
			}
			
			// 토큰분리한 것을 TokenList에 저장함
			TokenList.get(section - 1).putToken(lineList.get(i));
			
			// label이 null이 아닐 경우 symboltable에 저장
			if (token.label != null) {
				// symtabList.get(section - 1).putSymbol(token.label, pc);
				symtabList.get(section - 1).symbolList.add(token.label);
				if (!token.operator.equals("EQU") || (token.operand != null && token.operand[0].equals("*"))) {
					symtabList.get(section - 1).locationList.add(pc);
				}
				else if (token.operator.equals("EQU")) {
					// EQU일 경우만 따로 처리
					int loc = 0;
					String str[] = token.operand[0].split("-");
					// str[0]과 str[1]의 주소를 찾아서 빼 주어야 함
					for (int j = 0; j < symtabList.get(section-1).symbolList.size(); j++) {
						if (str[0].equals(symtabList.get(section-1).symbolList.get(j))) {
							loc += symtabList.get(section-1).locationList.get(j);
						}
						else if (str[1].equals(symtabList.get(section-1).symbolList.get(j))) {
								loc -= symtabList.get(section-1).locationList.get(j);
						}
					}
				//	System.out.println("loc의 값 : " + loc);
					symtabList.get(section - 1).locationList.add(loc);
				}
			}
			
			// operand[0]의 시작이 =일 경우 보관해놓았다가, 
			// LTORG나 END가 나오면 그때 literaltable에 저장
			
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
			
			// operator가 null이 아닐 경우 주소값(pc값)을 증가시킴
			if (token.operator != null) {
				// 값이 있다면 더함
				if (instTable.instMap.containsKey(token.operator)) {
					pc += instTable.instMap.get(token.operator).format;
				}
				else {
					// 이 경우는 START, LTORG, END, USE, EQU, ...
					// BYTE, WORD와 같은 경우도 생각해야 함. 여기서 처리하자!
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
						// str[0]과 str[1]의 주소를 찾아서 빼 주어야 함
						for (int j = 0; j < symtabList.get(section-1).symbolList.size(); j++) {
							if (str[0] == symtabList.get(section-1).symbolList.get(j)) {
								pc += symtabList.get(section-1).locationList.get(j);
							}
							else if (str[1] == symtabList.get(section-1).symbolList.get(j)) {
								pc -= symtabList.get(section-1).locationList.get(j);
							}
						}
						// BUFEND-BUFFER의 값을 알 수 없는 경우, pc값은 +3 자동 할당 (명령어는 000000)
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
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		
		System.out.println("\n<<< THIS IS SYMBOL >>>\n");
		
		File file = new File(fileName);
		FileWriter fw = null;

		try {
			for (int i = 0; i < symtabList.size(); i++) {
				// 파일의 맨 처음 쓰기 시작할 때는 덮어쓰고, 그 다음부터는 파일의 끝에 쓴다.
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
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		
		System.out.println("\n\n<<< THIS IS LITERAL >>>\n");
		
		File file = new File(fileName);
		FileWriter fw = null;

		try {
			for (int i = 0; i < literaltabList.size(); i++) {
				// 파일의 맨 처음 쓰기 시작할 때는 덮어쓰고, 그 다음부터는 파일의 끝에 쓴다.
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
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		
		int now_section = 0;
		int start_section = 0;
		
		// 한줄 한줄 기계어로 변환
		// 우선은 각 section별로 있음
		/*
		System.out.println("TokenList 크기 : " + TokenList.size());
		System.out.println("TokenList.get(0).tokenList.size 크기 : " + TokenList.get(0).tokenList.size());
		System.out.println("TokenList.get(1).tokenList.size 크기 : " + TokenList.get(1).tokenList.size());
		System.out.println("TokenList.get(2).tokenList.size 크기 : " + TokenList.get(2).tokenList.size());
		*/
		for (int i = 0; i < TokenList.size(); i++) {
			// 새로운 section에 들어왔으면 초기화
			EXTDEFS_all.add(new ArrayList<String>());
			EXTREFS_all.add(new ArrayList<String>());
			M_Line.add(new ArrayList<String>());
								
			// 한줄 한줄 변환함
			if (TokenList.get(i).tokenList == null) continue;
			
			// 기계어 List 구분하기 위함
			codeList.add("New List");
			codeList_loc.add("000000");
			
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				String op = TokenList.get(i).tokenList.get(j).operator;
				
				// operator부분이 NULL일 경우 주석이므로 기계어 생성하지 않음
				if (op == null)
					continue;
				// START줄도 기계어 생성하지 않음
				else if (op.equals("START")) 
					continue;
				else if (op.equals("CSECT")) {
					now_section++;
					continue;
				}
				// RSEW가 나왔을 경우
				else if (op.equals("RESW")) {
					codeList.add(""); // 빈 공간을 만들어서, T줄을 구분할 수 있게 한다.
					codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
				}
				// EXTREF가 나왔을 경우
				else if (op.equals("EXTREF")) {
					// 이미 ,을 기준으로 나눠 놓음
					// String[] str = TokenList.get(i).tokenList.get(j).operand[0].split(",");
					for (int k = 0; k < TokenList.get(i).tokenList.get(j).operand.length; k++) {
						EXTREFS_all.get(now_section).add(TokenList.get(i).tokenList.get(j).operand[k]);
					}
				}
				// EXTDEF가 나왔을 경우
				else if (op.equals("EXTDEF")) {
					for (int k = 0; k < TokenList.get(i).tokenList.get(j).operand.length; k++) {
						EXTDEFS_all.get(now_section).add(TokenList.get(i).tokenList.get(j).operand[k]);
					}
				}
				// BYTE가 나왔을 경우
				else if (op.equals("BYTE")) {
					String[] str = TokenList.get(i).tokenList.get(j).operand[0].split("\'");
					if (str[0].equals("X")) {
					//	System.out.println(str[1]);
						codeList.add(str[1]);
						codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
					}
				}
				// WORD가 나왔을 경우 - COPY 명령어에서는 WORD BUFEND-BUFFER만 있기 때문에 이만을 고려함
				else if (op.equals("WORD")) {
					// 뒤에 있는 영역의 계산이 불가능하다면, 000000을 추가
					// 상황에 따라 M라인을 추가해야 함
				//	System.out.println("000000\n");
					codeList.add("000000");
					codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
					String str = "M";
					str += tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6);
					M_Line.get(now_section).add(str + "06+BUFEND");
					M_Line.get(now_section).add(str + "06-BUFFER");
				}
				// LTORG 또는 END가 나왔을 경우
				else if (op.equals("LTORG") || op.equals("END")) {
					// 현재 section에 해당하는 곳까지 봄
					for (int k = start_section; k <= now_section; k++) {
						for (int p = 0; p < literaltabList.get(k).literalList.size(); p++) {
							// EOF, 05와 같은 것들을 출력해야 하는데
							// EOF의 경우는 아스키코드 변환을
							// 05의 경우는 그냥 출력을 하면 된다.
							if (literaltabList.get(k).literalList.get(p).equals("05")) {
								codeList.add(literaltabList.get(k).literalList.get(p));
								codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
							//	System.out.println(literaltabList.get(k).literalList.get(p));
							}
							else if (literaltabList.get(k).literalList.get(p).equals("EOF")) {
								codeList.add("454F46");	// EOF의 아스키코드
								codeList_loc.add(tenTosixteen(TokenList.get(i).tokenList.get(j).location, 6));
							//	System.out.println("454F46\n");
							}
						}
					}
					start_section = now_section + 1;
				}
				
				// 이제 본격적으로 명령어들에 대해서 기계어 생성 시작
				// opcode에 해당하는 기계어를 찾자, 이 때 형식도 인식해야 한다.
				// 형식에 따라서 기계어 길이가 달라지기 때문
				char[] machine = new char[9];
				if (instTable.instMap.containsKey(op)) {
					
				//	if (instTable.instMap.get(op).format == 2) machine = new char[5];
				//	else if (instTable.instMap.get(op).format == 3) machine = new char[7];
				//	else machine = new char[9];

					// 첫글자는 정해짐
					machine[0] = instTable.instMap.get(op).opcode.charAt(0);
					
					// 두번째 글자를 알아내야 함
					// 우선 format이 2이면, 그거에 맞게 대응해 보자
					if (instTable.instMap.get(op).format == 2) {
						// format이 2일 때는 opcode를 그대로 넣으면 된다. n i를 고려할 필요 없음
						machine[1] = instTable.instMap.get(op).opcode.charAt(1);
						
						// 레지스터에 맞는 기계어 번호를 붙여주면 된다.
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
						// format이 3 또는 4인 경우, 기계어 코드 3번째 자리까지는 방식이 동일하다.
			            // 두번째 글자는 _ _ n i 인데, 우선 앞의 두 글자는 opcode에서 알아내야 함
			            // 1. first와 second부터 채우자.
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
			            
			            // nixbpe를 통해서, 세 번쨰와 네 번째를 알 수 있음
			            if (TokenList.get(i).tokenList.get(j).getFlag(32) > 0) {
			                third_1 = 1;
			            }
			            
			            if (TokenList.get(i).tokenList.get(j).getFlag(16) > 0) {
			                fourth_1 = 1;
			            }
			            
			            // 두 번째 숫자 맞추기
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
			            
			            // 특별한 경우 예외 처리 - RSUB
			            if (TokenList.get(i).tokenList.get(j).operator.equals("RSUB")) {
			            	machine[1] = 'F'; // RSUB의 경우 2번째 자리 명령어가 C가 아니라 F이다.
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
			            
			            // 2. xbpe 인식
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

			            // 세 번째 숫자 맞추기
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
			            
			            // b : base-relative인 경우, 아직 모름
			            // p : pc-relative인 경우, 아직 모름
			            // 주소를 알아내보자
			            
			            // 3. PC-relative vs Base-relative
			            // 우선 pc-relative가 되는지 여부를 살펴봐야 한다. 우선 일반적인 경우부터
			            
			            // n : Indirect Addressing(간접 주소 접근)
			            if ((TokenList.get(i).tokenList.get(j).getFlag(32) > 0) && TokenList.get(i).tokenList.get(j).getFlag(16) == 0) {
			                // @ 뒤에 있는 주소의 값이 target이 됨
			                // 이 뒷 자리수는 @ 뒤에 나오는 숫자를 기반으로 함
			                // @ 뒤에 나오는 수를 봄
			                String[] str = TokenList.get(i).tokenList.get(j).operand[0].split("@");

			                // str[1]에 정보가 있음, 이곳의 주소를 찾아야 하는데 이때 sym_table을 이용함
			                int addr = 0;
			                for (int p = 0; p < symtabList.get(i).symbolList.size(); p++) {
			                	if (symtabList.get(i).symbolList.get(p).equals(str[1])) {
			                		addr = symtabList.get(i).locationList.get(p);
			                	}
			                }
			                
			                // 구한 주소에서 PC값을 빼야 함
			                addr -= TokenList.get(i).tokenList.get(j+1).location;
			                
			                // 이제 addr을 16진수 문자열로 변환 후, 
			                // 형식이 3이면 3개를, 형식이 4이면 4개를 넣으면 됨
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
			            // i : Immediate Addressing(즉시 주소 접근)
			            if ((TokenList.get(i).tokenList.get(j).getFlag(32) == 0) && TokenList.get(i).tokenList.get(j).getFlag(16) > 0) {
			                // 이 뒷 자리수는 # 뒤에 나오는 숫자를 기반으로 함
			                // # 뒤에 나오는 수를 봄
			                String[] str = TokenList.get(i).tokenList.get(j).operand[0].split("#");

			                // 이제 addr을 16진수 문자열로 변환 후, 
			                // 형식이 3이면 3개를, 형식이 4이면 4개를 넣으면 됨

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
			            	// 일반적인 경우

			                int target = -1;
			                int displacement = 0;
			                                          
			                // 우선 operand가 EXTREFS(외부에서 쓰이는 것)인지를 본다. 이 경우,기계어 주소값은 전부 0이 된다.
			                int sw = 0;
			                for (int pp = 0; pp < EXTREFS_all.get(now_section).size(); pp++) {
			                    if (EXTREFS_all.get(now_section).get(pp).equals(TokenList.get(i).tokenList.get(j).operand[0])) {
			                    	sw = 1;
			                    	break;
			                    }
			                }
			                
			                if (sw == 0) {
			                	// symbolList랑 literalList를 확인해서 target 주소를 확보하자.
			    
			                	for (int pp = 0; pp < symtabList.get(now_section).symbolList.size(); pp++) {
			                		if (symtabList.get(now_section).symbolList.get(pp).equals(TokenList.get(i).tokenList.get(j).operand[0])) {
			                			target = symtabList.get(now_section).locationList.get(pp); // target의 경우 다음 PC 주소
			                			break;
			                		}
			                	}
			                	
			                	if (target == -1) {
			                	// 이 경우 literal인지도 확인한다.	
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
			                	
			                	// symbolList와 literalList 둘 다 확인했는데도 불구하고 target이 -1이면 끝
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

			                        // pc-relative인지 확인
			                        if (Math.abs(target - TokenList.get(i).tokenList.get(j).location) >= 16*16*16) {
			                        	continue; // pc-relative가 아님 (Base-relative)
			                        }
			                        else {
			                        	
			                        	char hex[] = new char[10];
			                        	for (int ppp = 0; ppp < 9; ppp++) {
			                        		hex[ppp] = '0';
			                        	}
			                            int pos = 0;

			                            while (true) {
			                                int mod = displacement % 16; // 16으로 나누었을 때 나머지
			                                if (mod < 10) {
			                                    // 숫자 0의 ASCII 코드 값 48 + 나머지
			                                    hex[pos] = (char) ('0' + mod);
			                                }
			                                else {
			                                    // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
			                                    hex[pos] = (char) ('A' + mod - 10);
			                                }

			                                displacement = displacement / 16;

			                                pos++;

			                                if (displacement == 0) break;
			                            }

			                            if (instTable.instMap.get(op).format == 3) {
			                                // 끝에서부터 숫자 맞추기
			                                machine[6] = '\0';
			                                machine[5] = hex[0];
			                                machine[4] = hex[1];
			                                machine[3] = hex[2];
			                            }
			                            else if (instTable.instMap.get(op).format == 4) {
			                                // 끝에서부터 숫자 맞추기
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
			                // operand가 EXTREFS(외부에서 쓰이는 것)인 경우, 기계어 주소값은 전부 0이 된다.
			                // M_Line에도 넣는다.
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
					                // format이 3인 경우 (주소 + 1)번지부터 3개
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
					                // format이 4인 경우 (주소 + 1)번지부터 5개
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
				// 만족하는 opcode가 없을 경우 기계어를 만들 수 없으니 continue
				else {
					continue;
				}
			}
		}
	}
	
	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {

		// System.out.println("크기 : " + codeList.size());
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
						str_T += tenTosixteen(str2.length() / 2, 2); // 명령어의 총 길이
						str_T += str2;							 // 명령어들
						System.out.println(str_T);
						fw.write(str_T + "\n");
						fw.flush();
						str_T = "T";
						str2 = "";
						
						// M라인, E라인을 만든다.
						
						// M라인
						for (int j = 0; j < M_Line.get(now_section).size(); j++) {
							System.out.println(M_Line.get(now_section).get(j));
							fw.write(M_Line.get(now_section).get(j) + "\n");
							fw.flush();
						}
						
						// E라인
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
					str += "000000"; // 시작주소 넣기
					str += tenTosixteen(lengthList.get(now_section), 6); // 파일의 총 길이 넣기
					System.out.println(str);
					fw.write(str + "\n");
					fw.flush();

					
					if (EXTDEFS_all.get(now_section).size() != 0) {
						// 여기에서 D, R도 수행
						str = "D";
						for (int j = 0; j < EXTDEFS_all.get(now_section).size(); j++) {
							 // 변수 이름
							str += EXTDEFS_all.get(now_section).get(j);
							if (str.length() < 1 + 6 * (j + 1)) {
								str += " ";
							}
							
							// 변수 주소 - 이거는 어디서 찾지? -> symtabList에서 찾자!
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
						// 여기에서 D, R도 수행
						str = "R";
						for (int j = 0; j < EXTREFS_all.get(now_section).size(); j++) {
							 // 변수 이름
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
					// T부분 만들자
					// T + 6자리(시작지점) + 2자리(길이) + 58자리(명령어 나열, 1D = 29, 29*2글자의 16진수 명령어 나열 가능)
					if (str_T.equals("T")) {
						// 시작지점을 정해주자
						str_T += codeList_loc.get(i);
						str2 = ""; // str2 초기화
					}
					if (str2.length() + codeList.get(i).length() <= 58 && !(codeList.get(i).equals(""))) {
						str2 += codeList.get(i);
					}
					else {
						// 이때 더해야 함
						if (str2.length() != 0) {
							str_T += tenTosixteen(str2.length() / 2, 2); // 명령어의 총 길이
							str_T += str2;							 // 명령어들
							System.out.println(str_T);
							fw.write(str_T + "\n");
							fw.flush();
						}
						
						str_T = "T";
						if (!(codeList.get(i).equals(""))) {
							str_T += codeList_loc.get(i); // 시작지점
							str2 = codeList.get(i); // str2 초기화
						}
					}
				}	
			}
			
			// 맨 마지막 줄을 더해준다.
			str_T += tenTosixteen(str2.length() / 2, 2); // 명령어의 총 길이
			str_T += str2;							 	 // 명령어들
			System.out.println(str_T);
			fw.write(str_T + "\n");
			fw.flush();
			
			// M라인
			for (int j = 0; j < M_Line.get(now_section).size(); j++) {
				System.out.println(M_Line.get(now_section).get(j));
				fw.write(M_Line.get(now_section).get(j) + "\n");
				fw.flush();
			}
			// E라인
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
	
	// 10진수 정수값을 n자리의 16진수 String으로 리턴해주는 함수 
	private String tenTosixteen(int ten_int, int n) {
		String sixteen_str = "";
		
		// 파일의 총길이
        char cs_full_length[] = new char[n]; // 총 길이

        // 10진수를 16진수로 변환
        int temp = ten_int;
        int pos = 0;
        char hex[] = new char[n];

        while (true) {
            int mod = temp % 16; // 16으로 나누었을 때 나머지
            if (mod < 10) {
                // 숫자 0의 ASCII 코드 값 48 + 나머지
                hex[pos] = (char)(48 + mod);
            }
            else {
                // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                hex[pos] = (char)(65 + mod - 10);
            }

            temp = temp / 16;
            pos++;
            if (temp == 0) break;
        }
        // 문자열의 형태로 저장
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
