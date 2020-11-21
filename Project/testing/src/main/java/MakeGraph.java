import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;

import javax.swing.text.html.HTMLDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MakeGraph {
    public static ArrayList<ClassMethodPair> classMethodPairs;
    public static CHACallGraph chaCG;

    public static ArrayList<Object> initGraph(AnalysisScope scope) throws ClassHierarchyException, CancelException, IOException {
        //构建类层次对象,在缺失了某些分析所需的类时， makeWithRoot 方法会尽可能地为依赖这些缺失类的方法添加“Root”，即认为 java.lang.Object 为这些类的父类。
        //System.out.println(scope);
        ClassHierarchy cha= ClassHierarchyFactory.makeWithRoot(scope);
        //针对所有Application类（非原生类）生成进入点：
        Iterable<Entrypoint>  eps=new AllApplicationEntrypoints(scope,cha);
        //CHACallGraph ：使用类层次分析（Class Hierarchy Analysis）算法构建调用图。该方法构建的调用图精度较低，但是速度较快
        chaCG = new CHACallGraph(cha);
        chaCG.init(eps);

        for (IClass iClass : cha) {
            if(iClass.toString().contains("Application"))
                System.out.println(iClass);

        }System.out.println();
        String stats = CallGraphStats.getStats(chaCG);
        System.out.println(stats);


        Graph classGraph=new Graph(0);
        Graph methodGraph=new Graph(1);

        classMethodPairs=new ArrayList<ClassMethodPair>();

        for(CGNode node:chaCG){
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if(node.getMethod() instanceof ShrikeBTMethod){
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method=(ShrikeBTMethod)node.getMethod();
                //使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if("Application".equals(method.getDeclaringClass().getClassLoader().toString())){
                    //获取声名该方法的类的内部表示
                    String classInnerName=method.getDeclaringClass().getName().toString();
                    //获取方法签名
                    String signature =method.getSignature();
                    System.out.println(classInnerName+" "+signature);

                    //判断方法是否是一个测试用例
                    if(isTestMethod(method)){
                        classMethodPairs.add(new ClassMethodPair(classInnerName,signature));
                    }

                    //得到所有调用该方法节点的方法节点
                    Iterator<CGNode> iterator=chaCG.getPredNodes(node);
                    while (iterator.hasNext()){
                        CGNode node1=iterator.next();
                        String node1_classInnerName=node1.getMethod().getDeclaringClass().getName().toString();
                        String node1_signature=node1.getMethod().getSignature();
                        if(!(node1_classInnerName.startsWith("Ljava")||node1_classInnerName.startsWith("Ljavax"))){
                            classGraph.addNodes(classInnerName,node1_classInnerName);
                            methodGraph.addNodes(signature,node1_signature);
                        }
                    }

                }
            }else {
                System.out.println(String.format("'%s'不是一个ShrikeBTMehod:%s",node.getMethod(),node.getMethod().getClass()));
            }
        }
        //画图
        draw(classGraph,methodGraph);
        ArrayList<Object> result=new ArrayList<Object>();
        result.add(classGraph);
        result.add(methodGraph);
        result.add(classMethodPairs);
        return result;
    }
    
    private static boolean isTestMethod(ShrikeBTMethod method){
        boolean flag=false;
        Collection<Annotation> annotations=method.getAnnotations();
        for(Annotation annotation:annotations){
            if(annotation.getType().getName().toString().equals("Lorg/junit/Test")){
                flag=true;
                break;
            }
        }
        return flag;
    }

    private static void draw(Graph classGraph, Graph methodGraph) throws IOException {
        classGraph.makeDotFile();
        methodGraph.makeDotFile();
    }
}
