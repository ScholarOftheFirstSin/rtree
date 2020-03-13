package com.github.davidmoten.rtree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.internal.PointDouble;
import com.github.davidmoten.rtree.internal.EntryDefault;


import org.apache.commons.math3.util.Pair;
import org.junit.Test;
import rx.Observable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static com.github.davidmoten.rtree.geometry.Geometries.*;

public class MyTest {
    RTree<Integer, Point> myTree;

    /*@Test
    public void myTest() {
        RTree<String, Point> tree = RTree.maxChildren(5).create();
        tree = tree.add("DAVE", point(10, 20))
                .add("FRED", point(12, 25))
                .add("MARY", point(97, 125));

        Iterable<Entry<String, Point>> entries =
                tree.search(rectangle(8, 15, 30, 35)).toBlocking().toIterable();
        for (Entry e : entries) {
            System.out.println(e);
        }
    }*/

    @Test
    public void importDataSet() {
        myTree = RTree.create();
        BufferedReader reader;
        List<Entry<Integer, Point>> listToAdd = new ArrayList<Entry<Integer, Point>>();
        try {
            reader = new BufferedReader(new FileReader(
                    "C:/Users/qqq58/Desktop/BU2(github)/562/PA1/dataset1.txt/greek-earthquakes-1964-2000.txt"));
            String line = reader.readLine();
            Integer count = 1;
            while (line != null) {
                String[] aLine = line.split(" ");
                Point point = new PointDouble(Double.parseDouble(aLine[0]), Double.parseDouble(aLine[1]));
                Entry entry = new EntryDefault(count, point);
                listToAdd.add(entry);
                line = reader.readLine();
                count++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        myTree = myTree.add(listToAdd);
        myTree.visualize(600,600).save("target/mytree.png");
    }

    @Test
    public void skylineQuery () {
        Iterable<Entry<Integer, Point>> result = new ArrayList<Entry<Integer, Point>>();
        PriorityQueue<Pair<Node, Integer>> heap = new PriorityQueue<Pair<Node, Integer>>();
        Node root = myTree.root().get();
        System.out.print(root);
        /*Pair<Node, Integer> pair = new Pair(myTree.root(), Integer.MIN_VALUE);
        heap.add(pair);
        while(!heap.isEmpty()) {

        }*/
        //return result;
    }





}
