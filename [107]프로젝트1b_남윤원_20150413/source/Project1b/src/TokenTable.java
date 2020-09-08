import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList  = new ArrayList<Token>();
	
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		// C에서의 포인터와 같음, reference로 전달
		this.symTab = symTab;
		this.instTab = instTab;	
	}

	/**
	 * 초기화하면서 literalTable과 instTable을 링크시킨다.
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(LiteralTable literalTab, InstTable instTab) {
		// C에서의 포인터와 같음, reference로 전달
		 this.literalTab = literalTab;
		 this.instTab = instTab;
	}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		//...
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	private static final int nFlag = 32;
	private static final int iFlag = 16;
	private static final int xFlag = 8;
	private static final int pFlag = 2;
	private static final int eFlag = 1;
	private static final int bFlag = 4;
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 추가
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		
		location = Assembler.pc;
		
		// 이 line이 주석인지 아닌지 먼저 판단하자
		// line[0]이 .으로 시작할 경우 무조건 주석이다.
		if (line.charAt(0) == '.') {
			comment = line;
			
			label = null;
			operator = null;
			operand = null;
			nixbpe = 0;
		}
		else {
			String[] array = line.split("\t");
			// System.out.println(array.length);
			// array[0] : label
			// array[1] : operator
			// array[2] : operand
			// array[3] : comment
			if (array[0].isEmpty()) label = null;
			else 					label = array[0];
			
			if (array[1].isEmpty()) operator = null;
			else					operator = array[1];
			
			if (array.length < 3 || array[2].isEmpty()) operand = null;
			else {
				// ,을 기준으로 나눈다.
				String[] str = array[2].split(",");
				operand = str;
			}
			
			if (array.length < 4 || array[3].isEmpty()) comment = null;
			else 										comment = array[3];
			
			nixbpe = 0;
			setFlag(pFlag, 1);	// 우선 pFlag = 1로 두고, pFlag = 0인 지점이 발견되면 그때 pFlag = 0으로 두자.
			setFlag(bFlag, 0);	// 우선 bFlag = 0으로 두고, bFlag = 1인 지점이 발견되면 그때 bFlag = 1로 두자.

			// 먼저 n i를 판정
			// operand에 #이 붙어 있을 경우 n i는 각각 0 1로 판정 
			// operand에 @이 붙어 있을 경우 n i는 각각 1 0으로 판정
			// 나머지 경우는 1 1로 판정			
			if (operand != null) {
				if (operand[0].charAt(0) == '#') {
					//nixbpe += 16;
					setFlag(iFlag, 1);
					setFlag(pFlag, -1);	// 처음에 pFlag를 1로 선언했기 때문에, 0으로 만들어주려면 -1을 더해주어야 한다.
				}
				else if (operand[0].charAt(0) == '@') {
					//nixbpe += 32;
					setFlag(nFlag, 1);
				}
				else {
					//nixbpe += 32;
					//nixbpe += 16;
					setFlag(nFlag, 1);
					setFlag(iFlag, 1);
				}
						
				// x 판정 - operand에 x가 있으면 가능
				for (int i = 0; i < operand.length; i++) {
					if (operand[i].equals("X")) {
						//nixbpe += 8;
						setFlag(xFlag, 1);
					}
					else {
						setFlag(xFlag, 0);
					}
				}
				
				// e 판정 - 명령어가 4형식일 경우 가능
				if (operator.charAt(0) == '+') {
					// nixbpe += 1;
					setFlag(eFlag, 1);
					setFlag(pFlag, -1);
				}
				else {
					setFlag(eFlag, 0);
				}
				
			}
		}		
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		nixbpe += flag * value;		
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return (int)(nixbpe) & flags;
	}
}
