import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//依赖图
public class Graph {
    ArrayList<String> fatherNodes;//图中边开始的点
    ArrayList<String> sonNodes;//图中边指向的点
    int type;//0类,1方法

    public Graph(int t) {
        fatherNodes = new ArrayList<String>();
        sonNodes = new ArrayList<String>();
        type = t;
    }

    public void addNodes(String fatherNode, String sonNode) {
        fatherNodes.add(fatherNode);
        sonNodes.add(sonNode);
    }

    public void makeDotFile(String fileName, String s) throws IOException {
        //删除重复的内容
        for (int i = 0; i < fatherNodes.size(); i++) {
            for (int j = i + 1; j < fatherNodes.size(); j++) {
                if ((fatherNodes.get(i).equals(fatherNodes.get(j))) && (sonNodes.get(i).equals(sonNodes.get(j)))) {
                    fatherNodes.remove(j);
                    sonNodes.remove(j);
                    j--;
                }
            }

        }

        String name = "a";//digraph的名称
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        out.write("digraph " + name.toLowerCase() + "_" + s + "{\n");
        for (int i = 0; i < fatherNodes.size(); i++) {
            String str = "\t" + '"' + fatherNodes.get(i) + "\"->\"" + sonNodes.get(i) + "\";\n";
            out.write(str);
        }
        out.write("}");
        out.close();

    }
}