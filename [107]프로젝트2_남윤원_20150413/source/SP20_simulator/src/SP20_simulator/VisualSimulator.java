package SP20_simulator;

import java.awt.EventQueue;
import javax.swing.JFrame;
import java.io.File;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.JPasswordField;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JProgressBar;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;

import java.awt.Canvas;
import java.awt.List;
import javax.swing.Box;
import java.awt.TextField;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JSpinner;
import java.awt.FlowLayout;
import javax.swing.border.BevelBorder;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.FileDialog;
import javax.swing.DropMode;

/**
 * VisualSimulator�� ����ڿ��� ��ȣ�ۿ��� ����Ѵ�.<br>
 * ��, ��ư Ŭ������ �̺�Ʈ�� �����ϰ� �׿� ���� ������� ȭ�鿡 ������Ʈ �ϴ� ������ �����Ѵ�.<br>
 * �������� �۾��� SicSimulator���� �����ϵ��� �����Ѵ�.
 */
public class VisualSimulator extends JFrame{
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	private final JLabel lblNewLabel = new JLabel("FileName : ");
	private JTextField File_Name_Field;
	private JTextField UseDeviceViewer;
	private JTextField Target_Address_Field;
	private JTextField Program_Name_Field;
	private JTextField Start_Address_of_Object_Program_Field;
	private JTextField Length_of_Program_Field;
	private JTextField Address_of_First_instruction_Field;
	private JTextField A_Dec;
	private JTextField A_Hex;
	private JTextField X_Dec;
	private JTextField X_Hex;
	private JTextField L_Dec;
	private JTextField L_Hex;
	private JTextField B_Dec;
	private JTextField B_Hex;
	private JTextField S_Dec;
	private JTextField S_Hex;
	private JTextField T_Dec;
	private JTextField T_Hex;
	private JTextField F_Hex;
	private JTextField PC_Dec;
	private JTextField PC_Hex;
	private JTextField SW_Hex;
	private JTextField Start_Address_in_Memory_Field;
	private JTextArea Log_Field;
	private JTextArea Instructions_Field;
	
	public VisualSimulator() {
		getContentPane().setLayout(null);
		lblNewLabel.setBounds(14, 12, 118, 26);
		getContentPane().add(lblNewLabel);
		
		File_Name_Field = new JTextField();
		File_Name_Field.setBounds(86, 13, 116, 24);
		getContentPane().add(File_Name_Field);
		File_Name_Field.setColumns(10);
		
		JButton OpenButton = new JButton("open");
		OpenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(); // open�� ������ ������ �� JfileChooser�� ���� ��� ����
				int ret = chooser.showOpenDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					try {
						initialize(chooser.getSelectedFile());
						load(chooser.getSelectedFile());
						update();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		OpenButton.setBounds(213, 12, 105, 27);
		getContentPane().add(OpenButton);
		
		// ���α׷� ���� ��ư ����
		JButton Exit_Button = new JButton("\uC885\uB8CC");
		Exit_Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		Exit_Button.setBounds(534, 428, 126, 27);
		getContentPane().add(Exit_Button);
		
		// ����(all) ��ư ����
		JButton Exec_All_Button = new JButton("\uC2E4\uD589 (all)");
		Exec_All_Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// ��� Step�� ���� �����ϴ� �Լ� �߰�
				allStep();
			}
		});
		Exec_All_Button.setBounds(534, 389, 126, 27);
		getContentPane().add(Exec_All_Button);
		
		// ����(1step) ��ư ����
		JButton Exec_Onestep_Button = new JButton("\uC2E4\uD589 (1step)");  // ��ư ����
		Exec_Onestep_Button.addActionListener(new ActionListener() {	    // ��ư�� ������ ���� �۵���� ����, ���⼭�� �ӽ� ActionListener ����
			public void actionPerformed(ActionEvent arg0) {					// ActionListener�� ��ü���� ���� �ۼ�
				// �� Step�� �����ϴ� �Լ� �߰�
				oneStep();	// ���� �۾��� oneStep() �Լ��� ���ǵǾ� ����
				update();	// �۾��� ������ visualSimulator ȭ�� ������Ʈ
			}
		});
		Exec_Onestep_Button.setBounds(534, 350, 126, 27);
		getContentPane().add(Exec_Onestep_Button);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(372, 215, 150, 240);
		getContentPane().add(scrollPane);
		
		Instructions_Field = new JTextArea();
		Instructions_Field.setDropMode(DropMode.INSERT);
		scrollPane.setViewportView(Instructions_Field);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setBounds(14, 505, 646, 187);
		getContentPane().add(scrollPane_1);
		
		Log_Field = new JTextArea();
		scrollPane_1.setViewportView(Log_Field);
		Log_Field.setDropMode(DropMode.INSERT);
		
		JLabel lblNewLabel_1 = new JLabel("Log (\uBA85\uB839\uC5B4 \uC218\uD589 \uAD00\uB828) : ");
		lblNewLabel_1.setBounds(14, 467, 188, 26);
		getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Instructions :");
		lblNewLabel_2.setBounds(372, 185, 105, 18);
		getContentPane().add(lblNewLabel_2);
		
		UseDeviceViewer = new JTextField();
		UseDeviceViewer.setBounds(550, 252, 93, 26);
		getContentPane().add(UseDeviceViewer);
		UseDeviceViewer.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("\uC0AC\uC6A9\uC911\uC778 \uC7A5\uCE58");
		lblNewLabel_3.setBounds(550, 222, 93, 18);
		getContentPane().add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel("Target Address : ");
		lblNewLabel_4.setBounds(372, 155, 150, 18);
		getContentPane().add(lblNewLabel_4);
		
		Target_Address_Field = new JTextField();
		Target_Address_Field.setBounds(493, 152, 150, 24);
		getContentPane().add(Target_Address_Field);
		Target_Address_Field.setColumns(10);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "H (Header Record)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(14, 50, 298, 140);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel_5 = new JLabel("Program Name : ");
		lblNewLabel_5.setBounds(25, 25, 116, 18);
		panel.add(lblNewLabel_5);
		
		Program_Name_Field = new JTextField();
		Program_Name_Field.setBounds(141, 22, 143, 24);
		panel.add(Program_Name_Field);
		Program_Name_Field.setColumns(10);
		
		JLabel lblNewLabel_6 = new JLabel("Start Address of");
		lblNewLabel_6.setBounds(25, 55, 106, 18);
		panel.add(lblNewLabel_6);
		
		JLabel lblNewLabel_7 = new JLabel("Object Program : ");
		lblNewLabel_7.setBounds(25, 70, 129, 18);
		panel.add(lblNewLabel_7);
		
		Start_Address_of_Object_Program_Field = new JTextField();
		Start_Address_of_Object_Program_Field.setBounds(141, 67, 143, 24);
		panel.add(Start_Address_of_Object_Program_Field);
		Start_Address_of_Object_Program_Field.setColumns(10);
		
		JLabel lblNewLabel_8 = new JLabel("Length of Program : ");
		lblNewLabel_8.setBounds(25, 106, 155, 18);
		panel.add(lblNewLabel_8);
		
		Length_of_Program_Field = new JTextField();
		Length_of_Program_Field.setBounds(161, 103, 123, 24);
		panel.add(Length_of_Program_Field);
		Length_of_Program_Field.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Register", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(14, 202, 298, 253);
		getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel_11 = new JLabel("A (#0)");
		lblNewLabel_11.setBounds(14, 39, 62, 18);
		panel_1.add(lblNewLabel_11);
		
		JLabel lblNewLabel_12 = new JLabel("X (#1)");
		lblNewLabel_12.setBounds(14, 63, 62, 18);
		panel_1.add(lblNewLabel_12);
		
		JLabel lblNewLabel_13 = new JLabel("Dec");
		lblNewLabel_13.setBounds(69, 16, 62, 18);
		panel_1.add(lblNewLabel_13);
		
		A_Dec = new JTextField();
		A_Dec.setBounds(69, 37, 83, 21);
		panel_1.add(A_Dec);
		A_Dec.setColumns(10);
		
		A_Hex = new JTextField();
		A_Hex.setBounds(178, 37, 83, 21);
		A_Hex.setColumns(10);
		panel_1.add(A_Hex);
		
		X_Dec = new JTextField();
		X_Dec.setBounds(69, 61, 83, 21);
		X_Dec.setColumns(10);
		panel_1.add(X_Dec);
		
		X_Hex = new JTextField();
		X_Hex.setBounds(178, 61, 83, 21);
		X_Hex.setColumns(10);
		panel_1.add(X_Hex);
		
		JLabel lblNewLabel_13_1 = new JLabel("Hex");
		lblNewLabel_13_1.setBounds(179, 16, 62, 18);
		panel_1.add(lblNewLabel_13_1);
		
		JLabel lblNewLabel_14 = new JLabel("L (#2)");
		lblNewLabel_14.setBounds(14, 85, 62, 18);
		panel_1.add(lblNewLabel_14);
		
		L_Dec = new JTextField();
		L_Dec.setColumns(10);
		L_Dec.setBounds(69, 85, 83, 21);
		panel_1.add(L_Dec);
		
		L_Hex = new JTextField();
		L_Hex.setColumns(10);
		L_Hex.setBounds(178, 85, 83, 21);
		panel_1.add(L_Hex);
		
		B_Dec = new JTextField();
		B_Dec.setColumns(10);
		B_Dec.setBounds(69, 109, 83, 21);
		panel_1.add(B_Dec);
		
		B_Hex = new JTextField();
		B_Hex.setColumns(10);
		B_Hex.setBounds(178, 109, 83, 21);
		panel_1.add(B_Hex);
		
		JLabel lblNewLabel_14_1 = new JLabel("B (#3)");
		lblNewLabel_14_1.setBounds(14, 109, 62, 18);
		panel_1.add(lblNewLabel_14_1);
		
		S_Dec = new JTextField();
		S_Dec.setColumns(10);
		S_Dec.setBounds(69, 132, 83, 21);
		panel_1.add(S_Dec);
		
		S_Hex = new JTextField();
		S_Hex.setColumns(10);
		S_Hex.setBounds(178, 132, 83, 21);
		panel_1.add(S_Hex);
		
		JLabel lblNewLabel_14_2 = new JLabel("S (#4)");
		lblNewLabel_14_2.setBounds(14, 132, 62, 18);
		panel_1.add(lblNewLabel_14_2);
		
		T_Dec = new JTextField();
		T_Dec.setColumns(10);
		T_Dec.setBounds(69, 155, 83, 21);
		panel_1.add(T_Dec);
		
		T_Hex = new JTextField();
		T_Hex.setColumns(10);
		T_Hex.setBounds(178, 155, 83, 21);
		panel_1.add(T_Hex);
		
		JLabel lblNewLabel_14_3 = new JLabel("T (#5)");
		lblNewLabel_14_3.setBounds(14, 155, 62, 18);
		panel_1.add(lblNewLabel_14_3);
		
		JLabel lblNewLabel_14_3_1 = new JLabel("F (#6)");
		lblNewLabel_14_3_1.setBounds(14, 177, 62, 18);
		panel_1.add(lblNewLabel_14_3_1);
		
		F_Hex = new JTextField();
		F_Hex.setColumns(10);
		F_Hex.setBounds(69, 177, 192, 21);
		panel_1.add(F_Hex);
		
		JLabel lblNewLabel_14_3_1_1 = new JLabel("PC (#8)");
		lblNewLabel_14_3_1_1.setBounds(14, 200, 62, 18);
		panel_1.add(lblNewLabel_14_3_1_1);
		
		PC_Dec = new JTextField();
		PC_Dec.setColumns(10);
		PC_Dec.setBounds(69, 200, 83, 21);
		panel_1.add(PC_Dec);
		
		PC_Hex = new JTextField();
		PC_Hex.setColumns(10);
		PC_Hex.setBounds(178, 200, 83, 21);
		panel_1.add(PC_Hex);
		
		JLabel lblNewLabel_14_3_1_2 = new JLabel("SW (#9)");
		lblNewLabel_14_3_1_2.setFont(new Font("����", Font.PLAIN, 13));
		lblNewLabel_14_3_1_2.setBounds(14, 222, 62, 18);
		panel_1.add(lblNewLabel_14_3_1_2);
		
		SW_Hex = new JTextField();
		SW_Hex.setColumns(10);
		SW_Hex.setBounds(69, 222, 192, 21);
		panel_1.add(SW_Hex);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "E (End Record)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(372, 16, 288, 79);
		getContentPane().add(panel_2);
		panel_2.setLayout(null);
		
		Address_of_First_instruction_Field = new JTextField();
		Address_of_First_instruction_Field.setBounds(150, 43, 124, 24);
		panel_2.add(Address_of_First_instruction_Field);
		Address_of_First_instruction_Field.setColumns(10);
		
		JLabel lblNewLabel_9 = new JLabel("Address of First instruction");
		lblNewLabel_9.setBounds(14, 22, 193, 18);
		panel_2.add(lblNewLabel_9);
		
		JLabel lblNewLabel_10 = new JLabel("in Object Program : ");
		lblNewLabel_10.setBounds(14, 46, 155, 18);
		panel_2.add(lblNewLabel_10);
		
		JLabel lblNewLabel_15 = new JLabel("Start Address in Memory");
		lblNewLabel_15.setBounds(372, 98, 188, 18);
		getContentPane().add(lblNewLabel_15);
		
		Start_Address_in_Memory_Field = new JTextField();
		Start_Address_in_Memory_Field.setBounds(493, 119, 150, 24);
		getContentPane().add(Start_Address_in_Memory_Field);
		Start_Address_in_Memory_Field.setColumns(10);
		
		// ������ ���� �͵��� ���̰� Ȱ��ȭ�ϰ�, ũ�⸦ ��������.
		this.setVisible(true);
		this.setSize(694, 755);
		
		// ������â ���� �� ���μ������� �����Ű�� ����
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * VisualSimulator�� �ʱ�ȭ�ϴ� �Լ�
	 */
	public void initialize(File program) {
		// ���� �̸��� ���� �̰����� ��Ÿ����.
		File_Name_Field.setText(program.getName());		
	}
	
	/**
	 * ���α׷� �ε� ����� �����Ѵ�.
	 */
	public void load(File program){
		// ������� ���� ���� �ڵ带 �о�, 
		// Resource Manager�� ������ �����Ǿ� �ִ� ������ �޸� ������ �ε��ϴ� �Լ�
		sicLoader.load(program);
		
		// ���α׷��� load��ų �� �޸�, �������� �ʱ�ȭ ����ϴ� �Լ�
		sicSimulator.load(program);
	};

	/**
	 * �ϳ��� ��ɾ ������ ���� SicSimulator�� ��û�Ѵ�.
	 */
	public void oneStep(){
		sicSimulator.oneStep();
		Log_Field.setText(Log_Field.getText() + sicSimulator.log_print);
		Instructions_Field.setText(sicSimulator.ins_print);
		UseDeviceViewer.setText(sicSimulator.file_name);
		update();
	};

	/**
	 * �����ִ� ��� ��ɾ ������ ���� SicSimulator�� ��û�Ѵ�.
	 */
	public void allStep(){
		sicSimulator.allStep();
		Log_Field.setText(Log_Field.getText() + sicSimulator.log_print);
		Instructions_Field.setText(sicSimulator.ins_print);
		UseDeviceViewer.setText(sicSimulator.file_name);
		update();
	};
	
	/**
	 * ȭ���� �ֽŰ����� �����ϴ� ������ �����Ѵ�.
	 */
	public void update(){
		// ���ϸ� ��Ÿ���� - File_Name_Field.setText(); (�� ������ �ʱ�ȭ �ܰ迡�� ������)
		
		// H(Header Record) â ��Ÿ����
		// PC���� ��� �ִ����� ����
		int index = 0;
		for (index = 0; index < resourceManager.getSectionCount() - 1; index++) {
			// PC���� section i ���̿� ���� ���, section i�� ���� �ּ�, ����, �̸��� ���
			if ((Integer.parseInt(resourceManager.getStartADDR(index), 16) <= resourceManager.getRegister(8)) 
					&& (resourceManager.getRegister(8) < Integer.parseInt(resourceManager.getStartADDR(index + 1), 16))) {
				Program_Name_Field.setText(resourceManager.getProgname(index));
				Start_Address_of_Object_Program_Field.setText(resourceManager.getStartADDR(index));
				Length_of_Program_Field.setText(resourceManager.getProgLength(index));
				break;
			}
		}
		
		if (index == resourceManager.getSectionCount() - 1) {
			// �� ���� PC���� section�� �� �������� ������ �ǹ�
			Program_Name_Field.setText(resourceManager.getProgname(index));
			Start_Address_of_Object_Program_Field.setText(resourceManager.getStartADDR(index));
			Length_of_Program_Field.setText(resourceManager.getProgLength(index));
		}
		
		
		Address_of_First_instruction_Field.setText("000000");
		Start_Address_in_Memory_Field.setText("000000");
		Target_Address_Field.setText(String.format("%06X", resourceManager.getRegister(7)));
		
		// �������� �� ��Ÿ����
		A_Dec.setText(String.format("%06d", resourceManager.getRegister(0)));
		A_Hex.setText(String.format("%06X", resourceManager.getRegister(0)));
		X_Dec.setText(String.format("%06d", resourceManager.getRegister(1)));
		X_Hex.setText(String.format("%06X", resourceManager.getRegister(1)));
		L_Dec.setText(String.format("%06d", resourceManager.getRegister(2)));
		L_Hex.setText(String.format("%06X", resourceManager.getRegister(2)));
		B_Dec.setText(String.format("%06d", resourceManager.getRegister(3)));
		B_Hex.setText(String.format("%06X", resourceManager.getRegister(3)));
		S_Dec.setText(String.format("%06d", resourceManager.getRegister(4)));
		S_Hex.setText(String.format("%06X", resourceManager.getRegister(4)));
		T_Dec.setText(String.format("%06d", resourceManager.getRegister(5)));
		T_Hex.setText(String.format("%06X", resourceManager.getRegister(5)));
		F_Hex.setText(String.format("%06X", resourceManager.getRegister(6)));
		PC_Dec.setText(String.format("%06d", resourceManager.getRegister(8)));
		PC_Hex.setText(String.format("%06X", resourceManager.getRegister(8)));
		SW_Hex.setText(String.format("%06X", resourceManager.getRegister(9)));

		// Instructions_Field - ��ɾ� �ڵ� ���(16���� ����)
		// UseDeviceViewer - ������� ��ġ
		// Log_Field - ��ɾ� ���� ���� �α� ���
	};
	
	// main �Լ�
	public static void main(String[] args) {
		// Simulator ��ü ����, ����
		VisualSimulator Simulator = new VisualSimulator();
	}
}
