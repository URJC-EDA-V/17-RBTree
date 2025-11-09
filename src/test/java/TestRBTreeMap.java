import es.urjc.grafo.EDA.searchtree.AbstractBalanceableTreeMap;
import es.urjc.grafo.EDA.searchtree.RBTreeMap;
import es.urjc.grafo.EDA.util.Entry;
import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class TestRBTreeMap {

    private static Method sanityCheck;
    private static Method dumpTree;
    private static String expectedTree =
            "<4, SecondFour>(Black)\n" +
                    "  <2, Two>(Black)\n" +
                    "    <1, One>(Black)\n" +
                    "      leaf\n" +
                    "      leaf\n" +
                    "    <3, Three>(Black)\n" +
                    "      leaf\n" +
                    "      leaf\n" +
                    "  <8, Eight>(Black)\n" +
                    "    <6, Six>(Red)\n" +
                    "      <5, Five>(Black)\n" +
                    "        leaf\n" +
                    "        leaf\n" +
                    "      <7, Seven>(Black)\n" +
                    "        leaf\n" +
                    "        leaf\n" +
                    "    <10, Ten>(Red)\n" +
                    "      <9, Nine>(Black)\n" +
                    "        leaf\n" +
                    "        leaf\n" +
                    "      <11, Eleven>(Black)\n" +
                    "        leaf\n" +
                    "        <12, Twelve>(Red)\n" +
                    "          leaf\n" +
                    "          leaf\n";
    private static String expectedTree2 =
            "<2, Two>(Black)\n" +
                    "  <1, One>(Black)\n" +
                    "    leaf\n" +
                    "    leaf\n" +
                    "  <5, Five>(Red)\n" +
                    "    <3, Three>(Black)\n" +
                    "      leaf\n" +
                    "      leaf\n" +
                    "    <6, Six>(Black)\n" +
                    "      leaf\n" +
                    "      leaf\n";

    @BeforeAll
    public static void initReflection() throws NoSuchMethodException {
        sanityCheck = RBTreeMap.class.getDeclaredMethod("sanityCheck");
        sanityCheck.setAccessible(true);
        dumpTree = AbstractBalanceableTreeMap.class.getDeclaredMethod("dump");
        dumpTree.setAccessible(true);
    }

    private static boolean sanity(RBTreeMap<?, ?> t) {
        try {
            return (Boolean) sanityCheck.invoke(t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static String dump(RBTreeMap<?, ?> t) {
        try {
            return dumpTree.invoke(t).toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void emptyMapBehaviour() {
        RBTreeMap<Integer, String> rb = new RBTreeMap<>();
        Assertions.assertEquals(0, rb.size());
        Assertions.assertTrue(rb.isEmpty());
        Assertions.assertNull(rb.get(1));
        Assertions.assertNull(rb.remove(1));
        Assertions.assertNull(rb.firstEntry());
        Assertions.assertNull(rb.lastEntry());
        Assertions.assertNull(rb.floorEntry(5));
        Assertions.assertNull(rb.ceilingEntry(5));
        Assertions.assertNull(rb.previousEntry(5));
        Assertions.assertNull(rb.nextEntry(5));
        int count = 0;
        for (Entry<Integer, String> ignored : rb.entrySet()) count++;
        Assertions.assertEquals(0, count);
        count = 0;
        for (Entry<Integer, String> ignored : rb.subMap(0, 10)) count++;
        Assertions.assertEquals(0, count);
        count = 0;
        for (Entry<Integer, String> ignored : rb.subMapSnapshot(0, 10)) count++;
        Assertions.assertEquals(0, count);
        Assertions.assertTrue(sanity(rb));
    }

    @Test
    public void insertAscendingAndUpdate() {
        RBTreeMap<Integer, String> rb = new RBTreeMap<>();
        for (int i = 1; i <= 20; i++) {
            Assertions.assertNull(rb.put(i, "V" + i));
            Assertions.assertTrue(sanity(rb));
        }
        Assertions.assertEquals(20, rb.size());
        for (int i = 1; i <= 20; i++) {
            Assertions.assertEquals("V" + i, rb.get(i));
        }
        // update existing key
        Assertions.assertEquals("V10", rb.put(10, "New"));
        Assertions.assertEquals("New", rb.get(10));
        Assertions.assertEquals(20, rb.size());
        Assertions.assertTrue(sanity(rb));
        // order queries
        Assertions.assertEquals(1, rb.firstEntry().getKey());
        Assertions.assertEquals(20, rb.lastEntry().getKey());
        Assertions.assertNull(rb.previousEntry(1));
        Assertions.assertNull(rb.nextEntry(20));
        Assertions.assertEquals(11, rb.nextEntry(10).getKey());
        Assertions.assertEquals(9, rb.previousEntry(10).getKey());
        Assertions.assertEquals(10, rb.floorEntry(10).getKey());
        Assertions.assertEquals(10, rb.ceilingEntry(10).getKey());
        Assertions.assertEquals(10, rb.floorEntry(10).getKey());
        Assertions.assertNull(rb.floorEntry(0));
        Assertions.assertNull(rb.ceilingEntry(21));
        Assertions.assertEquals(1, rb.ceilingEntry(0).getKey());
        // iteration order
        int expected = 1;
        for (Entry<Integer, String> e : rb.entrySet()) {
            Assertions.assertEquals(expected, e.getKey());
            expected++;
        }
        Assertions.assertEquals(21, expected); // should finish at 21
        // keySet & values sizes
        int keys = 0; for (Integer k : rb.keySet()) { keys++; }
        int vals = 0; for (String v : rb.values()) { vals++; }
        Assertions.assertEquals(20, keys);
        Assertions.assertEquals(20, vals);
    }

    @Test
    public void insertDescending() {
        RBTreeMap<Integer, String> rb = new RBTreeMap<>();
        for (int i = 20; i >= 1; i--) {
            rb.put(i, "V" + i);
            Assertions.assertTrue(sanity(rb));
        }
        int expected = 1;
        for (Entry<Integer, String> e : rb.entrySet()) {
            Assertions.assertEquals(expected, e.getKey());
            expected++;
        }
        Assertions.assertEquals(21, expected);
        Assertions.assertEquals(20, rb.size());
    }

    @Test
    public void deleteVariousKeysWithOracle() {
        RBTreeMap<Integer, String> rb = new RBTreeMap<>();
        TreeMap<Integer, String> oracle = new TreeMap<>();
        // fill
        for (int i = 1; i <= 30; i++) {
            Assertions.assertNull(rb.put(i, "V" + i));
            Assertions.assertNull(oracle.put(i, "V" + i));
        }
        Assertions.assertTrue(sanity(rb));
        // delete some specific keys across categories
        int[] keys = {1, 2, 15, 16, 30, 14, 13, 20, 10, 25, 5, 6, 7};
        for (int k : keys) {
            String rv1 = rb.remove(k);
            String rv2 = oracle.remove(k);
            Assertions.assertEquals(rv2, rv1);
            // removing again returns null
            Assertions.assertNull(rb.remove(k));
            Assertions.assertTrue(sanity(rb));
            // compare sizes and content order with oracle
            Assertions.assertEquals(oracle.size(), rb.size());
            Iterator<Integer> itKeys = oracle.keySet().iterator();
            for (Entry<Integer, String> e : rb.entrySet()) {
                Assertions.assertTrue(itKeys.hasNext());
                Assertions.assertEquals(itKeys.next(), e.getKey());
            }
            Assertions.assertFalse(itKeys.hasNext());
        }
        // After deletions, put some keys back and ensure works
        Assertions.assertNull(rb.put(1, "V1"));
        oracle.put(1, "V1");
        Assertions.assertNull(rb.put(30, "V30"));
        oracle.put(30, "V30");
        Assertions.assertTrue(sanity(rb));
        Assertions.assertEquals(oracle.size(), rb.size());
    }

    @Test
    public void subMapBoundariesAndSnapshot() {
        RBTreeMap<Integer, String> rb = new RBTreeMap<>();
        for (int i = 1; i <= 10; i++) rb.put(i, "V" + i);
        // [3,7)
        List<Integer> keys = new ArrayList<>();
        for (Entry<Integer, String> e : rb.subMap(3, 7)) keys.add(e.getKey());
        Assertions.assertEquals(Arrays.asList(3,4,5,6), keys);
        // full range [1, 11)
        keys.clear();
        for (Entry<Integer, String> e : rb.subMap(1, 11)) keys.add(e.getKey());
        Assertions.assertEquals(Arrays.asList(1,2,3,4,5,6,7,8,9,10), keys);
        // empty ranges
        keys.clear();
        for (Entry<Integer, String> e : rb.subMap(0, 1)) keys.add(e.getKey());
        Assertions.assertTrue(keys.isEmpty());
        keys.clear();
        for (Entry<Integer, String> e : rb.subMap(8, 8)) keys.add(e.getKey());
        Assertions.assertTrue(keys.isEmpty());
        keys.clear();
        for (Entry<Integer, String> e : rb.subMap(9, 5)) keys.add(e.getKey());
        Assertions.assertTrue(keys.isEmpty());
        // snapshot immutability
        List<Integer> snap = new ArrayList<>();
        for (Entry<Integer, String> e : rb.subMapSnapshot(3, 7)) snap.add(e.getKey());
        // modify tree
        rb.put(0, "V0");
        rb.put(7, "V7");
        rb.remove(3);
        // snapshot must remain the same
        Assertions.assertEquals(Arrays.asList(3,4,5,6), snap);
    }

    @Test
    public void customComparatorReverseOrder() {
        Comparator<Integer> desc = Comparator.reverseOrder();
        RBTreeMap<Integer, String> rb = new RBTreeMap<>(desc);
        for (int i = 1; i <= 5; i++) rb.put(i, "V" + i);
        // With reverse order comparator, first is numerically largest
        Assertions.assertEquals(5, rb.firstEntry().getKey());
        Assertions.assertEquals(1, rb.lastEntry().getKey());
        // iteration should be 5..1
        int expected = 5;
        for (Entry<Integer, String> e : rb.entrySet()) {
            Assertions.assertEquals(expected, e.getKey());
            expected--;
        }
        Assertions.assertEquals(0, expected);
        // successor/predecessor according to comparator
        Assertions.assertEquals(4, rb.nextEntry(5).getKey());
        Assertions.assertEquals(4, rb.previousEntry(3).getKey());
        Assertions.assertEquals(2, rb.previousEntry(1).getKey());
        Assertions.assertNull(rb.nextEntry(1)); // since 1 is the smallest under reverse order
        Assertions.assertTrue(sanity(rb));
    }

    @Test
    public void keySetAndValuesConsistency() {
        RBTreeMap<Integer, String> rb = new RBTreeMap<>();
        rb.put(2, "two");
        rb.put(1, "one");
        rb.put(3, "three");
        rb.put(2, "TWO"); // update
        List<Integer> keys = new ArrayList<>();
        for (Integer k : rb.keySet()) keys.add(k);
        Assertions.assertEquals(Arrays.asList(1,2,3), keys);
        List<String> vals = new ArrayList<>();
        for (String v : rb.values()) vals.add(v);
        Assertions.assertEquals(Arrays.asList("one","TWO","three"), vals);
        Assertions.assertTrue(sanity(rb));
    }

    @Test
    public void randomizedFuzzAgainstJavaTreeMap() {
        RBTreeMap<Integer, String> rb = new RBTreeMap<>();
        TreeMap<Integer, String> oracle = new TreeMap<>();
        Random rnd = new Random(12345);
        for (int i = 0; i < 500; i++) {
            int key = rnd.nextInt(100);
            int op = rnd.nextInt(3);
            switch (op) {
                case 0 -> { // put
                    String newVal = "v" + rnd.nextInt(10);
                    String r1 = rb.put(key, newVal);
                    String r2 = oracle.put(key, newVal);
                    Assertions.assertEquals(r2, r1);
                }
                case 1 -> { // remove
                    String r1 = rb.remove(key);
                    String r2 = oracle.remove(key);
                    Assertions.assertEquals(r2, r1);
                }
                case 2 -> { // get
                    Assertions.assertEquals(oracle.get(key), rb.get(key));
                }
            }
            // validate sizes
            Assertions.assertEquals(oracle.size(), rb.size());
            // validate iteration order
            Iterator<Integer> itKeys = oracle.keySet().iterator();
            for (Entry<Integer, String> e : rb.entrySet()) {
                Assertions.assertTrue(itKeys.hasNext());
                Integer k = itKeys.next();
                Assertions.assertEquals(k, e.getKey());
                Assertions.assertEquals(oracle.get(k), e.getValue());
            }
            Assertions.assertFalse(itKeys.hasNext());
            // spot-check order queries
            if (!oracle.isEmpty()) {
                Assertions.assertEquals(oracle.firstKey(), rb.firstEntry().getKey());
                Assertions.assertEquals(oracle.lastKey(), rb.lastEntry().getKey());
                int probe = rnd.nextInt(110) - 5; // slightly outside range too
                Map.Entry<Integer, String> lo = oracle.floorEntry(probe);
                Map.Entry<Integer, String> hi = oracle.ceilingEntry(probe);
                Entry<Integer, String> lo2 = rb.floorEntry(probe);
                Entry<Integer, String> hi2 = rb.ceilingEntry(probe);
                Assertions.assertEquals(lo == null ? null : lo.getKey(), lo2 == null ? null : lo2.getKey());
                Assertions.assertEquals(hi == null ? null : hi.getKey(), hi2 == null ? null : hi2.getKey());
                Map.Entry<Integer, String> prev = oracle.lowerEntry(probe);
                Map.Entry<Integer, String> next = oracle.higherEntry(probe);
                Entry<Integer, String> prev2 = rb.previousEntry(probe);
                Entry<Integer, String> next2 = rb.nextEntry(probe);
                Assertions.assertEquals(prev == null ? null : prev.getKey(), prev2 == null ? null : prev2.getKey());
                Assertions.assertEquals(next == null ? null : next.getKey(), next2 == null ? null : next2.getKey());
            } else {
                Assertions.assertNull(rb.firstEntry());
                Assertions.assertNull(rb.lastEntry());
            }
            Assertions.assertTrue(sanity(rb));
        }
    }

    @Test
    public void RBTreeInsert() throws InvocationTargetException, IllegalAccessException {
        RBTreeMap<Integer, String> rbTree = new RBTreeMap<>();
        Assertions.assertNull(rbTree.put(1, "One"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(2, "Two"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(3, "Three"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(4, "Four"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(5, "Five"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(6, "Six"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(7, "Seven"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(8, "Eight"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(9, "Nine"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(10, "Ten"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(11, "Eleven"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertNull(rbTree.put(12, "Twelve"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertEquals(12, rbTree.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertEquals("Four", rbTree.put(4, "SecondFour"));
        Assertions.assertEquals("SecondFour", rbTree.get(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertEquals(12, rbTree.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertEquals(expectedTree, dumpTree.invoke(rbTree).toString());
    }

    @Test
    public void RBTreeRemove() throws InvocationTargetException, IllegalAccessException {
        RBTreeMap<Integer, String> rbTree = new RBTreeMap<>();
        rbTree.put(1, "One");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        rbTree.put(2, "Two");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        rbTree.put(3, "Three");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        rbTree.put(4, "Four");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        rbTree.put(5, "Five");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        rbTree.put(6, "Six");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        rbTree.put(7, "Seven");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertEquals(7, rbTree.size());
        Assertions.assertEquals("Seven", rbTree.remove(7));
        Assertions.assertEquals("Four", rbTree.remove(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertEquals(5, rbTree.size());
        Assertions.assertNull(rbTree.remove(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(rbTree));
        Assertions.assertEquals(5, rbTree.size());
        Assertions.assertEquals(expectedTree2, dumpTree.invoke(rbTree).toString());
    }

    private RBTreeMap<Integer, String> setUpTree(){
        RBTreeMap<Integer, String> rbTree = new RBTreeMap<>();
        rbTree.put(1, "One");
        rbTree.put(2, "Two");
        rbTree.put(3, "Three");
        rbTree.put(4, "Four");
        rbTree.put(5, "Five");
        rbTree.put(6, "Six");
        rbTree.put(7, "Seven");
        return rbTree;
    }

    @Test
    public void AVLTreeFirstEntry(){
        RBTreeMap<Integer, String> rbTree = setUpTree();
        Assertions.assertEquals(1, rbTree.firstEntry().getKey());
    }

    @Test
    public void AVLTreeLastEntry(){
        RBTreeMap<Integer, String> rbTree = setUpTree();
        Assertions.assertEquals(7, rbTree.lastEntry().getKey());
    }

    @Test
    public void AVLTreeSubMap(){
        RBTreeMap<Integer, String> rbTree = setUpTree();
        List<String> entries = new ArrayList<>(3);
        entries.add("Three");
        entries.add("Four");
        entries.add("Five");
        for (Entry<Integer, String> entry : rbTree.subMap(3, 6)){
            Assertions.assertEquals(entries.removeFirst(), entry.getValue());
        }
    }
}
