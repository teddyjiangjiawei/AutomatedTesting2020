import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.sun.org.apache.xpath.internal.objects.XObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Initiate {
    public static AnalysisScope scope;
    public static CHACallGraph cg;


    public static void main(String[] args) throws InvalidClassFileException, IOException, ClassHierarchyException, CancelException {
        String type=args[0];//-c:类级，-m:方法级
        String path=args[1];//target文件的路径
        String changeInfoPath=args[2];//变更信息的路径

//        type="-c";
//        path="C:\\Users\\asus\\Desktop\\自动化测试\\大作业\\AutomatedTesting2020\\Data\\0-CMD\\target";
//        changeInfoPath="C:\\Users\\asus\\Desktop\\自动化测试\\大作业\\AutomatedTesting2020\\Data\\0-CMD\\data\\change_info.txt";

        System.out.println(type);
        System.out.println(path);
        System.out.println(changeInfoPath);

        //生成分析域
        File exclusion=new FileProvider().getFile("exclusion.txt");
        ClassLoader classLoader=Initiate.class.getClassLoader();//获得Initiate这个对象的加载器
        //该方法能够返回一个只包含Java原生类的分析域，并排除一些不常用的原生类（如： sun.awt.* ）
        scope= AnalysisScopeReader.readJavaScope("scope.txt",exclusion,classLoader);
        //找到所有以.class结尾的文件，并把这些类文件加入分析域中
        findAllClasses(path);

//        System.out.println(scope);
//        System.out.println();
        //初始化载入点、初始化图
        MakeGraph makeGraph=new MakeGraph();
        ArrayList<Object> result=makeGraph.initGraph(scope);

        //取得类依赖图
        Graph classGraph=(Graph)result.get(0);
        //取得方法依赖图
        Graph methodGraph=(Graph)result.get(1);
        //取得测试用例的class-method集
        ArrayList<ClassMethodPair> classMethodPairs=(ArrayList<ClassMethodPair>)result.get(2);
        

        //根据依赖图进行测试用例选择
        if(type.equals("-m")){
            new Selector(classGraph,methodGraph,classMethodPairs,changeInfoPath).methodLevelSelect();
        }else{
            new Selector(classGraph,methodGraph,classMethodPairs,changeInfoPath).classLevelSelect();
        }

    }
    //找到所有的.class文件
    public static void findAllClasses(String path) throws InvalidClassFileException {
        File file=new File(path);
        if(file.exists()){
            File[] fileLists=file.listFiles();
            if(fileLists!=null){
                for(File file1:fileLists){
                    //如果是目录，递归
                    if(file1.isDirectory()){
                        findAllClasses(file1.getAbsolutePath());
                    } else if(file1.getName().endsWith(".class")){//如果以.class结尾
                        //将我们想要分析的类动态地加入到分析域中
                        scope.addClassFileToScope(ClassLoaderReference.Application,file1);
                    }
                }
            }
        }else{
            System.out.println("file not exists!");
        }
    }
}
