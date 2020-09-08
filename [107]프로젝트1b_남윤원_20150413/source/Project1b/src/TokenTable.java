import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList  = new ArrayList<Token>();
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		// C������ �����Ϳ� ����, reference�� ����
		this.symTab = symTab;
		this.instTab = instTab;	
	}

	/**
	 * �ʱ�ȭ�ϸ鼭 literalTable�� instTable�� ��ũ��Ų��.
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(LiteralTable literalTab, InstTable instTab) {
		// C������ �����Ϳ� ����, reference�� ����
		 this.literalTab = literalTab;
		 this.instTab = instTab;
	}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index){
		//...
	}
	
	/** 
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	private static final int nFlag = 32;
	private static final int iFlag = 16;
	private static final int xFlag = 8;
	private static final int pFlag = 2;
	private static final int eFlag = 1;
	private static final int bFlag = 4;
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		//initialize �߰�
		parsing(line);
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		
		location = Assembler.pc;
		
		// �� line�� �ּ����� �ƴ��� ���� �Ǵ�����
		// line[0]�� .���� ������ ��� ������ �ּ��̴�.
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
				// ,�� �������� ������.
				String[] str = array[2].split(",");
				operand = str;
			}
			
			if (array.length < 4 || array[3].isEmpty()) comment = null;
			else 										comment = array[3];
			
			nixbpe = 0;
			setFlag(pFlag, 1);	// �켱 pFlag = 1�� �ΰ�, pFlag = 0�� ������ �߰ߵǸ� �׶� pFlag = 0���� ����.
			setFlag(bFlag, 0);	// �켱 bFlag = 0���� �ΰ�, bFlag = 1�� ������ �߰ߵǸ� �׶� bFlag = 1�� ����.

			// ���� n i�� ����
			// operand�� #�� �پ� ���� ��� n i�� ���� 0 1�� ���� 
			// operand�� @�� �پ� ���� ��� n i�� ���� 1 0���� ����
			// ������ ���� 1 1�� ����			
			if (operand != null) {
				if (operand[0].charAt(0) == '#') {
					//nixbpe += 16;
					setFlag(iFlag, 1);
					setFlag(pFlag, -1);	// ó���� pFlag�� 1�� �����߱� ������, 0���� ������ַ��� -1�� �����־�� �Ѵ�.
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
						
				// x ���� - operand�� x�� ������ ����
				for (int i = 0; i < operand.length; i++) {
					if (operand[i].equals("X")) {
						//nixbpe += 8;
						setFlag(xFlag, 1);
					}
					else {
						setFlag(xFlag, 0);
					}
				}
				
				// e ���� - ��ɾ 4������ ��� ����
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
	 * n,i,x,b,p,e flag�� �����Ѵ�. 
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {
		nixbpe += flag * value;		
	}
	
	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� 
	 * 
	 * ��� �� : getFlag(nFlag)
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return (int)(nixbpe) & flags;
	}
}
