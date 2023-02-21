package com.itisacat.pdf.demo;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;

import java.util.Comparator;

public class LocationComparator implements Comparator<IPdfTextLocation> {
    private static final float EPS = 1.0E-4F;
    @Override
    public int compare(IPdfTextLocation l1, IPdfTextLocation  l2) {
        Rectangle o1 = l1.getRectangle();
        Rectangle o2 = l2.getRectangle();
        if (Math.abs(o1.getY() - o2.getY()) < EPS) {
            return Math.abs(o1.getX() - o2.getX()) < EPS ? 0 : ((o2.getX() - o1.getX()) > EPS ? -1 : 1);
        } else {
            return (o2.getY() - o1.getY()) < EPS ? -1 : 1;
        }
    }
}
