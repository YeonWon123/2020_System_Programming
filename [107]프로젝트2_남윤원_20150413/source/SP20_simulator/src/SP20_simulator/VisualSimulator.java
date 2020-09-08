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
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
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
				JFileChooser chooser = new JFileChooser(); // open할 파일을 선택할 때 JfileChooser를 쓰는 방식 예제
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
		
		// 프로그램 종료 버튼 구현
		JButton Exit_Button = new JButton("\uC885\uB8CC");
		Exit_Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		Exit_Button.setBounds(534, 428, 126, 27);
		getContentPane().add(Exit_Button);
		
		// 실행(all) 버튼 구현
		JButton Exec_All_Button = new JButton("\uC2E4\uD589 (all)");
		Exec_All_Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 모든 Step을 전부 실행하는 함수 추가
				allStep();
			}
		});
		Exec_All_Button.setBounds(534, 389, 126, 27);
		getContentPane().add(Exec_All_Button);
		
		// 실행(1step) 버튼 구현
		JButton Exec_Onestep_Button = new JButton("\uC2E4\uD589 (1step)");  // 버튼 생성
		Exec_Onestep_Button.addActionListener(new ActionListener() {	    // 버튼이 눌렸을 때의 작동방식 선언, 여기서는 임시 ActionListener 생성
			public void actionPerformed(ActionEvent arg0) {					// ActionListener의 구체적인 내용 작성
				// 한 Step씩 실행하는 함수 추가
				oneStep();	// 실제 작업은 oneStep() 함수에 정의되어 있음
				update();	// 작업이 끝나면 visualSimulator 화면 업데이트
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
		lblNewLabel_14_3_1_2.setFont(new Font("굴림", Font.PLAIN, 13));
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
		
		// 위에서 만든 것들을 보이게 활성화하고, 크기를 지정하자.
		this.setVisible(true);
		this.setSize(694, 755);
		
		// 윈도우창 종료 시 프로세스까지 종료시키게 하자
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * VisualSimulator를 초기화하는 함수
	 */
	public void initialize(File program) {
		// 파일 이름의 경우는 이곳에서 나타낸다.
		File_Name_Field.setText(program.getName());		
	}
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program){
		// 어셈블러로 만든 목적 코드를 읽어, 
		// Resource Manager에 변수로 지정되어 있는 가상의 메모리 영역에 로드하는 함수
		sicLoader.load(program);
		
		// 프로그램을 load시킬 때 메모리, 레지스터 초기화 담당하는 함수
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep(){
		sicSimulator.oneStep();
		Log_Field.setText(Log_Field.getText() + sicSimulator.log_print);
		Instructions_Field.setText(sicSimulator.ins_print);
		UseDeviceViewer.setText(sicSimulator.file_name);
		update();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		sicSimulator.allStep();
		Log_Field.setText(Log_Field.getText() + sicSimulator.log_print);
		Instructions_Field.setText(sicSimulator.ins_print);
		UseDeviceViewer.setText(sicSimulator.file_name);
		update();
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(){
		// 파일명 나타내기 - File_Name_Field.setText(); (이 과정은 초기화 단계에서 수행함)
		
		// H(Header Record) 창 나타내기
		// PC값이 어디에 있는지를 보자
		int index = 0;
		for (index = 0; index < resourceManager.getSectionCount() - 1; index++) {
			// PC값이 section i 사이에 있을 경우, section i의 시작 주소, 길이, 이름을 출력
			if ((Integer.parseInt(resourceManager.getStartADDR(index), 16) <= resourceManager.getRegister(8)) 
					&& (resourceManager.getRegister(8) < Integer.parseInt(resourceManager.getStartADDR(index + 1), 16))) {
				Program_Name_Field.setText(resourceManager.getProgname(index));
				Start_Address_of_Object_Program_Field.setText(resourceManager.getStartADDR(index));
				Length_of_Program_Field.setText(resourceManager.getProgLength(index));
				break;
			}
		}
		
		if (index == resourceManager.getSectionCount() - 1) {
			// 이 경우는 PC값이 section의 맨 마지막에 있음을 의미
			Program_Name_Field.setText(resourceManager.getProgname(index));
			Start_Address_of_Object_Program_Field.setText(resourceManager.getStartADDR(index));
			Length_of_Program_Field.setText(resourceManager.getProgLength(index));
		}
		
		
		Address_of_First_instruction_Field.setText("000000");
		Start_Address_in_Memory_Field.setText("000000");
		Target_Address_Field.setText(String.format("%06X", resourceManager.getRegister(7)));
		
		// 레지스터 값 나타내기
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

		// Instructions_Field - 명령어 코드 찍기(16진수 숫자)
		// UseDeviceViewer - 사용중인 장치
		// Log_Field - 명령어 수행 관련 로그 찍기
	};
	
	// main 함수
	public static void main(String[] args) {
		// Simulator 객체 생성, 실행
		VisualSimulator Simulator = new VisualSimulator();
	}
}
