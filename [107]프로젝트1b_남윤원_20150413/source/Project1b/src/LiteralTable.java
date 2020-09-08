import java.util.ArrayList;

/**
 * literal�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class LiteralTable {
	ArrayList<String> literalList = new ArrayList<String>();
	ArrayList<Integer> locationList = new ArrayList<Integer>();
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	/**
	 * ���ο� Literal�� table�� �߰��Ѵ�.
	 * @param literal : ���� �߰��Ǵ� literal�� label
	 * @param location : �ش� literal�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� literal�� putLiteral�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifyLiteral()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putLiteral(String literal, int location) {
		for (int i = 0; i < literalList.size(); i++) {
			if (literalList.get(i).equals(literal)) {
				// ���� �߻�
				System.out.println("Error! Same literal Detected!\n");
				System.exit(0);
			}
		}
		
		// ���� �߻����� �ʴ´ٸ� ����
		literalList.add(literal);
		locationList.add(location);
	}
	
	/**
	 * ������ �����ϴ� literal ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param literal : ������ ���ϴ� literal�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifyLiteral(String literal, int newLocation) {
		for (int i = 0; i < literalList.size(); i++) {
			if (literalList.get(i).equals(literal)) {
				locationList.set(i, newLocation);
				break;
			}
		}
	}
	
	/**
	 * ���ڷ� ���޵� literal�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param literal : �˻��� ���ϴ� literal�� label
	 * @return literal�� ������ �ִ� �ּҰ�. �ش� literal�� ���� ��� -1 ����
	 */
	public int search(String literal) {
		int address = -1;

		for (int i = 0; i < literalList.size(); i++) {
			if (literalList.get(i).equals(literal)) {
				return locationList.get(i);
			}
		}
		
		return address;
	}
	
	
	
}
