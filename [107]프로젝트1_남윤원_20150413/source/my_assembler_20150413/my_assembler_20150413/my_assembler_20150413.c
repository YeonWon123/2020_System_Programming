#define _CRT_SECURE_NO_WARNINGS

/*
 * 화일명 : my_assembler_20150413.c 
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

/*
 *
 * 프로그램의 헤더를 정의한다. 
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

// 파일명의 "00000000"은 자신의 학번으로 변경할 것.
#include "my_assembler_20150413.h"

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일 
 * 반환 : 성공 = 0, 실패 = < 0 
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다. 
 *		   또한 중간파일을 생성하지 않는다. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{
	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n");
		return -1;
	}

	if (assem_pass1() < 0)
	{
		printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n");
		return -1;
	}
	// make_opcode_output("output_20150413");

    // make_symtab_output(NULL); // cmd창에 출력
    // make_literaltab_output(NULL); // cmd창에 출력
	make_symtab_output("symtab_20150413");
	make_literaltab_output("literaltab_20150413");
	if (assem_pass2() < 0)
	{
		printf("assem_pass2: 패스2 과정에서 실패하였습니다.  \n");
		return -1;
	}

    // make_objectcode_output(NULL); // cmd창에 출력
	make_objectcode_output("output_20150413");

	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다. 
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기 
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다. 
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
	int result;

	if ((result = init_inst_file("inst.data")) < 0)
		return -1;
	if ((result = init_input_file("input.txt")) < 0)
		return -1;
	return result;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을 
 *        생성하는 함수이다. 
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *	
 *	===============================================================================
 *		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char* inst_file)
{
    FILE* file;
    int errno;

    /* add your code here */
    // 한줄씩 입력받아서 이름, 형식, 기계어 코드, 오퍼랜드의 갯수를 입력받는다.
    if ((file = fopen(inst_file, "r")) != NULL) { // 읽기 모드로 연다.

        for (inst_index = 0; ; inst_index++) {
            inst_table[inst_index] = malloc(sizeof(inst));
            if (fscanf(file, "%s %s %s %d", inst_table[inst_index]->mnemonic, inst_table[inst_index]->format, inst_table[inst_index]->opcode, &inst_table[inst_index]->operand) == EOF) {
                free(inst_table[inst_index]);
                inst_table[inst_index] = NULL;
                break;
            }
        }

        fclose(file);
    }
    else
        errno = -1;

    return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다. 
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0  
 * 주의 : 라인단위로 저장한다.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char* input_file)
{
    FILE* file;
    int errno;

    /* add your code here */
    if ((file = fopen(input_file, "r")) != NULL) { // 읽기 모드로 연다.

        for (line_num = 0; ; line_num++) {
            input_data[line_num] = malloc(sizeof(char) * MAX_LINES);
            if (fgets(input_data[line_num], MAX_LINES, file) == NULL) {
                free(input_data[line_num]);
                input_data[line_num] = NULL;
                break;
            }
        }

        fclose(file);
    }
    else
        errno = -1;

    return errno;
}


/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다.
 *        패스 1로 부터 호출된다.
 * 매계 : 파싱을 원하는 문자열
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다.
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char* str)
{
    /* add your code here */

    token_table[token_line] = malloc(sizeof(token));

    // 첫 문자가 공백이 아니라면 label이 있거나 주석이라는 의미
    // 포인터 변수의 경우, NULL로 초기화하거나 malloc로 메모리를 할당해주자.
    // 가만히 놔두면, 메모리 접근 자체가 되지 않아 여러가지 문제가 생긴다.
    char* ptr = NULL;
    char* buffer = NULL;

    // 우선 전부 NULL로 초기화시키자.
    token_table[token_line]->label = NULL;
    token_table[token_line]->operator = NULL;
    token_table[token_line]->operand[0] = NULL;
    token_table[token_line]->operand[1] = NULL;
    token_table[token_line]->operand[2] = NULL;
    token_table[token_line]->comment = NULL;

    // str 받고 나서 '\n'으로 토큰분리를 맨 처음에 하면 됨!
    ptr = strtok(str, "\n");

    if (!isspace(str[0])) {
        // 주석일 경우
        if (str[0] == '.') {
            token_table[token_line]->comment = str;
        }
        // 토큰이 있는 경우
        else {
            // label 분리
            ptr = strtok(ptr, "\t");
            token_table[token_line]->label = malloc(sizeof(char) * strlen(ptr));
            sprintf(token_table[token_line]->label, "%s", ptr);

            // operator 분리
            ptr = strtok(NULL, "\t");
            token_table[token_line]->operator = malloc(sizeof(char)* strlen(ptr));
            sprintf(token_table[token_line]->operator, "%s", ptr);
        }
    }
    // 첫 문자가 공백이라면, label이 없다는 의미
    // 따라서 이 경우는 operator부터 분리 시작
    else {
    ptr = strtok(ptr, "\t");
    if (ptr != NULL) {
        token_table[token_line]->operator = malloc(sizeof(char) * strlen(ptr));
        sprintf(token_table[token_line]->operator, "%s", ptr);
    }
    }

    // 공통 부분은 아래에 코딩 (한 줄 전체가 주석일 경우, ptr은 계속 NULL이 되므로 큰 문제가 없음)
    // operand 분리 (단, operand가 있을 경우, 없다면 NULL로 유지)
    ptr = strtok(NULL, "\n");

    if (ptr != NULL) {
        // operand가 존재하는지 여부를 살펴봄, 우선 operator와 comment만 있는지를 살펴봄
        if (ptr[0] != '\t') {
            // operand가 존재함.
            ptr = strtok(ptr, "\t");
            if (ptr != NULL) {
                // operand가 2개 이상 있을 경우 ','으로 구분해야 함
                // 일단은 넘어가고(strtok으로 일단 전체적으로 구분한 뒤에 시행)
                buffer = malloc(sizeof(char) * strlen(ptr));
                sprintf(buffer, "%s", ptr);
            }

            // comment 분리 (단, comment가 있을 경우, 없다면 NULL로 유지)
            ptr = strtok(NULL, "\0");
            if (ptr != NULL) {
                token_table[token_line]->comment = malloc(sizeof(char) * strlen(ptr));
                sprintf(token_table[token_line]->comment, "%s", ptr);
            }

            // operand 분리 시행, 2개 이상일수도 있고 그렇지 않을 수도 있음
            // 만약 buffer가 NULL이라면 operand가 없으므로 시행하지 않음
            int operand_index = 0;
            if (buffer != NULL) {
                ptr = strtok(buffer, ",");
                while (ptr != NULL) {
                    token_table[token_line]->operand[operand_index] = malloc(sizeof(char) * strlen(ptr));
                    sprintf(token_table[token_line]->operand[operand_index], "%s", ptr);
                    operand_index++;

                    ptr = strtok(NULL, ",");
                }
            }
        }
        else {
            // operand는 존재하지 않지만 comment는 존재할수도 있음. 따라서 comment 분리 작업 시행
            // comment 분리 (단, comment가 있을 경우, 없다면 NULL로 유지)
            ptr = strtok(ptr, "\0");
            if (ptr != NULL) {
                token_table[token_line]->comment = malloc(sizeof(char) * strlen(ptr));
                sprintf(token_table[token_line]->comment, "%s", ptr);
            }
        }
    }

    // 토큰에서, operand에
    // #이 붙어 있을 경우 n i는 각각 0 1 으로 판정 -> nixbpe = i
    // @이 붙어 있을 경우 n i는 각각 1 0 으로 판정 -> nixbpe = n
    // 나머지 경우는 1 1로 판정 -> nixbpe = 우선 NULL로 판정
    if (token_table[token_line]->operand[0] != NULL) {
        if (token_table[token_line]->operand[0][0] == '#') {
            token_table[token_line]->nixbpe = 'i';
        }
        else if (token_table[token_line]->operand[0][0] == '@') {
            token_table[token_line]->nixbpe = 'n';
        }
    }

    // token_line을 1 증가시켜서, 다음에 함수가 불릴 때 문제없이 동작하게끔 함.
    token_line++;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
 * 매계 : 토큰 단위로 구분된 문자열
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
 * 주의 :
 *
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char* str)
{
    pc_now_save[locctr_i] = pc_counts;
    locctr = pc_counts;

    /* add your code here */
    int i;
    for (i = 0; i < inst_index; i++) {
        // mnemonic을 발견하면 정상 종료 (return 0)
        if (strcmp(inst_table[i]->mnemonic, str) == 0) {
            // 이 때 주소값(locctr)은 명령어 형식만큼 증가시킴
            pc_counts += atoi(inst_table[i]->format);
            pc_counts_save[locctr_i] = pc_counts;
            return 0;
        }
    }

    // mnemonic을 발견하지 못했을 경우, directives인지 확인 (pseudo-instructions)
    // directives라면, 해당 directives에 맞는 활동 수행

    // START와 END의 경우는 locctr이 증가하지 않음
    if (strcmp("START", str) == 0) {
        return 0;
    }

    if (strcmp("END", str) == 0) {
        // 단 end의 경우 주소가 할당되지 않은 literal들의 주소를 할당해 주어야 함
        for (int j = lit_now; j < lit_count; j++) {
            literal_table[j].addr = locctr;
            int sum = 0;
            if (literal_table[j].literal[1] == 'X') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++;
                sum = sum / 2; // 16진수이고, 숫자이므로 2개당 1바이트씩 구성되어 있음
            }
            else if (literal_table[j].literal[1] == 'C') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++; // 문자 개수만큼 BYTE 공간 할당
            }
            pc_counts += sum;
            locctr += sum;
        }
        lit_now = lit_count;

        // 현재까지의 control section 길이을 저장함
        cs_length[cs_length_counts] = locctr;
        cs_length_counts++;

        return 0;
    }


    if (strcmp("BYTE", str) == 0) {
        // BYTE 크기만큼 locctr 증가시킴
        if (token_table[locctr_i]->operand[0] == NULL)
            return -1; // operand가 없어 오류
        else {
            int sum = 0;
            if (token_table[locctr_i]->operand[0][0] == 'X') {
                for (int k = 2; token_table[locctr_i]->operand[0][k] != '\''; k++)
                    sum++;
                sum = sum / 2;  // 16진수이고, 숫자이므로 2개당 1바이트씩 구성되어 있음
            }
            else if (token_table[locctr_i]->operand[0][0] == 'C') {
                for (int k = 2; token_table[locctr_i]->operand[0][k] != '\''; k++)
                    sum++;  // 문자 개수만큼 BYTE 공간 할당
            }
            pc_counts += sum;
        }
        return 0;
    }
        
    if (strcmp("WORD", str) == 0) {
        // WORD 크기만큼 locctr 증가시킴
        
        pc_counts += 3;
        return 0;
    }

    if (strcmp("RESB", str) == 0) {
        // 개수 * 1만큼 locctr 증가시킴 
        pc_counts += atoi(token_table[locctr_i]->operand[0]);
        return 0;
    }

    if (strcmp("RESW", str) == 0) {
        // 개수 * 3만큼 locctr 증가시킴
        pc_counts += atoi(token_table[locctr_i]->operand[0]) * 3;
        return 0;
    }

    if (strcmp("EXTDEF", str) == 0) 
        return 0;
 
    if (strcmp("EXTREF", str) == 0)
        return 0;

    if (strcmp("EQU", str) == 0) {
        // =의 역할을 수행하며, symtab에서 수행함
        return 0;
    }

    if (strcmp("CSECT", str) == 0) {
        // 현재까지의 control section 길이을 저장함
        cs_length[cs_length_counts] = locctr;
        cs_length_counts++;

        // 주소값을 0으로 초기화해 줌
        locctr = 0;
        pc_counts = 0;
        pc_now_save[locctr_i] = pc_counts;
        return 0;
    }


    if (strcmp("LTORG", str) == 0) {
        // 지금까지 찾았던 literal들 전부 주소를 할당해 준다.
        for (int j = lit_now; j < lit_count; j++) {
            literal_table[j].addr = locctr;

            int sum = 0;
            if (literal_table[j].literal[1] == 'X') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++;
                sum = sum / 2; // 16진수이고, 숫자이므로 2개당 1바이트씩 구성되어 있음
            }
            else if (literal_table[j].literal[1] == 'C') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++; // 문자 개수만큼 BYTE 공간 할당
            }

            locctr += sum;
            pc_counts += sum;
        }
        lit_now = lit_count;

        return 0;
    }

    if (strcmp("RSUB", str) == 0) {
        pc_counts += 3;
        return 0;
    }
     
    if (strcmp("USE", str) == 0) {
        return 0;
    }


    // 기계어 코드가 아님을 의미함
    return -1;
}


/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
    /* add your code here */

    /* input_data의 문자열을 한줄씩 입력 받아서
     * token_parsing()을 호출하여 token_unit에 저장
     */
    int i;
    for (i = 0; i < line_num; i++) {
        token_parsing(input_data[i]);
        // 예외 처리 : 에러시 -1 (0보다 작은 값) 리턴
        if (token_table[i]->operator != NULL && search_opcode(token_table[i]->operator) < 0) {
            return -1;
        }

        // symtab 처리
        if (token_table[i]->label != NULL) {
            if (token_table[i]->operator != NULL && strcmp(token_table[i]->operator, "CSECT") == 0)
                sym_count++;

            if (strcmp(token_table[i]->operator, "EQU") == 0 && (strcmp(token_table[i]->operand[0], "*") != 0)) {
                // 이 경우는 BUFEND-BUFFER와 같은 연산을 하기 위함
                // token_table[i]->operand[0]에 다 있음 : BUFEND-BUFFER
                strcpy(sym_table[sym_count].symbol, token_table[i]->label);
                // BUFEND-BUFFER를 계산하기 위함. 즉 A-B를 계산하기 위한 용도
                char temp1[7], temp2[7];
                int j;
                for (j = 0; token_table[i]->operand[0][j] != '-' && token_table[i]->operand[0][j] != '+'; j++) {
                    temp1[j] = token_table[i]->operand[0][j];
                }
                temp1[j] = '\0';
                j++;
                int k;
                for (k = 0; k < 6; k++) {
                    temp2[k] = token_table[i]->operand[0][k + j];
                }
                temp2[j-1] = '\0';

                int temp1a = -1, temp2a = -1;
                for (int p = 0; p < sym_count; p++) {
                    if (strcmp(sym_table[p].symbol, temp1) == 0) {
                        temp1a = sym_table[p].addr;
                        break;
                    }
                }

                for (int p = 0; p < sym_count; p++) {
                    if (strcmp(sym_table[p].symbol, temp2) == 0) {
                        temp2a = sym_table[p].addr;
                        break;
                    }
                }
                j--;
                if (token_table[i]->operand[0][j] == '-')
                    sym_table[sym_count].addr = temp1a - temp2a; // -부호를 기준으로 BUFEND와 BUFFER로 나누어 계산
                else if (token_table[i]->operand[0][j] == '+')
                    sym_table[sym_count].addr = temp1a + temp2a; // +부호를 기준으로 BUFEND와 BUFFER로 나누어 계산

                sym_count++;
            }
            else {
                strcpy(sym_table[sym_count].symbol, token_table[i]->label);
                sym_table[sym_count].addr = locctr;
                sym_count++;
            }
        }

        // literaltab 처리, 주소할당은 LTONG 또는 END 만났을 때
        if (token_table[i]->operand[0] != NULL && token_table[i]->operand[0][0] == '=') {
            // 기존에 있는 literal인지 확인, 없으면 추가
            int sw = 0;
            for (int j = 0; j < lit_count; j++) {
                if (strcmp(literal_table[j].literal, token_table[i]->operand[0]) == 0) { 
                    // =C'EOF', =X'05' 로 저장함. 출력할 때는 =C', ' 이런 부분들은 제외함, C나 X와 같은 정보가 중요하여 이렇게 저장
                    sw = 1;
                    break;
                }
            }

            if (sw == 0) {
                // =C'EOF', =X'05' 로 저장함. 출력할 때는 =C', ' 이런 부분들은 제외함, C나 X와 같은 정보가 중요하여 이렇게 저장
                strcpy(literal_table[lit_count].literal, token_table[i]->operand[0]);
                lit_count++;
            }
        }

        locctr_i++;
    }

    return 0;
}


/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 5번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 5번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/
// void make_opcode_output(char *file_name)
// {
// 	/* add your code here */

// }

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 SYMBOL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	/* add your code here */
    
    FILE* file;

    // 인자로 NULL값이 들어올 경우, 파일 포인터를 stdout으로 설정한다.
    // 그렇지 않다면 파일의 이름을 파일 포인터로 지정한다.
    if (file_name == NULL) {
        file = stdout;
    }
    else {
        file = fopen(file_name, "w"); // 쓰기 모드로 열며 파일이 없으면 파일을 생성한다.
    }

    /* add your code here */
  
    // fprintf를 사용해서 출력
    // 예시 : fprintf(file, "%s", token_table[i]->operand[k]);

    for (int i = 0; i < sym_count; i++) {
        // 중간에 임의로 칸을 띄워놓은 부분이 존재함
        if (sym_table[i].symbol[0] != '\0')
            fprintf(file, "%s\t\t%x\n", sym_table[i].symbol, sym_table[i].addr);
        else
            fprintf(file, "\n");
    }

    if (file_name != NULL) {
        fclose(file);
    }
    else {
        printf("\n\n"); // symtab과 literaltab을 구분하기 위한 용도
    }

}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 LITERAL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char *file_name)
{
	/* add your code here */

    FILE* file;

    // 인자로 NULL값이 들어올 경우, 파일 포인터를 stdout으로 설정한다.
    // 그렇지 않다면 파일의 이름을 파일 포인터로 지정한다.
    if (file_name == NULL) {
        file = stdout;
    }
    else {
        file = fopen(file_name, "w"); // 쓰기 모드로 열며 파일이 없으면 파일을 생성한다.
    }

    /* add your code here */

    // fprintf를 사용해서 출력
    // 예시 : fprintf(file, "%s", token_table[i]->operand[k]);

    for (int i = 0; i < lit_now; i++) {
        //printf("literal_table : %s\n", literal_table[i].literal);
        char temp[7];
        if (literal_table[i].literal[1] != 'C' && literal_table[i].literal[1] != 'X') {
            // 그냥 숫자
            temp[0] = literal_table[i].literal[1];
            temp[1] = '\0';
        }
        else {
            int j;
            for (j = 3; literal_table[i].literal[j] != '\''; j++) {
                temp[j - 3] = literal_table[i].literal[j];
            }
            temp[j - 3] = '\0';
        }
        fprintf(file, "%s\t\t%x\n", temp, literal_table[i].addr);
    }

    if (file_name != NULL) {
        fclose(file);
    }
    else {
        printf("\n\n"); // symtab과 literaltab을 구분하기 위한 용도
    }

}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{
	/* add your code here */
    locctr = 0;
    pc_counts = 0;
    int csect = 0;
    int i, j;
    char lit_part[10][10];
    int lit_index = 0;
    int lit_nows = 0;
    char EXTDEFS[10][10]; // EXTDEF 변수 저장
    char EXTREFS[10][10]; // EXTREF 변수 저장

    for (i = 0; i < token_line; i++) {
        locctr = pc_counts;

        // 만약 operator 부분이 NULL이라면 주석임, 따라서 기계어 생성하지 않음
        if (token_table[i]->operator == NULL) {
            continue;
        }

        // START 줄은 우선은 SKIP
        if (strcmp(token_table[i]->operator, "START") == 0) {
            continue;
        }

        // CSECT가 나왔을 경우, cesct int 변수를 1 증가시킴
        // 이는 자신의 영역에 있는 symtab 변수를 우선적으로 확인하기 위함
        if (strcmp(token_table[i]->operator, "CSECT") == 0) {
            csect++;
            memset(EXTREFS, "\0", sizeof(EXTREFS));
            continue;
        }

        // EXTREF가 나왔을 경우
        if (strcmp(token_table[i]->operator, "EXTREF") == 0) {
            for (int pp = 0; pp < MAX_OPERAND; pp++) {
                if (token_table[i]->operand[pp] != NULL) {
                    strcpy(EXTREFS[pp], token_table[i]->operand[pp]);
                }
            }
            continue;
        }

        // EXTDEF가 나왔을 경우
        if (strcmp(token_table[i]->operator, "EXTDEF") == 0) {
            for (int pp = 0; pp < MAX_OPERAND; pp++) {
                if (token_table[i]->operand[pp] != NULL) {
                    strcpy(EXTDEFS[pp], token_table[i]->operand[pp]);
                }
            }
            continue;
        }

        // BYTE가 나왔을 경우
        if (strcmp(token_table[i]->operator, "BYTE") == 0) {
            char temp11[20];
            memset(temp11, '\0', sizeof(temp11));
            if (token_table[i]->operand[0][0] == 'X') {
                // 16진수 입력받은 걸 그대로 기계어로 변환함
                int pp;
                for (pp = 2; token_table[i]->operand[0][pp] != '\''; pp++) {
                    machine_table[i][pp - 2] = token_table[i]->operand[0][pp];
                }
                machine_table[i][pp] = '\0';
            }
            
    //        printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);
            continue;
        }

        // WORD가 나왔을 경우
        if (strcmp(token_table[i]->operator, "WORD") == 0) {
            // 뒤에 있는 영역의 계산이 불가능할 경우 000000 할당
            if (atoi(token_table[i]->operand[0]) == 0) {
                // 영역 계산 불가능, 000000 할당
                machine_table[i][0] = '0';
                machine_table[i][1] = '0';
                machine_table[i][2] = '0';
                machine_table[i][3] = '0';
                machine_table[i][4] = '0';
                machine_table[i][5] = '0';
                machine_table[i][6] = '\0';
            }
            else {
                // 숫자로 변환된 만큼 할당하며 이를 16진수로 변환 후, 문자열의 형태로 machine_table에 삽입
                int cc = atoi(token_table[i]->operand[0]);

                // 10진수를 16진수로 변환
                int pos = 0;
                char hex[5];

                while (1) {
                    int mod = cc % 16; // 16으로 나누었을 때 나머지
                    if (mod < 10) {
                        // 숫자 0의 ASCII 코드 값 48 + 나머지
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                        hex[pos] = 65 + mod - 10;
                    }

                    cc = cc / 16;
                    pos++;
                    if (cc == 0) break;
                }
                int index3 = 5;
                for (int pp = 0; pp < pos; pp++) {
                    machine_table[i][index3] = hex[pp];
                    index3--;
                }
                for (; index3 > -1; index3--)
                    machine_table[i][index3] = '0';
                
                machine_table[i][6] = '\0';
            }

    //        printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);
            continue;

        }

        // LTORG가 나왔을 경우
        if (strcmp(token_table[i]->operator, "LTORG") == 0) {
            char temp11[20];
            memset(temp11, '\0', sizeof(temp11));
            for (; lit_nows < lit_index; lit_nows++) {
                if (lit_part[lit_nows][1] == 'C') {
                    // 문자열을 받는다.
                    int index3 = 0;
                    for (int pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                        temp11[pp - 3] = lit_part[lit_nows][pp];
                        // E : 45, O : 4F, F : 46 (16진수)
                        // 문자열은 ASCII code로 변환 후 machine_table[i]에 넣는다.
                        int cc = temp11[pp - 3];
                        
                        // 10진수를 16진수로 변환
                        int pos = 0;
                        char hex[5];

                        while (1) {
                            int mod = cc % 16; // 16으로 나누었을 때 나머지
                            if (mod < 10) {
                                // 숫자 0의 ASCII 코드 값 48 + 나머지
                                hex[pos] = 48 + mod;
                            }
                            else {
                                // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                                hex[pos] = 65 + mod - 10;
                            }

                            cc = cc / 16;
                            pos++;
                            if (cc == 0) break;
                        }

                        for (int pp = pos - 1; pp > -1; pp--) {
                            machine_table[i][index3] = hex[pp];
                            index3++;
                        }
                    }
                    machine_table[i][index3] = '\0';
                }
                else if (lit_part[lit_nows][1] == 'X') {
                    // 16진수를 받는다.
                    int pp = 3;
                    for (pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                        temp11[pp - 3] = lit_part[lit_nows][pp];

                        // 그대로 machine_table[i]에 넣는다.
                        machine_table[i][pp-3] = temp11[pp - 3];
                    }
                    machine_table[i][pp - 3] = '\0';
                }
                else {
                    // machine_table에 다음에 들어갈 자리를 생각
                    int pp = 3;
                    for (pp = 3; machine_table[i][pp] != '\0'; pp++);
                    // 숫자를 넣는다.
                    machine_table[i][pp] = '0';
                    machine_table[i][pp + 1] = '0';
                    machine_table[i][pp + 2] = '0';
                    machine_table[i][pp + 3] = '0';
                    machine_table[i][pp + 4] = '0';
                    machine_table[i][pp + 5] = lit_part[lit_nows][1];
                    machine_table[i][pp + 6] = '\0';
                }
            }

        //    printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);
            continue;
        }

        // END가 나왔을 경우
        if (strcmp(token_table[i]->operator, "END") == 0) {
            for (; lit_nows < lit_index; lit_nows++) {
                char temp11[20];
                memset(temp11, '\0', sizeof(temp11));
                for (; lit_nows < lit_index; lit_nows++) {
                    if (lit_part[lit_nows][1] == 'C') {
                        // 문자열을 받는다.
                        int index3 = 0;
                        for (int pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                            temp11[pp - 3] = lit_part[lit_nows][pp];
                            // E : 45, O : 4F, F : 46 (16진수)
                            // 문자열은 ASCII code로 변환 후 machine_table[i]에 넣는다.
                            int cc = temp11[pp - 3];

                            // 10진수를 16진수로 변환
                            int pos = 0;
                            char hex[5];

                            while (1) {
                                int mod = cc % 16; // 16으로 나누었을 때 나머지
                                if (mod < 10) {
                                    // 숫자 0의 ASCII 코드 값 48 + 나머지
                                    hex[pos] = 48 + mod;
                                }
                                else {
                                    // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                                    hex[pos] = 65 + mod - 10;
                                }

                                cc = cc / 16;
                                pos++;
                                if (cc == 0) break;
                            }

                            for (int pp = pos - 1; pp > -1; pp--) {
                                machine_table[i][index3] = hex[pp];
                                index3++;
                            }
                        }
                        machine_table[i][index3] = '\0';
                    }
                    else if (lit_part[lit_nows][1] == 'X') {
                        // 16진수를 받는다.
                        int pp = 3;
                        for (pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                            temp11[pp - 3] = lit_part[lit_nows][pp];

                            // 그대로 machine_table[i]에 넣는다.
                            machine_table[i][pp - 3] = temp11[pp - 3];
                        }
                        machine_table[i][pp - 3] = '\0';
                    }
                }
            }

    //      printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);
            continue;
        }


        // machine_table[i]에 저장됨
        // 첫줄 둘째줄 셋째줄 스킵, 넷째줄부터 시작
        // if (i < 4) continue;
        if (i > -1) {
            // 1. opcode 인식, 형식 인식
            char* opcode = NULL;
            char op_format;

            opcode = NULL; // 초기화
            // 만약 operator 부분이 NULL이라면 주석임, 따라서 기계어 생성하지 않음
            if (token_table[i]->operator == NULL) {
                continue;
            }
            else {
                // operator 확인
                if (token_table[i]->operator != NULL) {
                    for (j = 0; j < inst_index; j++) {
                        if (strcmp(token_table[i]->operator, inst_table[j]->mnemonic) == 0) {
                            opcode = inst_table[j]->opcode;
                            op_format = inst_table[j]->format[0];
                            break;
                        }
                    }
                }
            }

            // 만족하는 opcode가 없을 경우 기계어를 만들 수 없으니 continue
            if (opcode == NULL) continue;

            // 첫글자는 정해짐
            machine_table[i][0] = opcode[0];

            // 두번째 글자를 알아내야 함
            // 우선 format이 2인지부터 알아내기
            if (op_format == '2') {
                machine_table[i][1] = opcode[1];
                
                if (token_table[i]->operand[0] == NULL) {
                    machine_table[i][2] = '0';
                }
                else if (token_table[i]->operand[0][0] == 'X') {
                    machine_table[i][2] = '1';
                }
                else if (token_table[i]->operand[0][0] == 'A') {
                    machine_table[i][2] = '0';
                }
                else if (token_table[i]->operand[0][0] == 'S') {
                    machine_table[i][2] = '4';
                }
                else if (token_table[i]->operand[0][0] == 'T') {
                    machine_table[i][2] = '5';
                }

                if (token_table[i]->operand[1] == NULL) {
                    machine_table[i][3] = '0';
                }
                else if (token_table[i]->operand[1][0] == 'X') {
                    machine_table[i][3] = '1';
                }
                else if (token_table[i]->operand[1][0] == 'A') {
                    machine_table[i][3] = '0';
                }
                else if (token_table[i]->operand[1][0] == 'S') {
                    machine_table[i][3] = '4';
                }
                else if (token_table[i]->operand[1][0] == 'T') {
                    machine_table[i][3] = '5';
                }

                machine_table[i][4] = '\0';
    //            printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);
                continue;
            }

            // 두번째 글자는 _ _ n i 인데, 우선 앞의 두 글자는 opcode에서 알아내야 함
            // 우선 first와 second부터 채우자.
            int first_1 = 0;      // 2^3
            int second_1 = 0;     // 2^2
            int third_1 = 0;      // 2^1
            int fourth_1 = 0;     // 2^0
            int opcode1;

            switch (opcode[1]) {
            case 'A': opcode1 = 10; break;
            case 'B': opcode1 = 11; break;
            case 'C': opcode1 = 12; break;
            case 'D': opcode1 = 13; break;
            case 'E': opcode1 = 14; break;
            case 'F': opcode1 = 15; break;
            default:
                opcode1 = (int)(opcode[1] - '0'); break;
            }

            if (opcode1 - 8 >= 0) {
                opcode1 -= 8;
                first_1 = 1;
            }
            else {
                first_1 = 0;
            }

            if (opcode1 - 4 >= 0) {
                opcode1 -= 4;
                second_1 = 1;
            }
            else {
                second_1 = 0;
            }

            // #이 붙어 있을 경우 n i는 각각 0 1 로 판정, @이 붙어 있을 경우 n i는 각각 1 0으로 판정, 나머지 경우는 1 1로 판정
            // 이는 nixbpe를 통해 알 수 있음
            if (token_table[i]->nixbpe == 'n') {
                third_1 = 1; fourth_1 = 0;
            }
            else if (token_table[i]->nixbpe == 'i') {
                third_1 = 0; fourth_1 = 1;
            }
            else {
                third_1 = 1; fourth_1 = 1;
            }

            // 두 번째 숫자 맞추기
            opcode1 = first_1 * 8 + second_1 * 4 + third_1 * 2 + fourth_1 * 1;
            switch (opcode1) {
            case 15: machine_table[i][1] = 'F'; break;
            case 14: machine_table[i][1] = 'E'; break;
            case 13: machine_table[i][1] = 'D'; break;
            case 12: machine_table[i][1] = 'C'; break;
            case 11: machine_table[i][1] = 'B'; break;
            case 10: machine_table[i][1] = 'A'; break;
            case 9: machine_table[i][1] = '9'; break;
            case 8: machine_table[i][1] = '8'; break;
            case 7: machine_table[i][1] = '7'; break;
            case 6: machine_table[i][1] = '6'; break;
            case 5: machine_table[i][1] = '5'; break;
            case 4: machine_table[i][1] = '4'; break;
            case 3: machine_table[i][1] = '3'; break;
            case 2: machine_table[i][1] = '2'; break;
            case 1: machine_table[i][1] = '1'; break;
            case 0: machine_table[i][1] = '0'; break;
            default: machine_table[i][1] = '0'; break;
            }

            // 2. xbpe 인식
            // x : operand[2]에 x가 있는 경우
            int first_2 = 0;      // 2^3 : x
            int second_2 = 0;     // 2^2 : b
            int third_2 = 0;      // 2^1 : p
            int fourth_2 = 0;     // 2^0 : e

            if (token_table[i]->operand[1] != NULL && strcmp(token_table[i]->operand[1], "X") == 0) {
                first_2 = 1;
            }
            else {
                first_2 = 0;
            }

            // e : opcode가 4형식인 경우
            if (op_format == '4')
                fourth_2 = 1;
            else
                fourth_2 = 0;

            if (strcmp(token_table[i]->operator, "RSUB") == 0) {
                machine_table[i][6] = '\0';
                machine_table[i][5] = '0';
                machine_table[i][4] = '0';
                machine_table[i][3] = '0';
                machine_table[i][2] = '0';
     //           printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);
                continue;
            }

            // b : base-relative인 경우, 아직 모름
            // p : pc-relative인 경우, 아직 모름
            // 주소를 알아내보자
            // 3. PC-relative vs Base-relative
            // 우선 pc-relative가 되는지 여부를 살펴봐야 한다. 우선 일반적인 경우부터
            
            if (token_table[i]->nixbpe == 'n') {
                // Indirect Addressing (간접 주소 접근)
                second_2 = 0;
                third_2 = 1;

                opcode1 = first_2 * 8 + second_2 * 4 + third_2 * 2 + fourth_2 * 1;
                switch (opcode1) {
                case 15: machine_table[i][2] = 'F'; break;
                case 14: machine_table[i][2] = 'E'; break;
                case 13: machine_table[i][2] = 'D'; break;
                case 12: machine_table[i][2] = 'C'; break;
                case 11: machine_table[i][2] = 'B'; break;
                case 10: machine_table[i][2] = 'A'; break;
                case 9: machine_table[i][2] = '9'; break;
                case 8: machine_table[i][2] = '8'; break;
                case 7: machine_table[i][2] = '7'; break;
                case 6: machine_table[i][2] = '6'; break;
                case 5: machine_table[i][2] = '5'; break;
                case 4: machine_table[i][2] = '4'; break;
                case 3: machine_table[i][2] = '3'; break;
                case 2: machine_table[i][2] = '2'; break;
                case 1: machine_table[i][2] = '1'; break;
                case 0: machine_table[i][2] = '0'; break;
                default: machine_table[i][2] = '0'; break;
                }

                // @ 뒤에 있는 주소의 값이 target이 됨
                // 이 뒷 자리수는 @ 뒤에 나오는 숫자를 기반으로 함
                // @ 뒤에 나오는 수를 봄
                char temp[10] = { '\0', '\0', '\0', '\0', '\0', '\0','\0', '\0', '\0', '\0' };
                for (int p = 1; p < strlen(token_table[i]->operand[0]); p++) {
                    temp[p - 1] = token_table[i]->operand[0][p];
                }

                // 나온 곳(temp)의 주소를 찾아야 함 -> sym_table을 이용함
                int csect_temp = 0;
                int temp2 = 0;
                for (int k = 0; k < sym_count; k++) {
                    // 자신의 영역에 있는 symbol 변수를 먼저 본다.
                    // MAXLEN의 경우 두 영역에 모두 존재하기 때문에 이를 구분하기 위함
                    if (sym_table[k].symbol[0] == '\0') csect_temp++;
                    if (csect_temp != csect) continue;

                    if (sym_table[k].symbol != NULL && strcmp(sym_table[k].symbol, temp) == 0) {
                        temp2 = sym_table[k].addr;
                        break;
                    }
                }
                if (temp2 == 0) continue;

                pc_counts = pc_counts_save[i];
                temp2 = temp2 - pc_counts;
                // 16진수 연산
                char hex[5] = { '0', '0', '0', '0', '0' };
                int pos = 0;

                while (1) {
                    int mod = temp2 % 16; // 16으로 나누었을 때 나머지
                    if (mod < 10) {
                        // 숫자 0의 ASCII 코드 값 48 + 나머지
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                        hex[pos] = 65 + mod - 10;
                    }

                    temp2 = temp2 / 16;

                    pos++;

                    if (temp2 == 0) break;
                }

                // 끝에서부터 숫자 맞추기
                machine_table[i][6] = '\0';
                machine_table[i][5] = hex[0];
                machine_table[i][4] = hex[1];
                machine_table[i][3] = hex[2];

    //            printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);


            }
            else if (token_table[i]->nixbpe == 'i') {
                // Immediate addressing (즉시 주소 접근0
                // b, p 모두 0
                second_2 = 0;
                third_2 = 0;

                opcode1 = first_2 * 8 + second_2 * 4 + third_2 * 2 + fourth_2 * 1;
                switch (opcode1) {
                case 15: machine_table[i][2] = 'F'; break;
                case 14: machine_table[i][2] = 'E'; break;
                case 13: machine_table[i][2] = 'D'; break;
                case 12: machine_table[i][2] = 'C'; break;
                case 11: machine_table[i][2] = 'B'; break;
                case 10: machine_table[i][2] = 'A'; break;
                case 9: machine_table[i][2] = '9'; break;
                case 8: machine_table[i][2] = '8'; break;
                case 7: machine_table[i][2] = '7'; break;
                case 6: machine_table[i][2] = '6'; break;
                case 5: machine_table[i][2] = '5'; break;
                case 4: machine_table[i][2] = '4'; break;
                case 3: machine_table[i][2] = '3'; break;
                case 2: machine_table[i][2] = '2'; break;
                case 1: machine_table[i][2] = '1'; break;
                case 0: machine_table[i][2] = '0'; break;
                default: machine_table[i][2] = '0'; break;
                }

                // 이 뒷 자리수는 # 뒤에 나오는 숫자를 기반으로 함
                // # 뒤에 나오는 수를 봄
                char temp[10];
                for (int p = 1; p < strlen(token_table[i]->operand[0]); p++) {
                    temp[p - 1] = token_table[i]->operand[0][p];
                }
                int temp2;
                temp2 = atoi(temp);

                // 16진수 연산
                char hex[5] = { '0', '0', '0', '0', '0' };
                int pos = 0;

                while (1) {
                    int mod = temp2 % 16; // 16으로 나누었을 때 나머지
                    if (mod < 10) {
                        // 숫자 0의 ASCII 코드 값 48 + 나머지
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                        hex[pos] = 65 + mod - 10;
                    }

                    temp2 = temp2 / 16;

                    pos++;

                    if (temp2 == 0) break;
                }

                // 끝에서부터 숫자 맞추기
                machine_table[i][6] = '\0';
                machine_table[i][5] = hex[0];
                machine_table[i][4] = hex[1];
                machine_table[i][3] = hex[2];

     //           printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);

            }
            else {

                char hex[5] = { '0', '0', '0', '0', '0' };

                int target = -1;
                int displacement;
                int csect_count = 0;
                                          
                // 우선 operand가 EXTREFS(외부에서 쓰이는 것)인지를 본다. 이 경우, 기계어 주소값은 전부 0이 된다.
                for (int pp = 0; ; pp++) {
                    if (EXTREFS[pp][0] == '\0') break;
                    if (strcmp(token_table[i]->operand[0], EXTREFS[pp]) == 0) {
                        displacement = 0x0;
                        // 이 경우 b = 0, p = 0이고 그 이후 기계어 주소값에 0을 넣어 준다.
                        second_2 = 0;     // 2^2 : b
                        third_2 = 0;      // 2^1 : p

                        // 세 번째 숫자 맞추기
                        opcode1 = first_2 * 8 + second_2 * 4 + third_2 * 2 + fourth_2 * 1;
                        switch (opcode1) {
                        case 15: machine_table[i][2] = 'F'; break;
                        case 14: machine_table[i][2] = 'E'; break;
                        case 13: machine_table[i][2] = 'D'; break;
                        case 12: machine_table[i][2] = 'C'; break;
                        case 11: machine_table[i][2] = 'B'; break;
                        case 10: machine_table[i][2] = 'A'; break;
                        case 9: machine_table[i][2] = '9'; break;
                        case 8: machine_table[i][2] = '8'; break;
                        case 7: machine_table[i][2] = '7'; break;
                        case 6: machine_table[i][2] = '6'; break;
                        case 5: machine_table[i][2] = '5'; break;
                        case 4: machine_table[i][2] = '4'; break;
                        case 3: machine_table[i][2] = '3'; break;
                        case 2: machine_table[i][2] = '2'; break;
                        case 1: machine_table[i][2] = '1'; break;
                        case 0: machine_table[i][2] = '0'; break;
                        default: machine_table[i][2] = '0'; break;
                        }

                        if (op_format == '3') {
                            // 끝에서부터 숫자 맞추기
                            machine_table[i][6] = '\0';
                            machine_table[i][5] = hex[0];
                            machine_table[i][4] = hex[1];
                            machine_table[i][3] = hex[2];
                        }
                        else if (op_format == '4') {
                            // 끝에서부터 숫자 맞추기
                            machine_table[i][8] = '\0';
                            machine_table[i][7] = '0';
                            machine_table[i][6] = '0';
                            machine_table[i][5] = '0';
                            machine_table[i][4] = '0';
                            machine_table[i][3] = '0';

                        }
     //                   printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);

                        break;
                    }
                }

                for (int k = 0; k < sym_count; k++) {
                    // 자신의 영역에 있는 symbol 변수를 먼저 본다.
                    // MAXLEN의 경우 두 영역에 모두 존재하기 때문에 이를 구분하기 위함
                    if (sym_table[k].symbol[0] == '\0') csect_count++;
                    if (csect_count != csect) continue;

                    if (sym_table[k].symbol != NULL && strcmp(sym_table[k].symbol, token_table[i]->operand[0]) == 0) {
                        target = sym_table[k].addr;
                        break;
                    }
                }

                // 자신의 영역에 있는 literal 변수 또한 먼저 본다.
                // operand가 literal일 수도 있기 떄문
                if (token_table[i]->operand != NULL && token_table[i]->operand[0][0] == '=') {
                    for (int pp = 0; pp < lit_now; pp++) {
                        if (strcmp(literal_table[pp].literal, token_table[i]->operand[0]) == 0) {

                            // lit_part에 들어 있는지를 확인 (추후 LTONG나 END를 만났을 때 값을 찍기 위함)
                            int ppp;
                            for (ppp = 0; ppp < lit_index; ppp++) {
                                if (strcmp(lit_part[lit_index], literal_table[ppp].literal) == 0) break;
                            }
                            if (ppp == lit_index) {
                                strcpy(lit_part[lit_index], literal_table[pp].literal);
                                lit_index++;
                            }
                            target = literal_table[pp].addr;
                            break;
                        }
                    }
                }

                if (target == -1) {
                    continue;
                }
                else if (target == 0) {
                    displacement = 0x0;
                }
                else {
                    pc_counts = pc_counts_save[i];
                       
                    if (target > pc_counts)
                        displacement = target - pc_counts;
                    else {
                        if (op_format == '3')
                            displacement = 16 * 16 * 16 * 16 - pc_counts + target;
                        else if (op_format == '4')
                            displacement = 16 * 16 * 16 * 16 * 16 - pc_counts + target;
                    }
                        
                    int pos = 0;

                    while (1) {
                        int mod = displacement % 16; // 16으로 나누었을 때 나머지
                        if (mod < 10) {
                            // 숫자 0의 ASCII 코드 값 48 + 나머지
                            hex[pos] = 48 + mod;
                        }
                        else {
                            // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                            hex[pos] = 65 + mod - 10;
                        }

                        displacement = displacement / 16;

                        pos++;

                        if (displacement == 0) break;
                    }

                    if (op_format == '4' || abs(target - pc_counts) < 16*16*16) {
                        // pc-relative 성립
                        second_2 = 0;     // 2^2 : b
                        third_2 = 1;      // 2^1 : p

                        // 세 번째 숫자 맞추기
                        opcode1 = first_2 * 8 + second_2 * 4 + third_2 * 2 + fourth_2 * 1;
                        switch (opcode1) {
                        case 15: machine_table[i][2] = 'F'; break;
                        case 14: machine_table[i][2] = 'E'; break;
                        case 13: machine_table[i][2] = 'D'; break;
                        case 12: machine_table[i][2] = 'C'; break;
                        case 11: machine_table[i][2] = 'B'; break;
                        case 10: machine_table[i][2] = 'A'; break;
                        case 9: machine_table[i][2] = '9'; break;
                        case 8: machine_table[i][2] = '8'; break;
                        case 7: machine_table[i][2] = '7'; break;
                        case 6: machine_table[i][2] = '6'; break;
                        case 5: machine_table[i][2] = '5'; break;
                        case 4: machine_table[i][2] = '4'; break;
                        case 3: machine_table[i][2] = '3'; break;
                        case 2: machine_table[i][2] = '2'; break;
                        case 1: machine_table[i][2] = '1'; break;
                        case 0: machine_table[i][2] = '0'; break;
                        default: machine_table[i][2] = '0'; break;
                        }

                        if (op_format == '3') {
                            // 끝에서부터 숫자 맞추기
                            machine_table[i][6] = '\0';
                            machine_table[i][5] = hex[0];
                            machine_table[i][4] = hex[1];
                            machine_table[i][3] = hex[2];
                        }
                        else if (op_format == '4') {
                            // 끝에서부터 숫자 맞추기
                            machine_table[i][9] = '\0';
                            machine_table[i][8] = hex[0];
                            machine_table[i][7] = hex[1];
                            machine_table[i][6] = hex[2];
                            machine_table[i][5] = hex[3];

                        }
     //                   printf("%d번째 줄 변환된 기계어 : %s\n", i, machine_table[i]);
                    }


                }
            }
            

        }

    }
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	/* add your code here */

    FILE* file;

    // 인자로 NULL값이 들어올 경우, 파일 포인터를 stdout으로 설정한다.
    // 그렇지 않다면 파일의 이름을 파일 포인터로 지정한다.
    if (file_name == NULL) {
        file = stdout;
    }
    else {
        file = fopen(file_name, "w"); // 쓰기 모드로 열며 파일이 없으면 파일을 생성한다.
    }

    // 주소값이 저장되어 있는 pc_counts_save 배열과
    // 기계어를 저장했던 machine_table 배열을 이용한다.

    char RDREC_SAVE[10][7];
    int rdrec_save_count = 0;

    char M_SAVE[100][100];
    int m_save_count = 0;

    int start = 0;
    int ii = 0;
    int T_sw = 0; // T 스위치가 켜져 있으면 1, 꺼져 있으면 0
    int T_length = 0; // T 요소 길이
    int T_start = 0;  // T 시작주소 저장
    char T_SAVE[100]; // T Object Code가 한 줄씩 저장
    char T_SAVE_TEMP[100]; // T열에 들어가는 기계어(machine_table[i])들을 저장

    for (int i = 0; i < locctr_i; i++) {
        //if (i != 0) {
        //    printf("%s\n", T_SAVE_TEMP);
        //}
        // ---------------------------------------------------------- H -------------------------------------------------------------------------- //
        // 각각의 control section의 시작지점 찾기
        // 첫줄이 주석 줄이 아닌 시작지점이거나, 주소값이 0으로 시작하는 지점을 찾기 (첫줄이 주석줄일 경우를 대비해서 두번째 조건문 추가)
        if ((i == 0 && token_table[i]->label != NULL) || (pc_now_save[i] == 0 && start == 0) || (pc_now_save[i] == 0 && pc_now_save[i - 1] != 0)) {
            // T_sw가 1인 경우 마지막 T줄 출력
            if (T_sw == 1) {
                T_sw = 0;
                // T 시작지점 길이 내용
                // int T_start -> char T_start_char[7]
                // int T_length -> char T_length_char[3]
                // char T_SAVE_TEMP[]
                char T_start_char[7];
                char T_length_char[3];

                // 16진수로 변환 후 문자열로 바꿈
                int pos = 0;
                char hex[7];

                while (1) {
                    int mod = T_start % 16; // 16으로 나누었을 때 나머지
                    if (mod < 10) {
                        // 숫자 0의 ASCII 코드 값 48 + 나머지
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                        hex[pos] = 65 + mod - 10;
                    }

                    T_start = T_start / 16;
                    pos++;
                    if (T_start == 0) break;
                }

                for (int j = 0; j < pos; j++) {
                    T_start_char[5 - j] = hex[j];
                }
                for (int j = 5 - pos; j > -1; j--) {
                    T_start_char[j] = '0';
                }
                T_start_char[6] = '\0';

                // 16진수로 변환 후 문자열로 바꿈
                pos = 0;
                memset(hex, '\0', sizeof(hex));

                while (1) {
                    int mod = T_length % 16; // 16으로 나누었을 때 나머지
                    if (mod < 10) {
                        // 숫자 0의 ASCII 코드 값 48 + 나머지
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                        hex[pos] = 65 + mod - 10;
                    }

                    T_length = T_length / 16;
                    pos++;
                    if (T_length == 0) break;
                }

                for (int j = 0; j < pos; j++) {
                    T_length_char[1 - j] = hex[j];
                }
                for (int j = 1 - pos; j > -1; j--) {
                    T_length_char[j] = '0';
                }
                T_length_char[2] = '\0';

                // fprintf를 사용해서 출력
                // 예시 : fprintf(file, "%s", token_table[i]->operand[k]);
                fprintf(file, "T%s%s%s\n", T_start_char, T_length_char, T_SAVE_TEMP);

            }

            // M, E 출력 후 넘어감
            if (start != 0) {
                // M 출력
                for (int pp = 0; pp < m_save_count; pp++)
                    fprintf(file, "M%s\n", M_SAVE[pp]);
                m_save_count = 0;

                // E 출력
                if (start == 1) {
                    fprintf(file, "E000000\n\n"); // START가 있는 곳은 시작 주소를 적어준다.
                }
                else
                    fprintf(file, "E\n\n");
            }

            start++;
            char cs_name[7];
            for (int j = 0; j < strlen(token_table[i]->label); j++) {
                cs_name[j] = token_table[i]->label[j];
            }
            for (int j = strlen(token_table[i]->label); j < 7; j++) {
                cs_name[j] = ' ';
            }
            cs_name[6] = '\0';

            char cs_start_pos[7] = "000000"; // 시작주소는 모두 000000
            char cs_full_length[7]; // 총 길이
            // 총 길이는 cs_length에 저장되어 있음
            // cs_length를 16진수로 바꾸어서 저장하면 됨.


            // 10진수를 16진수로 변환
            int temp = cs_length[start - 1];
            int pos = 0;
            char hex[7];

            while (1) {
                int mod = temp % 16; // 16으로 나누었을 때 나머지
                if (mod < 10) {
                    // 숫자 0의 ASCII 코드 값 48 + 나머지
                    hex[pos] = 48 + mod;
                }
                else {
                    // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                    hex[pos] = 65 + mod - 10;
                }

                temp = temp / 16;
                pos++;
                if (temp == 0) break;
            }
            // 문자열의 형태로 저장
            cs_full_length[6] = '\0';
            for (int j = 0; j < pos; j++) {
                cs_full_length[5 - j] = hex[j];
            }
            for (int j = 5-pos; j > -1; j--) {
                cs_full_length[j] = '0';
            }

            fprintf(file, "H%s%s%s\n", cs_name, cs_start_pos, cs_full_length);
        }

        // ---------------------------------------------------------- D -------------------------------------------------------------------------- //
        else if (token_table[i]->operator != NULL && strcmp(token_table[i]->operator, "EXTDEF") == 0) {
            fprintf(file, "D");

            int counts2 = 0;
            for (int pp = 0; pp < MAX_OPERAND; pp++) {
                char temp3[7];

                // 변수이름 (ex: BUFFER)
                if (token_table[i]->operand[pp] != NULL) {
                    for (int ppp = 0; ppp < strlen(token_table[i]->operand[pp]); ppp++) {
                        temp3[ppp] = token_table[i]->operand[pp][ppp];
                    }
                    for (int ppp = strlen(token_table[i]->operand[pp]); ppp < 7; ppp++) {
                        temp3[ppp] = ' ';
                    }
                    temp3[6] = '\0';


                    char temp4[7];
                    // 변수주소 (ex: 000033)
                    for (int ppp = 0; ppp < sym_count; ppp++) {
                        if (strcmp(token_table[i]->operand[pp], sym_table[ppp].symbol) == 0) {
                            int addr_t = sym_table[ppp].addr;
                            // 10진수를 16진수로 바꿔야 함
                            int pos = 0;
                            char hex[7];

                            while (1) {
                                int mod = addr_t % 16; // 16으로 나누었을 때 나머지
                                if (mod < 10) {
                                    // 숫자 0의 ASCII 코드 값 48 + 나머지
                                    hex[pos] = 48 + mod;
                                }
                                else {
                                    // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                                    hex[pos] = 65 + mod - 10;
                                }

                                addr_t = addr_t / 16;
                                pos++;
                                if (addr_t == 0) break;
                            }

                            for (int j = 0; j < pos; j++) {
                                temp4[5 - j] = hex[j];
                            }
                            for (int j = 5 - pos; j > -1; j--) {
                                temp4[j] = '0';
                            }

                            temp4[6] = '\0';
                            break;
                        }
                    }

                    fprintf(file, "%s%s", temp3, temp4);
                }

            }
            fprintf(file, "\n");
        }

        // ---------------------------------------------------------- R -------------------------------------------------------------------------- //
        else if (token_table[i]->operator != NULL && strcmp(token_table[i]->operator, "EXTREF") == 0) {
            rdrec_save_count = 0;
            
            fprintf(file, "R");

            for (int pp = 0; pp < MAX_OPERAND; pp++) {
                if (token_table[i]->operand[pp] != NULL) {
                    strcpy(RDREC_SAVE[rdrec_save_count], token_table[i]->operand[pp]);
                    rdrec_save_count++;
                    fprintf(file, "%s", token_table[i]->operand[pp]);
                    for (int ppp = 0; ppp < 6 - strlen(token_table[i]->operand[pp]); ppp++) {
                        fprintf(file, " ");
                    }
                }
            }

            fprintf(file, "\n");
        }

        // ---------------------------------------------------------- T, M도 같이 확인------------------------------------------------------------- //
        else if (token_table[i]->operator != NULL) {

            // M에 들어갈 요소가 있는지 확인
            for (int pp = 0; token_table[i]->operand[pp] != NULL; pp++) {
                for (int ppp = rdrec_save_count - 1; ppp > -1; ppp--) {
                    if (strstr(token_table[i]->operand[pp], RDREC_SAVE[ppp]) != NULL) {
                        // M_SAVE[m_save_count]에 넣음
                        // 현재 주소 + 1, (3,5,6 중 하나), (+, - 부호), 문자열
                        char temp4[7];
                        int addr_t = pc_now_save[i] + 1;
                        // 현재 pc_now_save[i]에 +1을 하지 않아도 되는 경우 (BUFEND-BUFFER 이런식으로 주소를 모르는 경우에 대한 예외처리)
                        if (strcmp(machine_table[i], "000000") == 0)
                            addr_t--;

                        // 현재 주소를 16진수로 변환 후 문자열로 바꿈
                        int pos = 0;
                        char hex[7];

                        while (1) {
                            int mod = addr_t % 16; // 16으로 나누었을 때 나머지
                            if (mod < 10) {
                                // 숫자 0의 ASCII 코드 값 48 + 나머지
                                hex[pos] = 48 + mod;
                            }
                            else {
                                // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                                hex[pos] = 65 + mod - 10;
                            }

                            addr_t = addr_t / 16;
                            pos++;
                            if (addr_t == 0) break;
                        }

                        for (int j = 0; j < pos; j++) {
                            temp4[5 - j] = hex[j];
                        }
                        for (int j = 5 - pos; j > -1; j--) {
                            temp4[j] = '0';
                        }

                        temp4[6] = '\0';

                        char temp5[3];
                        if (strcmp(machine_table[i], "000000") == 0)
                            strcpy(temp5, "06");
                        else if (strlen(machine_table[i]) == 8)
                            strcpy(temp5, "05");
                        else
                            strcpy(temp5, "03");

                        // +- 부호와 문자열
                        char temp6[8];
                        if (strcmp(strstr(token_table[i]->operand[pp], RDREC_SAVE[ppp]), token_table[i]->operand[pp]) == 0)
                            temp6[0] = '+';
                        else
                            temp6[0] = '-';

                        int j;
                        for (j = 1; j <= strlen(RDREC_SAVE[ppp]); j++) {
                            temp6[j] = RDREC_SAVE[ppp][j - 1];
                        }
                        temp6[j] = '\0';

                        // m_save[m_save_count]에 넣음
                        M_SAVE[m_save_count][0] = '\0';
                        strcat(M_SAVE[m_save_count], temp4);
                        strcat(M_SAVE[m_save_count], temp5);
                        strcat(M_SAVE[m_save_count], temp6);

                    //    printf("M%s\n", M_SAVE[m_save_count]);
                        m_save_count++;

                    }
                }
            }


            // T에 들어가는 요소를 넣음
            if (T_sw == 0 && machine_table[i][0] != '\0') {
                // 초기화 먼저
                memset(T_SAVE_TEMP, '\0', sizeof(T_SAVE_TEMP));
                T_sw = 1;
                T_start = pc_now_save[i];
                T_length = 0;

                int size = strlen(machine_table[i]) / 2;
                if (size + T_length <= 29) {
                    T_length += size;
                    strcat(T_SAVE_TEMP, machine_table[i]);
                }
            }
            else if (T_sw != 0 && machine_table[i][0] != '\0') {
                int size = strlen(machine_table[i]) / 2;

                if (size + T_length <= 29) {
                    T_length += size;
                    strcat(T_SAVE_TEMP, machine_table[i]);
                }
                else {
                    // 이 경우는 지금까지 누적되었던 T_SAVE_TEMP를 먼저 T_SAVE에 담은 뒤 출력
                    // 그 뒤 다음 줄을 더함


                    // T 시작지점 길이 내용
                    // int T_start -> char T_start_char[7]
                    // int T_length -> char T_length_char[3]
                    // char T_SAVE_TEMP[]
                    char T_start_char[7];
                    char T_length_char[3];

                    // 16진수로 변환 후 문자열로 바꿈
                    int pos = 0;
                    char hex[7];

                    while (1) {
                        int mod = T_start % 16; // 16으로 나누었을 때 나머지
                        if (mod < 10) {
                            // 숫자 0의 ASCII 코드 값 48 + 나머지
                            hex[pos] = 48 + mod;
                        }
                        else {
                            // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                            hex[pos] = 65 + mod - 10;
                        }

                        T_start = T_start / 16;
                        pos++;
                        if (T_start == 0) break;
                    }

                    for (int j = 0; j < pos; j++) {
                        T_start_char[5 - j] = hex[j];
                    }
                    for (int j = 5 - pos; j > -1; j--) {
                        T_start_char[j] = '0';
                    }
                    T_start_char[6] = '\0';
                    // 16진수로 변환 후 문자열로 바꿈
                    pos = 0;
                    memset(hex, '\0', sizeof(hex));

                    while (1) {
                        int mod = T_length % 16; // 16으로 나누었을 때 나머지
                        if (mod < 10) {
                            // 숫자 0의 ASCII 코드 값 48 + 나머지
                            hex[pos] = 48 + mod;
                        }
                        else {
                            // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                            hex[pos] = 65 + mod - 10;
                        }

                        T_length = T_length / 16;
                        pos++;
                        if (T_length == 0) break;
                    }

                    for (int j = 0; j < pos; j++) {
                        T_length_char[1 - j] = hex[j];
                    }
                    for (int j = 1 - pos; j > -1; j--) {
                        T_length_char[j] = '0';
                    }
                    T_length_char[2] = '\0';

                    fprintf(file, "T%s%s%s\n", T_start_char, T_length_char, T_SAVE_TEMP);

                    // 초기화
                    T_start = pc_now_save[i];
                    T_length = size;
                    memset(T_SAVE_TEMP, '\0', sizeof(T_SAVE_TEMP));
                    strcat(T_SAVE_TEMP, machine_table[i]);
                }
            }
            else if (T_sw != 0 && machine_table[i][0] == '\0') {
                T_sw = 0;
                // 여기에서는 무조건 출력
                // T 시작지점 길이 내용
                // int T_start -> char T_start_char[7]
                // int T_length -> char T_length_char[3]
                // char T_SAVE_TEMP[]
                char T_start_char[7];
                char T_length_char[3];

                // 16진수로 변환 후 문자열로 바꿈
                int pos = 0;
                char hex[7];

                while (1) {
                    int mod = T_start % 16; // 16으로 나누었을 때 나머지
                    if (mod < 10) {
                        // 숫자 0의 ASCII 코드 값 48 + 나머지
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                        hex[pos] = 65 + mod - 10;
                    }

                    T_start = T_start / 16;
                    pos++;
                    if (T_start == 0) break;
                }

                for (int j = 0; j < pos; j++) {
                    T_start_char[5 - j] = hex[j];
                }
                for (int j = 5 - pos; j > -1; j--) {
                    T_start_char[j] = '0';
                }
                T_start_char[6] = '\0';

                // 16진수로 변환 후 문자열로 바꿈
                pos = 0;
                memset(hex, '\0', sizeof(hex));

                while (1) {
                    int mod = T_length % 16; // 16으로 나누었을 때 나머지
                    if (mod < 10) {
                        // 숫자 0의 ASCII 코드 값 48 + 나머지
                        hex[pos] = 48 + mod; 
                    }
                    else {
                        // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                        hex[pos] = 65 + mod - 10;
                    }

                    T_length = T_length / 16;
                    pos++;
                    if (T_start == 0) break;
                }
                
                for (int j = 0; j < pos; j++) {
                    T_length_char[1 - j] = hex[j];
                }
                for (int j = 1 - pos; j > -1; j--) {
                    T_length_char[j] = '0';
                }
                T_length_char[2] = '\0';
                // printf("T %s %s %s\n", T_start_char, T_length_char, T_SAVE_TEMP);
                fprintf(file, "T%s%s%s\n", T_start_char, T_length_char, T_SAVE_TEMP);
            }
            
        }

    }

    // 마지막 END 부분 정리 후 함수 종료
    // T_sw가 1인 경우 마지막 T줄 출력
    if (T_sw == 1) {
        T_sw = 0;
        // T 시작지점 길이 내용
        // int T_start -> char T_start_char[7]
        // int T_length -> char T_length_char[3]
        // char T_SAVE_TEMP[]
        char T_start_char[7];
        char T_length_char[3];

        // 16진수로 변환 후 문자열로 바꿈
        int pos = 0;
        char hex[7];

        while (1) {
            int mod = T_start % 16; // 16으로 나누었을 때 나머지
            if (mod < 10) {
                // 숫자 0의 ASCII 코드 값 48 + 나머지
                hex[pos] = 48 + mod;
            }
            else {
                // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                hex[pos] = 65 + mod - 10;
            }

            T_start = T_start / 16;
            pos++;
            if (T_start == 0) break;
        }

        for (int j = 0; j < pos; j++) {
            T_start_char[5 - j] = hex[j];
        }
        for (int j = 5 - pos; j > -1; j--) {
            T_start_char[j] = '0';
        }
        T_start_char[6] = '\0';

        // 16진수로 변환 후 문자열로 바꿈
        pos = 0;
        memset(hex, '\0', sizeof(hex));

        while (1) {
            int mod = T_length % 16; // 16으로 나누었을 때 나머지
            if (mod < 10) {
                // 숫자 0의 ASCII 코드 값 48 + 나머지
                hex[pos] = 48 + mod;
            }
            else {
                // 나머지에서 10을 뺀 값 + 영문 대문자 A의 ASCII 코드 값
                hex[pos] = 65 + mod - 10;
            }

            T_length = T_length / 16;
            pos++;
            if (T_length == 0) break;
        }

        for (int j = 0; j < pos; j++) {
            T_length_char[1 - j] = hex[j];
        }
        for (int j = 1 - pos; j > -1; j--) {
            T_length_char[j] = '0';
        }
        T_length_char[2] = '\0';
        fprintf(file, "T%s%s%s\n", T_start_char, T_length_char, T_SAVE_TEMP);

    }

    // M, E 출력 후 넘어감
    if (start != 0) {
        // M 출력
        for (int pp = 0; pp < m_save_count; pp++)
            fprintf(file, "M%s\n", M_SAVE[pp]);
        m_save_count = 0;
        
        // E 출력
        fprintf(file, "E\n\n");
    }

    // 파일 포인터 닫기
    if (file_name != NULL) {
        fclose(file);
    }

    return 0;
}