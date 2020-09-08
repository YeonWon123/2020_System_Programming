package SP20_simulator;

import java.io.File;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;
	String log_print = "";
	String ins_print = "";
	String file_name = "";
	
	public SicSimulator(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
	}
	
	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ ��*/
	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
	 */
	public void oneStep() {
		// PC���� ������ (ex : �� ó�� pc���� 0)
		int pc = rMgr.getRegister(8);
		int n = 0;
		int i = 0;
		int x = 0;
		int b = 0;
		int p = 0;
		int e = 0;
		
		int opcode_format = 3;
		
		// ������ pc���� ���ؼ� opcode�� ���� �� ����
		// ex) �� ó�� pc���� 0�̹Ƿ�, memory[0],memory[1]�� ����.
		// 17 = 0001 0111 -> 14 = 0001 0100���� ������ �ϸ� �̶� n = 1, i = 1���� �� �� ����
		int opcode = Integer.parseInt(rMgr.getMemory(pc * 2,  2), 16);
		ins_print += rMgr.getMemory(pc*2 , 2);
		if (opcode % 2 != 0) {
			i = 1;
			opcode = opcode - 1;
		}
		
		if ((opcode & 2) > 0) {
			n = 1;
			opcode = opcode - 2;
		}
		
		// ��ɾ �´���, �ƴ��� ��������
		if ((opcode_format = isOpcode(opcode)) == 0) {
			// ��ɾ �ƴ� ���
			pc = pc + 1;
			rMgr.setRegister(8, pc); // ���� pc�� ���� �� return
			ins_print += "\n";
			return;
		}
		else if (opcode_format == 3) {
			// opcode_format�� 2���, �� ������ �ʿ����� ����
			int third = Integer.parseInt(rMgr.getMemory(pc*2 + 2,  1), 16); // �� ��° memory��
			ins_print += rMgr.getMemory(pc*2 + 2, 1);
			// ���⿡���� x, b, p, e�� ������ �� ����
			if (third - 8 >= 0) {
				third = third - 8;
				x = 1;
			}
			if (third - 4 >= 0) {
				third = third - 4;
				b = 1;
			}
			if (third - 2 >= 0) {
				third = third - 2;
				p = 1;
			}
			if (third - 1 >= 0) {
				third = third - 1;
				e = 1;
				opcode_format = 4;
			}
		}
		
		// opcode, opcode_format, nixbpe�� �˸��� ������ ����
		// ���� Target Address���� �˾ƺ���
		// n = 1, b = 1�� ��� pc + disp�� Target Address�� �ȴ�.
		// n = 0, b = 1�� ��� disp�� Target Address�� �ȴ�. (immediate addressing)
		// n = 1, b = 0�� ��� @(pc + disp)�� Target Address�� �ȴ�.
		// �� pc + disp�� �ּҸ� �˾ƾ� �� (indirect addressing) 
		int target_address = 0;
		int disp = 0;
		int regA = 0;
		int regB = 0;
		if (opcode_format == 2) {
			disp = Integer.parseInt(rMgr.getMemory(pc*2 + 2, 2), 16);
			regA = Integer.parseInt(rMgr.getMemory(pc*2 + 2, 1), 16);
			regB = Integer.parseInt(rMgr.getMemory(pc*2 + 3, 1), 16);
			ins_print += rMgr.getMemory(pc*2 + 2, 2);
			pc = pc + 2;
		}
		else if (opcode_format == 3) {
			disp = Integer.parseInt(rMgr.getMemory(pc*2 + 3, 3), 16);
			ins_print += rMgr.getMemory(pc*2 + 3, 3);
			pc = pc + 3;
		}
		else { // opcode_format == 4
			disp = Integer.parseInt(rMgr.getMemory(pc*2 + 3, 5), 16);
			ins_print += rMgr.getMemory(pc*2 + 3, 5);
			pc = pc + 4;
		}
		
		if (n == 1 && i == 1) {
			target_address = pc + disp;
		}
		else if (n == 0 && b == 1) {
			target_address = disp;
		}
		else if (n == 1 && b == 0) {
			target_address = Integer.parseInt(rMgr.getMemory(pc + disp, 3), 16);
		}
		
		/*
		if (target_address < 0) { 
			target_address = target_address + 0x10000; // 2�� ������ ����
			target_address = target_address - 0xF000;  // �� ���� ���� ����
		}
		*/
		
		if (opcode == 0x14) {
			// STL : �������� L�� �ִ� ���� memory "target_address" ������ ����
			log_print = "STL\n";
			String value = String.format("%06X", rMgr.getRegister(2));
			rMgr.setMemory(target_address, value, 6);
		}
		else if (opcode == 0x48) {
			// JSUB : PC�� �ִ� ���� L Register�� ������, PC�� ���� memory�� �ִ� ������ ����.
			log_print = "JSUB\n";
			rMgr.setRegister(2, pc);
			pc = disp;
			//System.out.println("JSUB: pc�� : " + pc);
			rMgr.setRegister(8, pc);
		}
		else if (opcode == 0x00) {
			// LDA : target_address�� Register A�� ������
			log_print = "LDA\n";
			rMgr.setRegister(0, target_address);
		}
		else if (opcode == 0x50) {
			// LDCH : �������� L�� �ִ� char���� memory "target_address"������ ����
			log_print = "LDCH\n";
			// char ���� �����;� �ϹǷ�, �� �� 1byte�� �����;� �Ѵ�.
			String value = String.format("%02X", rMgr.getRegister(2));
			rMgr.setMemory(target_address, value, 2);
		}	
		else if (opcode == 0x28) {
			// COMP - register A�� memory�� ���� ��, �� ����� SW �������Ϳ� ����
			// COMP A B ����, ���� ���� ��� SW �������Ϳ� 2�� ����
			// COMP A B ����, A�� �� Ŭ ��� SW �������Ϳ� 1�� ����
			// COMP A B ����, B�� �� Ŭ ��� SW �������Ϳ� 4�� ����
			
			log_print = "COMP\n";
			
			if (rMgr.getRegister(0) == target_address) {
				rMgr.setRegister(9, 2); // sw Register�� ���� 2�� �д�.
			}
			else if (rMgr.getRegister(0) < target_address) {
				rMgr.setRegister(9, 4); // sw Register�� ���� 4�� �д�.
			}
			else { // rMgr.getRegister(0) > target_address
				rMgr.setRegister(9, 1); // sw Register�� ���� 1�� �д�.
			}
			
		}
		else if (opcode == 0xa0) {
			// COMPR - 2���� ��ɾ�
			// COMPR A B : Register A�� B�� ���� ��, �� ����� SW �������Ϳ� ����
			// COMPR A B ����, ���� ���� ��� SW �������Ϳ� 2�� ����
			// COMPR A B ����, A�� �� Ŭ ��� SW �������Ϳ� 1�� ����
			// COMPR A B ����, B�� �� Ŭ ��� SW �������Ϳ� 4�� ����
			log_print = "COMPR\n";
			// A�� B�� ��ɾ�� �о�� ��
			if (rMgr.getRegister(regA) == rMgr.getRegister(regB)) {
				rMgr.setRegister(9, 2);
			}
			else if (rMgr.getRegister(regA) < rMgr.getRegister(regB)) {
				rMgr.setRegister(9, 4);
			}
			else { // rMgr.getRegister(regA) > rMgr.getRegister(regB)
				rMgr.setRegister(9, 1);
			}
		}
		else if (opcode == 0x30) {
			// JEQ : �տ��� ���� ����� equal(SW Register�� ���� 2)�� ��� Jump
			log_print = "JEQ\n";
			if (rMgr.getRegister(9) == 2) { // ���� �տ��� ���� ����� 2���
				pc = disp;		// PC���� memory�� �ִ� ������ �̵�
			}
		}
		else if (opcode == 0x3c) {
			// J : PC�� ���� memory�� �ִ� ������ ����
			log_print = "J\n";
			pc = disp;
			//System.out.println("J: pc�� : " + pc);
		}
		else if (opcode == 0x38) {
			// JLT : �տ��� ���� ����� < �� ��� (SW Register�� ���� 4) Jump
			log_print = "JLT\n";
			if (rMgr.getRegister(9) == 4) { // ���� �տ��� ���� ����� 4���
				pc = disp;		// PC���� memory�� �ִ� ������ �̵�
			}
		}		
		else if (opcode == 0x0c) {
			// STA : �������� A�� �ִ� ���� memory "target_address" ������ ����
			log_print = "STA\n";
			String value = String.format("%06X", rMgr.getRegister(0));	
			rMgr.setMemory(target_address, value, 6);
		}
		else if (opcode == 0x10) {
			// STX : �������� X�� �ִ� ���� memory "target_address" ������ ����
			log_print = "STX\n";
			String value = String.format("%06X", rMgr.getRegister(1));	
			rMgr.setMemory(target_address, value, 6);
		}
		else if (opcode == 0x54) {
			// STCH : �������� A�� �ִ� char���� memory "target_address"������ ����
			log_print = "STCH\n";
			// char ���� �����;� �ϹǷ�, �� �� 1byte�� �����;� �Ѵ�.
			String value = String.format("%02X", rMgr.getRegister(0));
			rMgr.setMemory(target_address, value, 2);
		}
		else if (opcode == 0xb4) {
			// CLEAR
			log_print = "CLEAR\n";
			if (disp == 0x10) {
				// X register�� ���� 0���� ����
				//System.out.println("X�� 0");
				rMgr.setRegister(1, 0);
			}
			else if (disp == 0x00) {
				// A register�� ���� 0���� ����
				//System.out.println("A�� 0");
				rMgr.setRegister(0, 0);
			}
			else if (disp == 0x40) {
				// S register�� ���� 0���� ����
				//System.out.println("S�� 0");
				rMgr.setRegister(4, 0);
			}
			opcode_format = 2;
		}
		else if (opcode == 0xb8) {
			// TIXR : Register X�� ���� 1 ������Ű��, X�� ���� memory�� �ø���
			log_print = "TIXR\n";
			opcode_format = 2;
			rMgr.setRegister(1, rMgr.getRegister(1) + 1);
			String value = Integer.toHexString(rMgr.getRegister(1));
			rMgr.setMemory(target_address, value, 1);
		}
		else if (opcode == 0xdc) {
			// WD : m ���Ͽ��� �о���� ���� memory�� ���� ����
			// ���ǻ� m ������ 05��� ����.
			log_print = "WD\n";
			file_name = "05";
		}
		else if (opcode == 0xe0) {
			// TD : m ������ �ҷ��� �� �ִ��� �׽�Ʈ
			// ���ǻ� m ������ F1�̶�� ����.
			log_print = "TD\n";
			file_name = "F1";
		}
		else if (opcode == 0xD8) {
			// RD : m ���Ͽ��� ������ �о����
			// ���ǻ� m ������ F1�̶�� ����.
			log_print = "RD\n";
			file_name = "F1";
		}
		else if (opcode == 0x74) {
			// LDT : target_address�� Register T�� ������
			log_print = "LDT\n";
			rMgr.setRegister(5, target_address);
		}
		else if (opcode == 0x4c) {
			// RSUB : Register L�� �ִ� ���� PC�� ����
			log_print = "RSUB\n";
			pc = rMgr.getRegister(2);
		}

		rMgr.setRegister(7, target_address);
		rMgr.setRegister(8, pc);
		ins_print += "\n";
	}
	
	// ��ɾ Opcode�� �´��� Ȯ���ϴ� �Լ�
	// Opcode��� 2,3 �� �ϳ��� ��ȯ
	// Opcode�� �ƴ϶�� 0�� ��ȯ
	private int isOpcode(int opcode) {
		if (opcode == 0x14) {
			// STL
			log_print = "STL\n";
			return 3;
		}
		else if (opcode == 0x48) {
			// JSUB
			log_print = "JSUB\n";
			return 3;
		}
		else if (opcode == 0x00) {
			// LDA
			log_print = "LDA\n";
			return 3;
		}
		else if (opcode == 0x28) {
			// COMP
			log_print = "COMP\n";
			return 3;
		}
		else if (opcode == 0x30) {
			// JEQ
			log_print = "JEQ\n";
			return 3;
		}
		else if (opcode == 0x3c) {
			// J
			log_print = "J\n";
			return 3;
		}
		else if (opcode == 0x0c) {
			// STA
			log_print = "STA\n";
			return 3;
		}
		else if (opcode == 0x10) {
			// STX
			log_print = "STX\n";
			return 3;
		}
		else if (opcode == 0x54) {
			// STCH
			log_print = "STCH\n";
			return 3;
		}
		else if (opcode == 0x50) {
			// LDCH
			log_print = "LDCH\n";
			return 3;
		}
		else if (opcode == 0xa0) {
			// COMPR
			log_print = "COMPR\n";
			return 2;
		}
		else if (opcode == 0xb4) {
			// CLEAR
			log_print = "CLEAR\n";
			return 2;
		}
		else if (opcode == 0xb8) {
			// TIXR
			log_print = "TIXR\n";
			return 2;
		}
		else if (opcode == 0xdc) {
			// WD
			log_print = "WD\n";
			return 3;
		}
		else if (opcode == 0xe0) {
			// TD
			log_print = "TD\n";
			return 3;
		}
		else if (opcode == 0xD8) {
			// RD
			log_print = "RD\n";
			return 3;
		}
		else if (opcode == 0x74) {
			// LDT
			log_print = "LDT\n";
			return 3;
		}
		else if (opcode == 0x38) {
			// JLT
			log_print = "JLT\n";
			return 3;
		}
		else if (opcode == 0x4c) {
			// RSUB
			log_print = "RSUB\n";
			return 3;
		}
		
		// ��ɾ �ƴ� ���
		return 0;
	}

	/**
	 * ���� ��� instruction�� ����� ����� ���δ�.
	 */
	public void allStep() {
		for (int i = 0; i < 10; i++) {
			oneStep();
		}
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
	}	
}
