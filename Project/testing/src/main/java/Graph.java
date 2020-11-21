import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//依赖图
public class Graph {
    ArrayList<String> fatherNodes;
    ArrayList<String> sonNodes;
    int type;//0类,1方法

    public Graph(int t){
        fatherNodes=new ArrayList<String>();
        sonNodes=new ArrayList<String>();
        type=t;
    }
    //添加父子结点
    public void addNodes(String fatherNode,String sonNode){
        fatherNodes.add(fatherNode);
        sonNodes.add(sonNode);
    }

    public void makeDotFile() throws IOException {
        //删除重复的内容
        for(int i=0;i<fatherNodes.size();i++){
            for(int j=i+1;j<fatherNodes.size();j++){
                if((fatherNodes.get(i).equals(fatherNodes.get(j)))&&(sonNodes.get(i).equals(sonNodes.get(j)))){
                    fatherNodes.remove(j);
                    sonNodes.remove(j);
                    j--;
                }
            }

        }

        String name="a";
        if(type==0){
            //类级
            BufferedWriter out=new BufferedWriter(new FileWriter("classDraw.dot"));
            out.write("digraph "+name.toLowerCase()+"_class{\n");
            for(int i=0;i<fatherNodes.size();i++){
                String str="\t"+'"'+fatherNodes.get(i)+"\"->\""+sonNodes.get(i)+"\";\n";
                out.write(str);
            }
            out.write("}");
            out.close();


        }else{
            //方法级
            BufferedWriter out=new BufferedWriter(new FileWriter("methodDraw.dot"));
            out.write("digraph "+name.toLowerCase()+"_method{\n");
            for(int i=0;i<fatherNodes.size();i++){
                String str="\t"+'"'+fatherNodes.get(i)+"\"->\""+sonNodes.get(i)+"\";\n";
                out.write(str);
            }
            out.write("}");
            out.close();
        }
    }

}
