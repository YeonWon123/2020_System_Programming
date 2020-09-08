package SP20_simulator;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.BufferedReader;


/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	
	// 새로운 변수 선언
	private BufferedReader objectCode; // objectCode 파일을 Buffer를 이용하여 입출력하기 위해 사용, readline()을 사용해서 '\n' 단위로 끊어 읽을 수 있음
	private int currentSection = -1; // 현재 section의 위치를 나타냄
	private ArrayList<MTable> mTable = new ArrayList<MTable>(); // M줄을 읽고 저장되는 변수에 대한 정보를 보관
	
	// M줄을 읽고 저장되는 변수를 위한 클래스(구조체) 설정
	public class MTable {
		// ex) M00000405+RDREC, M00002806-BUFFER
		private int addr; 		// 주소 (000004)	(000028)
		private int size; 		// 개수 (05)		(06)
		private String op;	 	// 부호 (+)		(-)
		private String name; 	// 이름 (RDREC )	(BUFFER)
		
		// 생성자 설정
		public MTable() {
			
		}
		
		// set 함수를 대신하는 생성자 설정
		public MTable(int addr, int size, String op, String name) {
			this.addr = addr;
			this.size = size;
			this.op = op;
			this.name = name;
		}
		
		// get 함수 설정
		public int getAddr() { 
			return this.addr;
		}
		
		public int getSize() {
			return this.size; 
		}
		
		public String getOp() {
			return this.op;
		}
		
		public String getName() { 
			return this.name;
		}
		
		
	}
	
	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
		this.rMgr.initializeResource();
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode){
		String line;
		
		try {
			this.objectCode = new BufferedReader(new FileReader(objectCode));
			
			// 파일을 한 줄 한 줄 읽은 뒤 처리하자.
			while((line = this.objectCode.readLine()) != null) {
				//System.out.println(line);
				if (line.equals("")) continue; // 줄이 비어 있으면 예외처리 (line.charAt(0)에서 오류 발생!)
				
				// 로드하는 데이터의 태그별로 처리방법을 결정한다.
				switch(line.charAt(0)) {
					case 'H':
						// ex) HCOPY  000000001033
						// H와 관련된 정보를 Resource Manager를 통해서 저장
						// 필요한 변수와 함수는 자유롭게 선언 가능
						currentSection++;
						rMgr.plusSectionCount();
										
						rMgr.setProgname(line.substring(1,7), currentSection);		// 프로그램 이름 읽어오기
						rMgr.setProgLength(line.substring(13, 19), currentSection);	// 프로그램 길이 읽어오기
						rMgr.setStartADDR(currentSection);							// 프로그램 시작 주소 읽어오기
						
						// SYMTAB 등록 (ex : COPY -> 0, RDREC -> 0, WRREC -> 0)
						rMgr.symtabList.add(new SymbolTable());
						rMgr.symtabList.get(currentSection).putSymbol(line.substring(1,7), Integer.parseInt(rMgr.getStartADDR(currentSection), 16));
						//System.out.println("SYMTAB : " + line.substring(1, 7) + ", int : " +  Integer.parseInt(rMgr.getStartADDR(currentSection), 16));
						break;
					case 'D':
						// ex) DBUFFER000033BUFEND001033LENGTH00002D
						// D줄의 경우 SymbolTable에 저장해야 함
						// 한 symbol당 12칸을 차지하므로, 전체 symbol의 개수는 line.length() / 12
						for (int i = 0; i < line.length() / 12; i++) {
							int a, b, c, d;
							a = 1 + i * 12;	b = 7 + i * 12;  // symbol 이름 index
							c = 7 + i * 12;	d = 13 + i * 12; // symbol 주소 index
							rMgr.symtabList.get(currentSection).putSymbol(line.substring(a, b), Integer.parseInt(line.substring(c, d), 16));
						}
						break;
					case 'R':
						// ex) RRDREC WRREC 
						break;
					case 'T':
						// ex) T0000001D1720274B1000000320232900003320074B1000003F2FEC0320160F2016
						// 시작 주소(6글자) + 이 줄의 길이(2글자) + 명령어
						int start_loc = Integer.parseInt(line.substring(1, 7), 16) * 2 + Integer.parseInt(rMgr.getStartADDR(currentSection), 16) * 2; // 시작 위치 보관(접근 위치 인덱스)
						int length = Integer.parseInt(line.substring(7, 9), 16) * 2; // 이 줄의 명령어 길이를 저장
						String data = line.substring(9);
						// setMemory(접근 위치, 저장하려는 데이터, 저장하는 데이터의 개수)
						//System.out.println("T줄에서 저장된 data : " + data);
						rMgr.setMemory(start_loc, data, length);
						//System.out.println("시작지점 : " + start_loc + ", 길이 : " + length);
						//System.out.println("T줄에서 저장된 data : " + rMgr.getMemory(start_loc, length));
						
						break;
					case 'M':
						// ex) M00000405+RDREC, M00002806-BUFFER
						// 주소의 경우 읽어들인 주소 + 지금 section의 시작 주소
						int addr = Integer.parseInt(line.substring(1, 7), 16) * 2 + Integer.parseInt(rMgr.getStartADDR(currentSection), 16);
						int size = Integer.parseInt(line.substring(7, 9), 16);
						String op = line.substring(9, 10);
						String name = line.substring(10, 16);
						mTable.add(new MTable(addr, size, op, name));
						break;
					case 'E':
						break;
					default:
						break;
				}
			}
						
			//System.out.println("memory : " + rMgr.getMemory(0, 8600));
			
			// M의 경우 처음 section을 돌 때 RDREC, WRREC 이런 걸 모르기 때문에
			// 전부 돌고 나서 SymbolTable에 저장된 symbol들을 바탕으로
			// Modification에 따른 memory를 갱신한다.
			for (int i = 0; i < mTable.size(); i++) {
				String name = mTable.get(i).getName();
				String op = mTable.get(i).getOp();
				int addr = mTable.get(i).getAddr();
				int size = mTable.get(i).getSize();
				if (size % 2 == 1) addr++; // 홀수인 경우 반으로 나누어지게 된다. 따라서 시작 지점을 1 늘려준다.
				int pos = -1;
				// mTable에 저장된 name의 주소를 알아야 한다. 이걸 SymbolTable에서 찾아보자.
				for (int j = 0; j < rMgr.symtabList.size(); j++) {
					if ((pos = rMgr.symtabList.get(j).search(name)) != -1) {
						break;
					}
				}
				
				//System.out.println("pos : " + pos + ", name : " + name);
				//System.out.println("addr : " + addr + ", size : " + size);

				// 주소가 pos에 저장되었으니 이제 memory를 바꾸면 된다.
				// addr번지부터 size개만큼 넣자
				String data = rMgr.getMemory(addr, size);
				//System.out.println("data : " + data);
				//System.out.println("번역한 data : " + new String(data));
				if (op.equals("+"))
					pos = Integer.parseInt(data, 16) + pos;
				else
					pos = Integer.parseInt(data, 16) - pos;
				//System.out.println("pos : " + pos);
				
				String data3 = Integer.toHexString(pos);
				while(data3.length() < size) {
					data3 = "0" + data3;
				}
				
				//System.out.println("최종 삽입되는 data : " + data3);
				rMgr.setMemory(addr, data3, size);
			}	
			
			//System.out.println("memory : " + rMgr.getMemory(0, 8600));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

};

