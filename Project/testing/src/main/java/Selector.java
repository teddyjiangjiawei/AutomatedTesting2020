import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;

import java.io.*;
import java.util.ArrayList;

//根据变更信息和代码关系图，进行测试用例选择
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

    //读change_info.txt里的内容,找到所有改变的类和方法，加到changedClasses和changedMethods里
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


    //递归根据方法依赖图选择出所有受变更方法影响的方法
    private ArrayList<String> methodSelect(ArrayList<String> selectedMethod,String method1){
        for(int i=0;i<methodGraph.fatherNodes.size();i++){
            if(methodGraph.fatherNodes.get(i).equals(method1)){
                if(!selectedMethod.contains(methodGraph.sonNodes.get(i))){
                    selectedMethod.add(methodGraph.sonNodes.get(i));
                    selectedMethod=methodSelect(selectedMethod,methodGraph.sonNodes.get(i));
                }
            }
        }

        return selectedMethod;
    }

    //方法级测试用例选择
    public void methodLevelSelect() throws IOException {
        //找到change_info.txt中所有发生变更的类和方法
        findAllChangedClassesAndMethods();
        ArrayList<String> selectedMethod=new ArrayList<String>();
        //递归根据方法依赖图选择出所有受变更方法影响的方法
        for(String method1:changedMethods){
            selectedMethod=methodSelect(selectedMethod,method1);
        }

        //把方法级测试用例选取结果写进selection-method.txt中
        BufferedWriter out=new BufferedWriter((new FileWriter("selection-method.txt")));
        String line;
        for(int i=0;i<selectedMethod.size();i++){
            for(int j=0;j<classMethodPairs.size();j++){
                if ((classMethodPairs.get(j).getMethodName()).equals(selectedMethod.get(i))) {
                    line=classMethodPairs.get(j).getClassName()+" "+selectedMethod.get(i)+"\n";
                    out.write(line);
                    //System.out.println(line);
                }
            }
        }
        out.close();
    }

    //类级测试用例选择
    public void classLevelSelect() throws IOException {
        //找到change_info.txt中所有发生变更的类和方法
        findAllChangedClassesAndMethods();
        ArrayList<String> selectedClasses=new ArrayList<String>();//存放选出来的测试用例类
        //根据类依赖图选出所有受改变的类影响的类
        for(String class1:changedClasses){
            for(int i=0;i<classGraph.fatherNodes.size();i++){
                if(classGraph.fatherNodes.get(i).equals(class1)){
                    if(!selectedClasses.contains(classGraph.sonNodes.get(i))){
                        selectedClasses.add(classGraph.sonNodes.get(i));
                    }
                }
            }
        }
        //把类级测试用例选择结果写入selection-class.txt中
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
