/* 
 * my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�. 
 * 
 */
#define _CRT_SECURE_NO_WARNINGS
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

 // ���Ӱ� ������ ��ũ��
#define MAX_NAME 8              // ���� �̸� ���� �� �ִ� ����
#define MAX_FORMAT 4            // ���� ���� ���� �� �ִ� ����
#define MAX_LENGTH 256          // �� ���� �Է¹��� �� ���� �� �ִ� ����

/*
 * instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
 * ������ ������ instruction set�� ��Ŀ� ���� ���� �����ϵ�
 * ���� ���� �ϳ��� instruction�� �����Ѵ�.
 */
struct inst_unit
{
    /* add your code here */
    char mnemonic[MAX_NAME];    // ���� �̸�
    char format[MAX_FORMAT];    // ���� ���� (1, 2, 3, 4, 3/4 ��� �پ��� ������ �Է¹��� �� �ֱ� ����)
    char opcode[MAX_OPERAND];   // ���� �ڵ�
    int operand;                // ���۷����� ����
};

// instruction�� ������ ���� ����ü�� �����ϴ� ���̺� ����
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
 * ����� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
 */
char *input_data[MAX_LINES];
static int line_num;

/*
 * ����� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
 * operator�� renaming�� ����Ѵ�.
 * nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
 */
struct token_unit
{
	char *label;				//��ɾ� ���� �� label
	char *operator;				//��ɾ� ���� �� operator
	char *operand[MAX_OPERAND]; //��ɾ� ���� �� operand
	char *comment;				//��ɾ� ���� �� comment
	char nixbpe;				//���� 6bit ���: _ _ n i x b p e
};

typedef struct token_unit token;
token *token_table[MAX_LINES];
static int token_line;

/*
 * �ɺ��� �����ϴ� ����ü�̴�.
 * �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
 */
struct symbol_unit
{
	char symbol[10];
	int addr;
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];
static int sym_count = 0;

/*
* ���ͷ��� �����ϴ� ����ü�̴�.
* ���ͷ� ���̺��� ���ͷ��� �̸�, ���ͷ��� ��ġ�� �����ȴ�.
*/
struct literal_unit
{
	char literal[10];
	int addr;
};

typedef struct literal_unit literal;
literal literal_table[MAX_LINES];
static int lit_count = 0;       // �н�1�������� �߰ߵ� ���ͷ��� �� ������ ����
static int lit_now = 0;         // �н�1�������� �߰ߵ� ���ͷ� �� LTORG�̳� END�� ���� �ּҸ� �Ҵ���� ���ͷ��� ���� ����

static int pc_counts_save[MAX_LINES]; // ���� �ּҰ�(PC)�� �����ϴ� �迭
static int pc_now_save[MAX_LINES]; // ���� �ּҰ��� �����ϴ� �迭
static int cs_length[MAX_LINES]; // control section�� ���̸� ����
static int cs_length_counts = 0; // control section�� ������ ����

static int locctr = 0;  // �ּҰ� ���� ����
static int pc_counts = 0;  // ���� �ּҰ� ���� ����
static int locctr_i = 0; // �ּҰ� �����ϴ� ���� ���� �� �� (index�� ������ 0���� ����)

// ��� ����Ǵ� char �迭
static char machine_table[MAX_LINES][20];

//--------------

static char *input_file;
static char *output_file;
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int token_parsing(char *str);
int search_opcode(char *str);
static int assem_pass1(void);
//void make_opcode_output(char *file_name);

void make_symtab_output(char *file_name);
void make_literaltab_output(char *file_name);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);