import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;

import java.io.*;
import java.util.ArrayList;

public class Selector {
    public String changeInfoPath;
    public Graph classGraph;
    public Graph methodGraph;
    ArrayList<ClassMethodPair> classMethodPairs;
    ArrayList<String> changedClasses;
    ArrayList<String> changedMethods;


    public Selector(Graph classGraph,Graph methodGraph,ArrayList<ClassMethodPair> classMethodPairs,String changeInfoPath){
        this.classGraph=classGraph;
        this.methodGraph=methodGraph;
        this.classMethodPairs=classMethodPairs;
        this.changeInfoPath=changeInfoPath;
        changedClasses=new ArrayList<String>();
        changedMethods=new ArrayList<String>();
    }

    //找到所有改变的类和方法
    public void findAllChangedClassesAndMethods() throws IOException {
        FileInputStream fileInputStream=new FileInputStream(changeInfoPath);
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(fileInputStream));
        String str="";
        while ((str=bufferedReader.readLine())!=null){
            String changed_class=str.split(" ")[0];
            String changed_method=str.split(" ")[1];
            if(!changedClasses.contains(changed_class)){
                changedClasses.add(changed_class);
            }
            if(!changedMethods.contains(changed_method)){
                changedMethods.add(changed_method);
            }
        }
    }

//    private void outputSelectedMethodFile(int type){
//
//    }
    //递归进行方法级测试用例选择
    private ArrayList<String> methodSelect(ArrayList<String> result,String method1){
        for(int i=0;i<methodGraph.fatherNodes.size();i++){
            if(methodGraph.fatherNodes.get(i).equals(method1)){
                if(!result.contains(methodGraph.sonNodes.get(i))){
                    result.add(methodGraph.sonNodes.get(i));
                    result=methodSelect(result,methodGraph.sonNodes.get(i));
                }
            }
        }

        return result;
    }
    //方法级测试用例选择
    public void methodLevelSelect() throws IOException {
        findAllChangedClassesAndMethods();
        ArrayList<String> result=new ArrayList<String>();
        for(String method1:changedMethods){
            result=methodSelect(result,method1);
        }

        BufferedWriter out=new BufferedWriter((new FileWriter("selection-method.txt")));
        String line;
        for(int i=0;i<result.size();i++){
            for(int j=0;j<classMethodPairs.size();j++){
                if ((classMethodPairs.get(j).getMethodName()).equals(result.get(i))) {
                    line=classMethodPairs.get(j).getClassName()+" "+result.get(i)+"\n";
                    out.write(line);
                    System.out.println(line);
                }
            }
        }
        out.close();
    }
    //类级测试用例选择
    public void classLevelSelect() throws IOException {
        findAllChangedClassesAndMethods();
        ArrayList<String> selectedClasses=new ArrayList<String>();//存放选出来的测试用例类
        for(String class1:changedClasses){
            for(int i=0;i<classGraph.fatherNodes.size();i++){
                if(classGraph.fatherNodes.get(i).equals(class1)){
                    if(!selectedClasses.contains(classGraph.sonNodes.get(i))){
                        selectedClasses.add(classGraph.sonNodes.get(i));
                    }
                }
            }
        }

        BufferedWriter out=new BufferedWriter(new FileWriter("selection-class.txt"));
        String line;
        for(int i=0;i<selectedClasses.size();i++){
            for(int j=0;j<classMethodPairs.size();j++){
                if((classMethodPairs.get(j).getClassName()).equals(selectedClasses.get(i))){
                    line=selectedClasses.get(i)+" "+classMethodPairs.get(j).getMethodName()+"\n";
                    out.write(line);
                    System.out.println(line);
                }
            }
        }
        out.close();

    }
}
