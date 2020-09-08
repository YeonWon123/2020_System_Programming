package SP20_simulator;
import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList = new ArrayList<String>();
	ArrayList<Integer> addressList = new ArrayList<Integer>();
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param address : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int address) {
		// System.out.println("�ɺ� : " + symbol + ", " + location);
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).equals(symbol)) {
				// ���� ó�� : symbol�� �ߺ��� ������ �˸��� ����
				System.out.println("Error! Same Symbol Detected!\n");
				System.exit(0);
			}
		}
		
		// ���� �߻����� �ʴ´ٸ� ����
		symbolList.add(symbol);
		addressList.add(address);
	//	System.out.println(locationList.get(0) + "����");

	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newaddress : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newaddress) {
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).equals(symbol)) {
				addressList.set(i, newaddress);
			}
		}

	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = -1; 
		
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).equals(symbol)) {
				address = addressList.get(i);
				return address;
			}
		}
		return address;
	}

	
	
	
}
