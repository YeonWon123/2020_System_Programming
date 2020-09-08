package SP20_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * ����̽��� ���� ����� ��ġ���� �ǹ� ������ ���⼭�� ���Ϸ� ����̽��� ��ü�Ѵ�.<br>
	 * ��, 'F1'�̶�� ����̽��� 'F1'�̶�� �̸��� ������ �ǹ��Ѵ�. <br>
	 * deviceManager�� ����̽��� �̸��� �Է¹޾��� �� �ش� �̸��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	
	/**
	 * memory�� ��� ������ char[]���̾����� String������ �����Ͽ� ���
	 * 3���� ��ɾ� ��������, 6byte�� �ʿ�
	 * ex) 	��ɾ 172027�̶��, 6byte�� �Ҵ���� �� 1 7 2 0 2 7�� ������ ������
	 * 		memory[0] : 0001 (1)
	 * 		memory[1] : 0111 (7)
	 * 		memory[2] : 0010 (2)
	 * 		memory[3] : 0000 (0)
	 * 		memory[4] : 0010 (2) 
	 * 	    memory[5] : 0111 (7)
	 * 2���� ��ɾ� �Ǵ� 4���� ��ɾ ������ ��� ���� 4byte�� 8byte�� �ʿ��ϴ�.
	 */
	StringBuilder memory = new StringBuilder(""); // String���� �����ؼ� ����Ͽ��� ������.
	
	/**
	 * ���������� ���
	 * A �������� ��ȣ�� 00 : register[0] (Accumulator; for arithmetic operations)
	 * X ���������� ��ȣ�� 01 : register[1] (Index register; for addressing)
	 * L ���������� ��ȣ�� 02 : register[2] (Linkage register; store return address)
	 * B ���������� ��ȣ�� 03 : register[3] (Base Register; Used for addressing)
	 * S ���������� ��ȣ�� 04 : register[4] (General Working Register)
	 * T ���������� ��ȣ�� 05 : register[5] (General Working Register)
	 * F ���������� ��ȣ�� 06 : register[6] (Floating-point accumulator)
	 * PC ���������� ��ȣ�� 08 : register[8] (Program Counter; Contain the address of the next instruction) 
	 * SW ���������� ��ȣ�� 09 : register[9] (Status Word; Contain a variety of info, including a Condition Code)
	 * 
	 * register[7]���� Target Address�� �����Ѵ�.
	 */
	int[] register = new int[10];
	double register_F;
	
	// ArrayList�� ����ϴ� ������ �����Ͽ���
	ArrayList<SymbolTable> symtabList = new ArrayList<SymbolTable>();	// SYMTAB �����ϴ� ���, section���� 1����
	
	// �̿ܿ��� �ʿ��� ���� �����ؼ� ����� ��.
	ArrayList<String> progName = new ArrayList<String>();			// ���α׷� �̸� �����ϴ� ���, section���� 1����
	ArrayList<String> startADDR = new ArrayList<String>();		// ���α׷� ���� �ּ� �����ϴ� ���, section���� 1����
	ArrayList<String> progLength = new ArrayList<String>();		// ���α׷� ���� �����ϴ� ���, section���� 1����
	
	// section�� ������ ��Ÿ���� ����
	private int section_count = 0;
	
	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	public void initializeResource(){
		
		// �޸� �ʱ�ȭ - ��� '0'���� �ʱ�ȭ
		for (int i = 0; i < 65536; i++) {
			memory.append("0"); // ���ڿ��� ����
		}
		
		// �������� �ʱ�ȭ - ��� 0���� �ʱ�ȭ
		for (int i = 0; i < 10; i++) {
			register[i] = 0;
		}
		
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 */
	public void closeDevice() {
		
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 */
	public void testDevice(String devName) {
		
	}

	/**
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������
	 */
	// string������ �о����
	public String readDevice(String devName, int num){
		
		// ���� �д� �� �Ǵ� IO �������� ������ �� ���, ����ó�� ����
		try {
			// ���� �б�
			BufferedReader br = new BufferedReader(new FileReader(devName));
			
			// ���� ��������, �������� ������ ������ŭ char ũ�� ����
			char[] res = new char[num];

			if (br.read(res) < 0) {
				br.close();
				return null;
			}
			else {
				br.close();
				return res.toString();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}

		return null;		
	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 * @param num ������ ������ ����
	 */
	public void writeDevice(String devName, byte[] data, int num){

	
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public String getMemory(int location, int num){
		String res = "";
		
		for (int i = location; i < location + num; i++) {
			res += memory.substring(i, i+1);
		}
		
		return res;
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, String data, int num){
		for (int i = locate; i < locate + num; i++) {
			memory.setCharAt(i, data.charAt(i-locate));
		}
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return register[regNum];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� byte[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public byte[] intToByte(int data){
		// int�� String���� ����ȯ
		String hex = Integer.toHexString(data);
		
		// String�� byte�� ����ȯ
		return hex.getBytes();
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. byte[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	// byte[] to int�� �����Ͽ���
	public int byteToInt(byte[] data){
		// ���� byte[]�� string���� ����ȯ
		String res = new String(data);
		
		// String�� int�� ����ȯ
		return Integer.parseInt(res, 16);
	}

	/**
	 * ���α׷��� �̸��� �����ϴ� �Լ�
	 * @param progName
	 * @param currentSection
	 */
	public void setProgname(String progName, int currentSection) {
		// TODO Auto-generated method stub
		this.progName.add(progName);		
	}

	/**
	 * ���α׷��� �̸��� �ҷ����� �Լ�
	 * @param progName
	 * @return this.progName.get(currentSection)
	 */
	public String getProgname(int currentSection) {
		// TODO Auto-generated method stub
		return this.progName.get(currentSection);		
	}
	
	/**
	 * ���α׷��� ���̸� �����ϴ� �Լ�
	 * @param substring
	 * @param currentSection
	 */
	public void setProgLength(String Length, int currentSection) {
		// TODO Auto-generated method stub
		this.progLength.add(Length);
	}
	
	/**
	 * ���α׷��� ���̸� �ҷ����� �Լ�
	 * @param currentSection
	 * @return this.progLength.get(currentSection)
	 */
	public String getProgLength(int currentSection) {
		// TODO Auto-generated method stub
		return this.progLength.get(currentSection);
	}

	/**
	 * ���α׷��� ���� �ּҸ� ���� 
	 * (���� 0�� section�̸�, ���� �ּҴ� 000000)
	 * (�׷��� ������, ���� section�� ���� ���� section�� ���۰����� ����
	 * @param currentSection
	 */
	public void setStartADDR(int currentSection) {
		// TODO Auto-generated method stub
		// ���� ���� 0�� section�̶��
		if (currentSection == 0)
			this.startADDR.add("000000"); // ���� �ּҴ� 000000
		else {
			// String to int
			String str1 = getStartADDR(currentSection-1);
			String str2 = getProgLength(currentSection-1);
			//System.out.println("���� �������� : " + str1 + ", ���� : " + str2);
			// ���� section�� ���� ���� + ���� section�� ����
			int pos = Integer.parseInt(str1, 16) + Integer.parseInt(str2, 16);
			// int pos = Integer.parseInt(str1, 16) + Integer.parseInt(str2, 16) * 2;
			// int to String
			String res = Integer.toHexString(pos);
			//System.out.println("pos : " + pos + ", res : " + res);
			// 0 ���� ä���ֱ�
			while (res.length() < 6) {
				res = "0" + res;
			}
			this.startADDR.add(res);
		}
	}

	/**
	 * ���α׷��� ���� �ּҸ� ����
	 * @param currentSection
	 * @return "000000" // ���� �ּҴ� 000000
	 */
	public String getStartADDR(int currentSection) {
		// TODO Auto-generated method stub
		return this.startADDR.get(currentSection);
	}

	/**
	 * section_count�� ������ �ϳ� ������Ű�� �Լ�
	 */
	public void plusSectionCount() {
		this.section_count++;
	}
	
	/**
	 * Section�� ������ ����
	 * @return section_count
	 */
	public int getSectionCount() {
		// TODO Auto-generated method stub
		return this.section_count;
	}
}