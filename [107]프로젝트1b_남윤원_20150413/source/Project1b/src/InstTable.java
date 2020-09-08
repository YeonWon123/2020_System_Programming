import java.util.HashMap;

//���� ����°� ���� ó���� ���� ���̺귯����
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap = new HashMap();
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		// ���� ó��
		try {
			openFile(instFile);
		}
		catch (IOException e) {
			System.out.println("inst.data doesn't find! Initialization failed.\n");
			System.exit(0);
		}
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 * @throws IOException 
	 */
	public void openFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while(true) {
			String line = br.readLine();
			if (line == null) break;
			Instruction inst = new Instruction(line);
			instMap.put(inst.inst_name, inst);
		}
		br.close();
	}
	
	//get, set, search ���� �Լ��� ���� ����

}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	/* 
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	
	/** instruction�� �̸��� ����. */
	String inst_name;
	
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format;
	
	/** instruction�� �ڵ带 ����    */
	String opcode;
	
	/** instruction�� ���� �� �ִ� Operand�� ������ ����  */
	int numberOfOperand;
	
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		
		String[] array 	= line.split("\t");
		inst_name 		= array[0];
		format 			= Integer.parseInt(array[1]);
		opcode			= array[2];
		numberOfOperand = Integer.parseInt(array[3]);
		
	}
	
		
	//�� �� �Լ� ���� ����
	
	
}
