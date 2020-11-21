//如果方法是一个测试用例，就创建一个类方法对，把它加入到classMethodPairs,
//便于Selector类在进行测试用例选择时使用
public class ClassMethodPair {
    public String className;
    public String methodName;
    public ClassMethodPair(String c,String m){
        this.className=c;
        this.methodName=m;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }
}
