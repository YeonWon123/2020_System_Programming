#define _CRT_SECURE_NO_WARNINGS

/*
 * ȭ�ϸ� : my_assembler_20150413.c 
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 * ���� ������ ���Ǵ� ���ڿ� "00000000"���� �ڽ��� �й��� �����Ѵ�.
 */

/*
 *
 * ���α׷��� ����� �����Ѵ�. 
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

// ���ϸ��� "00000000"�� �ڽ��� �й����� ������ ��.
#include "my_assembler_20150413.h"

/* ----------------------------------------------------------------------------------
 * ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
 * �Ű� : ���� ����, ����� ���� 
 * ��ȯ : ���� = 0, ���� = < 0 
 * ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�. 
 *		   ���� �߰������� �������� �ʴ´�. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{
	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n");
		return -1;
	}

	if (assem_pass1() < 0)
	{
		printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}
	// make_opcode_output("output_20150413");

    // make_symtab_output(NULL); // cmdâ�� ���
    // make_literaltab_output(NULL); // cmdâ�� ���
	make_symtab_output("symtab_20150413");
	make_literaltab_output("literaltab_20150413");
	if (assem_pass2() < 0)
	{
		printf("assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}

    // make_objectcode_output(NULL); // cmdâ�� ���
	make_objectcode_output("output_20150413");

	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�. 
 * �Ű� : ����
 * ��ȯ : �������� = 0 , ���� �߻� = -1
 * ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ� 
 *		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
 *		   �����Ͽ���. 
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
 * ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)�� 
 *        �����ϴ� �Լ��̴�. 
 * �Ű� : ���� ��� ����
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
 *	
 *	===============================================================================
 *		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char* inst_file)
{
    FILE* file;
    int errno;

    /* add your code here */
    // ���پ� �Է¹޾Ƽ� �̸�, ����, ���� �ڵ�, ���۷����� ������ �Է¹޴´�.
    if ((file = fopen(inst_file, "r")) != NULL) { // �б� ���� ����.

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
 * ���� : ����� �� �ҽ��ڵ带 �о� �ҽ��ڵ� ���̺�(input_data)�� �����ϴ� �Լ��̴�. 
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0  
 * ���� : ���δ����� �����Ѵ�.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char* input_file)
{
    FILE* file;
    int errno;

    /* add your code here */
    if ((file = fopen(input_file, "r")) != NULL) { // �б� ���� ����.

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
 * ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�.
 *        �н� 1�� ���� ȣ��ȴ�.
 * �Ű� : �Ľ��� ���ϴ� ���ڿ�
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�.
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char* str)
{
    /* add your code here */

    token_table[token_line] = malloc(sizeof(token));

    // ù ���ڰ� ������ �ƴ϶�� label�� �ְų� �ּ��̶�� �ǹ�
    // ������ ������ ���, NULL�� �ʱ�ȭ�ϰų� malloc�� �޸𸮸� �Ҵ�������.
    // ������ ���θ�, �޸� ���� ��ü�� ���� �ʾ� �������� ������ �����.
    char* ptr = NULL;
    char* buffer = NULL;

    // �켱 ���� NULL�� �ʱ�ȭ��Ű��.
    token_table[token_line]->label = NULL;
    token_table[token_line]->operator = NULL;
    token_table[token_line]->operand[0] = NULL;
    token_table[token_line]->operand[1] = NULL;
    token_table[token_line]->operand[2] = NULL;
    token_table[token_line]->comment = NULL;

    // str �ް� ���� '\n'���� ��ū�и��� �� ó���� �ϸ� ��!
    ptr = strtok(str, "\n");

    if (!isspace(str[0])) {
        // �ּ��� ���
        if (str[0] == '.') {
            token_table[token_line]->comment = str;
        }
        // ��ū�� �ִ� ���
        else {
            // label �и�
            ptr = strtok(ptr, "\t");
            token_table[token_line]->label = malloc(sizeof(char) * strlen(ptr));
            sprintf(token_table[token_line]->label, "%s", ptr);

            // operator �и�
            ptr = strtok(NULL, "\t");
            token_table[token_line]->operator = malloc(sizeof(char)* strlen(ptr));
            sprintf(token_table[token_line]->operator, "%s", ptr);
        }
    }
    // ù ���ڰ� �����̶��, label�� ���ٴ� �ǹ�
    // ���� �� ���� operator���� �и� ����
    else {
    ptr = strtok(ptr, "\t");
    if (ptr != NULL) {
        token_table[token_line]->operator = malloc(sizeof(char) * strlen(ptr));
        sprintf(token_table[token_line]->operator, "%s", ptr);
    }
    }

    // ���� �κ��� �Ʒ��� �ڵ� (�� �� ��ü�� �ּ��� ���, ptr�� ��� NULL�� �ǹǷ� ū ������ ����)
    // operand �и� (��, operand�� ���� ���, ���ٸ� NULL�� ����)
    ptr = strtok(NULL, "\n");

    if (ptr != NULL) {
        // operand�� �����ϴ��� ���θ� ���캽, �켱 operator�� comment�� �ִ����� ���캽
        if (ptr[0] != '\t') {
            // operand�� ������.
            ptr = strtok(ptr, "\t");
            if (ptr != NULL) {
                // operand�� 2�� �̻� ���� ��� ','���� �����ؾ� ��
                // �ϴ��� �Ѿ��(strtok���� �ϴ� ��ü������ ������ �ڿ� ����)
                buffer = malloc(sizeof(char) * strlen(ptr));
                sprintf(buffer, "%s", ptr);
            }

            // comment �и� (��, comment�� ���� ���, ���ٸ� NULL�� ����)
            ptr = strtok(NULL, "\0");
            if (ptr != NULL) {
                token_table[token_line]->comment = malloc(sizeof(char) * strlen(ptr));
                sprintf(token_table[token_line]->comment, "%s", ptr);
            }

            // operand �и� ����, 2�� �̻��ϼ��� �ְ� �׷��� ���� ���� ����
            // ���� buffer�� NULL�̶�� operand�� �����Ƿ� �������� ����
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
            // operand�� �������� ������ comment�� �����Ҽ��� ����. ���� comment �и� �۾� ����
            // comment �и� (��, comment�� ���� ���, ���ٸ� NULL�� ����)
            ptr = strtok(ptr, "\0");
            if (ptr != NULL) {
                token_table[token_line]->comment = malloc(sizeof(char) * strlen(ptr));
                sprintf(token_table[token_line]->comment, "%s", ptr);
            }
        }
    }

    // ��ū����, operand��
    // #�� �پ� ���� ��� n i�� ���� 0 1 ���� ���� -> nixbpe = i
    // @�� �پ� ���� ��� n i�� ���� 1 0 ���� ���� -> nixbpe = n
    // ������ ���� 1 1�� ���� -> nixbpe = �켱 NULL�� ����
    if (token_table[token_line]->operand[0] != NULL) {
        if (token_table[token_line]->operand[0][0] == '#') {
            token_table[token_line]->nixbpe = 'i';
        }
        else if (token_table[token_line]->operand[0][0] == '@') {
            token_table[token_line]->nixbpe = 'n';
        }
    }

    // token_line�� 1 �������Ѽ�, ������ �Լ��� �Ҹ� �� �������� �����ϰԲ� ��.
    token_line++;
}

/* ----------------------------------------------------------------------------------
 * ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�.
 * �Ű� : ��ū ������ ���е� ���ڿ�
 * ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0
 * ���� :
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
        // mnemonic�� �߰��ϸ� ���� ���� (return 0)
        if (strcmp(inst_table[i]->mnemonic, str) == 0) {
            // �� �� �ּҰ�(locctr)�� ��ɾ� ���ĸ�ŭ ������Ŵ
            pc_counts += atoi(inst_table[i]->format);
            pc_counts_save[locctr_i] = pc_counts;
            return 0;
        }
    }

    // mnemonic�� �߰����� ������ ���, directives���� Ȯ�� (pseudo-instructions)
    // directives���, �ش� directives�� �´� Ȱ�� ����

    // START�� END�� ���� locctr�� �������� ����
    if (strcmp("START", str) == 0) {
        return 0;
    }

    if (strcmp("END", str) == 0) {
        // �� end�� ��� �ּҰ� �Ҵ���� ���� literal���� �ּҸ� �Ҵ��� �־�� ��
        for (int j = lit_now; j < lit_count; j++) {
            literal_table[j].addr = locctr;
            int sum = 0;
            if (literal_table[j].literal[1] == 'X') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++;
                sum = sum / 2; // 16�����̰�, �����̹Ƿ� 2���� 1����Ʈ�� �����Ǿ� ����
            }
            else if (literal_table[j].literal[1] == 'C') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++; // ���� ������ŭ BYTE ���� �Ҵ�
            }
            pc_counts += sum;
            locctr += sum;
        }
        lit_now = lit_count;

        // ��������� control section ������ ������
        cs_length[cs_length_counts] = locctr;
        cs_length_counts++;

        return 0;
    }


    if (strcmp("BYTE", str) == 0) {
        // BYTE ũ�⸸ŭ locctr ������Ŵ
        if (token_table[locctr_i]->operand[0] == NULL)
            return -1; // operand�� ���� ����
        else {
            int sum = 0;
            if (token_table[locctr_i]->operand[0][0] == 'X') {
                for (int k = 2; token_table[locctr_i]->operand[0][k] != '\''; k++)
                    sum++;
                sum = sum / 2;  // 16�����̰�, �����̹Ƿ� 2���� 1����Ʈ�� �����Ǿ� ����
            }
            else if (token_table[locctr_i]->operand[0][0] == 'C') {
                for (int k = 2; token_table[locctr_i]->operand[0][k] != '\''; k++)
                    sum++;  // ���� ������ŭ BYTE ���� �Ҵ�
            }
            pc_counts += sum;
        }
        return 0;
    }
        
    if (strcmp("WORD", str) == 0) {
        // WORD ũ�⸸ŭ locctr ������Ŵ
        
        pc_counts += 3;
        return 0;
    }

    if (strcmp("RESB", str) == 0) {
        // ���� * 1��ŭ locctr ������Ŵ 
        pc_counts += atoi(token_table[locctr_i]->operand[0]);
        return 0;
    }

    if (strcmp("RESW", str) == 0) {
        // ���� * 3��ŭ locctr ������Ŵ
        pc_counts += atoi(token_table[locctr_i]->operand[0]) * 3;
        return 0;
    }

    if (strcmp("EXTDEF", str) == 0) 
        return 0;
 
    if (strcmp("EXTREF", str) == 0)
        return 0;

    if (strcmp("EQU", str) == 0) {
        // =�� ������ �����ϸ�, symtab���� ������
        return 0;
    }

    if (strcmp("CSECT", str) == 0) {
        // ��������� control section ������ ������
        cs_length[cs_length_counts] = locctr;
        cs_length_counts++;

        // �ּҰ��� 0���� �ʱ�ȭ�� ��
        locctr = 0;
        pc_counts = 0;
        pc_now_save[locctr_i] = pc_counts;
        return 0;
    }


    if (strcmp("LTORG", str) == 0) {
        // ���ݱ��� ã�Ҵ� literal�� ���� �ּҸ� �Ҵ��� �ش�.
        for (int j = lit_now; j < lit_count; j++) {
            literal_table[j].addr = locctr;

            int sum = 0;
            if (literal_table[j].literal[1] == 'X') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++;
                sum = sum / 2; // 16�����̰�, �����̹Ƿ� 2���� 1����Ʈ�� �����Ǿ� ����
            }
            else if (literal_table[j].literal[1] == 'C') {
                for (int k = 3; literal_table[j].literal[k] != '\''; k++)
                    sum++; // ���� ������ŭ BYTE ���� �Ҵ�
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


    // ���� �ڵ尡 �ƴ��� �ǹ���
    return -1;
}


/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
*		   �н�1������..
*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
*		   ���̺��� �����Ѵ�.
*
* �Ű� : ����
* ��ȯ : ���� ���� = 0 , ���� = < 0
* ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
*	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
    /* add your code here */

    /* input_data�� ���ڿ��� ���پ� �Է� �޾Ƽ�
     * token_parsing()�� ȣ���Ͽ� token_unit�� ����
     */
    int i;
    for (i = 0; i < line_num; i++) {
        token_parsing(input_data[i]);
        // ���� ó�� : ������ -1 (0���� ���� ��) ����
        if (token_table[i]->operator != NULL && search_opcode(token_table[i]->operator) < 0) {
            return -1;
        }

        // symtab ó��
        if (token_table[i]->label != NULL) {
            if (token_table[i]->operator != NULL && strcmp(token_table[i]->operator, "CSECT") == 0)
                sym_count++;

            if (strcmp(token_table[i]->operator, "EQU") == 0 && (strcmp(token_table[i]->operand[0], "*") != 0)) {
                // �� ���� BUFEND-BUFFER�� ���� ������ �ϱ� ����
                // token_table[i]->operand[0]�� �� ���� : BUFEND-BUFFER
                strcpy(sym_table[sym_count].symbol, token_table[i]->label);
                // BUFEND-BUFFER�� ����ϱ� ����. �� A-B�� ����ϱ� ���� �뵵
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
                    sym_table[sym_count].addr = temp1a - temp2a; // -��ȣ�� �������� BUFEND�� BUFFER�� ������ ���
                else if (token_table[i]->operand[0][j] == '+')
                    sym_table[sym_count].addr = temp1a + temp2a; // +��ȣ�� �������� BUFEND�� BUFFER�� ������ ���

                sym_count++;
            }
            else {
                strcpy(sym_table[sym_count].symbol, token_table[i]->label);
                sym_table[sym_count].addr = locctr;
                sym_count++;
            }
        }

        // literaltab ó��, �ּ��Ҵ��� LTONG �Ǵ� END ������ ��
        if (token_table[i]->operand[0] != NULL && token_table[i]->operand[0][0] == '=') {
            // ������ �ִ� literal���� Ȯ��, ������ �߰�
            int sw = 0;
            for (int j = 0; j < lit_count; j++) {
                if (strcmp(literal_table[j].literal, token_table[i]->operand[0]) == 0) { 
                    // =C'EOF', =X'05' �� ������. ����� ���� =C', ' �̷� �κе��� ������, C�� X�� ���� ������ �߿��Ͽ� �̷��� ����
                    sw = 1;
                    break;
                }
            }

            if (sw == 0) {
                // =C'EOF', =X'05' �� ������. ����� ���� =C', ' �̷� �κе��� ������, C�� X�� ���� ������ �߿��Ͽ� �̷��� ����
                strcpy(literal_table[lit_count].literal, token_table[i]->operand[0]);
                lit_count++;
            }
        }

        locctr_i++;
    }

    return 0;
}


/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 5��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*        ���� ���� 5�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
* -----------------------------------------------------------------------------------
*/
// void make_opcode_output(char *file_name)
// {
// 	/* add your code here */

// }

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ SYMBOL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	/* add your code here */
    
    FILE* file;

    // ���ڷ� NULL���� ���� ���, ���� �����͸� stdout���� �����Ѵ�.
    // �׷��� �ʴٸ� ������ �̸��� ���� �����ͷ� �����Ѵ�.
    if (file_name == NULL) {
        file = stdout;
    }
    else {
        file = fopen(file_name, "w"); // ���� ���� ���� ������ ������ ������ �����Ѵ�.
    }

    /* add your code here */
  
    // fprintf�� ����ؼ� ���
    // ���� : fprintf(file, "%s", token_table[i]->operand[k]);

    for (int i = 0; i < sym_count; i++) {
        // �߰��� ���Ƿ� ĭ�� ������� �κ��� ������
        if (sym_table[i].symbol[0] != '\0')
            fprintf(file, "%s\t\t%x\n", sym_table[i].symbol, sym_table[i].addr);
        else
            fprintf(file, "\n");
    }

    if (file_name != NULL) {
        fclose(file);
    }
    else {
        printf("\n\n"); // symtab�� literaltab�� �����ϱ� ���� �뵵
    }

}

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ LITERAL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char *file_name)
{
	/* add your code here */

    FILE* file;

    // ���ڷ� NULL���� ���� ���, ���� �����͸� stdout���� �����Ѵ�.
    // �׷��� �ʴٸ� ������ �̸��� ���� �����ͷ� �����Ѵ�.
    if (file_name == NULL) {
        file = stdout;
    }
    else {
        file = fopen(file_name, "w"); // ���� ���� ���� ������ ������ ������ �����Ѵ�.
    }

    /* add your code here */

    // fprintf�� ����ؼ� ���
    // ���� : fprintf(file, "%s", token_table[i]->operand[k]);

    for (int i = 0; i < lit_now; i++) {
        //printf("literal_table : %s\n", literal_table[i].literal);
        char temp[7];
        if (literal_table[i].literal[1] != 'C' && literal_table[i].literal[1] != 'X') {
            // �׳� ����
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
        printf("\n\n"); // symtab�� literaltab�� �����ϱ� ���� �뵵
    }

}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
*		   ������ ���� �۾��� ����Ǿ� ����.
*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
* �Ű� : ����
* ��ȯ : �������� = 0, �����߻� = < 0
* ���� :
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
    char EXTDEFS[10][10]; // EXTDEF ���� ����
    char EXTREFS[10][10]; // EXTREF ���� ����

    for (i = 0; i < token_line; i++) {
        locctr = pc_counts;

        // ���� operator �κ��� NULL�̶�� �ּ���, ���� ���� �������� ����
        if (token_table[i]->operator == NULL) {
            continue;
        }

        // START ���� �켱�� SKIP
        if (strcmp(token_table[i]->operator, "START") == 0) {
            continue;
        }

        // CSECT�� ������ ���, cesct int ������ 1 ������Ŵ
        // �̴� �ڽ��� ������ �ִ� symtab ������ �켱������ Ȯ���ϱ� ����
        if (strcmp(token_table[i]->operator, "CSECT") == 0) {
            csect++;
            memset(EXTREFS, "\0", sizeof(EXTREFS));
            continue;
        }

        // EXTREF�� ������ ���
        if (strcmp(token_table[i]->operator, "EXTREF") == 0) {
            for (int pp = 0; pp < MAX_OPERAND; pp++) {
                if (token_table[i]->operand[pp] != NULL) {
                    strcpy(EXTREFS[pp], token_table[i]->operand[pp]);
                }
            }
            continue;
        }

        // EXTDEF�� ������ ���
        if (strcmp(token_table[i]->operator, "EXTDEF") == 0) {
            for (int pp = 0; pp < MAX_OPERAND; pp++) {
                if (token_table[i]->operand[pp] != NULL) {
                    strcpy(EXTDEFS[pp], token_table[i]->operand[pp]);
                }
            }
            continue;
        }

        // BYTE�� ������ ���
        if (strcmp(token_table[i]->operator, "BYTE") == 0) {
            char temp11[20];
            memset(temp11, '\0', sizeof(temp11));
            if (token_table[i]->operand[0][0] == 'X') {
                // 16���� �Է¹��� �� �״�� ����� ��ȯ��
                int pp;
                for (pp = 2; token_table[i]->operand[0][pp] != '\''; pp++) {
                    machine_table[i][pp - 2] = token_table[i]->operand[0][pp];
                }
                machine_table[i][pp] = '\0';
            }
            
    //        printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);
            continue;
        }

        // WORD�� ������ ���
        if (strcmp(token_table[i]->operator, "WORD") == 0) {
            // �ڿ� �ִ� ������ ����� �Ұ����� ��� 000000 �Ҵ�
            if (atoi(token_table[i]->operand[0]) == 0) {
                // ���� ��� �Ұ���, 000000 �Ҵ�
                machine_table[i][0] = '0';
                machine_table[i][1] = '0';
                machine_table[i][2] = '0';
                machine_table[i][3] = '0';
                machine_table[i][4] = '0';
                machine_table[i][5] = '0';
                machine_table[i][6] = '\0';
            }
            else {
                // ���ڷ� ��ȯ�� ��ŭ �Ҵ��ϸ� �̸� 16������ ��ȯ ��, ���ڿ��� ���·� machine_table�� ����
                int cc = atoi(token_table[i]->operand[0]);

                // 10������ 16������ ��ȯ
                int pos = 0;
                char hex[5];

                while (1) {
                    int mod = cc % 16; // 16���� �������� �� ������
                    if (mod < 10) {
                        // ���� 0�� ASCII �ڵ� �� 48 + ������
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

    //        printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);
            continue;

        }

        // LTORG�� ������ ���
        if (strcmp(token_table[i]->operator, "LTORG") == 0) {
            char temp11[20];
            memset(temp11, '\0', sizeof(temp11));
            for (; lit_nows < lit_index; lit_nows++) {
                if (lit_part[lit_nows][1] == 'C') {
                    // ���ڿ��� �޴´�.
                    int index3 = 0;
                    for (int pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                        temp11[pp - 3] = lit_part[lit_nows][pp];
                        // E : 45, O : 4F, F : 46 (16����)
                        // ���ڿ��� ASCII code�� ��ȯ �� machine_table[i]�� �ִ´�.
                        int cc = temp11[pp - 3];
                        
                        // 10������ 16������ ��ȯ
                        int pos = 0;
                        char hex[5];

                        while (1) {
                            int mod = cc % 16; // 16���� �������� �� ������
                            if (mod < 10) {
                                // ���� 0�� ASCII �ڵ� �� 48 + ������
                                hex[pos] = 48 + mod;
                            }
                            else {
                                // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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
                    // 16������ �޴´�.
                    int pp = 3;
                    for (pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                        temp11[pp - 3] = lit_part[lit_nows][pp];

                        // �״�� machine_table[i]�� �ִ´�.
                        machine_table[i][pp-3] = temp11[pp - 3];
                    }
                    machine_table[i][pp - 3] = '\0';
                }
                else {
                    // machine_table�� ������ �� �ڸ��� ����
                    int pp = 3;
                    for (pp = 3; machine_table[i][pp] != '\0'; pp++);
                    // ���ڸ� �ִ´�.
                    machine_table[i][pp] = '0';
                    machine_table[i][pp + 1] = '0';
                    machine_table[i][pp + 2] = '0';
                    machine_table[i][pp + 3] = '0';
                    machine_table[i][pp + 4] = '0';
                    machine_table[i][pp + 5] = lit_part[lit_nows][1];
                    machine_table[i][pp + 6] = '\0';
                }
            }

        //    printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);
            continue;
        }

        // END�� ������ ���
        if (strcmp(token_table[i]->operator, "END") == 0) {
            for (; lit_nows < lit_index; lit_nows++) {
                char temp11[20];
                memset(temp11, '\0', sizeof(temp11));
                for (; lit_nows < lit_index; lit_nows++) {
                    if (lit_part[lit_nows][1] == 'C') {
                        // ���ڿ��� �޴´�.
                        int index3 = 0;
                        for (int pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                            temp11[pp - 3] = lit_part[lit_nows][pp];
                            // E : 45, O : 4F, F : 46 (16����)
                            // ���ڿ��� ASCII code�� ��ȯ �� machine_table[i]�� �ִ´�.
                            int cc = temp11[pp - 3];

                            // 10������ 16������ ��ȯ
                            int pos = 0;
                            char hex[5];

                            while (1) {
                                int mod = cc % 16; // 16���� �������� �� ������
                                if (mod < 10) {
                                    // ���� 0�� ASCII �ڵ� �� 48 + ������
                                    hex[pos] = 48 + mod;
                                }
                                else {
                                    // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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
                        // 16������ �޴´�.
                        int pp = 3;
                        for (pp = 3; lit_part[lit_nows][pp] != '\''; pp++) {
                            temp11[pp - 3] = lit_part[lit_nows][pp];

                            // �״�� machine_table[i]�� �ִ´�.
                            machine_table[i][pp - 3] = temp11[pp - 3];
                        }
                        machine_table[i][pp - 3] = '\0';
                    }
                }
            }

    //      printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);
            continue;
        }


        // machine_table[i]�� �����
        // ù�� ��°�� ��°�� ��ŵ, ��°�ٺ��� ����
        // if (i < 4) continue;
        if (i > -1) {
            // 1. opcode �ν�, ���� �ν�
            char* opcode = NULL;
            char op_format;

            opcode = NULL; // �ʱ�ȭ
            // ���� operator �κ��� NULL�̶�� �ּ���, ���� ���� �������� ����
            if (token_table[i]->operator == NULL) {
                continue;
            }
            else {
                // operator Ȯ��
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

            // �����ϴ� opcode�� ���� ��� ��� ���� �� ������ continue
            if (opcode == NULL) continue;

            // ù���ڴ� ������
            machine_table[i][0] = opcode[0];

            // �ι�° ���ڸ� �˾Ƴ��� ��
            // �켱 format�� 2�������� �˾Ƴ���
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
    //            printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);
                continue;
            }

            // �ι�° ���ڴ� _ _ n i �ε�, �켱 ���� �� ���ڴ� opcode���� �˾Ƴ��� ��
            // �켱 first�� second���� ä����.
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

            // #�� �پ� ���� ��� n i�� ���� 0 1 �� ����, @�� �پ� ���� ��� n i�� ���� 1 0���� ����, ������ ���� 1 1�� ����
            // �̴� nixbpe�� ���� �� �� ����
            if (token_table[i]->nixbpe == 'n') {
                third_1 = 1; fourth_1 = 0;
            }
            else if (token_table[i]->nixbpe == 'i') {
                third_1 = 0; fourth_1 = 1;
            }
            else {
                third_1 = 1; fourth_1 = 1;
            }

            // �� ��° ���� ���߱�
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

            // 2. xbpe �ν�
            // x : operand[2]�� x�� �ִ� ���
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

            // e : opcode�� 4������ ���
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
     //           printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);
                continue;
            }

            // b : base-relative�� ���, ���� ��
            // p : pc-relative�� ���, ���� ��
            // �ּҸ� �˾Ƴ�����
            // 3. PC-relative vs Base-relative
            // �켱 pc-relative�� �Ǵ��� ���θ� ������� �Ѵ�. �켱 �Ϲ����� ������
            
            if (token_table[i]->nixbpe == 'n') {
                // Indirect Addressing (���� �ּ� ����)
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

                // @ �ڿ� �ִ� �ּ��� ���� target�� ��
                // �� �� �ڸ����� @ �ڿ� ������ ���ڸ� ������� ��
                // @ �ڿ� ������ ���� ��
                char temp[10] = { '\0', '\0', '\0', '\0', '\0', '\0','\0', '\0', '\0', '\0' };
                for (int p = 1; p < strlen(token_table[i]->operand[0]); p++) {
                    temp[p - 1] = token_table[i]->operand[0][p];
                }

                // ���� ��(temp)�� �ּҸ� ã�ƾ� �� -> sym_table�� �̿���
                int csect_temp = 0;
                int temp2 = 0;
                for (int k = 0; k < sym_count; k++) {
                    // �ڽ��� ������ �ִ� symbol ������ ���� ����.
                    // MAXLEN�� ��� �� ������ ��� �����ϱ� ������ �̸� �����ϱ� ����
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
                // 16���� ����
                char hex[5] = { '0', '0', '0', '0', '0' };
                int pos = 0;

                while (1) {
                    int mod = temp2 % 16; // 16���� �������� �� ������
                    if (mod < 10) {
                        // ���� 0�� ASCII �ڵ� �� 48 + ������
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
                        hex[pos] = 65 + mod - 10;
                    }

                    temp2 = temp2 / 16;

                    pos++;

                    if (temp2 == 0) break;
                }

                // ���������� ���� ���߱�
                machine_table[i][6] = '\0';
                machine_table[i][5] = hex[0];
                machine_table[i][4] = hex[1];
                machine_table[i][3] = hex[2];

    //            printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);


            }
            else if (token_table[i]->nixbpe == 'i') {
                // Immediate addressing (��� �ּ� ����0
                // b, p ��� 0
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

                // �� �� �ڸ����� # �ڿ� ������ ���ڸ� ������� ��
                // # �ڿ� ������ ���� ��
                char temp[10];
                for (int p = 1; p < strlen(token_table[i]->operand[0]); p++) {
                    temp[p - 1] = token_table[i]->operand[0][p];
                }
                int temp2;
                temp2 = atoi(temp);

                // 16���� ����
                char hex[5] = { '0', '0', '0', '0', '0' };
                int pos = 0;

                while (1) {
                    int mod = temp2 % 16; // 16���� �������� �� ������
                    if (mod < 10) {
                        // ���� 0�� ASCII �ڵ� �� 48 + ������
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
                        hex[pos] = 65 + mod - 10;
                    }

                    temp2 = temp2 / 16;

                    pos++;

                    if (temp2 == 0) break;
                }

                // ���������� ���� ���߱�
                machine_table[i][6] = '\0';
                machine_table[i][5] = hex[0];
                machine_table[i][4] = hex[1];
                machine_table[i][3] = hex[2];

     //           printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);

            }
            else {

                char hex[5] = { '0', '0', '0', '0', '0' };

                int target = -1;
                int displacement;
                int csect_count = 0;
                                          
                // �켱 operand�� EXTREFS(�ܺο��� ���̴� ��)������ ����. �� ���, ���� �ּҰ��� ���� 0�� �ȴ�.
                for (int pp = 0; ; pp++) {
                    if (EXTREFS[pp][0] == '\0') break;
                    if (strcmp(token_table[i]->operand[0], EXTREFS[pp]) == 0) {
                        displacement = 0x0;
                        // �� ��� b = 0, p = 0�̰� �� ���� ���� �ּҰ��� 0�� �־� �ش�.
                        second_2 = 0;     // 2^2 : b
                        third_2 = 0;      // 2^1 : p

                        // �� ��° ���� ���߱�
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
                            // ���������� ���� ���߱�
                            machine_table[i][6] = '\0';
                            machine_table[i][5] = hex[0];
                            machine_table[i][4] = hex[1];
                            machine_table[i][3] = hex[2];
                        }
                        else if (op_format == '4') {
                            // ���������� ���� ���߱�
                            machine_table[i][8] = '\0';
                            machine_table[i][7] = '0';
                            machine_table[i][6] = '0';
                            machine_table[i][5] = '0';
                            machine_table[i][4] = '0';
                            machine_table[i][3] = '0';

                        }
     //                   printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);

                        break;
                    }
                }

                for (int k = 0; k < sym_count; k++) {
                    // �ڽ��� ������ �ִ� symbol ������ ���� ����.
                    // MAXLEN�� ��� �� ������ ��� �����ϱ� ������ �̸� �����ϱ� ����
                    if (sym_table[k].symbol[0] == '\0') csect_count++;
                    if (csect_count != csect) continue;

                    if (sym_table[k].symbol != NULL && strcmp(sym_table[k].symbol, token_table[i]->operand[0]) == 0) {
                        target = sym_table[k].addr;
                        break;
                    }
                }

                // �ڽ��� ������ �ִ� literal ���� ���� ���� ����.
                // operand�� literal�� ���� �ֱ� ����
                if (token_table[i]->operand != NULL && token_table[i]->operand[0][0] == '=') {
                    for (int pp = 0; pp < lit_now; pp++) {
                        if (strcmp(literal_table[pp].literal, token_table[i]->operand[0]) == 0) {

                            // lit_part�� ��� �ִ����� Ȯ�� (���� LTONG�� END�� ������ �� ���� ��� ����)
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
                        int mod = displacement % 16; // 16���� �������� �� ������
                        if (mod < 10) {
                            // ���� 0�� ASCII �ڵ� �� 48 + ������
                            hex[pos] = 48 + mod;
                        }
                        else {
                            // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
                            hex[pos] = 65 + mod - 10;
                        }

                        displacement = displacement / 16;

                        pos++;

                        if (displacement == 0) break;
                    }

                    if (op_format == '4' || abs(target - pc_counts) < 16*16*16) {
                        // pc-relative ����
                        second_2 = 0;     // 2^2 : b
                        third_2 = 1;      // 2^1 : p

                        // �� ��° ���� ���߱�
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
                            // ���������� ���� ���߱�
                            machine_table[i][6] = '\0';
                            machine_table[i][5] = hex[0];
                            machine_table[i][4] = hex[1];
                            machine_table[i][3] = hex[2];
                        }
                        else if (op_format == '4') {
                            // ���������� ���� ���߱�
                            machine_table[i][9] = '\0';
                            machine_table[i][8] = hex[0];
                            machine_table[i][7] = hex[1];
                            machine_table[i][6] = hex[2];
                            machine_table[i][5] = hex[3];

                        }
     //                   printf("%d��° �� ��ȯ�� ���� : %s\n", i, machine_table[i]);
                    }


                }
            }
            

        }

    }
}

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	/* add your code here */

    FILE* file;

    // ���ڷ� NULL���� ���� ���, ���� �����͸� stdout���� �����Ѵ�.
    // �׷��� �ʴٸ� ������ �̸��� ���� �����ͷ� �����Ѵ�.
    if (file_name == NULL) {
        file = stdout;
    }
    else {
        file = fopen(file_name, "w"); // ���� ���� ���� ������ ������ ������ �����Ѵ�.
    }

    // �ּҰ��� ����Ǿ� �ִ� pc_counts_save �迭��
    // ��� �����ߴ� machine_table �迭�� �̿��Ѵ�.

    char RDREC_SAVE[10][7];
    int rdrec_save_count = 0;

    char M_SAVE[100][100];
    int m_save_count = 0;

    int start = 0;
    int ii = 0;
    int T_sw = 0; // T ����ġ�� ���� ������ 1, ���� ������ 0
    int T_length = 0; // T ��� ����
    int T_start = 0;  // T �����ּ� ����
    char T_SAVE[100]; // T Object Code�� �� �پ� ����
    char T_SAVE_TEMP[100]; // T���� ���� ����(machine_table[i])���� ����

    for (int i = 0; i < locctr_i; i++) {
        //if (i != 0) {
        //    printf("%s\n", T_SAVE_TEMP);
        //}
        // ---------------------------------------------------------- H -------------------------------------------------------------------------- //
        // ������ control section�� �������� ã��
        // ù���� �ּ� ���� �ƴ� ���������̰ų�, �ּҰ��� 0���� �����ϴ� ������ ã�� (ù���� �ּ����� ��츦 ����ؼ� �ι�° ���ǹ� �߰�)
        if ((i == 0 && token_table[i]->label != NULL) || (pc_now_save[i] == 0 && start == 0) || (pc_now_save[i] == 0 && pc_now_save[i - 1] != 0)) {
            // T_sw�� 1�� ��� ������ T�� ���
            if (T_sw == 1) {
                T_sw = 0;
                // T �������� ���� ����
                // int T_start -> char T_start_char[7]
                // int T_length -> char T_length_char[3]
                // char T_SAVE_TEMP[]
                char T_start_char[7];
                char T_length_char[3];

                // 16������ ��ȯ �� ���ڿ��� �ٲ�
                int pos = 0;
                char hex[7];

                while (1) {
                    int mod = T_start % 16; // 16���� �������� �� ������
                    if (mod < 10) {
                        // ���� 0�� ASCII �ڵ� �� 48 + ������
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

                // 16������ ��ȯ �� ���ڿ��� �ٲ�
                pos = 0;
                memset(hex, '\0', sizeof(hex));

                while (1) {
                    int mod = T_length % 16; // 16���� �������� �� ������
                    if (mod < 10) {
                        // ���� 0�� ASCII �ڵ� �� 48 + ������
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

                // fprintf�� ����ؼ� ���
                // ���� : fprintf(file, "%s", token_table[i]->operand[k]);
                fprintf(file, "T%s%s%s\n", T_start_char, T_length_char, T_SAVE_TEMP);

            }

            // M, E ��� �� �Ѿ
            if (start != 0) {
                // M ���
                for (int pp = 0; pp < m_save_count; pp++)
                    fprintf(file, "M%s\n", M_SAVE[pp]);
                m_save_count = 0;

                // E ���
                if (start == 1) {
                    fprintf(file, "E000000\n\n"); // START�� �ִ� ���� ���� �ּҸ� �����ش�.
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

            char cs_start_pos[7] = "000000"; // �����ּҴ� ��� 000000
            char cs_full_length[7]; // �� ����
            // �� ���̴� cs_length�� ����Ǿ� ����
            // cs_length�� 16������ �ٲپ �����ϸ� ��.


            // 10������ 16������ ��ȯ
            int temp = cs_length[start - 1];
            int pos = 0;
            char hex[7];

            while (1) {
                int mod = temp % 16; // 16���� �������� �� ������
                if (mod < 10) {
                    // ���� 0�� ASCII �ڵ� �� 48 + ������
                    hex[pos] = 48 + mod;
                }
                else {
                    // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
                    hex[pos] = 65 + mod - 10;
                }

                temp = temp / 16;
                pos++;
                if (temp == 0) break;
            }
            // ���ڿ��� ���·� ����
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

                // �����̸� (ex: BUFFER)
                if (token_table[i]->operand[pp] != NULL) {
                    for (int ppp = 0; ppp < strlen(token_table[i]->operand[pp]); ppp++) {
                        temp3[ppp] = token_table[i]->operand[pp][ppp];
                    }
                    for (int ppp = strlen(token_table[i]->operand[pp]); ppp < 7; ppp++) {
                        temp3[ppp] = ' ';
                    }
                    temp3[6] = '\0';


                    char temp4[7];
                    // �����ּ� (ex: 000033)
                    for (int ppp = 0; ppp < sym_count; ppp++) {
                        if (strcmp(token_table[i]->operand[pp], sym_table[ppp].symbol) == 0) {
                            int addr_t = sym_table[ppp].addr;
                            // 10������ 16������ �ٲ�� ��
                            int pos = 0;
                            char hex[7];

                            while (1) {
                                int mod = addr_t % 16; // 16���� �������� �� ������
                                if (mod < 10) {
                                    // ���� 0�� ASCII �ڵ� �� 48 + ������
                                    hex[pos] = 48 + mod;
                                }
                                else {
                                    // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

        // ---------------------------------------------------------- T, M�� ���� Ȯ��------------------------------------------------------------- //
        else if (token_table[i]->operator != NULL) {

            // M�� �� ��Ұ� �ִ��� Ȯ��
            for (int pp = 0; token_table[i]->operand[pp] != NULL; pp++) {
                for (int ppp = rdrec_save_count - 1; ppp > -1; ppp--) {
                    if (strstr(token_table[i]->operand[pp], RDREC_SAVE[ppp]) != NULL) {
                        // M_SAVE[m_save_count]�� ����
                        // ���� �ּ� + 1, (3,5,6 �� �ϳ�), (+, - ��ȣ), ���ڿ�
                        char temp4[7];
                        int addr_t = pc_now_save[i] + 1;
                        // ���� pc_now_save[i]�� +1�� ���� �ʾƵ� �Ǵ� ��� (BUFEND-BUFFER �̷������� �ּҸ� �𸣴� ��쿡 ���� ����ó��)
                        if (strcmp(machine_table[i], "000000") == 0)
                            addr_t--;

                        // ���� �ּҸ� 16������ ��ȯ �� ���ڿ��� �ٲ�
                        int pos = 0;
                        char hex[7];

                        while (1) {
                            int mod = addr_t % 16; // 16���� �������� �� ������
                            if (mod < 10) {
                                // ���� 0�� ASCII �ڵ� �� 48 + ������
                                hex[pos] = 48 + mod;
                            }
                            else {
                                // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

                        // +- ��ȣ�� ���ڿ�
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

                        // m_save[m_save_count]�� ����
                        M_SAVE[m_save_count][0] = '\0';
                        strcat(M_SAVE[m_save_count], temp4);
                        strcat(M_SAVE[m_save_count], temp5);
                        strcat(M_SAVE[m_save_count], temp6);

                    //    printf("M%s\n", M_SAVE[m_save_count]);
                        m_save_count++;

                    }
                }
            }


            // T�� ���� ��Ҹ� ����
            if (T_sw == 0 && machine_table[i][0] != '\0') {
                // �ʱ�ȭ ����
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
                    // �� ���� ���ݱ��� �����Ǿ��� T_SAVE_TEMP�� ���� T_SAVE�� ���� �� ���
                    // �� �� ���� ���� ����


                    // T �������� ���� ����
                    // int T_start -> char T_start_char[7]
                    // int T_length -> char T_length_char[3]
                    // char T_SAVE_TEMP[]
                    char T_start_char[7];
                    char T_length_char[3];

                    // 16������ ��ȯ �� ���ڿ��� �ٲ�
                    int pos = 0;
                    char hex[7];

                    while (1) {
                        int mod = T_start % 16; // 16���� �������� �� ������
                        if (mod < 10) {
                            // ���� 0�� ASCII �ڵ� �� 48 + ������
                            hex[pos] = 48 + mod;
                        }
                        else {
                            // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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
                    // 16������ ��ȯ �� ���ڿ��� �ٲ�
                    pos = 0;
                    memset(hex, '\0', sizeof(hex));

                    while (1) {
                        int mod = T_length % 16; // 16���� �������� �� ������
                        if (mod < 10) {
                            // ���� 0�� ASCII �ڵ� �� 48 + ������
                            hex[pos] = 48 + mod;
                        }
                        else {
                            // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

                    // �ʱ�ȭ
                    T_start = pc_now_save[i];
                    T_length = size;
                    memset(T_SAVE_TEMP, '\0', sizeof(T_SAVE_TEMP));
                    strcat(T_SAVE_TEMP, machine_table[i]);
                }
            }
            else if (T_sw != 0 && machine_table[i][0] == '\0') {
                T_sw = 0;
                // ���⿡���� ������ ���
                // T �������� ���� ����
                // int T_start -> char T_start_char[7]
                // int T_length -> char T_length_char[3]
                // char T_SAVE_TEMP[]
                char T_start_char[7];
                char T_length_char[3];

                // 16������ ��ȯ �� ���ڿ��� �ٲ�
                int pos = 0;
                char hex[7];

                while (1) {
                    int mod = T_start % 16; // 16���� �������� �� ������
                    if (mod < 10) {
                        // ���� 0�� ASCII �ڵ� �� 48 + ������
                        hex[pos] = 48 + mod;
                    }
                    else {
                        // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

                // 16������ ��ȯ �� ���ڿ��� �ٲ�
                pos = 0;
                memset(hex, '\0', sizeof(hex));

                while (1) {
                    int mod = T_length % 16; // 16���� �������� �� ������
                    if (mod < 10) {
                        // ���� 0�� ASCII �ڵ� �� 48 + ������
                        hex[pos] = 48 + mod; 
                    }
                    else {
                        // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

    // ������ END �κ� ���� �� �Լ� ����
    // T_sw�� 1�� ��� ������ T�� ���
    if (T_sw == 1) {
        T_sw = 0;
        // T �������� ���� ����
        // int T_start -> char T_start_char[7]
        // int T_length -> char T_length_char[3]
        // char T_SAVE_TEMP[]
        char T_start_char[7];
        char T_length_char[3];

        // 16������ ��ȯ �� ���ڿ��� �ٲ�
        int pos = 0;
        char hex[7];

        while (1) {
            int mod = T_start % 16; // 16���� �������� �� ������
            if (mod < 10) {
                // ���� 0�� ASCII �ڵ� �� 48 + ������
                hex[pos] = 48 + mod;
            }
            else {
                // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

        // 16������ ��ȯ �� ���ڿ��� �ٲ�
        pos = 0;
        memset(hex, '\0', sizeof(hex));

        while (1) {
            int mod = T_length % 16; // 16���� �������� �� ������
            if (mod < 10) {
                // ���� 0�� ASCII �ڵ� �� 48 + ������
                hex[pos] = 48 + mod;
            }
            else {
                // ���������� 10�� �� �� + ���� �빮�� A�� ASCII �ڵ� ��
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

    // M, E ��� �� �Ѿ
    if (start != 0) {
        // M ���
        for (int pp = 0; pp < m_save_count; pp++)
            fprintf(file, "M%s\n", M_SAVE[pp]);
        m_save_count = 0;
        
        // E ���
        fprintf(file, "E\n\n");
    }

    // ���� ������ �ݱ�
    if (file_name != NULL) {
        fclose(file);
    }

    return 0;
}