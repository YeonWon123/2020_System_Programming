/* 
 * my_assembler 함수를 위한 변수 선언 및 매크로를 담고 있는 헤더 파일이다. 
 * 
 */
#define _CRT_SECURE_NO_WARNINGS
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

 // 새롭게 정의한 매크로
#define MAX_NAME 8              // 기계어 이름 글자 수 최대 제한
#define MAX_FORMAT 4            // 기계어 형식 글자 수 최대 제한
#define MAX_LENGTH 256          // 한 줄을 입력받을 때 글자 수 최대 제한

/*
 * instruction 목록 파일로 부터 정보를 받아와서 생성하는 구조체 변수이다.
 * 구조는 각자의 instruction set의 양식에 맞춰 직접 구현하되
 * 라인 별로 하나의 instruction을 저장한다.
 */
struct inst_unit
{
    /* add your code here */
    char mnemonic[MAX_NAME];    // 기계어 이름
    char format[MAX_FORMAT];    // 기계어 형식 (1, 2, 3, 4, 3/4 등등 다양한 형식을 입력받을 수 있기 위함)
    char opcode[MAX_OPERAND];   // 기계어 코드
    int operand;                // 오퍼랜드의 개수
};

// instruction의 정보를 가진 구조체를 관리하는 테이블 생성
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
 * 어셈블리 할 소스코드를 입력받는 테이블이다. 라인 단위로 관리할 수 있다.
 */
char *input_data[MAX_LINES];
static int line_num;

/*
 * 어셈블리 할 소스코드를 토큰단위로 관리하기 위한 구조체 변수이다.
 * operator는 renaming을 허용한다.
 * nixbpe는 8bit 중 하위 6개의 bit를 이용하여 n,i,x,b,p,e를 표시한다.
 */
struct token_unit
{
	char *label;				//명령어 라인 중 label
	char *operator;				//명령어 라인 중 operator
	char *operand[MAX_OPERAND]; //명령어 라인 중 operand
	char *comment;				//명령어 라인 중 comment
	char nixbpe;				//하위 6bit 사용: _ _ n i x b p e
};

typedef struct token_unit token;
token *token_table[MAX_LINES];
static int token_line;

/*
 * 심볼을 관리하는 구조체이다.
 * 심볼 테이블은 심볼 이름, 심볼의 위치로 구성된다.
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
* 리터럴을 관리하는 구조체이다.
* 리터럴 테이블은 리터럴의 이름, 리터럴의 위치로 구성된다.
*/
struct literal_unit
{
	char literal[10];
	int addr;
};

typedef struct literal_unit literal;
literal literal_table[MAX_LINES];
static int lit_count = 0;       // 패스1과정에서 발견된 리터럴의 총 개수를 저장
static int lit_now = 0;         // 패스1과정에서 발견된 리터럴 중 LTORG이나 END를 통해 주소를 할당받은 리터럴의 개수 저장

static int pc_counts_save[MAX_LINES]; // 다음 주소값(PC)을 저장하는 배열
static int pc_now_save[MAX_LINES]; // 현재 주소값을 저장하는 배열
static int cs_length[MAX_LINES]; // control section의 길이를 저장
static int cs_length_counts = 0; // control section의 개수를 저장

static int locctr = 0;  // 주소값 저장 역할
static int pc_counts = 0;  // 다음 주소값 저장 역할
static int locctr_i = 0; // 주소값 저장하는 현재 라인 줄 수 (index와 같으며 0부터 시작)

// 기계어가 저장되는 char 배열
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