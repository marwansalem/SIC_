/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sic;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Assembler {
    static HashMap<Character, String> RegisterMap;
    static int sa=-1;
    static int StartAddress;
    static int literalOffset = 0;
    static ArrayList<Instruction> Instructions = new ArrayList<Instruction>();
    static ArrayList<Symb> labels;
    static ArrayList<String> LiteralTable ;
    static{
        labels=new ArrayList<>();
        LiteralTable = new ArrayList<String>();
        RegisterMap  = new HashMap<Character,String>();
        RegisterMap.put('a',"0000");
        RegisterMap.put('x', "0001");
        RegisterMap.put('l',"0010");
        RegisterMap.put('b',"0011");
        RegisterMap.put('s',"0100");
        RegisterMap.put('t',"0101");
        //                    Register Number
//                    -------- ------
//                        A      0
//                        X      1
//                        L      2
//                        B      3
//                        S      4
//                        T      5
//                        F      6
        
    }
    
    
    

    //1234567890123456789012345678901
    //label 1:8 , 9 unused
    //10 ;15 ,mnemonic,16:17 unused, 18:35 operand,36:66 comment
    public static ArrayList<String> readFile(String filename){
        
        BufferedReader reader = null;
        ArrayList<String> List = new ArrayList<String>();
        try {
            File file = new File(filename);
            reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
     //           line = line.toLowerCase();//lower case to process easie
                line = " "+line;

                List.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return List;
    }
    public static void processInput(ArrayList<String> lines,String output){
        for(int i = 0 ; i < lines.size();i++){
            
            String []tokens = lines.get(i).split(" ");
            for(int j = 0;j<tokens.length ;j++){
                // deal with each field H
            }
        }
    }
    public static void processPseudoOperation(Instruction ins){
        
        if(ins.getMnemonic().trim().equalsIgnoreCase("resw")){
                                                if(OperandHandler.isNumericString(ins.getOperand())){
                                                    
                                                    sa+=Integer.parseInt(ins.getOperand())*3;
                                                }else{
                                                    ins.appendErrorMessage("		****Error: Operand must a number"); // by tarek
                                                }
                        }
            		else if(ins.getMnemonic().trim().equalsIgnoreCase("resb")){
            			sa+=Integer.parseInt(ins.getOperand());
                        }
            		else if(ins.getMnemonic().trim().equalsIgnoreCase("byte")) {
            			if(ins.getOperand().charAt(0)=='c'||ins.getOperand().charAt(0)=='C') {
            				ins.setSize(ins.getOperand().length()-3);
            				sa+=ins.getOperand().length()-3;
            				}
            			else if(ins.getOperand().charAt(0)=='x'||ins.getOperand().charAt(0)=='X') {
            				String s=ins.getOperand().trim();
            				s=s.substring(2,s.length()-1);
            				if(s.length()%2==0) {
            					ins.setSize(s.length()/2);
            					sa+=s.length()/2;
            				}
            				else {
            					ins.setError(true);
            					ins.setErrorMessage("\t\t****Error: Not a hexadecimal String");
            				}
            				try {
            					int x=Integer.parseInt(s,16);
            				}
            				catch(Exception e){
            					ins.setError(true);
            					ins.setErrorMessage("\t\t****Error: Not a hexadecimal String");
            				}
            			}
            			else
            				sa++;
            		}
            		else if(ins.getMnemonic().trim().equalsIgnoreCase("word")) {
            			int c=1;
            			for(int j=0;j<ins.getOperand().length();j++) {
            				if(ins.getOperand().lastIndexOf(j)==',')
            					c++;
            			}
            			ins.setSize(3*c);
            			sa+=3*c;
            		}
            		else if(ins.getMnemonic().trim().equalsIgnoreCase("org")) 
            			sa+=3;
                        else if(ins.getMnemonic().trim().equalsIgnoreCase("equ")){
                            OperandHandler.simpleExpression(ins);
                            
                            String ad1=ins.getExpression1().trim();
                            ad1 = SymbolTable.SYMTBL.get(ad1);


                            String ad2 = "";
                            if(OperandHandler.isNumericString(ins.getExpression2())){
                                ad2 = ins.getExpression2();
                            }else {
                                ad2 = SymbolTable.SYMTBL.get(ins.getExpression2().toLowerCase());
                                if(ins.getExpression2().equals(""))
                                    ad2 = "0";
                            }
                            if(ad1 ==null || ad2 ==null){
                                
                                ins.appendErrorMessage("\t\t****Error: Undefined symbol in operand");
                                ins.setError(true);
                                
                            }else{
                                //change it to hex
                                int a1 = Integer.valueOf(ad1,16);
                                int a2 = Integer.valueOf(ad2, 16);
                                switch(ins.getExpressionType()){
                                    case '+':{
                                        a1 = a1+a2;
                                        break;}
                                    case '-':{
                                        a1 = a1-a2; break;}
                                    case '*':{
                                        a1 = a1*a2; break;}
                                    case '/':{
                                        a1 = a1/a2; break;
                                    }
                                    
                                }
                                if(a1 <0){
                                    ins.appendPass2Error("\t\t****Error: Value in symbol table can't be negative");
                                    ins.setAddress(0);// make it 0 in case of negative address
                                    ins.setError(true);
                                }
                                else{
                                    ins.setAddress(a1);
                                }
                            }
                            
                        }
        
    }
    public static void processFixedFormat(ArrayList<String> lines){// uses substring for each line to get specific field,, and remove extra spaces.
        int beg = -1;
        for(int i = 0 ; i < lines.size();i++){
            String line = lines.get(i);
            
            if(line.trim().equals(""))
                continue;
            Instruction ins = new Instruction(line);
            
            if(!ins.isCommentLine() && beg==-1)
                beg = i;
            if(beg == i)
            {
                if(!ins.getMnemonic().trim().equalsIgnoreCase("start")){
                    ins.error = true;
                    ins.appendErrorMessage("START Directive not found!");
                }

                sa=Integer.parseInt(ins.getOperand(),16);
                ins.setAddress(sa);
                StartAddress=sa;
                StartAddress = StartAddress%1048576; // max value for sic xe address
                SymbolTable.setStartLabel(ins.getLabel().toLowerCase().trim());
                labels.add(new Symb(ins.getLabel(),ins.getAddress()));
                if(ins.getLabel().equals("")==false)
                    SymbolTable.SYMTBL.put(ins.getLabel().trim().toLowerCase(),ins.getAddress());
                Instructions.add(ins);
            }else{
                Pass1.firstPass(ins);
        }
        }
    }
    public static void symtb() {
    	//for(int j=0;j<labels.size();j++) {
    		//System.out.println("\t"+labels.get(j).getLabel()+"\t"+labels.get(j).getAddress());
    	//}
    	SymbolTable.showTable();
    	System.out.println("\n");
    	opch(Instructions);

    	obcgen();
    	System.out.println("Line no.\tAddress\tObjectC\t\tLabel\t\tMnemonic\tOperands");
    	for(int i=0;i<Instructions.size();i++) {
    		if(!Instructions.get(i).isCommentLine()) {
    			System.out.println((i+1)+"\t\t"+Instructions.get(i).getAddress()+"\t"+Instructions.get(i).getObc()+"\t\t"+Instructions.get(i).getLabel()+"\t"+Instructions.get(i).getMnemonic()+"\t\t"+Instructions.get(i).getOperand());
    			if(Instructions.get(i).isError())
    			System.out.println(Instructions.get(i).getErrorMessage());
    		}
    			else 
    			System.out.println((i+1)+"\t"+Instructions.get(i).getComment());	
    	}
    	if(!Instructions.get(Instructions.size()-1).getMnemonic().trim().equalsIgnoreCase("end")) {
    	    Instructions.get(Instructions.size()-1).appendErrorMessage("\t\t****Error: Missing End Statment");
            System.out.println("\t\t****Error: Missing End Statment");
        }
    }
    public static void Literals(Instruction ins){

        String operand = ins.getOperand();
        operand = operand.trim();
        if(operand.equals(""))  return; // return on empty operand;
        if((operand.contains("=")  &&operand.contains("\'")  )== false) return;
        if(operand.charAt(0) == '='){
            if(operand.length()>1){
                char type = Character.toLowerCase(operand.charAt(1));
                int start = operand.indexOf('\'');
                int end = operand.lastIndexOf('\'');
                String val = operand.substring(start+1,end);
                switch(type) {
                    case 'w' : { // WORD
                        
                        if(val.contains(" "))
                        {
                            ins.appendErrorMessage("\t\t****Error: This literal cannot contain spaces");
                        }else{
                            if(val.equals("") ==false){
                                if(OperandHandler.isNumericString(val)){
                                    int value = Integer.parseInt(val);
                               
                                    if(value>9999 || value <-9999 ){
                                        ins.appendErrorMessage("\t\t****Error: Literal too large");
                                    }else{
                                           String wordString="00";
                                           ins.OperandBytes = new Byte[3];                    //get bytes
                                           if(value <0) {
                                               ins.OperandBytes[2] = (byte) 0xff;
                                               String negtemp = Integer.toBinaryString(value);
                                               while(negtemp.length()>24){
                                                   negtemp = negtemp.substring(1);
                                               }
                                               value = Integer.parseInt(negtemp,2);
                                               wordString="";
                                           }
                                           else
                                               ins.OperandBytes[2]=0;
                                           int lowerByte = (value%256);
                                           String low=Integer.toHexString(lowerByte);
                                           if(low.length()==1){
                                               low = '0'+low;
                                           }
                                           int middleByte = value/256;
                                           String mid = Integer.toHexString(middleByte);
                                            if(mid.length()==1){
                                                mid = '0'+mid;
                                            }
                                            wordString=wordString+mid+low;
                                            ins.literalWord = wordString;
                                           ins.OperandBytes[0] = (byte)lowerByte;
                                           ins.OperandBytes[1] = (byte)middleByte;
                                           LiteralTable.add(literalOffset,wordString);
                                            if(ins.getFormat()==3)
                                                ins.literalAddress = sa-StartAddress + (literalOffset*3);
                                            else
                                                ins.literalAddress = sa+literalOffset*3;
                                            literalOffset++;
                                            ins.isLiteral = true;
                                    }
                                    
                                }else{
                                    ins.appendErrorMessage("\t\t****Error: Literal is a not a number");
                                }

                            }
                        }
                        break;
                    }case 'x':{
                            if( !OperandHandler.isHexaDecimal(val)){
                            ins.appendErrorMessage("\t\t****Error: Not a Hexadecimal literal");
                            
                            }else{
                                int len =  val.length();
                                if(len <7){
                                    int numOfBytes =val.length()/2;
                                    if(len%2 ==1){
                                        val = '0'+val;//put a zeero at beginning
                                        numOfBytes ++;
                                    }
                                    ins.OperandBytes = new Byte[3];
                                    ins.literalWord = val;
                                    //11 22 33

                                    LiteralTable.add(literalOffset,val);
                                    if(ins.getFormat()==3)
                                        ins.literalAddress = sa-StartAddress + (literalOffset*3);
                                    else
                                        ins.literalAddress = sa+literalOffset*3;
                                    literalOffset++;
                                    ins.isLiteral = true;
                                }else{
                                    ins.appendErrorMessage("\t\t****Error: Hexadecimal Literal too long");
                                    
                                }
                            }
                        break; 
                    }case 'c':{ // character literals
                        if(val.length()>3 ){
                            ins.appendErrorMessage("\t\t****Error: Literla too long");
                        }else{

                            while(val.length()!=3){
                                val ="0"+ val; /// might have to reverse the bytes

                            }

                            String high,mid,low;
                            high = Integer.toHexString(val.charAt(0)) ;
                            if(high.length()==1){
                                high = '0'+high;
                            }
                            mid =  Integer.toHexString(val.charAt(1));
                            if(mid.length()==1){
                                mid = '0'+mid;
                            }
                            low =  Integer.toHexString(val.charAt(2));
                            if(low.length()==1){
                                low = '0'+low;
                            }
                            ins.literalWord= high + mid + low;
                            ins.OperandBytes= new Byte[3];
                            ins.OperandBytes[0] = (byte)(val.charAt(2)-'0');
                            ins.OperandBytes[1] = (byte)(val.charAt(1)-'0');
                            ins.OperandBytes[2] = (byte)(val.charAt(0)-'0');
                            LiteralTable.add(literalOffset,high+mid+low);
                            if(ins.getFormat()==3)
                                ins.literalAddress = sa-StartAddress + (literalOffset*3);
                            else
                                ins.literalAddress = sa+literalOffset*3;
                            literalOffset++;
                            ins.isLiteral = true;

                         }
                    }
                }
            }else{
               ins.setErrorMessage("literal too short");
            }
                    
            
        }
    }
    public static void opch(ArrayList<Instruction> in) {
    	String[] reg=new String[] {"A","X","L","B","S","T","F","PC","SW"};
    	for(int i=0;i<in.size();i++) {
                Instruction ins= in.get(i);
    		if(!in.get(i).isCommentLine()) {
	    		String op=in.get(i).getOperand().trim();
                        if(!op.equals("")) // for instructions without operand
	    		if(op.charAt(0)=='*') {
	    			if(op.equalsIgnoreCase("*"));
	            	else if(op.charAt(0)=='*') {
	            		if(op.charAt(1)=='+'||op.charAt(1)=='-') {
	            			try {
	            				int x=Integer.parseInt(op.substring(2,op.length()));
	            			}
	            			catch(Exception e) {
	            				in.get(i).setError(true);
	    		    			in.get(i).setErrorMessage("\t\t****Error: Problem with operand");
	            			}
	            		}
	            		else {
	            			in.get(i).setError(true);
			    			in.get(i).setErrorMessage("\t\t****Error: Problem with operand");
	            		}
	            	}
	    		}
	    		else if(!ins.isCommentLine() &&!ins.isDirect()){
	    			OperandHandler.memoryErrors(ins);
	    		}
    		}
    	}
    }
    public static void literalObjectCode(Instruction ins){
        if(ins.isLiteral){
            String op = ins.getOpcode();
            op = Integer.toBinaryString(Integer.parseInt(op,16));
            while(op.length()!=6){
                op = '0'+op;
            }
            int add = ins.literalAddress;
            if(ins.getFormat()==3){
                op = op+"110000";
                String displacement = Integer.toBinaryString(add);
                while(displacement.length()!=12){
                    displacement ='0'+ displacement;
                }
                op = op+displacement;
                int Code =Integer.parseInt(op,2);
                op =Integer.toHexString(Code);
                while(op.length()!=6){
                    op = '0'+op;
                }
                ins.setObc(op);
            }else if(ins.getFormat() ==4){
                op = op+"110001";
                String displacement = Integer.toBinaryString(add);
                while(displacement.length()!=20){
                    displacement ='0'+ displacement;
                }
                op = op+displacement;
                int Code =Integer.parseInt(op,2);
                op =Integer.toHexString(Code);
                while(op.length()!=8){
                    op = '0'+op;
                }
                ins.setObc(op);

            }
        }
    }
    public static void Objectcode() {
        String name = labels.get(0).getLabel().trim();
        while(name.length()!=6)
            name+=' ';
	String address = labels.get(0).getAddress().trim();
	int start = Integer.parseInt(labels.get(0).getAddress(),16);
	while(address.length()!=6 )
	    address= '0'+address;
	int end = Integer.parseInt(Instructions.get(Instructions.size()-1).getAddress(),16);
	int extra = 3*LiteralTable.size();
	int integerLength = end - start-1 +extra;
	String stringLength = Integer.toHexString(integerLength);
	while(stringLength.length()!=6)
	    stringLength='0'+stringLength;
        boolean NoWay= false;
        
	try{
            File file = new File("obj.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("H" + name + "^" + address.toUpperCase()+"^"+stringLength);
            writer.newLine();
            if(NoWay = false)
                NoWay = true;
            else
            {
                stringLength = "";
            }
            for (int i = 0; i < Instructions.size(); i++){
            	int sum=0;
            	ArrayList<Instruction> inss=new ArrayList<>();
            	if(Instructions.get(i).isCommentLine())
            		i++;
            	while(sum+Instructions.get(i).getSize()<=30&&!Instructions.get(i).getObc().equals("")) {
            		if(!Instructions.get(i).isCommentLine())
            		inss.add(Instructions.get(i));
            		i++;
            	}
            	if(inss.size()>=1) {
	            	end=Integer.parseInt(inss.get(inss.size()-1).getAddress(),16)-Integer.parseInt(inss.get(0).getAddress(),16)+inss.get(inss.size()-1).getSize();
	            	end = end+extra;
	            	extra = 0;
	            	String leadingZeroes ="";
	            	while(leadingZeroes.length()+inss.get(0).getAddress().length()!=6)
	            	    leadingZeroes+='0';
	            	String lead="";
                    if(Integer.toHexString(end).length()==1){
                        lead= "0";
                    }

	            	writer.write("T"+leadingZeroes+inss.get(0).getAddress().toUpperCase()+"^"+lead+Integer.toHexString(end).toUpperCase()+"^");
	            	for(int j=0;j<inss.size();j++) {
	            	    Instruction ins = inss.get(j);
	            	    String moreZeroes="";
	            	    String obc=ins.getObc();
	            	    if(ins.isDirective()==false){
	            	        while((obc.length()+moreZeroes.length())!=2*ins.getFormat())
	            	            moreZeroes= "0"+moreZeroes;
                        }
	            		writer.write(moreZeroes+""+obc.toUpperCase());
	            	}
	            	for (int counter = 0; counter<LiteralTable.size();counter++){
	            	    writer.write(LiteralTable.get(counter).toUpperCase());
                    }
	            	writer.newLine();
            	}
            }
            String lead="";
            while(lead.length()+labels.get(0).getAddress().length() !=6)
            {
                lead+="0";
            }
            writer.write("E"+lead+labels.get(0).getAddress());
			writer.close();
	}
	catch (IOException e)
	{
		e.printStackTrace();
	}
    }
    public static boolean listFileGenerator(String path){
        boolean noError = true;
        BufferedWriter writer = null;
        try {
            //create a temporary file
            //String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File logFile = new File(path);

            // This will output the full path where the file will be written to...
            
            String text="";
            
            opch(Instructions);
            text+="Line no.\tAddress\tObjectC\tLabel\t\tMnemonic\tOperands";
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write(text);
            writer.newLine();
            for(int i=0;i<Instructions.size();i++) {
                    if(!Instructions.get(i).isCommentLine()) {
                            text=""+(i+1)+"\t\t"+Instructions.get(i).getAddress()+"\t"+Instructions.get(i).getObc()+"\t"+Instructions.get(i).getLabel()+"\t"+Instructions.get(i).getMnemonic()+"\t\t"+Instructions.get(i).getOperand();
                            writer.write(text);
                            writer.newLine();
                            if(Instructions.get(i).isError()){
                                text =Instructions.get(i).getErrorMessage();
                                noError =false;
                                writer.write(text);
                                writer.newLine();
                            }
                    }
                            else { 
                            text =""+(i+1)+"\t\t"+Instructions.get(i).getAddress()+"\t"+Instructions.get(i).getComment();
                            writer.write(text);
                            writer.newLine();
                            }	
            }

            if(!Instructions.get(Instructions.size()-1).getMnemonic().trim().equalsIgnoreCase("end")){
                text ="\t\t****Error: Missing End Statment";
                noError = false;
                writer.write(text);
                writer.newLine();
            }
            text = "Symbol Table:";
            writer.write(text);
            writer.newLine();
            text="Label \t Address";
            writer.write(text);
            writer.newLine();
            for(int k=0;k<labels.size();k++) {
	            text=labels.get(k).getLabel()+"\t"+labels.get(k).getAddress();
	            writer.write(text);
	            writer.newLine();
            }
            String litTable=getLiteralTable();
            writer.write(litTable);
            writer.newLine();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
                return false;
            }
        }
        return noError;
        
    }

    public static void obcgen() {
        String base ="0";
        for(int i=0;i<Instructions.size();i++) {
            if(!Instructions.get(i).isCommentLine()) {
                if (Instructions.get(i).getMnemonic().trim().equalsIgnoreCase("ldb")) {
                    for (int j = 0; j < labels.size(); j++) {
                        if (Instructions.get(i).getOperand().equalsIgnoreCase(labels.get(j).getLabel())) {
                            base = labels.get(j).getAddress();

                            break;
                        }

                    }
                }
                Literals(Instructions.get(i));
                literalObjectCode(Instructions.get(i)); //returns if it's not a literal

            }
        }
        for(int i=0;i<Instructions.size();i++) {
            if(Instructions.get(i).isLiteral)
                continue;
            if(!Instructions.get(i).isCommentLine()  ) {
                if(Instructions.get(i).getFormat()==2&&!Instructions.get(i).isError()) {
                    String s1= RegisterMap.get(Instructions.get(i).getR1().toLowerCase().charAt(0));
                    String s2= RegisterMap.get(Instructions.get(i).getR2().toLowerCase().charAt(0));
                    if(Instructions.get(i).getMnemonic().trim().equals("tixr"))
                        s2 ="0000";

                    String s= s1+s2;
                    int rss = Integer.parseInt(s,2);
                    s = Integer.toHexString(rss);
                    //String s= Integer.toHexString(Integer.parseInt(s1,2))+Integer.toHexString(Integer.parseInt(s2,2));
                    Instructions.get(i).setObc(Instructions.get(i).getOpcode()+s);
                }
                else if(Instructions.get(i).getFormat()==3&&!Instructions.get(i).isError()) {
                    String s=Integer.toBinaryString(Integer.parseInt(Instructions.get(i).getOpcode(),16));
                    while(s.length()<8)
                        s="0"+s;
                    s=s.substring(0,6);

                    if(Instructions.get(i).isImmediate()||Instructions.get(i).operand.contains("#"))
                        s=s+"01";
                    else if(Instructions.get(i).isIndirect()||Instructions.get(i).operand.contains("@")) {
                        s = s + "10";
                        if(SymbolTable.SYMTBL.containsKey(Instructions.get(i).operand.substring(1))){
                            //do something
                        }
                    }
                    else
                        s=s+"11";
                    if(Instructions.get(i).isIndexing())
                        s=s+"1";
                    else
                        s=s+"0";
                    int add;
                    int TA = 0;
                    int lctr=Integer.parseInt(Instructions.get(i+1).getAddress(),16);
                    String op=Instructions.get(i).getOperand().trim();
                    int flag=0;
                    if(op.charAt(0)=='#'||op.charAt(0)=='@')
                        op=op.substring(1,op.length());
                    String ad[]=op.split("\\+");
                    String mi[]=op.split("-");
                    String div[]=op.split("/");
                    String mul[]=op.split("\\*");
                    //System.out.println(ad[0]);
                    if(ad.length<2&&mi.length<2&&div.length<2&&mul.length<2||op.charAt(0)=='*') {
                        for(int j=0;j<labels.size();j++) {
                            if(Instructions.get(i).getOperand().equalsIgnoreCase(labels.get(j).getLabel())) {
                                TA=Integer.parseInt(labels.get(j).getAddress(),16);
                                break;
                            }
                        }
                    }
                    else if(ad.length>=2) {
                        int k;
                        for(int j=0;j<ad.length;j++) {
                            for(k=0;k<labels.size();k++) {
                                if(ad[j].equalsIgnoreCase(labels.get(k).getLabel())) {
                                    TA+=Integer.parseInt(labels.get(k).getAddress(),16);
                                    flag=1;
                                    break;
                                }
                            }
                            if(k==labels.size()) {
                                try {
                                    TA+=Integer.parseInt(ad[j]);
                                }
                                catch(Exception e) {

                                }
                            }
                        }
                    }
                    else if(mi.length>=2) {
                        int k;
                        for(k=0;k<labels.size();k++) {
                            if(ad[0].equalsIgnoreCase(labels.get(k).getLabel())) {
                                TA+=Integer.parseInt(labels.get(k).getAddress(),16);
                                flag=1;
                                break;
                            }
                        }
                        for(int j=1;j<ad.length;j++) {
                            for(k=0;k<labels.size();k++) {
                                if(ad[j].equalsIgnoreCase(labels.get(k).getLabel())) {
                                    TA-=Integer.parseInt(labels.get(k).getAddress(),16);
                                    flag=1;
                                    break;
                                }
                            }
                            if(k==labels.size()) {
                                try {
                                    TA-=Integer.parseInt(ad[j]);
                                }
                                catch(Exception e) {

                                }
                            }
                        }
                    }
                    else if(div.length>=2) {
                        int k;
                        for(k=0;k<labels.size();k++) {
                            if(ad[0].equalsIgnoreCase(labels.get(k).getLabel())) {
                                TA+=Integer.parseInt(labels.get(k).getAddress(),16);
                                flag=1;
                                break;
                            }
                        }
                        for(int j=1;j<ad.length;j++) {
                            for(k=0;k<labels.size();k++) {
                                if(ad[j].equalsIgnoreCase(labels.get(k).getLabel())) {
                                    TA/=Integer.parseInt(labels.get(k).getAddress(),16);
                                    flag=1;
                                    break;
                                }
                            }
                            if(k==labels.size()) {
                                try {
                                    TA/=Integer.parseInt(ad[j]);
                                }
                                catch(Exception e) {

                                }
                            }
                        }
                    }
                    else if(mul.length>=2) {
                        int k;
                        for(k=0;k<labels.size();k++) {
                            if(ad[0].equalsIgnoreCase(labels.get(k).getLabel())) {
                                TA+=Integer.parseInt(labels.get(k).getAddress(),16);
                                break;
                            }
                        }
                        for(int j=1;j<ad.length;j++) {
                            for(k=0;k<labels.size();k++) {
                                if(ad[j].equalsIgnoreCase(labels.get(k).getLabel())) {
                                    TA*=Integer.parseInt(labels.get(k).getAddress(),16);
                                    break;
                                }
                            }
                            if(k==labels.size()) {
                                try {
                                    TA*=Integer.parseInt(ad[j]);
                                }
                                catch(Exception e) {

                                }
                            }
                        }
                    }

                    try {
                        add=Integer.parseInt(op);
                        s=s+"000";
                    }
                    catch(Exception e){
                        if(Instructions.get(i).isImmediate()&&flag==0) {
                            add=TA;
                            s=s+"000";
                        }
                        else {
                            add=TA-lctr;
                            if(add>-2045&&add<2045)
                                s=s+"010";
                            else{
                                add=TA-Integer.parseInt(base,16);
                                if(add<0)
                                    add=4095-add;
                                s=s+"100";
                            }
                        }
                    }
                    if(op.charAt(0)=='*') {
                        if(op.length()==1) {
                            add=0;
                        }
                        else {
                            op=op.substring(1,op.length());
                            if(op.charAt(0)=='+')
                                add=Integer.parseInt(op.substring(1,op.length()))-3;
                            else
                                add=4095-Integer.parseInt(op.substring(1,op.length()))-2;
                        }
                    }
                    String disp=Integer.toBinaryString(add);
                    while(disp.length()<12) {
                        disp="0"+disp;
                    }
                    while(disp.length()>12) {
                        disp=disp.substring(1,disp.length());
                    }
                    s=s+disp;
                    if(s.substring(0,4).equals("0000"))
                        Instructions.get(i).setObc("0"+Integer.toHexString(Integer.parseInt(s,2)));
                    else
                        Instructions.get(i).setObc(Integer.toHexString(Integer.parseInt(s,2)));
                }
                else if(Instructions.get(i).getFormat()==4&&!Instructions.get(i).isError()) {
                    String s=Integer.toBinaryString(Integer.parseInt(Instructions.get(i).getOpcode(),16));
                    s=s.substring(0,6);
                    if(Instructions.get(i).isImmediate())
                        s=s+"01";
                    else if(Instructions.get(i).isIndirect())
                        s=s+"10";
                    else
                        s=s+"00";
                    if(Instructions.get(i).isIndexing())
                        s=s+"1";
                    else
                        s=s+"0";
                    int TA = 0;
                    String op=Instructions.get(i).getOperand().trim();
                    int lctr=Integer.parseInt(Instructions.get(i).getAddress(),16);
                    if(op.charAt(0)=='#'||op.charAt(0)=='@')
                        op=op.substring(1,op.length());
                    try {
                        TA=Integer.parseInt(op);
                    }
                    catch(Exception e) {
                        for(int j=0;j<labels.size();j++) {
                            if(Instructions.get(i).getOperand().trim().equalsIgnoreCase(labels.get(j).getLabel())) {
                                TA=Integer.parseInt(labels.get(j).getAddress(),16);
                                break;
                            }
                        }
                    }
                    s=s+"001";
                    String disp=Integer.toBinaryString(TA);
                    while(disp.length()<20) {
                        disp="0"+disp;
                    }
                    while(disp.length()>20) {
                        disp=disp.substring(1,disp.length());
                    }
                    s=s+disp;
                    if(s.substring(0,4).equals("0000"))
                        Instructions.get(i).setObc("0"+Integer.toHexString(Integer.parseInt(s,2)));
                    else
                        Instructions.get(i).setObc(Integer.toHexString(Integer.parseInt(s,2)));

                }
                else if(Instructions.get(i).getMnemonic().trim().equalsIgnoreCase("byte")&&!Instructions.get(i).isError()) {
                    String op="";
                    String s=Instructions.get(i).getOperand();
                    if(s.charAt(0)=='c'||s.charAt(0)=='C') {
                        s=s.substring(2,s.length()-1);
                        for(int j=0;j<s.length();j++) {
                            op=op+Integer.toHexString((int)s.charAt(j));
                        }
                    }
                    else {
                        s=s.substring(2,s.length()-1);
                        op=s;
                    }
                    Instructions.get(i).setObc(op);
                }
                else if(Instructions.get(i).getMnemonic().trim().equalsIgnoreCase("word")&&!Instructions.get(i).isError()) {
                    String ob="";
                    int f=0;
                    String nums[]=Instructions.get(i).getOperand().split(",");
                    for(int j=0;j<nums.length;j++) {
                        ob=ob+Integer.toHexString(Integer.parseInt(nums[j]));
                    }
                    if(f==1) {
                        if(Integer.parseInt(nums[i])<16)
                            Instructions.get(i).setObc("00000"+ob);
                        else
                            Instructions.get(i).setObc("0000"+ob);
                    }
                    else {
                        int x=Integer.parseInt(Instructions.get(i).getOperand());
                        if(x<16)
                            Instructions.get(i).setObc("00000"+Integer.toHexString(x));
                        else
                            Instructions.get(i).setObc("0000"+Integer.toHexString(x));
                    }
                }
                else
                    Instructions.get(i).setObc("");
                Instructions.get(i).setObc(Instructions.get(i).getObc().toUpperCase());
            }
        }
    }


    public static String getLiteralTable(){
        if(LiteralTable.size()==0)
            return "";
        String Table="Literal Address\t\t\tLiteral Hex Value\n";
        for(int i = 0;i<LiteralTable.size();i++){
           Table+=Integer.toHexString(sa+i*3)+"\t\t\t\t\t\t"+LiteralTable.get(i)+"\n";
        }
        return Table;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        processFixedFormat(readFile("Code.txt"));
        symtb();
        Objectcode();
        if(listFileGenerator("listfile.txt")) // returns true if there are no Errors
            System.out.println("\n\n\t\tSuccessful Assembly");
        else System.out.println("\n\n\t\tIncomplete Assembly");

        try {
            new GUItext("List File","listfile.txt");
        } catch (IOException e) {
        }
        try {
            new GUItext("Object File","obj.txt");
        } catch (IOException e) {
        }


    }
    
}