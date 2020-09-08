package SP20_simulator;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.BufferedReader;


/**
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. 
 * <br><br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader {
	ResourceManager rMgr;
	
	// ���ο� ���� ����
	private BufferedReader objectCode; // objectCode ������ Buffer�� �̿��Ͽ� ������ϱ� ���� ���, readline()�� ����ؼ� '\n' ������ ���� ���� �� ����
	private int currentSection = -1; // ���� section�� ��ġ�� ��Ÿ��
	private ArrayList<MTable> mTable = new ArrayList<MTable>(); // M���� �а� ����Ǵ� ������ ���� ������ ����
	
	// M���� �а� ����Ǵ� ������ ���� Ŭ����(����ü) ����
	public class MTable {
		// ex) M00000405+RDREC, M00002806-BUFFER
		private int addr; 		// �ּ� (000004)	(000028)
		private int size; 		// ���� (05)		(06)
		private String op;	 	// ��ȣ (+)		(-)
		private String name; 	// �̸� (RDREC )	(BUFFER)
		
		// ������ ����
		public MTable() {
			
		}
		
		// set �Լ��� ����ϴ� ������ ����
		public MTable(int addr, int size, String op, String name) {
			this.addr = addr;
			this.size = size;
			this.op = op;
			this.name = name;
		}
		
		// get �Լ� ����
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
		// �ʿ��ϴٸ� �ʱ�ȭ
		setResourceManager(resourceManager);
	}

	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
		this.rMgr.initializeResource();
	}
	
	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
	 */
	public void load(File objectCode){
		String line;
		
		try {
			this.objectCode = new BufferedReader(new FileReader(objectCode));
			
			// ������ �� �� �� �� ���� �� ó������.
			while((line = this.objectCode.readLine()) != null) {
				//System.out.println(line);
				if (line.equals("")) continue; // ���� ��� ������ ����ó�� (line.charAt(0)���� ���� �߻�!)
				
				// �ε��ϴ� �������� �±׺��� ó������� �����Ѵ�.
				switch(line.charAt(0)) {
					case 'H':
						// ex) HCOPY  000000001033
						// H�� ���õ� ������ Resource Manager�� ���ؼ� ����
						// �ʿ��� ������ �Լ��� �����Ӱ� ���� ����
						currentSection++;
						rMgr.plusSectionCount();
										
						rMgr.setProgname(line.substring(1,7), currentSection);		// ���α׷� �̸� �о����
						rMgr.setProgLength(line.substring(13, 19), currentSection);	// ���α׷� ���� �о����
						rMgr.setStartADDR(currentSection);							// ���α׷� ���� �ּ� �о����
						
						// SYMTAB ��� (ex : COPY -> 0, RDREC -> 0, WRREC -> 0)
						rMgr.symtabList.add(new SymbolTable());
						rMgr.symtabList.get(currentSection).putSymbol(line.substring(1,7), Integer.parseInt(rMgr.getStartADDR(currentSection), 16));
						//System.out.println("SYMTAB : " + line.substring(1, 7) + ", int : " +  Integer.parseInt(rMgr.getStartADDR(currentSection), 16));
						break;
					case 'D':
						// ex) DBUFFER000033BUFEND001033LENGTH00002D
						// D���� ��� SymbolTable�� �����ؾ� ��
						// �� symbol�� 12ĭ�� �����ϹǷ�, ��ü symbol�� ������ line.length() / 12
						for (int i = 0; i < line.length() / 12; i++) {
							int a, b, c, d;
							a = 1 + i * 12;	b = 7 + i * 12;  // symbol �̸� index
							c = 7 + i * 12;	d = 13 + i * 12; // symbol �ּ� index
							rMgr.symtabList.get(currentSection).putSymbol(line.substring(a, b), Integer.parseInt(line.substring(c, d), 16));
						}
						break;
					case 'R':
						// ex) RRDREC WRREC 
						break;
					case 'T':
						// ex) T0000001D1720274B1000000320232900003320074B1000003F2FEC0320160F2016
						// ���� �ּ�(6����) + �� ���� ����(2����) + ��ɾ�
						int start_loc = Integer.parseInt(line.substring(1, 7), 16) * 2 + Integer.parseInt(rMgr.getStartADDR(currentSection), 16) * 2; // ���� ��ġ ����(���� ��ġ �ε���)
						int length = Integer.parseInt(line.substring(7, 9), 16) * 2; // �� ���� ��ɾ� ���̸� ����
						String data = line.substring(9);
						// setMemory(���� ��ġ, �����Ϸ��� ������, �����ϴ� �������� ����)
						//System.out.println("T�ٿ��� ����� data : " + data);
						rMgr.setMemory(start_loc, data, length);
						//System.out.println("�������� : " + start_loc + ", ���� : " + length);
						//System.out.println("T�ٿ��� ����� data : " + rMgr.getMemory(start_loc, length));
						
						break;
					case 'M':
						// ex) M00000405+RDREC, M00002806-BUFFER
						// �ּ��� ��� �о���� �ּ� + ���� section�� ���� �ּ�
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
			
			// M�� ��� ó�� section�� �� �� RDREC, WRREC �̷� �� �𸣱� ������
			// ���� ���� ���� SymbolTable�� ����� symbol���� ��������
			// Modification�� ���� memory�� �����Ѵ�.
			for (int i = 0; i < mTable.size(); i++) {
				String name = mTable.get(i).getName();
				String op = mTable.get(i).getOp();
				int addr = mTable.get(i).getAddr();
				int size = mTable.get(i).getSize();
				if (size % 2 == 1) addr++; // Ȧ���� ��� ������ ���������� �ȴ�. ���� ���� ������ 1 �÷��ش�.
				int pos = -1;
				// mTable�� ����� name�� �ּҸ� �˾ƾ� �Ѵ�. �̰� SymbolTable���� ã�ƺ���.
				for (int j = 0; j < rMgr.symtabList.size(); j++) {
					if ((pos = rMgr.symtabList.get(j).search(name)) != -1) {
						break;
					}
				}
				
				//System.out.println("pos : " + pos + ", name : " + name);
				//System.out.println("addr : " + addr + ", size : " + size);

				// �ּҰ� pos�� ����Ǿ����� ���� memory�� �ٲٸ� �ȴ�.
				// addr�������� size����ŭ ����
				String data = rMgr.getMemory(addr, size);
				//System.out.println("data : " + data);
				//System.out.println("������ data : " + new String(data));
				if (op.equals("+"))
					pos = Integer.parseInt(data, 16) + pos;
				else
					pos = Integer.parseInt(data, 16) - pos;
				//System.out.println("pos : " + pos);
				
				String data3 = Integer.toHexString(pos);
				while(data3.length() < size) {
					data3 = "0" + data3;
				}
				
				//System.out.println("���� ���ԵǴ� data : " + data3);
				rMgr.setMemory(addr, data3, size);
			}	
			
			//System.out.println("memory : " + rMgr.getMemory(0, 8600));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

};

