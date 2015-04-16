package jsas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.TreeMap;

public class JSAS {
    
    static String VERSION = "0.1";
    
    static String data;
    static TreeMap<String,String> hashops;
    static StringBuilder header = new StringBuilder();
    static StringBuilder methods = new StringBuilder();
    static StringBuilder classes = new StringBuilder();
    static int timed = 0;
    static int hashopsnospace = 0;
    
    public static void main(String[] args) throws Throwable{
        
        
        
        File file = new File(args[0]);
        if(!file.exists())
            return;
        
        boolean run = false;
        boolean compile = false;
        if(args[1].contains("run")){
            run = true;
            compile = true;
        }
        else if(args[1].contains("comp")){
            run = false;
            compile = true;
        }
        
        FileInputStream fis = new FileInputStream(file);
        StringBuilder strb = new StringBuilder((int)file.length());
        for(int i = 0; i < file.length(); i++)
            strb.append((char)fis.read());
        fis.close();
        data = strb.toString();
        
        long originalhash = data.hashCode();
        
        hashops = new TreeMap<String,String>();
        //FIND HASH CMDS
        int hl = data.indexOf('#');
        while(hl != -1){
            handleHashCommand(hl);
            hl = data.indexOf('#');
        }
        
        for(int i = 0; i < hashops.values().size(); i++){
            String s = (String)hashops.values().toArray()[i];
            String t = (String)hashops.keySet().toArray()[i];
            if(s.equals(t))
                continue;
            int l = data.indexOf(s);
            while(l != -1){
                replace(l,s,t);
                l = data.indexOf(s, l+1);
            }
        }
        
        replaceEnds();
        
        sortClasses();
        
        sortMethods();
        
        strb = new StringBuilder(header.toString()).append("\n//TRANSLATED FROM JSAS VERSION: "+VERSION+"\n//ORIGINAL FILE HASH: 0x"+Long.toHexString(originalhash).toUpperCase());
        strb.append("\nclass JSASDEFAULTCLASS {\n\npublic static void main(String[] args) throws Throwable {\n");
        if(timed==1)
            strb.append("long jSASNANOTIMEVAR = System.nanoTime();\n");
        strb.append(data).append("System.out.println((System.nanoTime()-jSASNANOTIMEVAR)/1000000.0);").append("\n}\n\n");
        strb.append(methods.toString()).append("}\n\n");
        strb.append(classes.toString());
        
        data = strb.toString();
        
        String fpath = "jsasout.java";
        if(args.length>=3)
            fpath = args[2];
        File outFile = new File(fpath);
        FileOutputStream fos = new FileOutputStream(outFile);
        for(int i = 0; i < data.length(); i++)
            fos.write((byte)data.charAt(i));
        fos.close();
        
        if(compile){
            ProcessBuilder compiler = new ProcessBuilder();
            compiler.command("javac", outFile.getAbsolutePath());
            compiler.redirectInput(ProcessBuilder.Redirect.INHERIT);
            compiler.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process compilerp = compiler.start();
            while(compilerp.isAlive());
            System.out.println();
            if(run){
                if(fpath.lastIndexOf("\\") != -1)
                    fpath = fpath.substring(0,fpath.lastIndexOf("\\"))+"JSASDEFAULTCLASS";
                else
                    fpath = "JSASDEFAULTCLASS";
                File compiled = new File(fpath);
                ProcessBuilder runner = new ProcessBuilder();
                runner.command("java", compiled.getAbsolutePath());
                runner.redirectInput(ProcessBuilder.Redirect.INHERIT);
                runner.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                Process runnerp = runner.start();
                while(runnerp.isAlive());
                
            }
        }
        
    }
    
    private static void replace(int loc, String a, String b){
        if(a.length() > 2 && !adjis(loc,a.length()))
            return;
        String part1 = data.substring(0,loc);
        String part2 = data.substring(loc+a.length(),data.length());
        StringBuilder tmp = new StringBuilder();
        tmp.append(part1);
        tmp.append(b);
        tmp.append(part2);
        data = tmp.toString();
    }

    private static boolean adjeq(int loc, int size, char c){
        return data.charAt(loc-1) == c || data.charAt(loc+size) == c;
    }
    private static boolean adjis(int loc, int size){
        if(loc == 0 )
            return data.charAt(loc+size)<=32;
        else if(loc+size >= data.length())
            return data.charAt(loc-1)<=32;
        return data.charAt(loc-1)<=32 && data.charAt(loc+size)<=32;
    }
    
    private static void sortMethods(){
        int start, end;
        int actual = data.indexOf('(');
        while(actual != -1){
            start = getLineStart(actual);
            end = data.indexOf(')',start);
            if(!data.substring(start,actual).matches(".*(for|while|do).*") && getWord(end+1).contains("{")){
                System.out.println("matches");
                StringBuilder tmp = new StringBuilder();
                tmp.append(data.substring(0,end+1)).append(" throws Throwable ").append(data.substring(end+1,data.length()));
                data = tmp.toString();
                String method = data.substring(start,findClosingBracket(findNext(end, '{'))+1);
                if(!method.contains("static"))
                    methods.append("static ");
                methods.append(method).append("\n\n");
                tmp = new StringBuilder();
                tmp.append(data.substring(0,start)).append(data.substring(findClosingBracket(findNext(end, '{'))+1,data.length()));
                data = tmp.toString();
            }
            actual = data.indexOf('(',actual+1);
        }
    }

    private static void sortClasses(){
        int start = data.indexOf("class");
        int end;
        while(start != -1){
            if(adjis(start, 5)){
                end = findClosingBracket(findNext(start,'{'));
                if(end == -1)
                    return;
                classes.append(data.substring(start,end+1)).append('\n');
                String part1 = data.substring(0,start-1);
                String part2 = data.substring(end+1,data.length());
                StringBuilder stmp = new StringBuilder();
                stmp.append(part1).append(part2);
                data = stmp.toString();
            }
            start = data.indexOf("class");
        }
    }
    
    private static int findNext(int l, char c){
        int a = l;
        while(a < data.length() && data.charAt(a) != c)
            a++;
        if(a == data.length())
            return Integer.MAX_VALUE;
        return a;
    }
    
    private static void handleHashCommand(int hl) {
        String cmd = getWord(hl+1);
        if(cmd.equals("redef") || cmd.equals("def"))
            hashops.put(getWord(nextSpace(hl)+1),getWord(nextSpace(nextSpace(hl))+1));
        else if (cmd.equals("import") || cmd.equals("imp"))
            header.append("import ").append(getWord(hl+cmd.length()+1)).append(";\n");
        else if (cmd.equals("staticimport") || cmd.equals("simp"))
            header.append("import static ").append(getWord(hl+cmd.length()+1)).append(";\n");
        else if (cmd.equals("package") || cmd.equals("pack"))
            header.insert(0, "package "+getWord(hl+cmd.length()+1)+";\n\n");
        else if (cmd.equals("time")){
            String arg = getWord(hl+cmd.length()+1);
            timed = arg.contains("nan")?1:arg.contains("mil")?2:arg.contains("sec")?3:0;
        }
        
        deleteLine(hl);
    }
    
    private static String getWord(int loc){
        String wrd = "";
        int a = loc;
        if(a>=data.length())
            return "";
        char c = data.charAt(a);
        if(c<=32){
            while(a < data.length() && c<=32){
                c = data.charAt(a);
                a++;
            }
            a--;
            c = data.charAt(a);
        }
        
        while(a < data.length()-1 && c>32){
            a++;
            wrd+=c;
            c = data.charAt(a);
        }
        return wrd;
    }
    
    private static int nextSpace(int loc){
        int a = loc+1;
        while(data.charAt(a)>32)
            a++;
        return a;
    }

    private static void deleteLine(int hl) {
        int max = hl;
        while(max < data.length() && data.charAt(max)!='\n')
            max++;
        
        int min = hl;
        while(min > 0 && data.charAt(min)!='\n')
            min--;
        
        String part1 = data.substring(0,min);
        String part2 = data.substring(max+1,data.length());
        StringBuilder tmp = new StringBuilder();
        tmp.append(part1).append(part2);
        data = tmp.toString();
    }

    private static int findClosingBracket(int l) {
        int num = 0;
        if(data.charAt(l)=='{')
            num++;
        int ltmp = l;
        while(ltmp < data.length()){
            int open = findNext(ltmp+1, '{');
            int close = findNext(ltmp+1, '}');
            if(open<close){
                num++;
                ltmp = open;
            }
            else{
                num--;
                ltmp = close;
            }
            if(num==0)
                return close;
        }
        return -1;
    }

    private static void replaceEnds() {
        int a = data.indexOf("end");
        while(a != -1){
            if(adjis(a, 3) && !getWord(a+3).matches("[=;]")){
                StringBuilder tmp = new StringBuilder();
                tmp.append(data.substring(0,a)).append('}').append(data.substring(a+3));
                data = tmp.toString();
            }
            a = data.indexOf("end");
        }
        
        if(hashops.get("}")!=null){
            int bl = hashops.get("}").length();
            int b = data.indexOf("end");
            while(b != -1){
                if(adjis(b, 3)){
                    StringBuilder tmp = new StringBuilder();
                    tmp.append(data.substring(0,b)).append('}').append(data.substring(b+bl));
                    data = tmp.toString();
                }
                b = data.indexOf("end");
            }
        }
    }

    private static int getLineStart(int l) {
        int a = l;
        while(a>0 && (data.charAt(a)!='\n' && data.charAt(a)!=';'))
            a--;
        return (data.charAt(a)=='\n' || data.charAt(a)==';')?a+1:a;
    }

}
