package SP20_simulator;

import java.io.File;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;
	String log_print = "";
	String ins_print = "";
	String file_name = "";
	
	public SicSimulator(ResourceManager resourceManager) {
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
	}
	
	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {
		/* 메모리 초기화, 레지스터 초기화 등*/
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 */
	public void oneStep() {
		// PC값을 가져옴 (ex : 맨 처음 pc값은 0)
		int pc = rMgr.getRegister(8);
		int n = 0;
		int i = 0;
		int x = 0;
		int b = 0;
		int p = 0;
		int e = 0;
		
		int opcode_format = 3;
		
		// 가져온 pc값을 통해서 opcode를 구할 수 있음
		// ex) 맨 처음 pc값은 0이므로, memory[0],memory[1]을 참조.
		// 17 = 0001 0111 -> 14 = 0001 0100으로 만들어야 하며 이때 n = 1, i = 1임을 알 수 있음
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
		
		// 명령어가 맞는지, 아닌지 가려보자
		if ((opcode_format = isOpcode(opcode)) == 0) {
			// 명령어가 아닌 경우
			pc = pc + 1;
			rMgr.setRegister(8, pc); // 다음 pc값 전달 후 return
			ins_print += "\n";
			return;
		}
		else if (opcode_format == 3) {
			// opcode_format이 2라면, 이 과정이 필요하지 않음
			int third = Integer.parseInt(rMgr.getMemory(pc*2 + 2,  1), 16); // 세 번째 memory값
			ins_print += rMgr.getMemory(pc*2 + 2, 1);
			// 여기에서는 x, b, p, e를 가려낼 수 있음
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
		
		// opcode, opcode_format, nixbpe에 알맞은 동작을 수행
		// 먼저 Target Address부터 알아보자
		// n = 1, b = 1인 경우 pc + disp가 Target Address가 된다.
		// n = 0, b = 1인 경우 disp가 Target Address가 된다. (immediate addressing)
		// n = 1, b = 0인 경우 @(pc + disp)가 Target Address가 된다.
		// 즉 pc + disp의 주소를 알아야 함 (indirect addressing) 
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
			target_address = target_address + 0x10000; // 2의 보수법 적용
			target_address = target_address - 0xF000;  // 맨 위의 값은 빼줌
		}
		*/
		
		if (opcode == 0x14) {
			// STL : 레지스터 L에 있는 값을 memory "target_address" 번지로 보냄
			log_print = "STL\n";
			String value = String.format("%06X", rMgr.getRegister(2));
			rMgr.setMemory(target_address, value, 6);
		}
		else if (opcode == 0x48) {
			// JSUB : PC에 있는 값을 L Register로 보내고, PC의 값을 memory에 있는 값으로 변경.
			log_print = "JSUB\n";
			rMgr.setRegister(2, pc);
			pc = disp;
			//System.out.println("JSUB: pc값 : " + pc);
			rMgr.setRegister(8, pc);
		}
		else if (opcode == 0x00) {
			// LDA : target_address를 Register A에 가져옴
			log_print = "LDA\n";
			rMgr.setRegister(0, target_address);
		}
		else if (opcode == 0x50) {
			// LDCH : 레지스터 L에 있는 char값을 memory "target_address"번지로 보냄
			log_print = "LDCH\n";
			// char 값을 가져와야 하므로, 맨 뒤 1byte만 가져와야 한다.
			String value = String.format("%02X", rMgr.getRegister(2));
			rMgr.setMemory(target_address, value, 2);
		}	
		else if (opcode == 0x28) {
			// COMP - register A와 memory를 비교한 뒤, 그 결과를 SW 레지스터에 저장
			// COMP A B 에서, 서로 같을 경우 SW 레지스터에 2를 저장
			// COMP A B 에서, A가 더 클 경우 SW 레지스터에 1를 저장
			// COMP A B 에서, B가 더 클 경우 SW 레지스터에 4를 저장
			
			log_print = "COMP\n";
			
			if (rMgr.getRegister(0) == target_address) {
				rMgr.setRegister(9, 2); // sw Register의 값을 2로 둔다.
			}
			else if (rMgr.getRegister(0) < target_address) {
				rMgr.setRegister(9, 4); // sw Register의 값을 4로 둔다.
			}
			else { // rMgr.getRegister(0) > target_address
				rMgr.setRegister(9, 1); // sw Register의 값을 1로 둔다.
			}
			
		}
		else if (opcode == 0xa0) {
			// COMPR - 2형식 명령어
			// COMPR A B : Register A와 B를 비교한 뒤, 그 결과를 SW 레지스터에 저장
			// COMPR A B 에서, 서로 같을 경우 SW 레지스터에 2를 저장
			// COMPR A B 에서, A가 더 클 경우 SW 레지스터에 1를 저장
			// COMPR A B 에서, B가 더 클 경우 SW 레지스터에 4를 저장
			log_print = "COMPR\n";
			// A와 B는 명령어에서 읽어야 함
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
			// JEQ : 앞에서 비교한 결과가 equal(SW Register의 값이 2)일 경우 Jump
			log_print = "JEQ\n";
			if (rMgr.getRegister(9) == 2) { // 만약 앞에서 비교한 결과가 2라면
				pc = disp;		// PC값을 memory에 있는 값으로 이동
			}
		}
		else if (opcode == 0x3c) {
			// J : PC의 값을 memory에 있는 값으로 변경
			log_print = "J\n";
			pc = disp;
			//System.out.println("J: pc값 : " + pc);
		}
		else if (opcode == 0x38) {
			// JLT : 앞에서 비교한 결과가 < 일 경우 (SW Register의 값이 4) Jump
			log_print = "JLT\n";
			if (rMgr.getRegister(9) == 4) { // 만약 앞에서 비교한 결과가 4라면
				pc = disp;		// PC값을 memory에 있는 값으로 이동
			}
		}		
		else if (opcode == 0x0c) {
			// STA : 레지스터 A에 있는 값을 memory "target_address" 번지로 보냄
			log_print = "STA\n";
			String value = String.format("%06X", rMgr.getRegister(0));	
			rMgr.setMemory(target_address, value, 6);
		}
		else if (opcode == 0x10) {
			// STX : 레지스터 X에 있는 값을 memory "target_address" 번지로 보냄
			log_print = "STX\n";
			String value = String.format("%06X", rMgr.getRegister(1));	
			rMgr.setMemory(target_address, value, 6);
		}
		else if (opcode == 0x54) {
			// STCH : 레지스터 A에 있는 char값을 memory "target_address"번지로 보냄
			log_print = "STCH\n";
			// char 값을 가져와야 하므로, 맨 뒤 1byte만 가져와야 한다.
			String value = String.format("%02X", rMgr.getRegister(0));
			rMgr.setMemory(target_address, value, 2);
		}
		else if (opcode == 0xb4) {
			// CLEAR
			log_print = "CLEAR\n";
			if (disp == 0x10) {
				// X register의 값을 0으로 변경
				//System.out.println("X값 0");
				rMgr.setRegister(1, 0);
			}
			else if (disp == 0x00) {
				// A register의 값을 0으로 변경
				//System.out.println("A값 0");
				rMgr.setRegister(0, 0);
			}
			else if (disp == 0x40) {
				// S register의 값을 0으로 변경
				//System.out.println("S값 0");
				rMgr.setRegister(4, 0);
			}
			opcode_format = 2;
		}
		else if (opcode == 0xb8) {
			// TIXR : Register X의 값을 1 증가시키고, X의 값을 memory에 올리기
			log_print = "TIXR\n";
			opcode_format = 2;
			rMgr.setRegister(1, rMgr.getRegister(1) + 1);
			String value = Integer.toHexString(rMgr.getRegister(1));
			rMgr.setMemory(target_address, value, 1);
		}
		else if (opcode == 0xdc) {
			// WD : m 파일에서 읽어들인 값을 memory에 적는 역할
			// 편의상 m 파일을 05라고 하자.
			log_print = "WD\n";
			file_name = "05";
		}
		else if (opcode == 0xe0) {
			// TD : m 파일을 불러올 수 있는지 테스트
			// 편의상 m 파일을 F1이라고 하자.
			log_print = "TD\n";
			file_name = "F1";
		}
		else if (opcode == 0xD8) {
			// RD : m 파일에서 파일을 읽어들임
			// 편의상 m 파일을 F1이라고 하자.
			log_print = "RD\n";
			file_name = "F1";
		}
		else if (opcode == 0x74) {
			// LDT : target_address를 Register T에 가져옴
			log_print = "LDT\n";
			rMgr.setRegister(5, target_address);
		}
		else if (opcode == 0x4c) {
			// RSUB : Register L에 있는 값을 PC에 넣음
			log_print = "RSUB\n";
			pc = rMgr.getRegister(2);
		}

		rMgr.setRegister(7, target_address);
		rMgr.setRegister(8, pc);
		ins_print += "\n";
	}
	
	// 명령어가 Opcode가 맞는지 확인하는 함수
	// Opcode라면 2,3 중 하나를 반환
	// Opcode가 아니라면 0을 반환
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
		
		// 명령어가 아닐 경우
		return 0;
	}

	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {
		for (int i = 0; i < 10; i++) {
			oneStep();
		}
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
	}	
}
