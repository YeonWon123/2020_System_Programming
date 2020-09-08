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
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다.
 * 크게 네가지의 가상 자원 공간을 선언하고, 이를 관리할 수 있는 함수들을 제공한다.<br><br>
 * 
 * 1) 입출력을 위한 외부 장치 또는 device<br>
 * 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.<br>
 * 3) 연산을 수행하는데 사용하는 레지스터 공간.<br>
 * 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들. 
 * <br><br>
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면,
 * 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서 차이가 있다.
 */
public class ResourceManager{
	/**
	 * 디바이스는 원래 입출력 장치들을 의미 하지만 여기서는 파일로 디바이스를 대체한다.<br>
	 * 즉, 'F1'이라는 디바이스는 'F1'이라는 이름의 파일을 의미한다. <br>
	 * deviceManager는 디바이스의 이름을 입력받았을 때 해당 이름의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
	 * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을 넣음으로서 이를 관리할 수 있다.
	 * <br><br>
	 * 변형된 형태로 사용하는 것 역시 허용한다.<br>
	 * 예를 들면 key값으로 String대신 Integer를 사용할 수 있다.
	 * 파일 입출력을 위해 사용하는 stream 역시 자유로이 선택, 구현한다.
	 * <br><br>
	 * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	
	/**
	 * memory의 경우 원래는 char[]형이었으나 String형으로 수정하여 사용
	 * 3형식 명령어 기준으로, 6byte가 필요
	 * ex) 	명령어가 172027이라면, 6byte를 할당받은 후 1 7 2 0 2 7로 나누어 저장함
	 * 		memory[0] : 0001 (1)
	 * 		memory[1] : 0111 (7)
	 * 		memory[2] : 0010 (2)
	 * 		memory[3] : 0000 (0)
	 * 		memory[4] : 0010 (2) 
	 * 	    memory[5] : 0111 (7)
	 * 2형식 명령어 또는 4형식 명령어가 등장할 경우 각각 4byte와 8byte가 필요하다.
	 */
	StringBuilder memory = new StringBuilder(""); // String으로 수정해서 사용하여도 무방함.
	
	/**
	 * 레지스터의 경우
	 * A 레지스터 번호는 00 : register[0] (Accumulator; for arithmetic operations)
	 * X 레지스터의 번호는 01 : register[1] (Index register; for addressing)
	 * L 레지스터의 번호는 02 : register[2] (Linkage register; store return address)
	 * B 레지스터의 번호는 03 : register[3] (Base Register; Used for addressing)
	 * S 레지스터의 번호는 04 : register[4] (General Working Register)
	 * T 레지스터의 번호는 05 : register[5] (General Working Register)
	 * F 레지스터의 번호는 06 : register[6] (Floating-point accumulator)
	 * PC 레지스터의 번호는 08 : register[8] (Program Counter; Contain the address of the next instruction) 
	 * SW 레지스터의 번호는 09 : register[9] (Status Word; Contain a variety of info, including a Condition Code)
	 * 
	 * register[7]에는 Target Address를 저장한다.
	 */
	int[] register = new int[10];
	double register_F;
	
	// ArrayList를 사용하는 것으로 변경하였음
	ArrayList<SymbolTable> symtabList = new ArrayList<SymbolTable>();	// SYMTAB 보관하는 장소, section마다 1개씩
	
	// 이외에도 필요한 변수 선언해서 사용할 것.
	ArrayList<String> progName = new ArrayList<String>();			// 프로그램 이름 보관하는 장소, section마다 1개씩
	ArrayList<String> startADDR = new ArrayList<String>();		// 프로그램 시작 주소 보관하는 장소, section마다 1개씩
	ArrayList<String> progLength = new ArrayList<String>();		// 프로그램 길이 보관하는 장소, section마다 1개씩
	
	// section의 개수를 나타내는 변수
	private int section_count = 0;
	
	/**
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
	 */
	public void initializeResource(){
		
		// 메모리 초기화 - 모두 '0'으로 초기화
		for (int i = 0; i < 65536; i++) {
			memory.append("0"); // 문자열을 더함
		}
		
		// 레지스터 초기화 - 모두 0으로 초기화
		for (int i = 0; i < 10; i++) {
			register[i] = 0;
		}
		
	}
	
	/**
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
	 */
	public void closeDevice() {
		
	}
	
	/**
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
	 */
	public void testDevice(String devName) {
		
	}

	/**
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터
	 */
	// string형으로 읽어들임
	public String readDevice(String devName, int num){
		
		// 파일 읽는 곳 또는 IO 과정에서 오류가 날 경우, 예외처리 수행
		try {
			// 파일 읽기
			BufferedReader br = new BufferedReader(new FileReader(devName));
			
			// 글자 가져오기, 가져오는 글자의 개수만큼 char 크기 지정
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
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param data 보내는 데이터
	 * @param num 보내는 글자의 개수
	 */
	public void writeDevice(String devName, byte[] data, int num){

	
	}
	
	/**
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	public String getMemory(int location, int num){
		String res = "";
		
		for (int i = location; i < location + num; i++) {
			res += memory.substring(i, i+1);
		}
		
		return res;
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	public void setMemory(int locate, String data, int num){
		for (int i = locate; i < locate + num; i++) {
			memory.setCharAt(i, data.charAt(i-locate));
		}
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return register[regNum];
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 byte[]형태로 변경한다.
	 * @param data
	 * @return
	 */
	public byte[] intToByte(int data){
		// int를 String으로 형변환
		String hex = Integer.toHexString(data);
		
		// String을 byte로 형변환
		return hex.getBytes();
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. byte[]값을 int형태로 변경한다.
	 * @param data
	 * @return
	 */
	// byte[] to int로 수정하였음
	public int byteToInt(byte[] data){
		// 먼저 byte[]를 string으로 형변환
		String res = new String(data);
		
		// String을 int로 형변환
		return Integer.parseInt(res, 16);
	}

	/**
	 * 프로그램의 이름을 저장하는 함수
	 * @param progName
	 * @param currentSection
	 */
	public void setProgname(String progName, int currentSection) {
		// TODO Auto-generated method stub
		this.progName.add(progName);		
	}

	/**
	 * 프로그램의 이름을 불러오는 함수
	 * @param progName
	 * @return this.progName.get(currentSection)
	 */
	public String getProgname(int currentSection) {
		// TODO Auto-generated method stub
		return this.progName.get(currentSection);		
	}
	
	/**
	 * 프로그램의 길이를 저장하는 함수
	 * @param substring
	 * @param currentSection
	 */
	public void setProgLength(String Length, int currentSection) {
		// TODO Auto-generated method stub
		this.progLength.add(Length);
	}
	
	/**
	 * 프로그램의 길이를 불러오는 함수
	 * @param currentSection
	 * @return this.progLength.get(currentSection)
	 */
	public String getProgLength(int currentSection) {
		// TODO Auto-generated method stub
		return this.progLength.get(currentSection);
	}

	/**
	 * 프로그램의 시작 주소를 지정 
	 * (내가 0번 section이면, 시작 주소는 000000)
	 * (그렇지 않으면, 이전 section의 끝을 다음 section의 시작값으로 설정
	 * @param currentSection
	 */
	public void setStartADDR(int currentSection) {
		// TODO Auto-generated method stub
		// 내가 만약 0번 section이라면
		if (currentSection == 0)
			this.startADDR.add("000000"); // 시작 주소는 000000
		else {
			// String to int
			String str1 = getStartADDR(currentSection-1);
			String str2 = getProgLength(currentSection-1);
			//System.out.println("저번 시작지점 : " + str1 + ", 길이 : " + str2);
			// 이전 section의 시작 지점 + 이전 section의 길이
			int pos = Integer.parseInt(str1, 16) + Integer.parseInt(str2, 16);
			// int pos = Integer.parseInt(str1, 16) + Integer.parseInt(str2, 16) * 2;
			// int to String
			String res = Integer.toHexString(pos);
			//System.out.println("pos : " + pos + ", res : " + res);
			// 0 개수 채워주기
			while (res.length() < 6) {
				res = "0" + res;
			}
			this.startADDR.add(res);
		}
	}

	/**
	 * 프로그램의 시작 주소를 리턴
	 * @param currentSection
	 * @return "000000" // 시작 주소는 000000
	 */
	public String getStartADDR(int currentSection) {
		// TODO Auto-generated method stub
		return this.startADDR.get(currentSection);
	}

	/**
	 * section_count의 개수를 하나 증가시키는 함수
	 */
	public void plusSectionCount() {
		this.section_count++;
	}
	
	/**
	 * Section의 개수를 리턴
	 * @return section_count
	 */
	public int getSectionCount() {
		// TODO Auto-generated method stub
		return this.section_count;
	}
}