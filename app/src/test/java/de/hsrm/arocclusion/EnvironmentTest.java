package de.hsrm.arocclusion;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EnvironmentTest {

    class A extends ReferencePoint {
        public A() {
            super("A");
        }
    }

    class B extends ReferencePoint {
        public B() {
            super("B");
        }
    }


    @Test
    public void getReferencePointsWithType() {
        Environment e = new Environment();
        List<ReferencePoint> list = new ArrayList<>();
        A a1 = new A();
        A a2 = new A();
        A a3 = new A();
        B b1 = new B();
        B b2 = new B();
        list.add(a1);
        list.add(a2);
        list.add(b1);
        list.add(a3);
        list.add(b2);
        e.setReferencePoints(list);
        List<A> listA = e.getReferencePointsWithType(A.class);
        List<B> listB = e.getReferencePointsWithType(B.class);

        assertEquals(Arrays.asList(a1, a2, a3), listA);
        assertEquals(Arrays.asList(b1, b2), listB);
    }
}