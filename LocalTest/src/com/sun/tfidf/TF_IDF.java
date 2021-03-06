package com.sun.tfidf;

import java.io.*;
import java.util.*;

import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.util.*;

import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 摘自CSDN BLOG
 * http://www.cnblogs.com/ywl925/archive/2013/08/26/3275878.html
 *
 * @author sunx(sunxin@strongit.com.cn)
 * @version V0.0.1
 */
public class TF_IDF {

    // the list of file
    /**
     * 数据资源文件集合
     */
    private static ArrayList<String> FileList = new ArrayList<String>();

    // get list of file for the directory, including sub-directory of it
    /**
     * 读取文件集合，包含其中包含的子文件夹
     * @param filepath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> readDirs(String filepath) throws FileNotFoundException, IOException {
        try {
            File file = new File(filepath);
            if (!file.isDirectory()) {
                System.out.println("输入的[]");
                System.out.println("filepath:" + file.getAbsolutePath());
            } else {
                String[] flist = file.list();
                for (int i = 0; i < flist.length; i++) {
                    File newfile = new File(filepath + "\\" + flist[i]);
                    if (!newfile.isDirectory()) {
                        FileList.add(newfile.getAbsolutePath());
                    } else if (newfile.isDirectory()) // if file is a directory, call ReadDirs
                    {
                        readDirs(filepath + "\\" + flist[i]);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return FileList;
    }

    // read file
    /**
     * 将文件读取文字符串
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(String file) throws FileNotFoundException, IOException {
        StringBuffer strSb = new StringBuffer(); // String is constant， StringBuffer can be changed.
        InputStreamReader inStrR = new InputStreamReader(new FileInputStream(file), "gbk"); // byte streams to character streams
        BufferedReader br = new BufferedReader(inStrR);
        String line = br.readLine();
        while (line != null) {
            strSb.append(line).append("\r\n");
            line = br.readLine();
        }

        return strSb.toString();
    }

    // word segmentation
    /**
     * 分词，调用IKAnalyzer
     * @param file
     * @return
     * @throws IOException
     */
    public static ArrayList<String> cutWords(String file) throws IOException {

        ArrayList<String> words = new ArrayList<String>();
        String text = TF_IDF.readFile(file);
        IKAnalyzer analyzer = new IKAnalyzer();
        words = analyzer.split(text);

        return words;
    }

    // term frequency in a file, times for each word
    /**
     * 遍历分词结果，统计每个词的出现频率
     * @param cutwords
     * @return
     */
    public static HashMap<String, Integer> normalTF(ArrayList<String> cutwords) {
        HashMap<String, Integer> resTF = new HashMap<String, Integer>();

        for (String word : cutwords) {
            if (resTF.get(word) == null) {
                resTF.put(word, 1);
                System.out.println(word);
            } else {
                resTF.put(word, resTF.get(word) + 1);
                System.out.println(word.toString());
            }
        }
        return resTF;
    }

    // term frequency in a file, frequency of each word
    /**
     * 将分词改为float
     * @param cutwords
     * @return
     */
    public static HashMap<String, Float> tf(ArrayList<String> cutwords) {
        HashMap<String, Float> resTF = new HashMap<String, Float>();

        int wordLen = cutwords.size();
        HashMap<String, Integer> intTF = TF_IDF.normalTF(cutwords);

        Iterator iter = intTF.entrySet().iterator(); // iterator for that get from TF
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            resTF.put(entry.getKey().toString(), Float.parseFloat(entry.getValue().toString()) / wordLen);
            System.out.println(entry.getKey().toString() + " = " + Float.parseFloat(entry.getValue().toString()) / wordLen);
        }
        return resTF;
    }

    // tf times for file
    /**
     * 所有文件进行词频统计
     * @param dirc
     * @return
     * @throws IOException
     */
    public static HashMap<String, HashMap<String, Integer>> normalTFAllFiles(String dirc) throws IOException {
        HashMap<String, HashMap<String, Integer>> allNormalTF = new HashMap<String, HashMap<String, Integer>>();

        List<String> filelist = TF_IDF.readDirs(dirc);
        for (String file : filelist) {
            HashMap<String, Integer> dict = new HashMap<String, Integer>();
            ArrayList<String> cutwords = TF_IDF.cutWords(file); // get cut word for one file

            dict = TF_IDF.normalTF(cutwords);
            allNormalTF.put(file, dict);
        }
        return allNormalTF;
    }

    // tf for all file
    /**
     * 所有文件词频统计float
     * @param dirc
     * @return
     * @throws IOException
     */
    public static HashMap<String, HashMap<String, Float>> tfAllFiles(String dirc) throws IOException {
        HashMap<String, HashMap<String, Float>> allTF = new HashMap<String, HashMap<String, Float>>();
        List<String> filelist = TF_IDF.readDirs(dirc);

        for (String file : filelist) {
            HashMap<String, Float> dict = new HashMap<String, Float>();
            ArrayList<String> cutwords = TF_IDF.cutWords(file); // get cut words for one file

            dict = TF_IDF.tf(cutwords);
            allTF.put(file, dict);
        }
        return allTF;
    }

    /**
     * 逆文档频率计算，该值由数据内容作为预料库。
     * @param all_tf
     * @return
     */
    public static HashMap<String, Float> idf(HashMap<String, HashMap<String, Float>> all_tf) {
        HashMap<String, Float> resIdf = new HashMap<String, Float>();
        HashMap<String, Integer> dict = new HashMap<String, Integer>();
        int docNum = FileList.size();

        for (int i = 0; i < docNum; i++) {
            HashMap<String, Float> temp = all_tf.get(FileList.get(i));
            Iterator iter = temp.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String word = entry.getKey().toString();
                if (dict.get(word) == null) {
                    dict.put(word, 1);
                } else {
                    dict.put(word, dict.get(word) + 1);
                }
            }
        }
        System.out.println("IDF for every word is:");
        Iterator iter_dict = dict.entrySet().iterator();
        while (iter_dict.hasNext()) {
            Map.Entry entry = (Map.Entry) iter_dict.next();
            float value = (float) Math.log(docNum / Float.parseFloat(entry.getValue().toString()));
            resIdf.put(entry.getKey().toString(), value);
            System.out.println(entry.getKey().toString() + " = " + value);
        }
        return resIdf;
    }

    /**
     * 计算TF-IDF值
     * @param all_tf
     * @param idfs
     */
    public static void tf_idf(HashMap<String, HashMap<String, Float>> all_tf, HashMap<String, Float> idfs) {
        HashMap<String, HashMap<String, Float>> resTfIdf = new HashMap<String, HashMap<String, Float>>();

        int docNum = FileList.size();
        for (int i = 0; i < docNum; i++) {
            String filepath = FileList.get(i);
            HashMap<String, Float> tfidf = new HashMap<String, Float>();
            HashMap<String, Float> temp = all_tf.get(filepath);
            Iterator iter = temp.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String word = entry.getKey().toString();
                Float value = (float) Float.parseFloat(entry.getValue().toString()) * idfs.get(word);
                tfidf.put(word, value);
            }
            resTfIdf.put(filepath, tfidf);
        }
        System.out.println("TF-IDF for Every file is :");
        DisTfIdf(resTfIdf);
    }

    /**
     * syso
     * .....不明觉厉
     * @param tfidf
     */
    public static void DisTfIdf(HashMap<String, HashMap<String, Float>> tfidf) {
        Iterator iter1 = tfidf.entrySet().iterator();
        while (iter1.hasNext()) {
            Map.Entry entrys = (Map.Entry) iter1.next();
            System.out.println("FileName: " + entrys.getKey().toString());
            System.out.print("{");
            HashMap<String, Float> temp = (HashMap<String, Float>) entrys.getValue();
            Iterator iter2 = temp.entrySet().iterator();
            while (iter2.hasNext()) {
                Map.Entry entry = (Map.Entry) iter2.next();
                System.out.print(entry.getKey().toString() + " = " + entry.getValue().toString() + ", ");
            }
            System.out.println("}");
        }

    }

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        String file = "D:/testfiles";

        HashMap<String, HashMap<String, Float>> all_tf = tfAllFiles(file);
        System.out.println();
        HashMap<String, Float> idfs = idf(all_tf);
        System.out.println();
        tf_idf(all_tf, idfs);

    }

}
