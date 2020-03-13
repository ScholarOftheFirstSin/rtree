package com.github.davidmoten.rtree;

import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.PointDouble;
import com.github.davidmoten.rtree.internal.EntryDefault;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SkylineQuery {


    public  RTree importDataSet(RTree rTree) {
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

        rTree = rTree.add(listToAdd);
        rTree.visualize(600, 600).save("target/mytree.png");
        System.out.println(rTree.size());
        return rTree;
    }


    public Iterable<Entry<Integer, Point>> skylineQuery(RTree<Integer, Point> rTree) {
        Iterable<Entry<Integer, Point>> result = new ArrayList<Entry<Integer, Point>>();
        PriorityQueue<Pair> heap = new PriorityQueue<Pair>(1000, new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                if (o1.mindist < o2.mindist) {
                    return -1;
                } else if (o1.mindist > o2.mindist) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        Node root = rTree.root().get();
        //System.out.print(root);
        Pair<Node> pair = new Pair(root, Double.MIN_VALUE);
        heap.add(pair);
        while(!heap.isEmpty()) {
            Pair tempPair = heap.poll();

            if (tempPair.nodeOrEntry instanceof NonLeafDefault) {
                NonLeafDefault internalNode = (NonLeafDefault) tempPair.nodeOrEntry;
                List<Node<Integer, Point>> children = internalNode.children();
                for (int i = 0; i < children.size(); i++) {
                    Node node = children.get(i);
                    if (dominanceCheck(node, (List<Entry<Integer, Point>>) result)) {
                        Double mindist;
                        if (node instanceof NonLeafDefault) {
                            mindist = ((NonLeafDefault) node).getMbr().x1() + ((NonLeafDefault) node).getMbr().y1();
                        } else {
                            mindist = ((LeafDefault) node).getMbr().x1() + ((LeafDefault) node).getMbr().y1();
                        }
                        Pair<Node<Integer, Point>> tPair = new Pair<Node<Integer, Point>>(node, mindist);
                        heap.add(tPair);
                    }


                }

            } else if (tempPair.nodeOrEntry instanceof LeafDefault) {
                LeafDefault leafNode = (LeafDefault) tempPair.nodeOrEntry;
                List<Entry<Integer, Point>> children = leafNode.entries();
                for (int i = 0; i < children.size(); i++) {
                    Entry entry = children.get(i);
                    if (dominanceCheck(entry, (List<Entry<Integer, Point>>) result)) {
                        Double mindist;
                        mindist = ((Point) entry.geometry()).x() + ((Point) entry.geometry()).y();
                        Pair<Entry<Integer, Point>> tPair = new Pair<Entry<Integer, Point>>(entry, mindist);
                        heap.add(tPair);
                    }

                }
            } else {
                if (dominanceCheck((Entry<Integer, Point>)tempPair.nodeOrEntry, (List<Entry<Integer, Point>>) result)) {
                    ((ArrayList<Entry<Integer, Point>>) result).add((Entry<Integer, Point>)tempPair.nodeOrEntry);
                }

            }

        }
        return result;
    }

    private <T> boolean dominanceCheck(T nodeOrEntry, List<Entry<Integer, Point>> result) {
        if (nodeOrEntry instanceof NonLeafDefault) {
            NonLeafDefault nld = (NonLeafDefault) nodeOrEntry;
            Rectangle mbr = nld.getMbr();
            Double x1 = mbr.x1();
            Double y1 = mbr.y1();

            for (Entry e : result) {
                Double tempx = ((Point) e.geometry()).x();
                Double tempy = ((Point) e.geometry()).y();
                if (x1 >= tempx && y1 >= tempy ) {
                    return false;
                }
            }
            return true;

        } else if (nodeOrEntry instanceof LeafDefault) {
            LeafDefault ld = (LeafDefault) nodeOrEntry;
            Rectangle mbr = ld.getMbr();
            Double x1 = mbr.x1();
            Double y1 = mbr.y1();

            for (Entry e : result) {
                Double tempx = ((Point) e.geometry()).x();
                Double tempy = ((Point) e.geometry()).y();
                if (x1 >= tempx && y1 >= tempy ) {
                    return false;
                }
            }
            return true;
        } else {
            Entry entry = (Entry) nodeOrEntry;
            Double x = ((Point) entry.geometry()).x();
            Double y = ((Point) entry.geometry()).y();
            for (Entry e : result) {
                Double tempx = ((Point) e.geometry()).x();
                Double tempy = ((Point) e.geometry()).y();
                if (x >= tempx && y >= tempy) {
                    return false;    // Prune the dominated node
                }
            }
            return true;  // Add the free node to our heap

        }
    }

    public List dynamicInsert (Entry insertedEntry, List<Entry<Integer, Point>> skylinePoints) {

        Double x = ((Point) insertedEntry.geometry()).x();
        Double y = ((Point) insertedEntry.geometry()).y();
        for (int i = 0; i < skylinePoints.size(); i++) {
            Double tempx = ((Point) skylinePoints.get(i).geometry()).x();
            Double tempy = ((Point) skylinePoints.get(i).geometry()).y();
            if (x >= tempx && y >= tempy) {
                return skylinePoints;
            } else if (x <= tempx && y <= tempy) {
                skylinePoints.remove(i);
                i--;
            }
        }
        skylinePoints.add(insertedEntry);
        return skylinePoints;
    }

    public List dynamicDelete (Entry deletedEntry, List<Entry<Integer, Point>> skylinePoints, RTree myTree) {
        Collections.sort(skylinePoints, new Comparator<Entry<Integer, Point>>() {
            @Override
            public int compare(Entry<Integer, Point> o1, Entry<Integer, Point> o2) {
                if (o1.geometry().x() > o2.geometry().x()) {
                    return 1;
                } else if (o1.geometry().x() < o2.geometry().x()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        Double x = ((Point) deletedEntry.geometry()).x();
        Double y = ((Point) deletedEntry.geometry()).y();
        for (int i = 0; i < skylinePoints.size(); i++) {
            Double tempx = ((Point) skylinePoints.get(i).geometry()).x();
            Double tempy = ((Point) skylinePoints.get(i).geometry()).y();
            if (x > tempx && y > tempy) {
                return skylinePoints;
            } else if (Double.compare(x, tempx) == 0 && Double.compare(y, tempy) == 0) {
                Double x2;
                Double y2;
                if (i == skylinePoints.size() - 1) {
                    x2 = 100.0;
                    y2 = skylinePoints.get(i-1).geometry().y();

                } else if (i == 0) {
                    x2 = skylinePoints.get(i+1).geometry().x();
                    y2 = 100.0;

                }  else {
                    x2 = skylinePoints.get(i+1).geometry().x();
                    y2 = skylinePoints.get(i-1).geometry().y();

                }
                myTree = myTree.delete(deletedEntry);
                Iterable<Entry<Integer, Point>> dominanceArea = myTree.search(Geometries.rectangle(x,y,x2,y2)).toBlocking().toIterable();
                //List<Entry<Integer, Point>> dominanceArea = (List<Entry<Integer, Point>>) (myTree.search(Geometries.rectangle(x,y,x2,y2)).toBlocking().toIterable());
                RTree<Integer, Point> rtree = RTree.create();
                rtree = rtree.add(dominanceArea);
                SkylineQuery sq = new SkylineQuery();
                Iterable<Entry<Integer, Point>> newPoints = sq.skylineQuery(rtree);
                skylinePoints.addAll((List<Entry<Integer, Point>>)newPoints);
                skylinePoints.remove(i);
                i--;
            }
        }
        return skylinePoints;
    }


    public static void main(String[] args) {
        RTree<Integer, Point> myTree = RTree.create();;
        SkylineQuery sq = new SkylineQuery();
        myTree = sq.importDataSet(myTree);
        Iterable<Entry<Integer, Point>> result = sq.skylineQuery(myTree);
        System.out.println(((ArrayList<Entry<Integer, Point>>)result).size());
        for (Entry r : result) {
            System.out.println(r);
        }
        System.out.println("__________________");
        Entry insertedEntry = new EntryDefault(0, new PointDouble(33.91,20.07));
        result = sq.dynamicInsert(insertedEntry, (List<Entry<Integer, Point>>) result);
        myTree = myTree.add(insertedEntry);
        for (Entry r : result) {
            System.out.println(r);
        }
        Entry deletedEntry = new EntryDefault(36517, new PointDouble(33.92, 20.08));
        result = sq.dynamicDelete(deletedEntry, (List<Entry<Integer, Point>>) result, myTree);
        myTree = myTree.delete(deletedEntry);
        System.out.println("__________________");
        for (Entry r : result) {
            System.out.println(r);
        }
    }

    private class Pair<T> {
        private T nodeOrEntry;
        private Double mindist;

        public Pair (T nodeOrEntry, Double mindist) {
            this.nodeOrEntry = nodeOrEntry;
            this.mindist = mindist;
        }

    }
}

