package org.levk.rmp2.errorCorrection;

import java.util.Vector;

public class PolyUtils {
    static int[] glogtable = new int[65536];
    static int[] gexptable = new int[196608];

    static final int ROOT_CUTOFF = 32;

    public static void main(String[] args) {
        initializeTables();

        int[] xs = new int[4096];
        int[] ys = new int[4096];

        for (int v = 0; v < 4096; v++) {
            ys[v] = v * 3;
            xs[v] = 1000 + v * 7;
        }

        for (int a = 0; a < 10; a++) {
            ys[0] = a;
            int[] poly = lagrangeInterp(ys, xs);
            int[] logpoly = polyToLogs(poly);
            System.out.println(evalPolyAt(poly, 1700));
            int o = 0;
            for (int i = 4096; i < 4096 * 2; i++) {
                o += evalLogPolyAt(logpoly, i);
            }
            System.out.println(o);
        }
    }

    public static void initializeTables() {
        int v = 1;
        for (int i = 0; i < 65536; i++) {
            glogtable[v] = i;
            gexptable[i] = v;
            gexptable[i + 65535] = v;
            gexptable[i + 131070] = v;
            if ((v & 32768) != 0) {
                v = (v * 2) ^ v ^ 103425;
            } else {
                v = (v * 2) ^ v;
            }
        }
    }

    public static int evalPolyAt(int[] poly, int x) {
        if (x == 0) return poly[0];
        int logx = glogtable[x];
        int y = 0;
        for (int i = 0; i < poly.length; i++) {
            if (poly[i] != 0) {
                y ^= gexptable[(logx * i + glogtable[poly[i]]) % 65535];
            }
        }

        return y;
    }

    public static int evalLogPolyAt(int[] poly, int x) {
        if (x == 0) return poly[0] == 65537 ? 0 : gexptable[poly[0]];
        int logx = glogtable[x];
        int y = 0;
        for (int i = 0; i < poly.length; i++) {
            if (poly[i] != 65537) {
                y ^= gexptable[(logx * i + poly[i]) % 65535];
            }
        }

        return y;
    }

    public static int[] karatsubaMul(int[] p, int[] q) {
        int[] p2 = new int[3];
        int[] q2 = new int[3];
        int L = p.length;
        if (L <= 64) {
            int[] o = new int[L * 2];
            int[] logq = new int[L];
            for (int i = 0; i < L; i++) {
                logq[i] = glogtable[q[i]];
            }
            for (int i = 0; i < L; i++) {
                int log_pi = glogtable[p[i]];
                for (int j = 0; j < L; j++) {
                    if (p[i] != 0 && q[j] != 0) {
                        o[i + j] ^= gexptable[log_pi + logq[j]];
                    }
                }
            }
            return o;
        }
        if ((L % 2) != 0) {
            L += 1;
            p2 = pushback(p, 0);
            q2 = pushback(q, 0);
        }
        int halflen = L / 2;
        int[] low1 = declare(p2, halflen);
        int[] low2 = declare(q2, halflen);
        int[] high1 = declare(p2, halflen, halflen);
        int[] high2 = declare(q2, halflen, halflen);
        int[] sum1 = new int[halflen];
        int[] sum2 = new int[halflen];
        for (int i = 0; i < halflen; i++) {
            sum1[i] = low1[i] ^ high1[i];
            sum2[i] = low2[i] ^ high2[i];
        }

        int[] z0 = karatsubaMul(low1, low2);
        int[] z2 = karatsubaMul(high1, high2);
        int[] m = karatsubaMul(sum1, sum2);
        int[] o = new int[L * 2];
        for (int i = 0; i < L; i++) {
            o[i] ^= z0[i];
            o[i + halflen] ^= (m[i] ^ z0[i] ^ z2[i]);
            o[i + L] ^= z2[i];
        }
        return o;
    }

    public static int[] mkRoot(int[] xs) {
        int L = xs.length;
        if (L >= ROOT_CUTOFF) {
            int halflen = L / 2;
            int[] left = declare(xs, halflen);
            int[] right = declare(xs, halflen, halflen);
            int[] o = karatsubaMul(mkRoot(left), mkRoot(right));
            o = resize(o, L + 1);
            return o;
        }
        int[] root = new int[L + 1];
        root[L] = 1;
        for (int i = 0; i < L; i++) {
            int logx = glogtable[xs[i]];
            int offset = L - i - 1;
            root[offset] = 0;
            for (int j = offset; j < i + 1 + offset; j++) {
                if (root[j + 1] != 0 && xs[i] != 0) {
                    root[j] ^= gexptable[glogtable[root[j + 1]] + logx];
                }
            }
        }
        return root;
    }

    public static int[] subrootLinearCombination(int[] xs, int[] factors) {
        int L = xs.length;

        if (L == 1) {
            int[] o = new int[2];
            o[0] = factors[0];
            return o;
        }

        int halflen = L / 2;
        int[] xsLeft = declare(xs, halflen);
        int[] xsRight = declare(xs, halflen, halflen);
        int[] factorsLeft = declare(factors, halflen);
        int[] factorsRight = declare(factors, halflen, halflen);
        int[] r1 = mkRoot(xsLeft);
        int[] r2 = mkRoot(xsRight);
        int[] o1 = karatsubaMul(r1, subrootLinearCombination(xsRight, factorsRight));
        int[] o2 = karatsubaMul(r2, subrootLinearCombination(xsLeft, factorsLeft));
        int[] o = new int[L + 1];
        for (int i = 0; i < L; i++) {
            o[i] = o1[i] ^ o2[i];
        }
        return o;
    }

    public static int[] derivativeAndSquareBase(int[] p) {
        int[] o = new int[(p.length - 1) / 2];
        for (int i = 0; i < o.length; i+= 1) {
            o[i] = p[i * 2 + 1];
        }
        return o;
    }

    public static int[] polyToLogs(int[] p) {
        int[] o = new int[p.length];
        for (int i = 0; i < p.length; i++) {
            if (p[i] != 0) {
                o[i] = glogtable[p[i]];
            } else {
                o[i] = 65537;
            }
        }
        return o;
    }

    public static int[] lagrangeInterp(int[] ys, int[] xs) {
        int xsSize = xs.length;
        int[] root = mkRoot(xs);
        int[] logRootPrime = polyToLogs(derivativeAndSquareBase(root));
        int[] factors = new int[xsSize];
        for (int i = 0; i < xsSize; i++) {
            int xSquare = xs[i] != 0 ? gexptable[glogtable[xs[i]] * 2] : 0;
            int denom = evalLogPolyAt(logRootPrime, xSquare);
            if (ys[i] != 0) {
                factors[i] = gexptable[glogtable[ys[i]] + 65535 - glogtable[denom]];
            }
        }

        return subrootLinearCombination(xs, factors);
    }

    public static int[] berlekampWelchAttempt(byte[] pieces, int[] xs, int masterDegree) {
        int errorLocatorDegree = (pieces.length - masterDegree - 1) / 2;
        int[][] eqs = new int[0][0];
        for (int i = 0; i < 2 * errorLocatorDegree + masterDegree + 1; i++) {
            eqs[i] = pushback(eqs[i], 0);
        }
        for (int i = 0; i < 2 * errorLocatorDegree + masterDegree + 1; i++) {
            int negXToTheJ = -1;
            for (int j = 0; j < errorLocatorDegree + masterDegree + 1; i++) {
                eqs[i] = pushback(eqs[i], negXToTheJ);
                negXToTheJ *= xs[i];
            }
            int xToTheJ = 1;
            for (int j = 0; j <errorLocatorDegree + 1; i++) {
                eqs[i] = pushback(eqs[i], xToTheJ * pieces[i]);;
                xToTheJ *= xs[i];
            }
        }

        int errors = errorLocatorDegree;
        int ones = 1;
        while (errors >= 0) {
            try {
                int[] polys = sysSolve(eqs);
                for (int i = 0; i < ones; i++) {
                    polys = pushback(polys, 1);
                }
                int[] qpolys = new int[errors + masterDegree + 1];
                System.arraycopy(polys, 0, qpolys, 0, errors + masterDegree + 1);
                int[] epoly = new int[polys.length - (errors + masterDegree + 1)];
                System.arraycopy(polys, errors + masterDegree 1, epoly, 0, epoly.length);


            }
        }
    }

    public static int[] resize(int[] val, int len) {
        int[] out = new int[len];
        System.arraycopy(val, 0, out, 0, (len > val.length) ? val.length : len);
        return out;
    }

    public static int[] declare(int[] vec, int len, int start) {
        int[] out = new int[len];
        System.arraycopy(vec, start, out, 0, len);
        return out;
    }

    public static int[] declare(int[] begin, int len) {
        int[] out = new int[len];
        System.arraycopy(begin, 0, out, 0, len);
        return out;
    }

    public static int[] declare(int len, int val) {
        int[] out = new int[len];
        for (int i = 0; i < len; i++) {
            out[i] = val;
        }

        return out;
    }

    public static int[] pushback(int[] in, int val) {
        int[] out = new int[in.length + 1];
        System.arraycopy(in, 0, out, 0, in.length);
        out[in.length] = val;
        return out;
    }
}
