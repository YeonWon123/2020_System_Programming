import pickle # 파이썬 객체를 파일에 저장함

 # 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 # instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.

class Instruction():
     
    def __init__(self, lines):
        line = lines.split("\n")
        self.parsing(line[0])
        
    def parsing(self, line):
        self.arr = line.split("\t")
        self.inst_name = self.arr[0]
        self.nFormat = self.arr[1]
        self.opcode = self.arr[2]
        self.numberOfOperand = self.arr[3]
        
# 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
# 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.

class InstTable():    

    # 클래스 초기화. 파싱을 동시에 처리한다.
    # @param instFile : instuction에 대한 명세가 저장된 파일 이름
            
    def __init__(self, instFile):
        self.instMap = dict()
        self.openFile(instFile)
            
    # 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
        
    def openFile(self, instFile):
        f = open(instFile, 'r')
        lines = f.readlines()
        for line in lines:
            inst = Instruction(line)
            #print([inst.nFormat, inst.opcode, inst.numberOfOperand])
            self.instMap[inst.inst_name] = [inst.nFormat, inst.opcode, inst.numberOfOperand]
        f.close()
        #print(self.instMap)
        #print(self.instMap.keys())
        #print(self.instMap.values())
        
#inst = InstTable("inst.data")

class Token:
    # bit 조작의 가독성을 위한 선언
    nFlag = 32
    iFlag = 16
    xFlag = 8
    bFlag = 4
    pFlag = 2
    eFlag = 1
    
    # 의미 분석 단계에서 사용되는 변수들
    location = 0
    label = ""
    operator = ""
    operand = []
    comment = ""
    nixbpe = 0

    # object code 생성 단계에서 사용되는 변수들
    objectCode = ""
    byteSize = 0
    
    def __init__(self, line):
        self.parsing(line)
        
    def parsing(self, line):
        if (line[0] == '.'):
            comment = line
        else:
            #print("파싱시작!")
            #print(line)
            arr = line.split('\t')
            #print(arr)
            if (len(arr) > 0):
                self.label = arr[0]
            if (len(arr) > 1):
                self.operator = arr[1]
            if (len(arr) > 2):
                self.operand = arr[2].split(',')
            if (len(arr) > 3):
                self.comment = arr[3]
               
            self.setFlag(self.pFlag, 1)
            self.setFlag(self.bFlag, 0)
            if (len(self.operand) > 0):
                if (len(self.operand[0]) > 0 and self.operand[0][0] == '#'):
                    self.setFlag(self.iFlag, 1)
                    self.setFlag(self.pFlag, -1)
                elif (len(self.operand[0]) > 0 and self.operand[0][0] == '@'):
                    self.setFlag(self.nFlag, 1)
                else:
                    self.setFlag(self.nFlag, 1)
                    self.setFlag(self.iFlag, 1)
                    
            for i in range (0, len(self.operand)):
                if (self.operand[i] == "X"):
                    self.setFlag(self.xFlag, 1)
                else:
                    self.setFlag(self.xFlag, 0)
                    
            if (self.operator[0] == '+'):
                self.setFlag(self.eFlag, 1)
                self.setFlag(self.pFlag, -1)
            else:
                self.setFlag(self.eFlag, 0)
    
    def setFlag(self, flag, value):
        self.nixbpe = self.nixbpe + flag * value
    
    def getFlag(self, flags):
        return nixbpe & flags
    

class TokenTable:  
    tokenList = []

    def __init__(self, symTab, instTab):
        self.symTab = symTab
        self.instTab = instTab

    # 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
    # @param line : 분리되지 않은 일반 문자열
    def putToken(self, line):
        tokenList.append(Token(line))

    # tokenList에서 index에 해당하는 Token을 리턴한다.
    # @param index
    # @return : index번호에 해당하는 코드를 분석한 Token 클래스   
    def getToken(self, index):
        return tokenList[index]
   
class SymbolTable:
    
    def __init__(self):
        self.symbol = []
        
    def get_symbol(self, inputs):
        self.symbol.append(inputs)
    

class LiteralTable:
    
    def __init__(self):
        self.sym = SymbolTable()
    
    def others(self, input):
        self.sym.get_symbol(input)
    

class Assembler():
    lineList = []       # 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간
    tokenList = []      # 프로그램의 section별로 프로그램을 저장하는 공간
    symtabList = []     # 프로그램의 section별로 symbol table을 저장하는 공간
    literaltabList = [] # 프로그램의 section별로 literal table을 저장하는 공간
    codeList = []       # 만들어진 오브젝트 코드들을 저장하는 공간
    codeList_loc = []   # 만들어진 오브젝트 코드의 주소값을 저장하는 공간
    lengthList = []     # 프로그램의 section별로 총 코드 길이를 저장하는 공간
    Name = []           # Object Code에서, H라인에 이름을 적을 때 사용
    
    EXTDEF_all = []
    EXTREF_all = []
    section = []
    Lit_temp = []       # operand[0]의 시작이 =일 경우 보관해놓았다가, LTORG나 END가 나오면 그 때 literaltable에 저장함
    M_Line = []
    
    def __init__(self, instFile):
        self.inst = InstTable(instFile)
        #print(self.inst.instMap)
        
    # inputFile을 읽어들여서 lineList에 저장한다.
    # @param inputFile : input 파일 이름        
    def loadInputFile(self, inputFile):
        f = open(inputFile, 'r')
        lines = f.readlines()
        for line in lines:
            arr = line.split('\n')
            self.lineList.append(arr[0])
        f.close()
        #for index in Assembler.lineList:
        #    print(index)        
            
    # 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤, 토큰테이블 생성
    # label을 symbolTable에 정리
    # symbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 함
    def pass1(self):
        # 각 token별로 계산을 한다. 단, section에 맞게 나누어서 계산을 하는 것이 좋다.
        now_section = 0; # section 번호
        pc = 0; # PC값
        
        self.tokenList.append([])
        self.symtabList.append({})
        self.literaltabList.append({})
        self.M_Line.append([])
        
        for line in Assembler.lineList:
            # 토큰을 만들자
            token = Token(line)
            #print(token.label)
            #print(token.operator)
            #print(token.operand)
            #print(token.comment)
            
            label = token.label
            operator = token.operator
            operand = token.operand
            comment = token.comment
            token.location = pc
            
            # save = [token.label, token.operator, token.operand, token.comment, pc]
            
            # section이 바뀔 때마다 하나씩 만들어 놓는다.
            if (operator == "CSECT"):
                self.symtabList.append({})
                self.literaltabList.append({})
                self.tokenList.append([])
                self.M_Line.append([])
                now_section = now_section + 1
                self.lengthList.append(pc)
                pc = 0

            # 토큰분리한 것을 tokenList에 저장한다.
            self.tokenList[now_section].append(token) # save 사용?
            
            # label이 null이 아닐 경우 symtabList에 저장한다.
            # symtabList에는 {label 이름, pc값} 이렇게 딕셔너리로 저장된다.
            if (label != "" and label != '.'):
                if (operator != "EQU" or (len(operand) > 0 and operand[0] == '*')):
                    self.symtabList[now_section][label] = hex(pc)
                    if (operator == "START" or operator == "CSECT"):
                        self.Name.append(label)
                elif (operator == "EQU"):
                    # EQU일 경우만 따로 처리한다.
                    loc = 0
                    strs = operand[0].split('-')
                    # str[0]과 str[1]의 주소를 찾아서 연산을 수행
                    #print(type(self.symtabList[now_section]))
                    #print(strs[0])
                    #print(strs[1])
                    loc = loc + int(self.symtabList[now_section][strs[0]], 16)
                    loc = loc - int(self.symtabList[now_section][strs[1]], 16)
                    self.symtabList[now_section][label] = hex(loc)
                
                
            # operand[0]의 시작이 =일 경우 보관해놓았다가,
            # LTORG나 END가 나오면 그때 literaltable에 저장함
            if (len(operand) > 0 and operand[0].find('=') == 0):
                sw = 0
                for k in self.Lit_temp:
                    if (k == operand[0]):
                        sw = 1
                        break
                if (sw == 0):
                    self.Lit_temp.append(operand[0])
                    
            if (operator != "" and (operator == "LTORG" or operator == "END")):
                for k in self.Lit_temp:
                    str = k.split("\'")
                    self.literaltabList[now_section][k] = hex(pc)
                    if (str[0] == "=X"):
                        pc = pc + len(str[1]) // 2
                    elif (str[0] == "=C"):
                        pc = pc + len(str[1])
            
                if (operator == "END"):
                    self.lengthList.append(pc)
                    
                self.Lit_temp = []
            
            
            
            # operator가 null이 아닐 경우 주소값(pc값)을 증가시킴
            if (operator != ""):
                # 명령어 확인되었으면 format만큼 더한다
                if (operator in self.inst.instMap):
                    pc = pc + int(self.inst.instMap[operator][0]) # format
                else:
                    # 이 경우는 START, LTORG, END, USE, EQU, ...
                    # BYTE, WORD의 경우도 이곳에서 처리한다!
                    if (operator == "BYTE"):
                        strs = token.operand[0].split("\'")
                        if (strs[0] == "X"):
                            pc = pc + len(strs[1]) // 2
                        elif (strs[0] == "C"):
                            pc = pc + len(strs[1])
                            
                    elif (operator == "WORD"):
                        pc_orig = pc
                        strs = token.operand[0].split("-")
                        if (strs[0] in self.symtabList[now_section]):
                            pc = pc + int(self.symtabList[now_section][strs[0]], 16)
                        if (strs[1] in self.symtabList[now_section]):    
                            pc = pc - int(self.symtabList[now_section][strs[1]], 16)
                            
                        # BUFEND-BUFFER처럼 그 값을 알 수 없는 경우, pc값은 3 자동 할당 (명령어가 000000이기 때문)
                        if (pc_orig == pc):
                            pc = pc + 3
                        str2 = "M"
                        str2 += self.tenTosixteen(pc-3, 6)
                        self.M_Line[now_section].append(str2 + "06+BUFEND")
                        self.M_Line[now_section].append(str2 + "06-BUFFER")
                            
                    elif (operator == "RESW"):
                        pc = pc + int(operand[0]) * 3
                    
                    elif (operator == "RESB"):
                        pc = pc + int(operand[0])

                        
    def printSymbolTable(self, fileName):
        f = open(fileName, 'w')
        
        print("<<< THIS IS SYMBOL >>>")
        for line in self.symtabList:
            print(line)
            for key in line.keys():
                f.write(key)
                f.write(" : ")
                f.write(line[key])
                f.write("\n")
            f.write("\n")
        f.close()
    
    def printLiteralTable(self, fileName):
        f = open(fileName, 'w')
        
        print("<<< THIS IS LITERAL >>>")
        for line in self.literaltabList:
            print(line)
            for key in line.keys():
                strs = key.split('\'')
                f.write(strs[1])
                f.write(" : ")
                f.write(line[key])
                f.write("\n")
        f.close()
    
    def pass2(self):
        start_section = 0;
        
        for i in range (0, len(self.tokenList)):
            self.EXTDEF_all.append({})
            self.EXTREF_all.append([])
            self.codeList.append("New List")
            self.codeList_loc.append("000000")
            
            #lengthList 길이 알아내기
            #s = format(self.lengthList[i], 'x')
            #print(s)
            
            for j in range (0, len(self.tokenList[i])):
                label = self.tokenList[i][j].label
                operator = self.tokenList[i][j].operator
                operand = self.tokenList[i][j].operand
                comment = self.tokenList[i][j].comment
                location = self.tokenList[i][j].location
                nixbpe = self.tokenList[i][j].nixbpe

                #print("label: ", label)
                #print("operator: ", operator)
                #print("operand: ", operand)
                #print("comment: ", comment)
                #print("location: ", location)
                #print("nixbpe: ", nixbpe)
                #print("next")
                
                # operator를 보고 기계어를 생성해 보자
                # 먼저, operator 부분이 null일 경우 주석이므로, 기계어를 생성하지 않는다.
                if (len(operator) == 0):
                    continue
                # START줄도 기계어를 생성하지 않는다.
                elif (operator == "START"):
                    continue
                # CSECT줄도 기계어를 생성하지 않는다.
                elif (operator == "CSECT"):
                    continue
                elif (operator == "RESW"):
                    self.codeList.append("") # 빈 공간을 만들어서, T줄을 구별할 수 있게 한다.
                    self.codeList_loc.append(location)
                # EXTREF 변수는 []로 구성하였다. (Object Program 생성시에 이름만 필요)
                elif (operator == "EXTREF"):
                    for k in operand:
                        self.EXTREF_all[i].append(k)
                # EXTDEF 변수는 {}로 구성하였다. (Object Program 생성이에 이름 + 위치 필요)
                elif (operator == "EXTDEF"):
                    for k in operand:
                        self.EXTDEF_all[i][k] = location
                elif (operator == "BYTE"):
                    strs = operand[0].split("\'")
                    if (strs[0] == "X"):
                        self.codeList.append(strs[1])
                        self.codeList_loc.append(self.tenTosixteen(location, 6))
                # WORD가 나왔을 경우 - COPY 명령어에서는 WORD BUFEND-BUFFER만 있기 때문에 이것만을 고려함
                elif (operator == "WORD"):
                    # 뒤에 있는 영역의 계산이 불가능하다면, 000000을 추가
                    # 상황에 따라 이곳에 M라인을 추가하는게 좋음, 일단은 넘어가자
                    self.codeList.append("000000")
                    self.codeList_loc.append(self.tenTosixteen(location, 6))
                # LTORG 또는 END가 나왔을 경우
                elif (operator == "LTORG" or operator == "END"):
                    # 현재 section에 해당하는 곳까지 봄
                    for k in range (start_section, i+1):
                        for key in self.literaltabList[k].keys():
                            #print("key는:", key)
                            if (key == "=X\'05\'"):
                                self.codeList.append("05")
                                self.codeList_loc.append(self.tenTosixteen(int(self.literaltabList[k][key], 16), 6))
                            elif (key == "=C\'EOF\'"):
                                self.codeList.append("454F46")  # EOF의 아스키코드
                                self.codeList_loc.append(self.tenTosixteen(int(self.literaltabList[k][key], 16), 6))
                            start_section = i+1
                else:
                    # 이 외의 명령어들은 opcode에 해당하는 기계어를 찾아서, 기계어를 생성해야 한다.
                    machine = ""
                    if (operator in self.inst.instMap):
                        machine += self.inst.instMap[operator][1][0] # 기계어 첫 글자 = opcode 첫 글자
                                                
                        if (int(self.inst.instMap[operator][0]) == 2): # 명령어 포맷이 2형식인 경우
                            # format이 2일 때는 opcode를 그대로 넣으면 된다. n i를 고려할 필요가 없다.
                            machine += self.inst.instMap[operator][1][1]
                            
                            #이제 레지스터에 맞는 기계어 번호를 붙여주자
                            if (len(operand) == 0):
                                machine += "0"
                                machine += "0"
                            elif (len(operand) > 0):
                                if (len(operand[0]) > 0 and operand[0] == "X"):
                                    machine += "1"
                                elif (len(operand[0]) > 0 and operand[0] == "A"):
                                    machine += "0"
                                elif (len(operand[0]) > 0 and operand[0] == "S"):
                                    machine += "4"
                                elif (len(operand[0]) > 0 and operand[0] == "T"):
                                    machine += "5"
                                else:
                                    machine += "0"
                                    
                            if (len(operand) > 1):
                                if (len(operand[1]) > 0 and operand[1] == "X"):
                                    machine += "1"
                                elif (len(operand[1]) > 0 and operand[1] == "A"):
                                    machine += "0"
                                elif (len(operand[1]) > 0 and operand[1] == "S"):
                                    machine += "4"
                                elif (len(operand[1]) > 0 and operand[1] == "T"):
                                    machine += "5"
                                else:
                                    machine += "0"
                            else:
                                machine += "0"
                                
                            self.codeList.append(machine)
                            self.codeList_loc.append(self.tenTosixteen(location, 6))
                    
                        # format이 3 또는 4인 경우 중 특별한 경우 먼저 처리
                        # operator가 RSUB인 경우
                        elif (operator == "RSUB"):
                            machine += "F0000"
                            self.codeList.append(machine)
                            self.codeList_loc.append(self.tenTosixteen(location, 6))

                        else:
                            # format이 3 또는 4인 경우, 기계어 코드 3번째 자리까지는 방식이 동일하다.
                            # 두번째 글자는 _ _ n i 인데, 우선 앞의 두 글자는 opcode에서 알아내자
                            first_1 = 0     # 2^3
                            second_1 = 0    # 2^2
                            third_1 = 0     # 2^1
                            fourth_1 = 0    # 2^0
                            opcode_1 = 0
                            # 먼저 first와 second부터 채우자.

                            if (self.inst.instMap[operator][1][1] == "A"):
                                opcode_1 = 10
                            elif (self.inst.instMap[operator][1][1] == "B"):
                                opcode_1 = 11
                            elif (self.inst.instMap[operator][1][1] == "C"):
                                opcode_1 = 12
                            elif (self.inst.instMap[operator][1][1] == "D"):
                                opcode_1 = 13
                            elif (self.inst.instMap[operator][1][1] == "E"):
                                opcode_1 = 14
                            elif (self.inst.instMap[operator][1][1] == "F"):
                                opcode_1 = 15
                            else:
                                opcode_1 = int(self.inst.instMap[operator][1][1])

                            if (opcode_1 - 8 >= 0):
                                opcode_1 -= 8
                                first_1 = 1
                            else:
                                first_1 = 0

                            if (opcode_1 - 4 >= 0):
                                opcode_1 -= 4
                                second_1 = 1
                            else:
                                second_1 = 0

                            if (nixbpe & 32 > 0):
                                third_1 = 1

                            if (nixbpe & 16 > 0):
                                fourth_1 = 1

                            # 두 번째 숫자 맞추기
                            opcode_1 = first_1 * 8 + second_1 * 4 + third_1 * 2 + fourth_1 * 1
                            machine += self.tenTosixteen(opcode_1, 1)

                            # 이제 xbpe를 인식할 차례
                            first_2 = 0
                            second_2 = 0
                            third_2 = 0
                            fourth_2 = 0
                            if (nixbpe & 8 > 0):
                                first_2 = 1
                            if (nixbpe & 4 > 0):
                                second_2 = 1
                            if (nixbpe & 2 > 0):
                                third_2 = 1
                            if (nixbpe & 1 > 0):
                                fourth_2 = 1
                            
                            # 세 번째 숫자 맞추기
                            opcode_2 = first_2 * 8 + second_2 * 4 + third_2 * 2 + fourth_2 * 1
                            machine += self.tenTosixteen(opcode_2, 1)
                            
                            # 이제 간접 주소 접근인지, 직접 주소 접근인지 살펴보자
                            if (nixbpe & 32 > 0 and nixbpe & 16 == 0):
                                # 이 경우 간접 주소 접근 (@)
                                machine += "000"
                                self.codeList.append(machine)
                                self.codeList_loc.append(self.tenTosixteen(location, 6))
                                
                            elif (nixbpe & 32 == 0 and nixbpe & 16 > 0):
                                #print("즉시주소접근")
                                # 이 경우 즉시 주소 접근 (#)
                                # machine의 뒷 자리수는 # 뒤에 나오는 숫자를 기반으로 한다.
                                # 뒤에 나오는 수를 보고 주소값으로 바꿔준다.
                                strs = operand[0].split("#")
                                k = 0
                                
                                if (int(self.inst.instMap[operator][0]) == 3):
                                    while k + len(strs[1]) < 3:
                                        machine += "0"
                                        k = k + 1
                                    machine += strs[1]
                                
                                    self.codeList.append(machine)
                                    self.codeList_loc.append(self.tenTosixteen(location, 6))    
                                    
                                elif (int(self.inst.instMap[operator][0]) == 4):
                                    while k + len(strs[1]) < 5:
                                        machine += "0"
                                        k = k + 1
                                    machine += strs[1]
                                
                                    self.codeList.append(machine)
                                    self.codeList_loc.append(self.tenTosixteen(location, 6))
                    
                            else:
                                #print("일반적인 경우", machine)
                                # 일반적인 경우
                                target = -1
                                displacement = 0

                                # 우선 operand가 EXTREFS(외부에서 쓰이는 것)인지를 본다. 이 경우, 기계어 주소값은 전부 0이 된다.
                                sw = 0
                                for pp in self.EXTREF_all[i]:
                                    if (pp == operand[0]):
                                        sw = 1
                                        break

                                if (sw == 0):
                                    # symbolList랑 literalList를 확인해서 target 주소를 확보한다.
                                    if (operand[0] in self.symtabList[i]):
                                        target = self.symtabList[i][operand[0]]

                                    # 만약 앞 과정을 전부 수행했는데도 target이 -1일 경우, literal인지도 확인한다.   
                                    if (target == -1):
                                        for pp in self.literaltabList[i]:
                                            #print(pp, operand[0])
                                            if (pp == operand[0]):
                                                #print(self.literaltabList[i][pp])
                                                target = self.literaltabList[i][pp]
                                                break

                                    #print(target)
                                    target = int(target, 0)

                                    # symbolList와 LiteralList 둘 다 확인했는데도 불구하고 target이 -1이면 error
                                    if (target == -1):
                                        print("error!")
                                        continue
                                    elif (target == 0):
                                        displacement = 0
                                        continue

                                    else:
                                        #target이 0x2a (str)로 되어 있음...
                                        if (target > location + int(self.inst.instMap[operator][0])):
                                            displacement = target - (location + int(self.inst.instMap[operator][0]))

                                        else:
                                            # 보수 적용
                                            if (int(self.inst.instMap[operator][0]) == 3):
                                                displacement = 16 * 16 * 16 * 16 - (location + int(self.inst.instMap[operator][0])) + target
                                            elif (int(self.inst.instMap[operator][0]) == 4):
                                                displacement = 16 * 16 * 16 * 16 * 16 - (location + int(self.inst.instMap[operator][0])) + target

                                        #print(target, " ", location, " ", displacement)
                                        #print(self.tenTosixteen(displacement, 6))
                                                
                                        # pc-relative인지 확인
                                        if (abs(target - location) >= 16*16*16):
                                            continue # pc-relative가 아님

                                        else:
                                            if (int(self.inst.instMap[operator][0]) == 3):
                                                machine += self.tenTosixteen(displacement, 3)
                                            elif (int(self.inst.instMap[operator][0]) == 4):
                                                machine += self.tenTosixteen(displacement, 5)

                                    self.codeList.append(machine)
                                    self.codeList_loc.append(self.tenTosixteen(location, 6))

                                else:    
                                    # operand가 EXTREFS이므로 기계어 주소값은 전부 0이다.
                                    machine += "0"
                                    machine += "0"
                                    machine += "0"
                                    if (int(self.inst.instMap[operator][0]) == 4):
                                        machine += "0"
                                        machine += "0"
                                    self.codeList.append(machine)
                                    self.codeList_loc.append(self.tenTosixteen(location, 6))
                                    # 여기에 M라인에 넣는거 만들자
                                    str2 = "M"
                                    str2 += self.tenTosixteen(location+1, 6)
                                    if (int(self.inst.instMap[operator][0]) == 3):
                                        str2 += "03+"
                                    elif (int(self.inst.instMap[operator][0]) == 4):
                                        str2 += "05+"
                                    str2 += operand[0]
                                    self.M_Line[i].append(str2)
                                    

    
    def printObjectCode(self, fileName):
        f = open(fileName, 'w')
        
        print("<<< THIS IS OBJECTCODE >>>")
        now_section = -1
        temp = 0
        str_T = "T"
        i = -1
        str2 = ""
        for line in self.codeList:
            i = i + 1
            # print(line)
            if (line == "New List"):
                if (temp == 0):
                    temp = 1
                else:
                    # 마지막 T라인, M라인, E라인을 만든다.
                    # T라인
                    str_T += self.tenTosixteen(len(str2) // 2, 2) # 명령어의 총 길이
                    str_T += str2   # 명령어들
                    print(str_T)
                    f.write(str_T)
                    f.write("\n")

                    str_T = "T"
                    str2 = ""
                        
                    # M라인
                    for j in self.M_Line[now_section]:
                        print(j)
                        f.write(j)
                        f.write("\n")
                    
                    # E라인
                    if (temp == 1):
                        print("E000000\n")
                        f.write("E000000\n\n")
                    else:
                        print("E\n")
                        f.write("E\n\n")
                    temp = temp + 1
                
                # 새로운 H라인을 만들자
                now_section = now_section + 1
                strs = "H" + self.Name[now_section]
                a = len(strs)
                for j in range(a, 7):
                    strs += " "
                strs += "000000" # 시작주소 넣기
                strs += self.tenTosixteen(self.lengthList[now_section], 6)
                print(strs)
                f.write(strs)
                f.write("\n")
                
                # D라인을 만들자
                if (len(self.EXTDEF_all[now_section]) > 0):
                    strs = "D"
                    for j in self.EXTDEF_all[now_section]:
                        strs += j  # 변수 이름
                        # 변수 주소는 symtabList에서 찾기
                        strs += self.tenTosixteen(int(self.symtabList[now_section][j], 16), 6)
                    print(strs)
                    f.write(strs)
                    f.write("\n")             
                    
                # R라인을 만들자
                if (len(self.EXTREF_all[now_section]) > 0):
                    strs = "R"
                    p = 0
                    for j in self.EXTREF_all[now_section]:
                        strs += j
                        if (len(strs) < 1 + 6 * (p+1)):
                            strs += " "
                        p = p + 1
                    print(strs)
                    f.write(strs)
                    f.write("\n")
                    
            else:                    
                # T라인을 만들자
                # T + 6자리(시작지점) + 2자리(길이) + 58자리(명령어 나열, 1D = 29, 29*2글자의 16진수 명령어 나열 가능)
                if (str_T == "T" and line != ""):
                    str_T += str(self.codeList_loc[i])
                    str2 = "" # str2 초기화
                    
                if (len(str2) + len(line) <= 58 and line != ""):
                    str2 += line
                else:
                    # 이때 더해야 한다
                    if (len(str2) != 0):
                        str_T += self.tenTosixteen(len(str2) // 2, 2) # 명령어의 총 길이
                        str_T += str2   # 명령어들
                        print(str_T)
                        f.write(str_T)
                        f.write("\n")
                        
                        str_T = "T"
                        if (line != ""):
                            str_T += str(self.codeList_loc[i]) # 시작지점
                            str2 = line # str2 초기화
                            
        # 맨 마지막 줄을 더해주자
        str_T += self.tenTosixteen(len(str2) // 2, 2)
        str_T += str2
        print(str_T)
        f.write(str_T)
        f.write("\n")

        # M라인
        for j in self.M_Line[now_section]:
            print(j)
            f.write(j)
            f.write('\n')
        

        # E라인
        print("E\n")
        f.write("E\n\n")
                
                    
            
            
        f.close()
    
    # 10진수 정수값을 n자리의 16진수 String으로 리턴해주는 함수
    def tenTosixteen(self, ten, n):
        sixteen = format(ten, 'x')
        i = 0
        str_sixteen = ""
        while len(sixteen) + i < n:
            str_sixteen += "0"
            i = i + 1

        str_sixteen += sixteen
        if (len(str_sixteen) > n):
            j = len(str_sixteen) - n
            strs = ""
            while (len(strs) < n):
                strs += str_sixteen[j]
                j = j + 1
            return strs
                
        else:    
            return str_sixteen
    
def main():
    assembler = Assembler("inst.data")
    assembler.loadInputFile("input.txt")
    assembler.pass1()
    assembler.printSymbolTable("symtab_20150413")
    print("\n")
    assembler.printLiteralTable("literaltab_20150413")
    print("\n")
    assembler.pass2()
    assembler.printObjectCode("output_20150413")

if __name__ == "__main__":
    main()