import java.util.*;
import java.io.*;

/**
 * ClosestPair.java
 *
 * Reads N from standard input (single integer).
 * Generates N random points (floating point by default).
 * Computes closest pair by:
 *   - Brute-force O(N^2)
 *   - Divide-and-conquer O(N log N)
 *
 * Output format (exact):
 *  Line 1 (Brute-Force): Minimum distance (6 decimal places) (x1,y1) (x2,y2)
 *  Line 2 (Divide-and-Conquer): Minimum distance (6 decimal places) (x1,y1) (x2,y2)
 *  Line 3: Times (ms): Brute-Force=... Divide-and-Conquer=...
 *
 * The program also prints a small performance sample (N vs times) after the required lines.
 *
 * Corner cases handled:
 *  - N < 2 : prints "insufficient points" lines and exits
 *  - N = 2 : both algorithms return the single pair
 *  - duplicate points : early exit when distance == 0.0
 *  - collinear / same x or y coordinates : handled by tie-breaking in partitioning
 *
 * Usage:
 *  javac ClosestPair.java
 *  java ClosestPair
 *  (then input N and press Enter)
 */
public class ClosestPair {

    static final Random RNG = new Random();

    static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
        @Override public String toString() {
            return String.format("(%.6f,%.6f)", x, y);
        }
    }

    static class Result {
        double dist;
        Point a, b;
        Result(double dist, Point a, Point b) {
            this.dist = dist; this.a = a; this.b = b;
        }
    }

    // Euclidean distance
    static double dist(Point p, Point q) {
        double dx = p.x - q.x;
        double dy = p.y - q.y;
        return Math.hypot(dx, dy);
    }

    // Brute-force O(n^2)
    static Result bruteForce(Point[] pts) {
        int n = pts.length;
        if (n < 2) return null;
        double best = Double.POSITIVE_INFINITY;
        Point pa = null, pb = null;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                double d = dist(pts[i], pts[j]);
                if (d < best) {
                    best = d;
                    pa = pts[i];
                    pb = pts[j];
                    if (best == 0.0) return new Result(0.0, pa, pb); // early exit on duplicate
                }
            }
        }
        return new Result(best, pa, pb);
    }

    // Divide and conquer wrapper
    static Result divideAndConquer(Point[] pts) {
        int n = pts.length;
        if (n < 2) return null;
        Point[] px = pts.clone();
        Arrays.sort(px, Comparator.comparingDouble(p -> p.x));
        Point[] py = pts.clone();
        Arrays.sort(py, Comparator.comparingDouble(p -> p.y));
        return closestRec(px, py, 0, n);
    }

    // Single recursive function: px is sorted by x for the subarray [l, r), py is the points for this subproblem sorted by y
    static Result closestRec(Point[] px, Point[] py, int l, int r) {
        int n = r - l;
        if (n <= 3) {
            double best = Double.POSITIVE_INFINITY;
            Point pa = null, pb = null;
            for (int i = l; i < r; ++i) {
                for (int j = i + 1; j < r; ++j) {
                    double d = dist(px[i], px[j]);
                    if (d < best) {
                        best = d; pa = px[i]; pb = px[j];
                        if (best == 0.0) return new Result(0.0, pa, pb);
                    }
                }
            }
            return new Result(best, pa, pb);
        }

        int mid = (l + r) >>> 1;
        double midX = px[mid].x;

        // Partition py into leftY and rightY (both sorted by y)
        ArrayList<Point> leftY = new ArrayList<>();
        ArrayList<Point> rightY = new ArrayList<>();
        for (Point p : py) {
            if (p.x < midX || (p.x == midX && indexInRange(px, l, mid, p))) leftY.add(p);
            else rightY.add(p);
        }

        Result leftRes = closestRec(px, leftY.toArray(new Point[0]), l, mid);
        Result rightRes = closestRec(px, rightY.toArray(new Point[0]), mid, r);

        Result bestRes = leftRes;
        if (rightRes != null && (bestRes == null || rightRes.dist < bestRes.dist)) bestRes = rightRes;
        if (bestRes == null) return null;

        double d = bestRes.dist;

        // Build strip: points within d of midX, sorted by y (py is sorted by y)
        ArrayList<Point> strip = new ArrayList<>();
        for (Point p : py) {
            if (Math.abs(p.x - midX) < d) strip.add(p);
        }

        // Check up to next 7 points in strip (standard result)
        double best = d;
        Point pa = bestRes.a, pb = bestRes.b;
        for (int i = 0; i < strip.size(); ++i) {
            Point p = strip.get(i);
            for (int j = i + 1; j < strip.size() && j <= i + 7; ++j) {
                Point q = strip.get(j);
                double dy = q.y - p.y;
                if (dy >= best) break; // further points cannot be closer in y
                double dd = dist(p, q);
                if (dd < best) {
                    best = dd; pa = p; pb = q;
                    if (best == 0.0) return new Result(0.0, pa, pb);
                }
            }
        }

        return new Result(best, pa, pb);
    }

    // Helper: check if point p is in px[l..r) by identity (since px may contain duplicates in x)
    static boolean indexInRange(Point[] px, int l, int r, Point p) {
        for (int i = l; i < r; ++i) if (px[i] == p) return true;
        return false;
    }

    // Generate random points (uniform in [0, scale))
    static Point[] generateRandomPoints(int n, double scale, boolean integerCoords, Long seed) {
        Random rng = (seed == null) ? new Random() : new Random(seed);
        Point[] pts = new Point[n];
        for (int i = 0; i < n; ++i) {
            double x = rng.nextDouble() * scale;
            double y = rng.nextDouble() * scale;
            if (integerCoords) {
                x = Math.round(x);
                y = Math.round(y);
            }
            pts[i] = new Point(x, y);
        }
        return pts;
    }

    // Format output: distance and pair
    static String formatResult(String label, Result res) {
        if (res == null) return label + ": insufficient points";
        return String.format("%s: %.6f %s %s", label, res.dist, res.a.toString(), res.b.toString());
    }

    public static void main(String[] args) throws Exception {
        // Read N from standard input (single integer)
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();
        if (line == null) {
            System.err.println("No input provided. Please input an integer N.");
            return;
        }
        line = line.trim();
        int N;
        try {
            N = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please provide an integer N.");
            return;
        }

        // Default parameters
        Long seed = null;           // null => random seed
        boolean integerCoords = false;
        double scale = 1000.0;

        if (N < 2) {
            System.out.println("Brute-Force: insufficient points");
            System.out.println("Divide-and-Conquer: insufficient points");
            System.out.println("Times (ms): Brute-Force=0 Divide-and-Conquer=0");
            return;
        }

        // Generate points
        Point[] pts = generateRandomPoints(N, scale, integerCoords, seed);

        // Copies for algorithms (they may sort)
        Point[] ptsForBF = new Point[N];
        Point[] ptsForDC = new Point[N];
        for (int i = 0; i < N; ++i) { ptsForBF[i] = pts[i]; ptsForDC[i] = pts[i]; }

        // Brute-force timing
        long t0 = System.nanoTime();
        Result bfRes = bruteForce(ptsForBF);
        long t1 = System.nanoTime();

        // Divide-and-conquer timing
        long t2 = System.nanoTime();
        Result dcRes = divideAndConquer(ptsForDC);
        long t3 = System.nanoTime();

        double bfMs = (t1 - t0) / 1_000_000.0;
        double dcMs = (t3 - t2) / 1_000_000.0;

        // Required outputs
        System.out.println(formatResult("Brute-Force", bfRes));
        System.out.println(formatResult("Divide-and-Conquer", dcRes));
        System.out.printf("Times (ms): Brute-Force=%.3f Divide-and-Conquer=%.3f%n", bfMs, dcMs);

        // Verification
        if (bfRes != null && dcRes != null) {
            if (Math.abs(bfRes.dist - dcRes.dist) > 1e-6) {
                System.err.printf("Warning: algorithms disagree: BF=%.9f DC=%.9f%n", bfRes.dist, dcRes.dist);
            } else {
                System.out.println("Verification: distances agree (within tolerance).");
            }
        }

        // Small performance sample for increasing N (prints CSV-like lines)
        // This helps analyze performance; brute-force is capped to avoid extremely long runs.
        int[] sampleNs = {128, 256, 512, 1024};
        System.out.println();
        System.out.println("Performance sample (N,BruteForce_ms,DivideConquer_ms):");
        for (int n : sampleNs) {
            if (n > 4096) continue; // safety cap
            Point[] sample = generateRandomPoints(n, scale, integerCoords, null);
            long s0 = System.nanoTime();
            Result rbf = bruteForce(sample);
            long s1 = System.nanoTime();
            Point[] sample2 = new Point[n];
            for (int i = 0; i < n; ++i) sample2[i] = sample[i];
            long s2 = System.nanoTime();
            Result rdc = divideAndConquer(sample2);
            long s3 = System.nanoTime();
            double bfSampleMs = (s1 - s0) / 1_000_000.0;
            double dcSampleMs = (s3 - s2) / 1_000_000.0;
            System.out.printf("%d,%.3f,%.3f%n", n, bfSampleMs, dcSampleMs);
        }
    }
}
